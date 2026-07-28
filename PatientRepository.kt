package com.kiminini.hospital.data.repository

import android.content.Context
import android.util.Log
import com.kiminini.hospital.data.database.AppointmentEntity
import com.kiminini.hospital.data.database.HospitalDao
import com.kiminini.hospital.data.database.PatientEntity
import com.kiminini.hospital.data.model.Patient
import com.kiminini.hospital.data.model.EmergencyContact
import com.kiminini.hospital.utils.NetworkMonitor
import com.kiminini.hospital.data.network.PatientNetwork
import com.kiminini.hospital.data.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PatientRepository(
    private val context: Context,
    private val hospitalDao: HospitalDao
) {
    private val networkMonitor = NetworkMonitor(context)

    suspend fun createPatient(
        patientId: String,
        name: String,
        phone: String,
        email: String,
        dateOfBirth: String,
        gender: String,
        address: String,
        bloodType: String,
        allergies: String,
        chronicConditions: String,
        primaryDoctor: String,
        emergencyContact: EmergencyContact
    ): Long {
        val isOnline = networkMonitor.isNetworkAvailable()
        val syncStatus = if (isOnline) "SYNCED" else "PENDING"

        val patient = PatientEntity(
            patientId = patientId,
            name = name,
            phone = phone,
            email = email,
            dateOfBirth = dateOfBirth,
            gender = gender,
            address = address,
            bloodType = bloodType,
            allergies = allergies,
            chronicConditions = chronicConditions,
            primaryDoctor = primaryDoctor,
            emergencyContactName = emergencyContact.name,
            emergencyContactRelationship = emergencyContact.relationship,
            emergencyContactPhone = emergencyContact.phone,
            syncStatus = syncStatus
        )
        val id = hospitalDao.insertPatient(patient)
        Log.d("PatientRepo", "Created patient with local id $id, phone: $phone, syncStatus: $syncStatus")

        if (isOnline) {
            syncPatientToCloud(patient.toPatient())
        }
        return id
    }

    suspend fun updatePatient(patient: Patient) {
        val entity = hospitalDao.getPatientByPatientId(patient.id)
        entity?.let {
            val isOnline = networkMonitor.isNetworkAvailable()
            val syncStatus = if (isOnline) "SYNCED" else "PENDING"

            val updated = it.copy(
                name = patient.name,
                phone = patient.phone,
                email = patient.email,
                dateOfBirth = patient.dateOfBirth,
                gender = patient.gender,
                address = patient.address,
                bloodType = patient.bloodType,
                allergies = patient.allergies,
                chronicConditions = patient.chronicConditions,
                primaryDoctor = patient.primaryDoctor,
                emergencyContactName = patient.emergencyContact.name,
                emergencyContactRelationship = patient.emergencyContact.relationship,
                emergencyContactPhone = patient.emergencyContact.phone,
                updatedAt = System.currentTimeMillis(),
                syncStatus = syncStatus
            )
            hospitalDao.updatePatient(updated)
            Log.d("PatientRepo", "Updated patient with id ${patient.id}, syncStatus: $syncStatus")

            if (isOnline) {
                syncPatientToCloud(patient)
            }
        }
    }

    fun getPatientById(patientId: String): Flow<Patient?> {
        return hospitalDao.getAllPatients().map { patients ->
            patients.find { it.patientId == patientId }?.toPatient()
        }
    }

    fun getAllPatients(): Flow<List<Patient>> {
        return hospitalDao.getAllPatients().map { entities ->
            entities.map { it.toPatient() }
        }
    }

    private fun normalizePhone(phone: String): String {
        val digits = phone.replace(Regex("[^0-9]"), "")
        return if (digits.length >= 9) digits.takeLast(9) else digits
    }

    suspend fun getPatientByPhone(phone: String): Patient? {
        val normalizedInput = normalizePhone(phone)
        Log.d("PatientRepo", "getPatientByPhone: input=$phone normalized=$normalizedInput")
        val patients = getAllPatients().firstOrNull() ?: emptyList()
        val result = patients.find {
            normalizePhone(it.phone) == normalizedInput
        }
        Log.d("PatientRepo", "Found: ${result?.name}")
        return result
    }

    // NEW method
    suspend fun getPatientLocalIdByPhone(phone: String): Long? {
        val normalizedInput = normalizePhone(phone)
        val patients = hospitalDao.getAllPatients().firstOrNull() ?: emptyList()
        return patients.find {
            normalizePhone(it.phone) == normalizedInput
        }?.id
    }

    suspend fun getLatestAppointment(patientLocalId: Long): AppointmentEntity? {
        val appointments = hospitalDao.getAppointmentsByPatient(patientLocalId).firstOrNull()
        return appointments?.maxByOrNull { it.date }
    }

    private suspend fun syncPatientToCloud(patient: Patient) {
        withContext(Dispatchers.IO) {
            try {
                val networkPatient = PatientNetwork(
                    id = patient.id,
                    name = patient.name,
                    phone = patient.phone,
                    email = patient.email,
                    dateOfBirth = patient.dateOfBirth,
                    gender = patient.gender,
                    address = patient.address,
                    bloodType = patient.bloodType,
                    allergies = patient.allergies,
                    chronicConditions = patient.chronicConditions,
                    primaryDoctor = patient.primaryDoctor,
                    emergencyContactName = patient.emergencyContact.name,
                    emergencyContactRelationship = patient.emergencyContact.relationship,
                    emergencyContactPhone = patient.emergencyContact.phone
                )
                val response = RetrofitClient.instance.savePatient(networkPatient)
                if (response.isSuccessful) {
                    Log.d("PatientRepo", "Patient synced to cloud: ${patient.id}")
                    val entity = hospitalDao.getPatientByPatientId(patient.id)
                    entity?.let {
                        hospitalDao.updatePatientSyncStatus(it.id, "SYNCED")
                    }
                } else {
                    Log.e("PatientRepo", "Cloud sync failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("PatientRepo", "Cloud sync error: ${e.message}")
            }
        }
    }

    suspend fun seedSamplePatients() {
        // existing code if any
    }

    private fun PatientEntity.toPatient(): Patient {
        return Patient(
            id = this.patientId,
            name = this.name,
            phone = this.phone,
            email = this.email,
            dateOfBirth = this.dateOfBirth,
            gender = this.gender,
            address = this.address,
            bloodType = this.bloodType,
            allergies = this.allergies,
            chronicConditions = this.chronicConditions,
            primaryDoctor = this.primaryDoctor,
            emergencyContact = EmergencyContact(
                name = this.emergencyContactName,
                relationship = this.emergencyContactRelationship,
                phone = this.emergencyContactPhone
            )
        )
    }
}