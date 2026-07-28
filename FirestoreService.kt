package com.kiminini.hospital.data.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kiminini.hospital.data.model.queue.QueueTicket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class FirestoreService {
    private val db: FirebaseFirestore = Firebase.firestore

    suspend fun setQueueTicket(ticket: QueueTicket) {
        db.collection("queue_tickets")
            .document(ticket.ticketId)
            .set(ticket.toFirestoreMap())
            .await()
    }

    suspend fun deleteQueueTicket(ticketId: String) {
        db.collection("queue_tickets")
            .document(ticketId)
            .delete()
            .await()
    }

    // ========== TRANSACTION METHODS (NEW) ==========

    suspend fun startConsultationTransaction(ticketId: String, startTime: Long): Boolean {
        return try {
            val documentRef = db.collection("queue_tickets").document(ticketId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(documentRef)
                val currentStatus = snapshot.getString("status")
                if (currentStatus == "WAITING") {
                    transaction.update(documentRef, mapOf(
                        "status" to "IN_PROGRESS",
                        "startTime" to startTime
                    ))
                    true
                } else {
                    false
                }
            }.await()
        } catch (e: Exception) {
            Log.e("FirestoreService", "Start consultation transaction failed", e)
            false
        }
    }

    suspend fun completeConsultationTransaction(ticketId: String, endTime: Long): Boolean {
        return try {
            val documentRef = db.collection("queue_tickets").document(ticketId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(documentRef)
                val currentStatus = snapshot.getString("status")
                if (currentStatus == "IN_PROGRESS") {
                    transaction.update(documentRef, mapOf(
                        "status" to "COMPLETED",
                        "endTime" to endTime
                    ))
                    true
                } else {
                    false
                }
            }.await()
        } catch (e: Exception) {
            Log.e("FirestoreService", "Complete consultation transaction failed", e)
            false
        }
    }

    // ========== LISTENERS (unchanged from previous fix) ==========

    fun listenToWaitingQueue(): Flow<List<QueueTicket>> = callbackFlow {
        val snapshotListener = db.collection("queue_tickets")
            .whereEqualTo("status", "WAITING")
            .orderBy("priorityOrder", Query.Direction.ASCENDING)
            .orderBy("checkInTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreService", "Waiting queue listener error", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tickets = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        QueueTicket.fromFirestoreMap(doc.data ?: emptyMap())
                    } catch (e: Exception) {
                        Log.e("FirestoreService", "Error parsing ticket ${doc.id}", e)
                        null
                    }
                } ?: emptyList()
                trySend(tickets)
            }
        awaitClose { snapshotListener.remove() }
    }

    suspend fun getWaitingTicketsDirect(): List<QueueTicket> {
        val snapshot = db.collection("queue_tickets")
            .whereEqualTo("status", "WAITING")
            .orderBy("priorityOrder", Query.Direction.ASCENDING)
            .orderBy("checkInTime", Query.Direction.ASCENDING)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                QueueTicket.fromFirestoreMap(doc.data ?: emptyMap())
            } catch (e: Exception) {
                Log.e("FirestoreService", "Error parsing direct ticket ${doc.id}", e)
                null
            }
        }
    }

    fun listenToInProgressQueue(): Flow<List<QueueTicket>> = callbackFlow {
        val snapshotListener = db.collection("queue_tickets")
            .whereEqualTo("status", "IN_PROGRESS")
            .orderBy("startTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreService", "In-progress queue listener error", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tickets = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        QueueTicket.fromFirestoreMap(doc.data ?: emptyMap())
                    } catch (e: Exception) {
                        Log.e("FirestoreService", "Error parsing ticket ${doc.id}", e)
                        null
                    }
                } ?: emptyList()
                trySend(tickets)
            }
        awaitClose { snapshotListener.remove() }
    }

    suspend fun getInProgressTicketsDirect(): List<QueueTicket> {
        val snapshot = db.collection("queue_tickets")
            .whereEqualTo("status", "IN_PROGRESS")
            .orderBy("startTime", Query.Direction.ASCENDING)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                QueueTicket.fromFirestoreMap(doc.data ?: emptyMap())
            } catch (e: Exception) {
                Log.e("FirestoreService", "Error parsing direct in-progress ticket ${doc.id}", e)
                null
            }
        }
    }

    fun listenToCompletedToday(): Flow<List<QueueTicket>> = callbackFlow {
        val startOfDay = getStartOfDay()
        val endOfDay = startOfDay + 86400000
        val snapshotListener = db.collection("queue_tickets")
            .whereEqualTo("status", "COMPLETED")
            .whereGreaterThanOrEqualTo("endTime", startOfDay)
            .whereLessThan("endTime", endOfDay)
            .orderBy("endTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreService", "Completed today listener error", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tickets = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        QueueTicket.fromFirestoreMap(doc.data ?: emptyMap())
                    } catch (e: Exception) {
                        Log.e("FirestoreService", "Error parsing ticket ${doc.id}", e)
                        null
                    }
                } ?: emptyList()
                trySend(tickets)
            }
        awaitClose { snapshotListener.remove() }
    }

    fun listenToPatientTicket(patientId: String): Flow<QueueTicket?> = callbackFlow {
        val snapshotListener = db.collection("queue_tickets")
            .whereEqualTo("patientId", patientId)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreService", "Patient ticket listener error for $patientId", error)
                    trySend(null)
                    return@addSnapshotListener
                }
                val ticket = snapshot?.documents?.firstOrNull()?.let { doc ->
                    try {
                        QueueTicket.fromFirestoreMap(doc.data ?: emptyMap())
                    } catch (e: Exception) {
                        Log.e("FirestoreService", "Error parsing patient ticket ${doc.id}", e)
                        null
                    }
                }
                trySend(ticket)
            }
        awaitClose { snapshotListener.remove() }
    }

    suspend fun getNextPatient(): QueueTicket? {
        val snapshot = db.collection("queue_tickets")
            .whereEqualTo("status", "WAITING")
            .orderBy("priorityOrder", Query.Direction.ASCENDING)
            .orderBy("checkInTime", Query.Direction.ASCENDING)
            .limit(1)
            .get()
            .await()
        return snapshot.documents.firstOrNull()?.let { doc ->
            QueueTicket.fromFirestoreMap(doc.data ?: emptyMap())
        }
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}