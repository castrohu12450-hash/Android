// DashboardModels.kt
package com.kiminini.hospital.data.model

data class DashboardActivity(
    val id: String,
    val icon: String,
    val title: String,
    val description: String,
    val time: String,
    val isRemovable: Boolean = false
)