// MedicalRecordRepository.kt
package com.kiminini.hospital.data.repository

import com.kiminini.hospital.data.database.HospitalDao
import com.kiminini.hospital.data.database.MedicalRecordEntity
import com.kiminini.hospital.data.model.MedicalRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MedicalRecordRepository(
    private val hospitalDao: HospitalDao
) {
    fun getMedicalRecordsByPatient(patientId: Long): Flow<List<MedicalRecord>> {
        return hospitalDao.getMedicalRecordsByPatient(patientId).map { entities ->
            entities.map { entity ->
                MedicalRecord(
                    id = entity.recordId,
                    type = entity.type,
                    title = entity.title,
                    doctor = entity.doctor,
                    date = entity.date,
                    status = entity.status,
                    notes = entity.notes,
                    fileUrl = entity.fileUrl
                )
            }
        }
    }

    suspend fun addMedicalRecord(
        patientId: Long,
        type: String,
        title: String,
        doctor: String,
        date: String,
        status: String = "Available",
        notes: String = "",
        fileUrl: String = ""
    ): String {
        val recordId = "MR-${System.currentTimeMillis()}"
        val record = MedicalRecordEntity(
            recordId = recordId,
            patientId = patientId,
            type = type,
            title = title,
            doctor = doctor,
            date = date,
            status = status,
            notes = notes,
            fileUrl = fileUrl
        )
        hospitalDao.insertMedicalRecord(record)
        return recordId
    }

    suspend fun addSampleMedicalRecords(patientId: Long) {
        // Check if records already exist
        val existingRecords = hospitalDao.getMedicalRecordsByPatient(patientId)
        var hasRecords = false
        existingRecords.collect { records ->
            hasRecords = records.isNotEmpty()
        }

        if (!hasRecords) {
            val sampleRecords = listOf(
                MedicalRecordEntity(
                    recordId = "LAB001",
                    patientId = patientId,
                    type = "Lab Results",
                    title = "Complete Blood Count (CBC)",
                    doctor = "Dr. Sarah Kimani",
                    date = "Mar 10, 2025",
                    status = "Available",
                    notes = "All values within normal range",
                    fileUrl = ""
                ),
                MedicalRecordEntity(
                    recordId = "LAB002",
                    patientId = patientId,
                    type = "Lab Results",
                    title = "Liver Function Test",
                    doctor = "Dr. James Omondi",
                    date = "Feb 28, 2025",
                    status = "Available",
                    notes = "Slightly elevated ALT levels",
                    fileUrl = ""
                ),
                MedicalRecordEntity(
                    recordId = "LAB003",
                    patientId = patientId,
                    type = "Lab Results",
                    title = "Thyroid Profile",
                    doctor = "Dr. Mary Atieno",
                    date = "Feb 15, 2025",
                    status = "Available",
                    notes = "Normal TSH levels",
                    fileUrl = ""
                ),
                MedicalRecordEntity(
                    recordId = "RX001",
                    patientId = patientId,
                    type = "Prescription",
                    title = "Amoxicillin 500mg",
                    doctor = "Dr. Sarah Kimani",
                    date = "Mar 12, 2025",
                    status = "Active",
                    notes = "Take twice daily for 7 days",
                    fileUrl = ""
                ),
                MedicalRecordEntity(
                    recordId = "RX002",
                    patientId = patientId,
                    type = "Prescription",
                    title = "Ibuprofen 400mg",
                    doctor = "Dr. Robert Kipchoge",
                    date = "Mar 1, 2025",
                    status = "Completed",
                    notes = "As needed for pain",
                    fileUrl = ""
                )
            )

            sampleRecords.forEach { record ->
                hospitalDao.insertMedicalRecord(record)
            }
        }
    }

    fun getMedicalRecordsByType(patientId: Long, type: String): Flow<List<MedicalRecord>> {
        return hospitalDao.getMedicalRecordsByPatient(patientId).map { entities ->
            entities.filter { it.type == type }.map { entity ->
                MedicalRecord(
                    id = entity.recordId,
                    type = entity.type,
                    title = entity.title,
                    doctor = entity.doctor,
                    date = entity.date,
                    status = entity.status,
                    notes = entity.notes,
                    fileUrl = entity.fileUrl
                )
            }
        }
    }
}