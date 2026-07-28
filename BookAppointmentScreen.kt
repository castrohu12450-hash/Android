package com.kiminini.hospital.ui.appointments

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kiminini.hospital.data.database.HospitalDatabase
import com.kiminini.hospital.data.model.Doctor
import com.kiminini.hospital.data.repository.AppointmentRepository
import com.kiminini.hospital.data.repository.DoctorRepository
import com.kiminini.hospital.ui.theme.LocalDarkMode
import com.kiminini.hospital.ui.viewmodel.AppointmentViewModel
import com.kiminini.hospital.ui.viewmodel.AppointmentViewModelFactory
import com.kiminini.hospital.utils.NotificationHelper
import com.kiminini.hospital.utils.SharedPrefsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(navController: NavController) {
    val context = LocalContext.current
    val database = HospitalDatabase.getDatabase(context)
    val appointmentRepository = AppointmentRepository(database.hospitalDao())
    val notificationHelper = NotificationHelper(context)
    val sharedPrefs = SharedPrefsManager(context)
    val patientLocalId = sharedPrefs.getPatientLocalId()

    val viewModel: AppointmentViewModel = viewModel(
        factory = AppointmentViewModelFactory(context, patientLocalId)
    )

    val isDarkMode = LocalDarkMode.current

    var selectedDepartment by remember { mutableStateOf("") }
    var selectedDoctor by remember { mutableStateOf<Doctor?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    var showDatePicker by remember { mutableStateOf(false) }
    var showBookingSuccess by remember { mutableStateOf(false) }

    // Live current time – updates every second
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = LocalTime.now()
        }
    }

    fun getAllTimeSlots(): List<String> = listOf(
        "9:00 AM", "9:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
        "12:00 PM", "12:30 PM", "1:00 PM", "1:30 PM", "2:00 PM", "2:30 PM",
        "3:00 PM", "3:30 PM", "4:00 PM", "4:30 PM", "5:00 PM"
    )

    fun parseSlotTo24Hour(slot: String): Pair<Int, Int> {
        val hour = slot.substringBefore(":").toInt()
        val minute = if (slot.contains(":30")) 30 else 0
        val isAm = slot.contains("AM")
        val convertedHour = when {
            isAm && hour == 12 -> 0
            !isAm && hour == 12 -> 12
            !isAm -> hour + 12
            else -> hour
        }
        return convertedHour to minute
    }

    val today = LocalDate.now()
    val isToday = selectedDate != null && selectedDate!!.isEqual(today)

    val timeSlots = if (isToday) {
        getAllTimeSlots().filter { slot ->
            val (slotHour, slotMinute) = parseSlotTo24Hour(slot)
            val currentHour = currentTime.hour
            val currentMinute = currentTime.minute
            slotHour > currentHour || (slotHour == currentHour && slotMinute > currentMinute)
        }
    } else {
        getAllTimeSlots()
    }

    val currentStep = remember(selectedDepartment, selectedDoctor, selectedDate, selectedTime, reason) {
        when {
            selectedDepartment.isEmpty() -> 1
            selectedDoctor == null -> 2
            selectedDate == null -> 3
            selectedTime.isEmpty() -> 4
            reason.isEmpty() -> 5
            else -> 6
        }
    }

    val departments = listOf(
        "General Medicine", "Cardiology", "Dermatology", "Pediatrics",
        "Orthopedics", "Gynecology", "Neurology", "Ophthalmology",
        "ENT", "Dentistry", "Psychiatry", "Urology"
    )

    val primaryColor = if (isDarkMode) Color(0xFF8AB4F8) else Color(0xFF1A73E8)
    val secondaryColor = if (isDarkMode) Color(0xFF81C995) else Color(0xFF34A853)
    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF5F7FA)
    val textPrimary = if (isDarkMode) Color.White else Color(0xFF202124)
    val textSecondary = if (isDarkMode) Color(0xFF9AA0A6) else Color(0xFF5F6368)
    val cardSelectedColor = if (isDarkMode) primaryColor.copy(alpha = 0.2f) else Color(0xFFE8F0FE)

    LaunchedEffect(selectedDoctor, selectedDate, selectedTime) {
        if (selectedDoctor != null && selectedDate != null && selectedTime.isNotEmpty()) {
            try {
                val isAvailable = appointmentRepository.isTimeSlotAvailable(
                    selectedDoctor!!.name,
                    selectedDate!!.format(dateFormatter),
                    selectedTime
                )
                if (!isAvailable) {
                    Toast.makeText(context, "Time slot taken. Choose another.", Toast.LENGTH_SHORT).show()
                    selectedTime = ""
                }
            } catch (_: Exception) { }
        }
    }

    LaunchedEffect(selectedDepartment) {
        if (selectedDepartment.isNotEmpty()) {
            selectedDoctor = null
        }
    }

    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showBookingSuccess) {
        if (showBookingSuccess) {
            delay(2000)
            showBookingSuccess = false
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Appointment", fontWeight = FontWeight.SemiBold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor,
                    titleContentColor = primaryColor
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, tint = primaryColor, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .background(backgroundColor)
                    .padding(16.dp)
            ) {
                // Progress Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AppRegistration, tint = primaryColor, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Booking Progress", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textSecondary)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = currentStep / 6f,
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = primaryColor,
                            trackColor = if (isDarkMode) Color(0xFF37474F) else Color(0xFFE8F0FE)
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                            for (step in 1..6) {
                                AnimatedCheckmark(
                                    completed = step <= currentStep,
                                    stepNumber = step,
                                    primaryColor = primaryColor,
                                    textColor = textPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Hero Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isDarkMode)
                                Brush.horizontalGradient(colors = listOf(Color(0xFF1A237E), Color(0xFF004D40)))
                            else
                                Brush.horizontalGradient(colors = listOf(Color(0xFF1A73E8), Color(0xFF34A853)))
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text("Schedule Your Visit", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Fill in details to book with a specialist", fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f))
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Department
                SectionHeader(
                    icon = Icons.Default.MedicalServices,
                    title = "Department",
                    step = 1,
                    completed = selectedDepartment.isNotEmpty(),
                    primaryColor = primaryColor,
                    textColor = textPrimary
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(departments) { dept ->
                        FilterChip(
                            selected = selectedDepartment == dept,
                            onClick = { selectedDepartment = dept },
                            label = { Text(dept) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = Color.White,
                                disabledContainerColor = surfaceColor,
                                disabledLabelColor = textSecondary
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                    }
                }

                // Doctor selection
                if (selectedDepartment.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    SectionHeader(
                        icon = Icons.Default.Person,
                        title = "Doctor",
                        step = 2,
                        completed = selectedDoctor != null,
                        primaryColor = primaryColor,
                        textColor = textPrimary
                    )
                    Spacer(Modifier.height(8.dp))

                    if (uiState.isLoading) {
                        repeat(2) {
                            ShimmerDoctorCard(surfaceColor = surfaceColor)
                            Spacer(Modifier.height(8.dp))
                        }
                    } else {
                        val doctors = DoctorRepository.getDoctorsByDepartment(selectedDepartment)
                        if (doctors.isEmpty()) {
                            Text("No doctors available", color = Color(0xFFEA4335))
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                doctors.forEach { doctor ->
                                    DoctorCardEnhanced(
                                        doctor = doctor,
                                        isSelected = selectedDoctor?.id == doctor.id,
                                        onSelect = { selectedDoctor = doctor },
                                        primaryColor = primaryColor,
                                        secondaryColor = secondaryColor,
                                        cardSelectedColor = cardSelectedColor,
                                        surfaceColor = surfaceColor,
                                        textPrimary = textPrimary,
                                        textSecondary = textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Date selection
                if (selectedDoctor != null) {
                    Spacer(Modifier.height(24.dp))
                    SectionHeader(
                        icon = Icons.Default.Event,
                        title = "Date",
                        step = 3,
                        completed = selectedDate != null,
                        primaryColor = primaryColor,
                        textColor = textPrimary
                    )
                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = surfaceColor, contentColor = primaryColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(selectedDate?.let { dateFormatter.format(it) } ?: "Choose a date")
                    }

                    if (showDatePicker) {
                        val todayMillis = LocalDate.now().atStartOfDay(java.time.ZoneOffset.UTC).toEpochSecond() * 1000
                        val datePickerState = rememberDatePickerState(
                            initialSelectedDateMillis = selectedDate?.atStartOfDay()?.toEpochSecond(java.time.ZoneOffset.UTC)?.times(1000)
                                ?: todayMillis
                        )
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            val newDate = LocalDate.ofEpochDay(millis / 86400000)
                                            if (newDate.isBefore(LocalDate.now())) {
                                                Toast.makeText(context, "Cannot select a past date", Toast.LENGTH_SHORT).show()
                                            } else {
                                                selectedDate = newDate
                                            }
                                        }
                                        showDatePicker = false
                                    }
                                ) { Text("OK") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }

                    if (selectedDate != null) {
                        Text(
                            text = "✓ ${selectedDate?.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))}",
                            fontSize = 13.sp,
                            color = secondaryColor,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Time selection
                if (selectedDate != null && selectedDoctor != null) {
                    Spacer(Modifier.height(24.dp))
                    SectionHeader(
                        icon = Icons.Default.Schedule,
                        title = "Time",
                        step = 4,
                        completed = selectedTime.isNotEmpty(),
                        primaryColor = primaryColor,
                        textColor = textPrimary
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Available time slots",
                            fontSize = 14.sp,
                            color = textSecondary
                        )
                        TextButton(
                            onClick = { currentTime = LocalTime.now() },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Refresh")
                        }
                    }

                    if (timeSlots.isEmpty()) {
                        Text(
                            text = "No available time slots for today (all passed). Please choose another date.",
                            fontSize = 13.sp,
                            color = Color(0xFFEA4335),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(timeSlots) { slot ->
                                FilterChip(
                                    selected = selectedTime == slot,
                                    onClick = { selectedTime = slot },
                                    label = { Text(slot) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = secondaryColor,
                                        selectedLabelColor = Color.White,
                                        disabledContainerColor = surfaceColor,
                                        disabledLabelColor = textSecondary
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                )
                            }
                        }
                    }
                    if (selectedTime.isNotEmpty()) {
                        Text(
                            text = "✓ $selectedTime",
                            fontSize = 13.sp,
                            color = secondaryColor,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Reason
                if (selectedTime.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    SectionHeader(
                        icon = Icons.Default.EditNote,
                        title = "Reason",
                        step = 5,
                        completed = reason.isNotEmpty(),
                        primaryColor = primaryColor,
                        textColor = textPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        placeholder = { Text("Describe symptoms or reason for visit...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = textSecondary,
                            focusedLabelColor = primaryColor,
                            cursorColor = primaryColor
                        ),
                        minLines = 3
                    )
                }

                // Summary Card
                if (selectedDoctor != null && selectedDate != null && selectedTime.isNotEmpty() && reason.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardSelectedColor),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, tint = secondaryColor, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Appointment Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textPrimary)
                            }
                            Spacer(Modifier.height(12.dp))
                            SummaryRow("Doctor", selectedDoctor!!.name, Icons.Default.Person, primaryColor = primaryColor, textSecondary = textSecondary)
                            SummaryRow("Department", selectedDoctor!!.department, Icons.Default.MedicalServices, primaryColor = primaryColor, textSecondary = textSecondary)
                            SummaryRow("Date", selectedDate!!.format(dateFormatter), Icons.Default.Event, primaryColor = primaryColor, textSecondary = textSecondary)
                            SummaryRow("Time", selectedTime, Icons.Default.Schedule, primaryColor = primaryColor, textSecondary = textSecondary)
                            SummaryRow("Reason", reason, Icons.Default.EditNote, primaryColor = primaryColor, textSecondary = textSecondary)
                        }
                    }
                }

                // Confirm Button
                Button(
                    onClick = {
                        if (selectedDate != null && selectedDate!!.isBefore(LocalDate.now())) {
                            Toast.makeText(context, "Cannot book appointment on a past date", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (isToday) {
                            val (selectedHour, selectedMinute) = parseSlotTo24Hour(selectedTime)
                            if (selectedHour < currentTime.hour || (selectedHour == currentTime.hour && selectedMinute <= currentTime.minute)) {
                                Toast.makeText(context, "Cannot book a time that has already passed today", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                        }
                        showConfirmDialog = true
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = selectedDoctor != null && selectedDate != null && selectedTime.isNotEmpty() && reason.isNotEmpty() && !uiState.isLoading && timeSlots.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        disabledContainerColor = textSecondary.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("CONFIRM & BOOK", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "You'll receive a confirmation notification",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = textSecondary,
                    fontSize = 12.sp
                )
            }

            if (showBookingSuccess) {
                BookingSuccessAnimation()
            }
        }
    }

    if (showConfirmDialog && selectedDoctor != null && selectedDate != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Appointment", fontWeight = FontWeight.Bold, color = textPrimary) },
            text = {
                Column {
                    Text("👨‍⚕️ ${selectedDoctor!!.name}", color = textPrimary)
                    Text("🏥 ${selectedDoctor!!.department}", color = textPrimary)
                    Text("📅 ${selectedDate!!.format(dateFormatter)}", color = textPrimary)
                    Text("⏰ $selectedTime", color = textPrimary)
                    Text("📝 $reason", color = textPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text("Confirm this appointment?", fontWeight = FontWeight.Medium, color = textPrimary)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        val actualDate = selectedDate!!.format(dateFormatter)
                        viewModel.bookAppointment(
                            doctorName = selectedDoctor!!.name,
                            department = selectedDoctor!!.department,
                            date = actualDate,
                            time = selectedTime,
                            reason = reason
                        )
                        notificationHelper.scheduleAppointmentReminder(
                            "Appointment on $actualDate at $selectedTime with ${selectedDoctor!!.name}",
                            System.currentTimeMillis()
                        )
                        showBookingSuccess = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
                ) {
                    Text("Yes, Book")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ============================
// Helper Composables
// ============================

@Composable
fun AnimatedCheckmark(completed: Boolean, stepNumber: Int, primaryColor: Color, textColor: Color) {
    val scale by animateFloatAsState(targetValue = if (completed) 1f else 0.8f, animationSpec = spring())
    val color = if (completed) primaryColor else Color(0xFFDADCE0)
    Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
        if (completed) {
            Icon(Icons.Default.CheckCircle, tint = color, contentDescription = "Completed", modifier = Modifier.scale(scale))
        } else {
            Text("$stepNumber", fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SectionHeader(icon: ImageVector, title: String, step: Int, completed: Boolean, primaryColor: Color, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = if (completed) primaryColor else Color(0xFF9AA0A6), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = if (completed) primaryColor else textColor)
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (completed) primaryColor else Color(0xFFE8F0FE))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text("Step $step", fontSize = 10.sp, color = if (completed) Color.White else primaryColor)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorCardEnhanced(
    doctor: Doctor,
    isSelected: Boolean,
    onSelect: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    cardSelectedColor: Color,
    surfaceColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) cardSelectedColor else surfaceColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        onClick = onSelect
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(primaryColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text("👨‍⚕️", fontSize = 28.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(doctor.name, fontWeight = FontWeight.Bold, color = primaryColor)
                Text(doctor.specialty, fontSize = 12.sp, color = textSecondary)
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, tint = secondaryColor, contentDescription = "Selected")
            }
        }
    }
}

@Composable
fun ShimmerDoctorCard(surfaceColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(24.dp)).background(Color.LightGray))
            Spacer(Modifier.width(12.dp))
            Column {
                Box(modifier = Modifier.width(120.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).background(Color.LightGray))
                Spacer(Modifier.height(4.dp))
                Box(modifier = Modifier.width(80.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).background(Color.LightGray))
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, icon: ImageVector, primaryColor: Color, textSecondary: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = textSecondary)
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 14.sp, color = textSecondary)
        }
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primaryColor)
    }
}

@Composable
fun BookingSuccessAnimation() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(Icons.Default.CheckCircle, tint = Color(0xFF34A853), contentDescription = "Success", modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                Text("Appointment Booked!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202124))
                Text("You'll receive a reminder shortly.", fontSize = 14.sp, color = Color(0xFF5F6368), textAlign = TextAlign.Center)
            }
        }
    }
}