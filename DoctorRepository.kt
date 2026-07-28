package com.kiminini.hospital.data.repository

import com.kiminini.hospital.data.model.Doctor

object DoctorRepository {

    val allDoctors = listOf(
        // General Medicine
        Doctor("1", "Dr. James Omondi", "General Physician", "General Medicine"),
        Doctor("2", "Dr. Mary Wanjiku", "Family Medicine", "General Medicine"),
        Doctor("3", "Dr. John Kamau", "General Practice", "General Medicine"),
        Doctor("4", "Dr. Lucy Akinyi", "Internal Medicine", "General Medicine"),

        // Cardiology
        Doctor("5", "Dr. Sarah Kimani", "Cardiologist", "Cardiology"),
        Doctor("6", "Dr. Peter Odhiambo", "Interventional Cardiologist", "Cardiology"),
        Doctor("7", "Dr. Alice Muthoni", "Pediatric Cardiologist", "Cardiology"),
        Doctor("8", "Dr. Michael Otieno", "Cardiac Electrophysiologist", "Cardiology"),

        // Dermatology
        Doctor("9", "Dr. Brian Otieno", "Dermatologist", "Dermatology"),
        Doctor("10", "Dr. Cynthia Achieng", "Cosmetic Dermatologist", "Dermatology"),
        Doctor("11", "Dr. David Mwangi", "Pediatric Dermatologist", "Dermatology"),

        // Pediatrics
        Doctor("12", "Dr. Grace Wambui", "Pediatrician", "Pediatrics"),
        Doctor("13", "Dr. Michael Omondi", "Child Specialist", "Pediatrics"),
        Doctor("14", "Dr. Esther Njeri", "Neonatologist", "Pediatrics"),

        // Orthopedics
        Doctor("15", "Dr. David Mwangi", "Orthopedic Surgeon", "Orthopedics"),
        Doctor("16", "Dr. Lucy Njeri", "Sports Medicine", "Orthopedics"),
        Doctor("17", "Dr. Joseph Kipchoge", "Joint Replacement Specialist", "Orthopedics"),

        // Gynecology
        Doctor("18", "Dr. Elizabeth Akinyi", "Gynecologist", "Gynecology"),
        Doctor("19", "Dr. Faith Chebet", "OB-GYN", "Gynecology"),
        Doctor("20", "Dr. Margaret Waweru", "Reproductive Health", "Gynecology"),

        // Neurology
        Doctor("21", "Dr. Robert Kipchoge", "Neurologist", "Neurology"),
        Doctor("22", "Dr. Susan Jepkosgei", "Pediatric Neurologist", "Neurology"),
        Doctor("23", "Dr. Thomas Kiprono", "Neurosurgeon", "Neurology"),

        // ENT
        Doctor("24", "Dr. David Otieno", "ENT Specialist", "ENT (Ear, Nose, Throat)"),
        Doctor("25", "Dr. Jane Akinyi", "Audiologist", "ENT (Ear, Nose, Throat)"),
        Doctor("26", "Dr. Paul Kiprop", "Otolaryngologist", "ENT (Ear, Nose, Throat)"),

        // Ophthalmology
        Doctor("27", "Dr. Vincent Ochieng", "Ophthalmologist", "Ophthalmology"),
        Doctor("28", "Dr. Catherine Wanjiru", "Optometrist", "Ophthalmology"),
        Doctor("29", "Dr. Peter Kimani", "Cornea Specialist", "Ophthalmology"),

        // Dentistry
        Doctor("30", "Dr. Michael Mwangi", "Dentist", "Dentistry"),
        Doctor("31", "Dr. Ruth Achieng", "Orthodontist", "Dentistry"),
        Doctor("32", "Dr. Kenneth Otieno", "Oral Surgeon", "Dentistry"),

        // Psychiatry
        Doctor("33", "Dr. Francis Kimani", "Psychiatrist", "Psychiatry"),
        Doctor("34", "Dr. Esther Odhiambo", "Psychologist", "Psychiatry"),
        Doctor("35", "Dr. Beatrice Akoth", "Child Psychiatrist", "Psychiatry"),

        // Urology
        Doctor("36", "Dr. Charles Kipkorir", "Urologist", "Urology"),
        Doctor("37", "Dr. Beatrice Jepchirchir", "Urology Surgeon", "Urology"),
        Doctor("38", "Dr. Simon Kiprop", "Renal Specialist", "Urology")
    )

    fun getDoctorsByDepartment(department: String): List<Doctor> {
        return allDoctors.filter { it.department == department }
    }

    fun getDepartmentEmoji(department: String): String {
        return when(department) {
            "Cardiology" -> "❤️"
            "Dermatology" -> "🌿"
            "Pediatrics" -> "👶"
            "Orthopedics" -> "🦴"
            "Gynecology" -> "👩"
            "Neurology" -> "🧠"
            "Ophthalmology" -> "👁️"
            "ENT (Ear, Nose, Throat)" -> "👂"
            "Dentistry" -> "🦷"
            "Psychiatry" -> "🧠"
            "Urology" -> "💧"
            else -> "🏥"
        }
    }
}