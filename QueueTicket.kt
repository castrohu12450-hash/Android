package com.kiminini.hospital.data.model.queue

// SyncStatus is defined in its own separate file – do NOT redeclare here

enum class QueuePriority {
    CRITICAL, HIGH, NORMAL, LOW
}

enum class QueueStatus {
    WAITING, IN_PROGRESS, COMPLETED, CANCELLED
}

data class QueueTicket(
    val ticketId: String,
    val patientId: String,
    val patientName: String,
    val department: String,
    val priority: QueuePriority,
    val status: QueueStatus,
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
    val redFlags: List<String> = emptyList(),
    val nurseNotes: String? = null,
    val requiresNurseReview: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val age: Int = 0,                     // ✅ patient age
    val priorityScore: Int = 0,           // computed score 0-10
    val priorityLabel: String = "Normal",
    val scoreBreakdown: Map<String, Int> = emptyMap()
) {
    // Numeric order for sorting (1 = CRITICAL .. 4 = LOW)
    val priorityOrder: Int
        get() = when (priority) {
            QueuePriority.CRITICAL -> 1
            QueuePriority.HIGH -> 2
            QueuePriority.NORMAL -> 3
            QueuePriority.LOW -> 4
        }

    fun toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "ticketId" to ticketId,
            "patientId" to patientId,
            "patientName" to patientName,
            "department" to department,
            "priority" to priority.name,
            "priorityOrder" to priorityOrder,
            "status" to status.name,
            "positionInQueue" to positionInQueue,
            "estimatedWaitTime" to estimatedWaitTime,
            "checkInTime" to checkInTime,
            "startTime" to (startTime ?: 0L),
            "endTime" to (endTime ?: 0L),
            "isVerified" to isVerified,
            "verifiedBy" to (verifiedBy ?: ""),
            "verifiedAt" to (verifiedAt ?: 0L),
            "selfReportedScore" to selfReportedScore,
            "verifiedScore" to verifiedScore,
            "redFlags" to redFlags,
            "nurseNotes" to (nurseNotes ?: ""),
            "requiresNurseReview" to requiresNurseReview,
            "syncStatus" to syncStatus.name,
            "age" to age,
            "priorityScore" to priorityScore,
            "priorityLabel" to priorityLabel,
            "scoreBreakdown" to scoreBreakdown.entries.joinToString(",") { "${it.key}=${it.value}" }
        )
    }

    companion object {
        fun fromFirestoreMap(map: Map<String, Any>): QueueTicket {
            return QueueTicket(
                ticketId = map["ticketId"] as String,
                patientId = map["patientId"] as String,
                patientName = map["patientName"] as String,
                department = map["department"] as String,
                priority = QueuePriority.valueOf(map["priority"] as String),
                status = QueueStatus.valueOf(map["status"] as String),
                positionInQueue = (map["positionInQueue"] as Long).toInt(),
                estimatedWaitTime = (map["estimatedWaitTime"] as Long).toInt(),
                checkInTime = map["checkInTime"] as Long,
                startTime = (map["startTime"] as? Long)?.takeIf { it != 0L },
                endTime = (map["endTime"] as? Long)?.takeIf { it != 0L },
                isVerified = map["isVerified"] as Boolean,
                verifiedBy = (map["verifiedBy"] as? String)?.takeIf { it.isNotEmpty() },
                verifiedAt = (map["verifiedAt"] as? Long)?.takeIf { it != 0L },
                selfReportedScore = (map["selfReportedScore"] as Long).toInt(),
                verifiedScore = (map["verifiedScore"] as Long).toInt(),
                redFlags = (map["redFlags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                nurseNotes = (map["nurseNotes"] as? String)?.takeIf { it.isNotEmpty() },
                requiresNurseReview = map["requiresNurseReview"] as Boolean,
                syncStatus = SyncStatus.valueOf(map["syncStatus"] as String),
                age = (map["age"] as? Long)?.toInt() ?: 0,
                priorityScore = (map["priorityScore"] as? Long)?.toInt() ?: 0,
                priorityLabel = map["priorityLabel"] as? String ?: "Normal",
                scoreBreakdown = (map["scoreBreakdown"] as? String)?.split(",")?.mapNotNull {
                    val parts = it.split("=")
                    if (parts.size == 2) parts[0] to parts[1].toInt() else null
                }?.toMap() ?: emptyMap()
            )
        }
    }
}