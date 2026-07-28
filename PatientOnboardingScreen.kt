
package com.kiminini.hospital.ui.auth
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kiminini.hospital.ui.viewmodel.PatientViewModel
import com.kiminini.hospital.ui.viewmodel.PatientViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientOnboardingScreen(
    phoneNumber: String,
    onComplete: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: PatientViewModel = viewModel(factory = PatientViewModelFactory(context, phoneNumber))
    val coroutineScope = rememberCoroutineScope()

    var isSubmitting by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var chronicConditions by remember { mutableStateOf("") }

    var currentStep by remember { mutableStateOf(1) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val totalSteps = 3
    val progress = currentStep / totalSteps.toFloat()

    val stepComplete = when (currentStep) {
        1 -> fullName.isNotBlank() && dateOfBirth.isNotBlank() && gender.isNotBlank()
        2 -> email.isNotBlank() && address.isNotBlank()
        3 -> bloodType.isNotBlank()
        else -> false
    }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    val backgroundGradient = Brush.verticalGradient(colors = listOf(Color(0xFFF8F9FA), Color(0xFFE8F0FE)))

    Box(modifier = Modifier.fillMaxSize().background(backgroundGradient)) {
        Box(modifier = Modifier.size(250.dp).offset(x = (-50).dp, y = (-50).dp)
            .background(Brush.radialGradient(colors = listOf(Color(0xFF1A73E8).copy(alpha = 0.05f), Color.Transparent)), CircleShape))
        Box(modifier = Modifier.size(300.dp).align(Alignment.BottomEnd).offset(x = 100.dp, y = 100.dp)
            .background(Brush.radialGradient(colors = listOf(Color(0xFF34A853).copy(alpha = 0.05f), Color.Transparent)), CircleShape))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { -it })
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).shadow(4.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Complete Your Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F52BA))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            AnimatedStepIndicator(1, currentStep, "Personal")
                            AnimatedStepIndicator(2, currentStep, "Contact")
                            AnimatedStepIndicator(3, currentStep, "Medical")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF34A853),
                            trackColor = Color(0xFFE8F0FE)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE8E6)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("⚠️", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(errorMessage!!, color = Color(0xFFEA4335), fontSize = 14.sp)
                    }
                }
            }

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300)) using SizeTransform(clip = true)
                }
            ) { step ->
                when (step) {
                    1 -> {
                        Card(
                            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text("Personal Information", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A73E8), modifier = Modifier.padding(bottom = 16.dp))
                                OutlinedTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it },
                                    label = { Text("Full Name") },
                                    placeholder = { Text("Christine Wanyonyi") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name", tint = Color(0xFF1A73E8)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = dateOfBirth,
                                    onValueChange = { dateOfBirth = it },
                                    label = { Text("Date of Birth") },
                                    placeholder = { Text("MM/DD/YYYY") },
                                    leadingIcon = { Icon(Icons.Default.Cake, contentDescription = "Birthday", tint = Color(0xFF1A73E8)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text("Gender", fontSize = 14.sp, color = Color(0xFF5F6368), modifier = Modifier.padding(bottom = 8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                    Box(Modifier.weight(1f)) { GenderChip("Male", gender == "Male") { gender = "Male" } }
                                    Box(Modifier.weight(1f)) { GenderChip("Female", gender == "Female") { gender = "Female" } }
                                    Box(Modifier.weight(1f)) { GenderChip("Other", gender == "Other") { gender = "Other" } }
                                }
                            }
                        }
                    }
                    2 -> {
                        Card(
                            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text("Contact Information", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A73E8), modifier = Modifier.padding(bottom = 16.dp))
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = {},
                                    label = { Text("Phone Number") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone", tint = Color(0xFF1A73E8)) },
                                    enabled = false,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = Color(0xFF202124),
                                        disabledBorderColor = Color(0xFFDADCE0)
                                    )
                                )
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Email Address") },
                                    placeholder = { Text("christine@example.com") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = Color(0xFF1A73E8)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = address,
                                    onValueChange = { address = it },
                                    label = { Text("Address") },
                                    placeholder = { Text("123 Kiminini, Trans-Nzoia") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Address", tint = Color(0xFF1A73E8)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                    }
                    3 -> {
                        Card(
                            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text("Medical Information", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34A853), modifier = Modifier.padding(bottom = 16.dp))
                                Text("Blood Type", fontSize = 14.sp, color = Color(0xFF5F6368), modifier = Modifier.padding(bottom = 8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                                    listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-").forEach { type ->
                                        BloodTypeChip(type, bloodType == type) { bloodType = type }
                                    }
                                }
                                OutlinedTextField(
                                    value = allergies,
                                    onValueChange = { allergies = it },
                                    label = { Text("Allergies (comma separated)") },
                                    placeholder = { Text("Penicillin, Peanuts") },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                OutlinedTextField(
                                    value = chronicConditions,
                                    onValueChange = { chronicConditions = it },
                                    label = { Text("Chronic Conditions") },
                                    placeholder = { Text("Hypertension, Diabetes") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }
                    Spacer(Modifier.width(12.dp))
                }
                Button(
                    onClick = {
                        when (currentStep) {
                            1 -> if (fullName.isNotBlank() && dateOfBirth.isNotBlank() && gender.isNotBlank()) {
                                currentStep++
                                errorMessage = null
                            } else errorMessage = "Please fill all fields"
                            2 -> if (email.isNotBlank() && address.isNotBlank()) {
                                currentStep++
                                errorMessage = null
                            } else errorMessage = "Please fill all fields"
                            3 -> if (bloodType.isNotBlank()) {
                                if (isSubmitting) return@Button
                                isSubmitting = true
                                coroutineScope.launch {
                                    viewModel.savePatientProfile(
                                        name = fullName,
                                        email = email,
                                        dateOfBirth = dateOfBirth,
                                        gender = gender,
                                        address = address,
                                        bloodType = bloodType,
                                        allergies = allergies.ifBlank { "None" },
                                        chronicConditions = chronicConditions.ifBlank { "None" }
                                    ) { _ ->
                                        Toast.makeText(context, "Profile saved", Toast.LENGTH_SHORT).show()
                                        onComplete()
                                    }
                                }
                            } else errorMessage = "Please select blood type"
                        }
                    },
                    modifier = Modifier
                        .weight(if (currentStep > 1) 1f else 1f)
                        .height(50.dp)
                        .scale(if (stepComplete && !isSubmitting) buttonScale else 1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34A853)),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text(if (currentStep == totalSteps) "COMPLETE" else "NEXT", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "I'll complete this later",
                modifier = Modifier.clickable {
                    if (isSubmitting) return@clickable
                    isSubmitting = true
                    coroutineScope.launch {
                        viewModel.saveMinimalProfile(phoneNumber) { _ ->
                            Toast.makeText(context, "Minimal profile saved", Toast.LENGTH_SHORT).show()
                            onComplete()
                        }
                    }
                }.padding(16.dp),
                color = Color(0xFF5F6368),
                fontSize = 14.sp,
                textDecoration = TextDecoration.Underline
            )
        }
    }
}

@Composable
fun AnimatedStepIndicator(step: Int, currentStep: Int, label: String) {
    val isActive = step <= currentStep
    val isPassed = step < currentStep
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "stepScale"
    )
    val backgroundColor = if (isActive) Color(0xFF34A853) else Color(0xFFE8F0FE)
    val textColor = if (isActive) Color.White else Color(0xFF5F6368)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .scale(scale)
                .background(backgroundColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isPassed) {
                Icon(Icons.Default.Check, contentDescription = "Completed", tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
                Text(step.toString(), color = textColor, fontWeight = FontWeight.Bold)
            }
        }
        Text(label, fontSize = 11.sp, color = if (isActive) Color(0xFF34A853) else Color(0xFF5F6368), modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun GenderChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFF1A73E8) else Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            label,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            color = if (selected) Color.White else Color(0xFF202124),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun BloodTypeChip(type: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(50.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFFEA4335) else Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            type,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            textAlign = TextAlign.Center,
            color = if (selected) Color.White else Color(0xFF202124),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp
        )
    }
}