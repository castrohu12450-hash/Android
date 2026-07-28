package com.kiminini.hospital.data.repository

import com.kiminini.hospital.data.database.AppointmentEntity
import com.kiminini.hospital.data.database.HospitalDao
import com.kiminini.hospital.data.model.Appointment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull

class AppointmentRepository(
    private val hospitalDao: HospitalDao
) {
    fun getAppointmentsByPatient(patientId: Long): Flow<List<Appointment>> {
        return hospitalDao.getAppointmentsByPatient(patientId).map { entities ->
            entities.map { entity ->
                Appointment(
                    id = entity.appointmentId,
                    doctorName = entity.doctorName,
                    department = entity.department,
                    date = entity.date,
                    time = entity.time,
                    status = entity.status,
                    reason = entity.reason
                )
            }
        }
    }

    suspend fun createAppointment(
        patientId: Long,
        doctorName: String,
        department: String,
        date: String,
        time: String,
        reason: String
    ): String {
        val appointmentId = "APT-${System.currentTimeMillis()}"
        val appointment = AppointmentEntity(
            appointmentId = appointmentId,
            patientId = patientId,
            doctorName = doctorName,
            department = department,
            date = date,
            time = time,
            status = "Pending",
            reason = reason,
            syncStatus = "PENDING"   // ← Ensures it syncs
        )
        hospitalDao.insertAppointment(appointment)
        return appointmentId
    }

    suspend fun updateAppointmentStatus(appointmentId: String, status: String) {
        val entity = hospitalDao.getAppointmentByAppointmentId(appointmentId)
        entity?.let {
            val updated = it.copy(
                status = status,
                syncStatus = "PENDING"   // ← Ensures update syncs
            )
            hospitalDao.updateAppointment(updated)
        }
    }

    suspend fun cancelAppointment(appointmentId: String) {
        updateAppointmentStatus(appointmentId, "Cancelled")
    }

    suspend fun isTimeSlotAvailable(doctorName: String, date: String, time: String): Boolean {
        val allAppointments = hospitalDao.getAllAppointments().firstOrNull() ?: emptyList()
        val conflicting = allAppointments.any {
            it.doctorName == doctorName && it.date == date && it.time == time && it.status != "Cancelled"
        }
        return !conflicting
    }
}