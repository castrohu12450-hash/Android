package com.kiminini.hospital.data.repository

import android.util.Log
import com.kiminini.hospital.data.database.HospitalDao
import com.kiminini.hospital.data.database.QueueTicketEntity
import com.kiminini.hospital.data.firestore.FirestoreService
import com.kiminini.hospital.data.model.queue.QueueTicket
import com.kiminini.hospital.data.model.queue.QueueStatus
import com.kiminini.hospital.data.model.queue.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class QueueRepository(
    private val hospitalDao: HospitalDao,
    private val firestoreService: FirestoreService
) {

    suspend fun insertQueueTicket(ticket: QueueTicket): Boolean {
        return try {
            firestoreService.setQueueTicket(ticket)
            Log.d("QueueRepository", "Ticket written to Firestore: ${ticket.ticketId}")
            val syncedTicket = ticket.copy(syncStatus = SyncStatus.SYNCED)
            val entity = QueueTicketEntity.fromQueueTicket(syncedTicket)
            hospitalDao.insertQueueTicket(entity)
            true
        } catch (e: Exception) {
            Log.e("QueueRepository", "Failed to write to Firestore", e)
            false
        }
    }

    suspend fun updateQueueTicket(ticket: QueueTicket): Boolean {
        return try {
            firestoreService.setQueueTicket(ticket)
            val syncedTicket = ticket.copy(syncStatus = SyncStatus.SYNCED)
            val entity = QueueTicketEntity.fromQueueTicket(syncedTicket)
            hospitalDao.updateQueueTicket(entity)
            true
        } catch (e: Exception) {
            Log.e("QueueRepository", "Failed to update Firestore", e)
            false
        }
    }

    suspend fun getActiveQueueTicket(patientId: String): QueueTicket? {
        val entity = hospitalDao.getActiveQueueTicket(patientId)
        return entity?.toQueueTicket()
    }

    suspend fun getActiveQueueTicketByTicketId(ticketId: String): QueueTicket? {
        val entity = hospitalDao.getQueueTicketById(ticketId)
        return entity?.toQueueTicket()
    }

    fun getWaitingQueueFlow(): Flow<List<QueueTicket>> = firestoreService.listenToWaitingQueue()
    fun getInProgressQueueFlow(): Flow<List<QueueTicket>> = firestoreService.listenToInProgressQueue()
    fun getCompletedTodayFlow(): Flow<List<QueueTicket>> = firestoreService.listenToCompletedToday()
    fun listenToPatientTicket(patientId: String): Flow<QueueTicket?> = firestoreService.listenToPatientTicket(patientId)

    suspend fun getWaitingCount(): Int = hospitalDao.getWaitingCount()
    suspend fun getCriticalCount(): Int = hospitalDao.getCriticalCount()
    suspend fun getPendingCount(): Int = hospitalDao.getPendingQueueCount()

    suspend fun syncPendingTickets(): Int {
        val pendingEntities = hospitalDao.getQueueTicketsBySyncStatus(listOf("PENDING", "FAILED"))
        var successCount = 0
        for (entity in pendingEntities) {
            val ticket = entity.toQueueTicket()
            try {
                firestoreService.setQueueTicket(ticket)
                val syncedTicket = ticket.copy(syncStatus = SyncStatus.SYNCED)
                val syncedEntity = QueueTicketEntity.fromQueueTicket(syncedTicket)
                hospitalDao.updateQueueTicket(syncedEntity)
                successCount++
            } catch (e: Exception) {
                Log.e("QueueRepository", "Sync failed for ticket ${ticket.ticketId}", e)
            }
        }
        return successCount
    }

    suspend fun updateQueuePosition(ticketId: String, position: Int) {
        val ticket = getActiveQueueTicketByTicketId(ticketId)
        ticket?.let {
            val updated = it.copy(positionInQueue = position, syncStatus = SyncStatus.SYNCED)
            updateQueueTicket(updated)
        }
    }

    suspend fun getNextPatient(): QueueTicket? = firestoreService.getNextPatient()

    suspend fun startConsultation(ticketId: String): Boolean {
        Log.d("QueueRepository", "startConsultation called for ticketId: $ticketId")
        val startTime = System.currentTimeMillis()
        val success = firestoreService.startConsultationTransaction(ticketId, startTime)
        if (success) {
            val ticket = getActiveQueueTicketByTicketId(ticketId)
            ticket?.let {
                val updated = it.copy(
                    status = QueueStatus.IN_PROGRESS,
                    startTime = startTime,
                    syncStatus = SyncStatus.SYNCED
                )
                updateQueueTicket(updated)
            }
        } else {
            Log.e("QueueRepository", "Failed to start consultation for $ticketId (status may have changed)")
        }
        return success
    }

    suspend fun completeConsultation(ticketId: String): Boolean {
        val endTime = System.currentTimeMillis()
        val success = firestoreService.completeConsultationTransaction(ticketId, endTime)
        if (success) {
            val ticket = getActiveQueueTicketByTicketId(ticketId)
            ticket?.let {
                val updated = it.copy(
                    status = QueueStatus.COMPLETED,
                    endTime = endTime,
                    syncStatus = SyncStatus.SYNCED
                )
                updateQueueTicket(updated)
            }
        } else {
            Log.e("QueueRepository", "Failed to complete consultation for $ticketId (status may have changed)")
        }
        return success
    }

    suspend fun cancelQueueTicket(ticketId: String) {
        val ticket = getActiveQueueTicketByTicketId(ticketId)
        ticket?.let {
            val updated = it.copy(
                status = QueueStatus.CANCELLED,
                syncStatus = SyncStatus.SYNCED
            )
            updateQueueTicket(updated)
        }
    }

    suspend fun getInProgressTickets(): List<QueueTicket> {
        return hospitalDao.getInProgressTickets().map { it.toQueueTicket() }
    }

    suspend fun getInProgressCount(): Int = hospitalDao.getInProgressCount()

    suspend fun getCompletedTicketsToday(): List<QueueTicket> {
        val startOfDay = getStartOfDay()
        return hospitalDao.getCompletedTicketsSince(startOfDay).map { it.toQueueTicket() }
    }

    suspend fun getCompletedCountToday(): Int = hospitalDao.getCompletedCountSince(getStartOfDay())

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // ========== FIXED SKIP ==========
    suspend fun skipPatient(ticketId: String) {
        try {
            val waitingTickets = firestoreService.getWaitingTicketsDirect()
            if (waitingTickets.isEmpty()) {
                Log.w("QueueRepository", "No waiting tickets to skip")
                return
            }
            val index = waitingTickets.indexOfFirst { it.ticketId == ticketId }
            if (index == -1) {
                Log.w("QueueRepository", "Ticket $ticketId not found in waiting list")
                return
            }
            val reordered = waitingTickets.toMutableList()
            val skippedTicket = reordered.removeAt(index)
            reordered.add(skippedTicket)

            reordered.forEachIndexed { newPosition, ticket ->
                val updatedTicket = ticket.copy(
                    positionInQueue = newPosition + 1,
                    syncStatus = SyncStatus.SYNCED
                )
                firestoreService.setQueueTicket(updatedTicket)
                val entity = QueueTicketEntity.fromQueueTicket(updatedTicket)
                hospitalDao.updateQueueTicket(entity)
            }
            Log.d("QueueRepository", "Skip successful for $ticketId")
        } catch (e: Exception) {
            Log.e("QueueRepository", "Skip failed for $ticketId", e)
            throw e
        }
    }

    // ========== NEW: Force sync Firestore waiting list to Room ==========
    suspend fun syncWaitingQueueToRoom() {
        val waitingList = firestoreService.getWaitingTicketsDirect()
        waitingList.forEach { ticket ->
            val entity = QueueTicketEntity.fromQueueTicket(ticket)
            hospitalDao.insertQueueTicket(entity)
        }
        Log.d("QueueRepository", "Synced ${waitingList.size} waiting tickets to Room")
    }

    suspend fun verifyTicket(ticketId: String, nurseNotes: String) {
        val ticket = getActiveQueueTicketByTicketId(ticketId)
        ticket?.let {
            val updated = it.copy(
                isVerified = true,
                nurseNotes = nurseNotes,
                verifiedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.SYNCED
            )
            updateQueueTicket(updated)
        }
    }
}