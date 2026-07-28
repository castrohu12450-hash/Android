package com.kiminini.hospital.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: String,
    val name: String,
    val phone: String,
    val email: String,
    val dateOfBirth: String,
    val gender: String,
    val address: String,
    val bloodType: String,
    val allergies: String,
    val chronicConditions: String,
    val primaryDoctor: String,
    val emergencyContactName: String,
    val emergencyContactRelationship: String,
    val emergencyContactPhone: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "SYNCED",
    val fcmToken: String? = null      // NEW: Firebase Cloud Messaging token
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appointmentId: String,
    val patientId: Long,
    val doctorName: String,
    val department: String,
    val date: String,
    val time: String,
    val status: String,
    val reason: String,
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "SYNCED"
)

@Entity(tableName = "medical_records")
data class MedicalRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recordId: String,
    val patientId: Long,
    val type: String,
    val title: String,
    val doctor: String,
    val date: String,
    val status: String,
    val notes: String,
    val fileUrl: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "dashboard_activities")
data class DashboardActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val activityId: String,
    val patientId: Long,
    val icon: String,
    val title: String,
    val description: String,
    val time: String,
    val isRemovable: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)