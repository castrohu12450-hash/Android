package com.kiminini.hospital.data.network

import com.google.gson.annotations.SerializedName

data class AppointmentNetwork(
    @SerializedName("appointment_id") val appointmentId: String,
    @SerializedName("patient_id") val patientId: String,
    @SerializedName("patient_name") val patientName: String,
    @SerializedName("doctor_name") val doctorName: String,
    @SerializedName("department") val department: String,
    @SerializedName("appointment_date") val appointmentDate: String,
    @SerializedName("appointment_time") val appointmentTime: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("status") val status: String
)