package com.kiminini.hospital.ui.patient

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kiminini.hospital.data.model.queue.TriageData
import com.kiminini.hospital.ui.viewmodel.QueueViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriageScreen(
    navController: NavController,
    patientId: String,
    patientName: String,
    queueViewModel: QueueViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentStep by remember { mutableStateOf(0) }
    val answers = remember { mutableStateMapOf<String, Int>() }
    var age by remember { mutableStateOf("") }
    var ageError by remember { mutableStateOf<String?>(null) }
    var showSummary by remember { mutableStateOf(false) }
    var currentScore by remember { mutableStateOf(0) }
    var currentPriority by remember { mutableStateOf("") }
    var redFlags by remember { mutableStateOf<List<String>>(emptyList()) }
    var isJoining by remember { mutableStateOf(false) }

    val questions = TriageData.questions
    val totalSteps = questions.size + 1
    val progress = (currentStep + 1).toFloat() / totalSteps

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Triage Questionnaire", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!showSummary) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Warning Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Important Notice", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                Text("A nurse will verify your responses. False claims may result in penalties.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    if (currentStep == 0) "Age Information" else "Question $currentStep of ${questions.size}",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "${(progress * 100).roundToInt()}%",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Main Content Card – fixed to avoid overflow
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), // Takes remaining space, pushes buttons to bottom
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(24.dp)
                        ) {
                            when (currentStep) {
                                0 -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .background(Brush.horizontalGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🎂", fontSize = 28.sp)
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text("What is your age?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.height(24.dp))
                                        OutlinedTextField(
                                            value = age,
                                            onValueChange = { if (it.all { c -> c.isDigit() }) { age = it; ageError = null } },
                                            label = { Text("Age in years") },
                                            placeholder = { Text("e.g., 45") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(16.dp),
                                            isError = ageError != null,
                                            supportingText = { ageError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                            )
                                        )
                                    }
                                }
                                else -> {
                                    val questionIndex = currentStep - 1
                                    val question = questions[questionIndex]
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .background(Brush.horizontalGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                when (question.id) {
                                                    "breathing" -> "🫁"
                                                    "chest_pain" -> "❤️"
                                                    "consciousness" -> "🧠"
                                                    "bleeding" -> "🩸"
                                                    "fever" -> "🌡️"
                                                    "pain" -> "😖"
                                                    "symptom_duration" -> "⏱️"
                                                    "chronic_conditions" -> "📋"
                                                    "medications" -> "💊"
                                                    "exposure" -> "🦠"
                                                    else -> "📋"
                                                },
                                                fontSize = 28.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = question.question,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (question.requiresVerification) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "⚠️ This will be verified by a nurse",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            question.options.forEach { option ->
                                                val isUrgent = option.isUrgent
                                                val needsVerification = option.requiresNurseOverride
                                                Button(
                                                    onClick = {
                                                        answers[question.id] = option.score
                                                        if (questionIndex < questions.size - 1) {
                                                            currentStep++
                                                        } else {
                                                            val ageInt = age.toIntOrNull() ?: 0
                                                            val (priorityScore, _) = TriageData.calculatePriorityScoreWithBreakdown(answers.toMap(), ageInt)
                                                            currentScore = priorityScore
                                                            redFlags = TriageData.detectRedFlags(answers.toMap())
                                                            val (adjPriority, _) = TriageData.calculateVerifiedPriority(priorityScore, redFlags)
                                                            currentPriority = adjPriority.name
                                                            showSummary = true
                                                        }
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isUrgent) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                                                    ),
                                                    shape = RoundedCornerShape(16.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                        horizontalArrangement = Arrangement.Start
                                                    ) {
                                                        if (isUrgent) {
                                                            Text("🚨", fontSize = 18.sp)
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                        }
                                                        Text(
                                                            text = option.text,
                                                            fontSize = 16.sp,
                                                            color = if (isUrgent) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        if (needsVerification) {
                                                            Text(
                                                                text = "Nurse verify",
                                                                fontSize = 10.sp,
                                                                color = MaterialTheme.colorScheme.error
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Navigation Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (currentStep > 0) {
                            OutlinedButton(
                                onClick = { currentStep-- },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Previous", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Previous")
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Button(
                            onClick = {
                                when (currentStep) {
                                    0 -> {
                                        val ageInt = age.toIntOrNull()
                                        if (ageInt != null && ageInt in 1..120) {
                                            currentStep++
                                            ageError = null
                                        } else {
                                            ageError = "Please enter a valid age (1-120)"
                                        }
                                    }
                                    else -> { /* No action – answers captured by button clicks */ }
                                }
                            },
                            modifier = Modifier.weight(if (currentStep > 0) 1f else 1f),
                            enabled = when (currentStep) {
                                0 -> age.isNotBlank() && ageError == null
                                else -> true
                            }
                        ) {
                            Text(if (currentStep == totalSteps - 1) "Finish" else "Next")
                        }
                    }
                }
            } else {
                // Summary screen – scrollable
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val priorityColor = when (currentPriority) {
                        "CRITICAL" -> MaterialTheme.colorScheme.error
                        "HIGH" -> MaterialTheme.colorScheme.tertiary
                        "NORMAL" -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.primary
                    }
                    val priorityBgColor = when (currentPriority) {
                        "CRITICAL" -> MaterialTheme.colorScheme.errorContainer
                        "HIGH" -> MaterialTheme.colorScheme.tertiaryContainer
                        "NORMAL" -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }

                    // Success icon
                    AnimatedVisibility(visible = true, enter = scaleIn() + fadeIn()) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(MaterialTheme.colorScheme.secondary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Complete", tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(48.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Triage Complete!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Your responses have been recorded", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(24.dp))

                    // Priority card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = priorityBgColor),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Your Priority Level", fontSize = 14.sp, color = priorityColor)
                            if (currentPriority == "CRITICAL") {
                                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                val scale by infiniteTransition.animateFloat(
                                    initialValue = 1f,
                                    targetValue = 1.1f,
                                    animationSpec = infiniteRepeatable(animation = tween(500), repeatMode = RepeatMode.Reverse),
                                    label = "scale"
                                )
                                Text(currentPriority, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = priorityColor, modifier = Modifier.scale(scale))
                            } else {
                                Text(currentPriority, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = priorityColor)
                            }
                            Text("Score: $currentScore / 10", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (redFlags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("⚠️ Inconsistencies Detected", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                redFlags.take(2).forEach { flag ->
                                    Text("• $flag", fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                                Text("A nurse will review your case.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("What this means", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = when (currentPriority) {
                                    "CRITICAL" -> "⚠️ Immediate attention required. You will be seen as soon as possible. (Nurse verification required)"
                                    "HIGH" -> "🔴 Urgent case. You will be prioritized over normal cases. (Nurse verification required)"
                                    "NORMAL" -> "🟢 Standard priority. You will be seen in order of arrival."
                                    else -> "✅ Low priority. Routine cases that can wait."
                                },
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showSummary = false
                                currentStep = 0
                                answers.clear()
                                age = ""
                                redFlags = emptyList()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Restart", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restart")
                        }

                        Button(
                            onClick = {
                                if (isJoining) return@Button
                                val ageInt = age.toIntOrNull() ?: 0
                                isJoining = true
                                coroutineScope.launch {
                                    try {
                                        queueViewModel.submitTriage(answers.toMap(), ageInt)
                                        navController.navigate("queue") { popUpTo("triage") { inclusive = true } }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Failed to join queue: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isJoining = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            enabled = !isJoining
                        ) {
                            if (isJoining) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onSecondary)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Join Queue", modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Join Queue", color = MaterialTheme.colorScheme.onSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}