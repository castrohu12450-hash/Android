package com.kiminini.hospital.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HospitalDao {
    // ========== PATIENT OPERATIONS ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientEntity): Long

    @Update
    suspend fun updatePatient(patient: PatientEntity)

    @Delete
    suspend fun deletePatient(patient: PatientEntity)

    @Query("SELECT * FROM patients ORDER BY createdAt DESC")
    fun getAllPatients(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getPatientById(id: Long): PatientEntity?

    @Query("SELECT * FROM patients WHERE patientId = :patientId")
    suspend fun getPatientByPatientId(patientId: String): PatientEntity?

    @Query("SELECT * FROM patients WHERE phone = :phone")
    suspend fun getPatientByPhone(phone: String): PatientEntity?    // NEW

    @Query("SELECT * FROM patients WHERE syncStatus = 'PENDING'")
    suspend fun getPendingPatients(): List<PatientEntity>

    @Query("UPDATE patients SET syncStatus = :status WHERE id = :id")
    suspend fun updatePatientSyncStatus(id: Long, status: String)

    @Query("UPDATE patients SET fcmToken = :token WHERE id = :id")    // NEW
    suspend fun updateFcmToken(id: Long, token: String)

    // ========== APPOINTMENT OPERATIONS ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Delete
    suspend fun deleteAppointment(appointment: AppointmentEntity)

    @Query("SELECT * FROM appointments WHERE patientId = :patientId ORDER BY date DESC")
    fun getAppointmentsByPatient(patientId: Long): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE appointmentId = :appointmentId")
    suspend fun getAppointmentByAppointmentId(appointmentId: String): AppointmentEntity?

    @Query("SELECT * FROM appointments WHERE syncStatus = 'PENDING'")
    suspend fun getPendingAppointments(): List<AppointmentEntity>

    @Query("UPDATE appointments SET syncStatus = :status WHERE id = :id")
    suspend fun updateAppointmentSyncStatus(id: Long, status: String)

    // ========== MEDICAL RECORD OPERATIONS ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicalRecord(record: MedicalRecordEntity): Long

    @Update
    suspend fun updateMedicalRecord(record: MedicalRecordEntity)

    @Delete
    suspend fun deleteMedicalRecord(record: MedicalRecordEntity)

    @Query("SELECT * FROM medical_records WHERE patientId = :patientId ORDER BY date DESC")
    fun getMedicalRecordsByPatient(patientId: Long): Flow<List<MedicalRecordEntity>>

    // ========== DASHBOARD ACTIVITY OPERATIONS ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDashboardActivity(activity: DashboardActivityEntity): Long

    @Delete
    suspend fun deleteDashboardActivity(activity: DashboardActivityEntity)

    @Query("SELECT * FROM dashboard_activities WHERE patientId = :patientId ORDER BY createdAt DESC")
    fun getDashboardActivitiesByPatient(patientId: Long): Flow<List<DashboardActivityEntity>>

    @Query("DELETE FROM dashboard_activities WHERE activityId = :activityId AND patientId = :patientId")
    suspend fun deleteDashboardActivityById(activityId: String, patientId: Long)

    // ========== QUEUE OPERATIONS ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueTicket(ticket: QueueTicketEntity): Long

    @Update
    suspend fun updateQueueTicket(ticket: QueueTicketEntity)

    @Delete
    suspend fun deleteQueueTicket(ticket: QueueTicketEntity)

    @Query("SELECT * FROM queue_tickets WHERE patientId = :patientId AND status = 'WAITING'")
    suspend fun getActiveQueueTicket(patientId: String): QueueTicketEntity?

    @Query("SELECT * FROM queue_tickets WHERE status = 'WAITING' ORDER BY priorityOrder ASC, checkInTime ASC")
    fun getWaitingQueue(): Flow<List<QueueTicketEntity>>

    @Query("SELECT COUNT(*) FROM queue_tickets WHERE status = 'WAITING'")
    suspend fun getWaitingCount(): Int

    @Query("SELECT COUNT(*) FROM queue_tickets WHERE status = 'WAITING' AND priority = 'CRITICAL'")
    suspend fun getCriticalCount(): Int

    @Query("UPDATE queue_tickets SET positionInQueue = :position WHERE ticketId = :ticketId")
    suspend fun updateQueuePosition(ticketId: String, position: Int)

    @Query("SELECT * FROM queue_tickets WHERE status = 'WAITING' ORDER BY priorityOrder ASC, checkInTime ASC LIMIT 1")
    suspend fun getNextPatient(): QueueTicketEntity?

    @Query("UPDATE queue_tickets SET status = 'IN_PROGRESS', startTime = :startTime WHERE ticketId = :ticketId")
    suspend fun startPatientConsultation(ticketId: String, startTime: Long)

    @Query("UPDATE queue_tickets SET status = 'COMPLETED', endTime = :endTime WHERE ticketId = :ticketId")
    suspend fun completePatientConsultation(ticketId: String, endTime: Long)

    @Query("UPDATE queue_tickets SET status = 'CANCELLED' WHERE ticketId = :ticketId")
    suspend fun cancelQueueTicket(ticketId: String)

    @Query("SELECT * FROM queue_tickets WHERE status = 'IN_PROGRESS' ORDER BY startTime DESC")
    suspend fun getInProgressTickets(): List<QueueTicketEntity>

    @Query("SELECT * FROM queue_tickets WHERE status = 'COMPLETED' AND endTime >= :since ORDER BY endTime DESC")
    suspend fun getCompletedTicketsSince(since: Long): List<QueueTicketEntity>

    @Query("SELECT COUNT(*) FROM queue_tickets WHERE status = 'COMPLETED' AND endTime >= :since")
    suspend fun getCompletedCountSince(since: Long): Int

    @Query("UPDATE queue_tickets SET isVerified = 1, verifiedAt = :verifiedAt, nurseNotes = :nurseNotes WHERE ticketId = :ticketId")
    suspend fun verifyTicket(ticketId: String, nurseNotes: String, verifiedAt: Long)

    @Query("SELECT * FROM queue_tickets WHERE ticketId = :ticketId")
    suspend fun getQueueTicketById(ticketId: String): QueueTicketEntity?

    @Query("SELECT COUNT(*) FROM queue_tickets WHERE syncStatus = 'PENDING'")
    suspend fun getPendingQueueCount(): Int

    @Query("SELECT * FROM queue_tickets WHERE syncStatus IN (:statuses)")
    suspend fun getQueueTicketsBySyncStatus(statuses: List<String>): List<QueueTicketEntity>

    @Query("SELECT * FROM appointments")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT COUNT(*) FROM queue_tickets WHERE status = 'IN_PROGRESS'")
    suspend fun getInProgressCount(): Int
}