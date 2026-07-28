package com.kiminini.hospital.ui.patient

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kiminini.hospital.ui.components.LoadingScreen
import com.kiminini.hospital.ui.viewmodel.PatientViewModel
import com.kiminini.hospital.ui.viewmodel.PatientViewModelFactory
import androidx.compose.foundation.border
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    phoneNumber: String,
    onLogout: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    // ✅ Use key = phoneNumber to force a new ViewModel when phone number changes
    val viewModel: PatientViewModel = viewModel(
        key = phoneNumber,
        factory = PatientViewModelFactory(context, phoneNumber)
    )
    val uiState by viewModel.uiState.collectAsState()
    var isEditMode by remember { mutableStateOf(false) }

    // Editable fields
    var editedName by remember { mutableStateOf("") }
    var editedEmail by remember { mutableStateOf("") }
    var editedPhone by remember { mutableStateOf("") }
    var editedDateOfBirth by remember { mutableStateOf("") }
    var editedGender by remember { mutableStateOf("") }
    var editedAddress by remember { mutableStateOf("") }
    var editedBloodType by remember { mutableStateOf("") }
    var editedAllergies by remember { mutableStateOf("") }
    var editedChronicConditions by remember { mutableStateOf("") }
    var editedPrimaryDoctor by remember { mutableStateOf("") }
    var editedEmergencyName by remember { mutableStateOf("") }
    var editedEmergencyRelationship by remember { mutableStateOf("") }
    var editedEmergencyPhone by remember { mutableStateOf("") }

    // Populate edit fields when patient data loads
    LaunchedEffect(uiState.patient) {
        uiState.patient?.let {
            editedName = it.name
            editedEmail = it.email
            editedPhone = it.phone
            editedDateOfBirth = it.dateOfBirth
            editedGender = it.gender
            editedAddress = it.address
            editedBloodType = it.bloodType
            editedAllergies = it.allergies
            editedChronicConditions = it.chronicConditions
            editedPrimaryDoctor = it.primaryDoctor
            editedEmergencyName = it.emergencyContact.name
            editedEmergencyRelationship = it.emergencyContact.relationship
            editedEmergencyPhone = it.emergencyContact.phone
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = (-50).dp, y = (-50).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "My Profile",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )
                // Edit / Save toggle
                TextButton(
                    onClick = {
                        if (isEditMode) {
                            val updatedPatient = uiState.patient?.copy(
                                name = editedName,
                                email = editedEmail,
                                phone = editedPhone,
                                dateOfBirth = editedDateOfBirth,
                                gender = editedGender,
                                address = editedAddress,
                                bloodType = editedBloodType,
                                allergies = editedAllergies,
                                chronicConditions = editedChronicConditions,
                                primaryDoctor = editedPrimaryDoctor,
                                emergencyContact = com.kiminini.hospital.data.model.EmergencyContact(
                                    name = editedEmergencyName,
                                    relationship = editedEmergencyRelationship,
                                    phone = editedEmergencyPhone
                                )
                            )
                            updatedPatient?.let {
                                viewModel.updateFullPatient(it)
                            }
                            isEditMode = false
                        } else {
                            isEditMode = true
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                ) {
                    Text(
                        if (isEditMode) "SAVE" else "EDIT",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            if (uiState.isLoading && uiState.patient == null) {
                LoadingScreen()
            } else {
                val patient = uiState.patient

                // Profile Header Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(
                                    Brush.sweepGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary,
                                            MaterialTheme.colorScheme.primary
                                        )
                                    ),
                                    CircleShape
                                )
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(92.dp)
                                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = patient?.name?.take(1)?.uppercase() ?: "👤",
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isEditMode) {
                            OutlinedTextField(
                                value = editedName,
                                onValueChange = { editedName = it },
                                label = { Text("Full Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            )
                        } else {
                            Text(
                                text = patient?.name ?: "Patient",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Card(
                            modifier = Modifier.padding(top = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(30.dp)
                        ) {
                            Text(
                                text = "ID: ${patient?.id ?: "----"}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(value = "0", label = "Visits", icon = "📅")
                            StatItem(value = "0", label = "Records", icon = "📋")
                            StatItem(value = "0", label = "Prescriptions", icon = "💊")
                        }
                    }
                }

                // Personal Information
                InfoSectionCard(
                    title = "Personal Information",
                    icon = "👤",
                    iconColor = MaterialTheme.colorScheme.primary
                ) {
                    EditableInfoRow(
                        icon = "📱",
                        label = "Phone Number",
                        value = editedPhone,
                        isEditable = isEditMode,
                        onValueChange = { editedPhone = it }
                    )
                    EditableInfoRow(
                        icon = "📧",
                        label = "Email Address",
                        value = editedEmail,
                        isEditable = isEditMode,
                        onValueChange = { editedEmail = it }
                    )
                    EditableInfoRow(
                        icon = "🎂",
                        label = "Date of Birth",
                        value = editedDateOfBirth,
                        isEditable = isEditMode,
                        onValueChange = { editedDateOfBirth = it }
                    )
                    EditableInfoRow(
                        icon = "⚥",
                        label = "Gender",
                        value = editedGender,
                        isEditable = isEditMode,
                        onValueChange = { editedGender = it }
                    )
                    EditableInfoRow(
                        icon = "📍",
                        label = "Address",
                        value = editedAddress,
                        isEditable = isEditMode,
                        onValueChange = { editedAddress = it }
                    )
                }

                // Medical Information
                InfoSectionCard(
                    title = "Medical Information",
                    icon = "🩺",
                    iconColor = MaterialTheme.colorScheme.secondary
                ) {
                    EditableInfoRow(
                        icon = "🩸",
                        label = "Blood Type",
                        value = editedBloodType,
                        isEditable = isEditMode,
                        onValueChange = { editedBloodType = it }
                    )
                    EditableInfoRow(
                        icon = "⚠️",
                        label = "Allergies",
                        value = editedAllergies,
                        isEditable = isEditMode,
                        onValueChange = { editedAllergies = it }
                    )
                    EditableInfoRow(
                        icon = "💊",
                        label = "Chronic Conditions",
                        value = editedChronicConditions,
                        isEditable = isEditMode,
                        onValueChange = { editedChronicConditions = it }
                    )
                    EditableInfoRow(
                        icon = "🏥",
                        label = "Primary Doctor",
                        value = editedPrimaryDoctor,
                        isEditable = isEditMode,
                        onValueChange = { editedPrimaryDoctor = it }
                    )
                }

                // Emergency Contact
                InfoSectionCard(
                    title = "Emergency Contact",
                    icon = "🚨",
                    iconColor = MaterialTheme.colorScheme.error
                ) {
                    EditableInfoRow(
                        icon = "👤",
                        label = "Contact Name",
                        value = editedEmergencyName,
                        isEditable = isEditMode,
                        onValueChange = { editedEmergencyName = it }
                    )
                    EditableInfoRow(
                        icon = "🤝",
                        label = "Relationship",
                        value = editedEmergencyRelationship,
                        isEditable = isEditMode,
                        onValueChange = { editedEmergencyRelationship = it }
                    )
                    EditableInfoRow(
                        icon = "📞",
                        label = "Phone Number",
                        value = editedEmergencyPhone,
                        isEditable = isEditMode,
                        onValueChange = { editedEmergencyPhone = it }
                    )
                }

                // Logout Button
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("LOGOUT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Kiminini Hospital Portal v1.0",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// Helper Composables
@Composable
fun StatItem(value: String, label: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 24.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun InfoSectionCard(
    title: String,
    icon: String,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(iconColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = iconColor)
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun EditableInfoRow(
    icon: String,
    label: String,
    value: String,
    isEditable: Boolean,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (isEditable) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            } else {
                Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}