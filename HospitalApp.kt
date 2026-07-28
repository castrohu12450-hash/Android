package com.kiminini.hospital

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.kiminini.hospital.data.database.HospitalDatabase
import com.kiminini.hospital.data.firestore.FirestoreService
import com.kiminini.hospital.data.repository.*
import com.kiminini.hospital.ui.appointments.AppointmentsListScreen
import com.kiminini.hospital.ui.appointments.BookAppointmentScreen
import com.kiminini.hospital.ui.auth.LoginScreen
import com.kiminini.hospital.ui.auth.OTPScreen
import com.kiminini.hospital.ui.auth.PatientOnboardingScreen
import com.kiminini.hospital.ui.patient.*
import com.kiminini.hospital.ui.staff.StaffDashboardScreen
import com.kiminini.hospital.ui.viewmodel.*
import com.kiminini.hospital.utils.SharedPrefsManager
import com.kiminini.hospital.work.QueueAdvanceWorker
import com.kiminini.hospital.work.SyncWorker
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun HospitalApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    val database = HospitalDatabase.getDatabase(context)
    val firestoreService = FirestoreService()
    val queueRepository = QueueRepository(database.hospitalDao(), firestoreService)
    val medicalRecordRepository = MedicalRecordRepository(database.hospitalDao())
    val patientRepository = PatientRepository(context, database.hospitalDao())
    val coroutineScope = rememberCoroutineScope()
    val sharedPrefs = SharedPrefsManager(context)

    fun isStaffNumber(phoneNumber: String): Boolean {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9]"), "")
        val staffNumbers = listOf(
            "722222222", "722222223", "722222224", "722222225", "722222226",
            "711111111", "711111112", "711111113", "711111114", "711111115",
            "711111116", "711111117", "711111118", "711111119", "711111120",
            "711111121", "711111122", "711111123", "711111124", "711111125",
            "711111126", "711111127", "711111128", "711111129", "711111130",
            "711111131", "711111132", "711111133", "711111134", "711111135",
            "711111136", "711111137", "711111138", "711111139", "711111140",
            "711111141", "711111142", "711111143", "711111144", "711111145",
            "711111146", "711111147", "711111148"
        )
        return staffNumbers.contains(cleanNumber)
    }

    LaunchedEffect(Unit) {
        val queueAdvanceRequest = PeriodicWorkRequestBuilder<QueueAdvanceWorker>(5, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("queue_worker", ExistingPeriodicWorkPolicy.KEEP, queueAdvanceRequest)

        // TEMPORARY: One-time sync for testing appointments
        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "sync_now",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onNavigateToOTP = { phoneNumber ->
                navController.navigate("otp/$phoneNumber")
            })
        }

        composable("otp/{phoneNumber}") { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            OTPScreen(
                phoneNumber = phoneNumber,
                navController = navController,
                onBack = { navController.popBackStack() },
                onVerificationSuccess = { rawPhone ->
                    coroutineScope.launch {
                        sharedPrefs.saveUserPhone(rawPhone)
                        if (isStaffNumber(rawPhone)) {
                            Log.d("HospitalApp", "Staff login, navigating to staff dashboard")
                            navController.navigate("staff_dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                            return@launch
                        }
                        val savedPatientId = sharedPrefs.getPatientId()
                        if (savedPatientId != null) {
                            val patient = patientRepository.getPatientById(savedPatientId).firstOrNull()
                            if (patient != null) {
                                Log.d("HospitalApp", "Found patient by saved ID")
                                sharedPrefs.savePatientName(patient.name)
                                navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                                return@launch
                            }
                        }
                        val existingPatient = patientRepository.getPatientByPhone(rawPhone)
                        if (existingPatient != null) {
                            sharedPrefs.savePatientId(existingPatient.id)
                            sharedPrefs.savePatientName(existingPatient.name)
                            val localId = patientRepository.getPatientLocalIdByPhone(rawPhone)
                            if (localId != null) sharedPrefs.savePatientLocalId(localId)
                            Log.d("HospitalApp", "Found patient by phone")
                            navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                        } else {
                            Log.d("HospitalApp", "New patient, going to onboarding with phone: $rawPhone")
                            navController.currentBackStackEntry?.savedStateHandle?.set("rawPhone", rawPhone)
                            navController.navigate("onboarding")
                        }
                    }
                }
            )
        }

        composable("onboarding") { backStackEntry ->
            val rawPhone = backStackEntry.savedStateHandle.get<String>("rawPhone") ?: sharedPrefs.getUserPhone() ?: ""
            Log.d("HospitalApp", "Onboarding with phone: $rawPhone")
            PatientOnboardingScreen(
                phoneNumber = rawPhone,
                onComplete = {
                    navController.navigate("dashboard") { popUpTo("onboarding") { inclusive = true } }
                },
                navController = navController
            )
        }

        composable("dashboard") {
            val patientName = sharedPrefs.getPatientName() ?: "Patient"
            val dashboardViewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModelFactory(context, queueRepository, patientName)
            )
            DashboardScreen(
                patientName = patientName,
                onLogout = {
                    authViewModel.logout()
                    sharedPrefs.clearPatientId()
                    sharedPrefs.clearPatientLocalId()
                    sharedPrefs.clearUserPhone()
                    sharedPrefs.clearPatientName()
                    navController.navigate("login") { popUpTo("dashboard") { inclusive = true } }
                },
                navController = navController,
                viewModel = dashboardViewModel
            )
        }

        composable("book_appointment") {
            BookAppointmentScreen(navController = navController)
        }

        composable("appointments_list") {
            val patientLocalId = sharedPrefs.getPatientLocalId()
            if (patientLocalId == -1L) {
                navController.navigate("login") { popUpTo("appointments_list") { inclusive = true } }
                return@composable
            }
            val appointmentViewModel: AppointmentViewModel = viewModel(
                factory = AppointmentViewModelFactory(context, patientLocalId)
            )
            AppointmentsListScreen(navController = navController, viewModel = appointmentViewModel)
        }

        composable("medical_records") {
            MedicalRecordsScreen(navController = navController)
        }

        composable("profile") {
            val phoneNumber = sharedPrefs.getUserPhone() ?: authState.phoneNumber ?: ""
            ProfileScreen(
                phoneNumber = phoneNumber,
                onLogout = {
                    authViewModel.logout()
                    sharedPrefs.clearPatientId()
                    sharedPrefs.clearPatientLocalId()
                    sharedPrefs.clearUserPhone()
                    sharedPrefs.clearPatientName()
                    navController.navigate("login") { popUpTo("profile") { inclusive = true } }
                },
                navController = navController
            )
        }

        composable("more") {
            MoreScreen(navController = navController)
        }

        composable("billing") {
            BillingScreen(navController = navController)
        }

        composable("health_metrics") {
            HealthMetricsScreen(navController = navController)
        }

        composable("settings") {
            SettingsScreen(
                navController = navController,
                onLogout = {
                    authViewModel.logout()
                    sharedPrefs.clearPatientId()
                    sharedPrefs.clearPatientLocalId()
                    sharedPrefs.clearUserPhone()
                    sharedPrefs.clearPatientName()
                    navController.navigate("login") { popUpTo("settings") { inclusive = true } }
                }
            )
        }

        composable("support") {
            SupportScreen(navController = navController)
        }

        composable("triage") {
            val patientId = sharedPrefs.getUserPhone() ?: ""
            if (patientId.isEmpty()) {
                Log.e("HospitalApp", "No phone number found, returning to login")
                navController.navigate("login") { popUpTo("triage") { inclusive = true } }
                return@composable
            }
            val patientName = sharedPrefs.getPatientName() ?: "Patient"
            val queueViewModel: QueueViewModel = viewModel(
                factory = QueueViewModelFactory(queueRepository, patientId, patientName)
            )
            TriageScreen(
                navController = navController,
                patientId = patientId,
                patientName = patientName,
                queueViewModel = queueViewModel
            )
        }

        composable("queue") {
            val patientId = sharedPrefs.getUserPhone() ?: ""
            if (patientId.isEmpty()) {
                navController.navigate("login") { popUpTo("queue") { inclusive = true } }
                return@composable
            }
            val patientName = sharedPrefs.getPatientName() ?: "Patient"
            val queueViewModel: QueueViewModel = viewModel(
                factory = QueueViewModelFactory(queueRepository, patientId, patientName)
            )
            QueueScreen(
                navController = navController,
                queueViewModel = queueViewModel
            )
        }

        composable("staff_dashboard") {
            val dao = database.hospitalDao()
            StaffDashboardScreen(
                navController = navController,
                queueRepository = queueRepository,
                medicalRecordRepository = medicalRecordRepository,
                patientRepository = patientRepository,
                dao = dao,
                onLogout = {
                    authViewModel.logout()
                    sharedPrefs.clearPatientId()
                    sharedPrefs.clearPatientLocalId()
                    sharedPrefs.clearUserPhone()
                    sharedPrefs.clearPatientName()
                    navController.navigate("login") { popUpTo("staff_dashboard") { inclusive = true } }
                }
            )
        }
    }
}