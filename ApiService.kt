package com.kiminini.hospital.data.network

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("get_patient.php")
    suspend fun getPatient(@Query("phone") phone: String): PatientNetwork

    @POST("save_patient.php")
    suspend fun savePatient(@Body patient: PatientNetwork): Response<Map<String, Any>>

    @POST("save_appointment.php")
    suspend fun saveAppointment(@Body appointment: AppointmentNetwork): Response<Map<String, Any>>
}