package com.kiminini.hospital.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kiminini.hospital.data.model.queue.QueuePriority
import com.kiminini.hospital.data.model.queue.QueueStatus
import com.kiminini.hospital.data.model.queue.QueueTicket
import com.kiminini.hospital.data.model.queue.SyncStatus

@Entity(tableName = "queue_tickets")
data class QueueTicketEntity(
    @PrimaryKey
    val ticketId: String,
    val patientId: String,
    val patientName: String,
    val department: String,
    val priority: String,
    val priorityOrder: Int,
    val status: String,
    val positionInQueue: Int,
    val estimatedWaitTime: Int,
    val checkInTime: Long,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val isVerified: Boolean = false,
    val verifiedBy: String? = null,
    val verifiedAt: Long? = null,
    val selfReportedScore: Int = 0,
    val verifiedScore: Int = 0,
    val redFlags: String = "",
    val nurseNotes: String? = null,
    val requiresNurseReview: Boolean = false,
    val syncStatus: String = SyncStatus.SYNCED.name,
    val age: Int = 0                    // ✅ ADD THIS
) {
    fun toQueueTicket(): QueueTicket {
        return QueueTicket(
            ticketId = ticketId,
            patientId = patientId,
            patientName = patientName,
            department = department,
            priority = QueuePriority.valueOf(priority),
            status = QueueStatus.valueOf(status),
            positionInQueue = positionInQueue,
            estimatedWaitTime = estimatedWaitTime,
            checkInTime = checkInTime,
            startTime = startTime,
            endTime = endTime,
            isVerified = isVerified,
            verifiedBy = verifiedBy,
            verifiedAt = verifiedAt,
            selfReportedScore = selfReportedScore,
            verifiedScore = verifiedScore,
            redFlags = if (redFlags.isNotEmpty()) redFlags.split(",") else emptyList(),
            nurseNotes = nurseNotes,
            requiresNurseReview = requiresNurseReview,
            syncStatus = SyncStatus.valueOf(syncStatus),
            age = age                    // ✅ ADD THIS
        )
    }

    companion object {
        fun fromQueueTicket(ticket: QueueTicket): QueueTicketEntity {
            return QueueTicketEntity(
                ticketId = ticket.ticketId,
                patientId = ticket.patientId,
                patientName = ticket.patientName,
                department = ticket.department,
                priority = ticket.priority.name,
                priorityOrder = ticket.priorityOrder,
                status = ticket.status.name,
                positionInQueue = ticket.positionInQueue,
                estimatedWaitTime = ticket.estimatedWaitTime,
                checkInTime = ticket.checkInTime,
                startTime = ticket.startTime,
                endTime = ticket.endTime,
                isVerified = ticket.isVerified,
                verifiedBy = ticket.verifiedBy,
                verifiedAt = ticket.verifiedAt,
                selfReportedScore = ticket.selfReportedScore,
                verifiedScore = ticket.verifiedScore,
                redFlags = ticket.redFlags.joinToString(","),
                nurseNotes = ticket.nurseNotes,
                requiresNurseReview = ticket.requiresNurseReview,
                syncStatus = ticket.syncStatus.name,
                age = ticket.age            // ✅ ADD THIS
            )
        }
    }
}