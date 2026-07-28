package com.kiminini.hospital.data.network

import com.google.gson.annotations.SerializedName

data class PatientNetwork(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String,
    @SerializedName("date_of_birth") val dateOfBirth: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("address") val address: String,
    @SerializedName("blood_type") val bloodType: String,
    @SerializedName("allergies") val allergies: String,
    @SerializedName("chronic_conditions") val chronicConditions: String,
    @SerializedName("primary_doctor") val primaryDoctor: String,
    @SerializedName("emergency_contact_name") val emergencyContactName: String,
    @SerializedName("emergency_contact_relationship") val emergencyContactRelationship: String,
    @SerializedName("emergency_contact_phone") val emergencyContactPhone: String
)