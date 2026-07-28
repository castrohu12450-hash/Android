// DashboardRepository.kt
package com.kiminini.hospital.data.repository

import com.kiminini.hospital.data.database.HospitalDao
import com.kiminini.hospital.data.database.DashboardActivityEntity
import com.kiminini.hospital.data.model.DashboardActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull

class DashboardRepository(
    private val hospitalDao: HospitalDao
) {
    fun getDashboardActivities(patientId: Long): Flow<List<DashboardActivity>> {
        return hospitalDao.getDashboardActivitiesByPatient(patientId).map { entities ->
            entities.map { entity ->
                DashboardActivity(
                    id = entity.activityId,
                    icon = entity.icon,
                    title = entity.title,
                    description = entity.description,
                    time = entity.time,
                    isRemovable = entity.isRemovable
                )
            }
        }
    }

    suspend fun addDashboardActivity(
        patientId: Long,
        icon: String,
        title: String,
        description: String,
        time: String
    ): String {
        val activityId = "ACT-${System.currentTimeMillis()}"
        val activity = DashboardActivityEntity(
            activityId = activityId,
            patientId = patientId,
            icon = icon,
            title = title,
            description = description,
            time = time,
            isRemovable = true
        )
        hospitalDao.insertDashboardActivity(activity)
        return activityId
    }

    suspend fun deleteDashboardActivity(activityId: String, patientId: Long) {
        hospitalDao.deleteDashboardActivityById(activityId, patientId)
    }

    suspend fun addSampleActivities(patientId: Long) {
        // Add sample activities if none exist
        val existingActivities = hospitalDao.getDashboardActivitiesByPatient(patientId)
            .firstOrNull() ?: emptyList()

        if (existingActivities.isEmpty()) {
            val sampleActivities = listOf(
                DashboardActivityEntity(
                    activityId = "ACT-1",
                    patientId = patientId,
                    icon = "📋",
                    title = "Lab Test Results",
                    description = "Blood test results are ready",
                    time = "Yesterday",
                    isRemovable = false
                ),
                DashboardActivityEntity(
                    activityId = "ACT-2",
                    patientId = patientId,
                    icon = "💊",
                    title = "Prescription Renewed",
                    description = "Amoxicillin prescription renewed",
                    time = "2 days ago",
                    isRemovable = true
                ),
                DashboardActivityEntity(
                    activityId = "ACT-3",
                    patientId = patientId,
                    icon = "💰",
                    title = "Bill Payment",
                    description = "Payment of Ksh 2,500 received",
                    time = "1 week ago",
                    isRemovable = true
                ),
                DashboardActivityEntity(
                    activityId = "ACT-4",
                    patientId = patientId,
                    icon = "📅",
                    title = "Appointment Reminder",
                    description = "Cardiology appointment tomorrow",
                    time = "Today",
                    isRemovable = false
                )
            )

            sampleActivities.forEach { activity ->
                hospitalDao.insertDashboardActivity(activity)
            }
        }
    }
}