// Patient.kt - UPDATED VERSION
package com.kiminini.hospital.data.model

import androidx.compose.ui.graphics.Color

data class Patient(
    val id: String,
    var name: String,
    var phone: String,
    var email: String,
    var dateOfBirth: String,
    var gender: String,
    var address: String,
    var bloodType: String,
    var allergies: String,
    var chronicConditions: String,
    var primaryDoctor: String,
    var emergencyContact: EmergencyContact
)

data class EmergencyContact(
    var name: String,
    var relationship: String,
    var phone: String
)

data class Appointment(
    val id: String,
    var doctorName: String,
    var department: String,
    var date: String,
    var time: String,
    var status: String,
    var reason: String = ""
)

data class MedicalRecord(
    val id: String,
    var type: String,
    var title: String,
    var doctor: String,
    var date: String,
    var status: String,
    var notes: String = "",
    var fileUrl: String = ""
)