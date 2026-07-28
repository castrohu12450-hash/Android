package com.kiminini.hospital.ui.auth

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import com.kiminini.hospital.ui.theme.LocalDarkMode
import com.kiminini.hospital.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTPScreen(
    phoneNumber: String,
    navController: NavController,
    onBack: () -> Unit,
    onVerificationSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val isDarkMode = LocalDarkMode.current

    var otp by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var timeLeft by remember { mutableStateOf(60) }
    var isResending by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(timeLeft) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    LaunchedEffect(authState.isAuthenticated, authState.userRole) {
        if (authState.isAuthenticated) {
            delay(500)
            onVerificationSuccess(phoneNumber)
        }
    }

    LaunchedEffect(authState.error) {
        authState.error?.let { error ->
            errorMessage = error
            coroutineScope.launch {
                delay(5000)
                errorMessage = null
                authViewModel.clearError()
            }
        }
    }

    val primaryColor = if (isDarkMode) Color(0xFF8AB4F8) else Color(0xFF1A73E8)
    val secondaryColor = if (isDarkMode) Color(0xFF81C995) else Color(0xFF34A853)
    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textSecondary = if (isDarkMode) Color(0xFF9AA0A6) else Color(0xFF5F6368)
    val errorBg = if (isDarkMode) Color(0xFFB00020).copy(alpha = 0.2f) else Color(0xFFFCE8E6)
    val errorText = if (isDarkMode) Color(0xFFFF8A80) else Color(0xFFEA4335)
    val dividerColor = if (isDarkMode) Color(0xFF3C4043) else Color(0xFFDADCE0)

    val backgroundGradient = Brush.verticalGradient(
        colors = if (isDarkMode) listOf(Color(0xFF121212), Color(0xFF1A1A1A))
        else listOf(Color(0xFFF8F9FA), Color(0xFFE8F0FE))
    )

    val otpValid = otp.length == 6
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    Box(modifier = Modifier.fillMaxSize().background(backgroundGradient)) {
        // Decorative circles
        Box(modifier = Modifier.size(200.dp).offset(x = (-50).dp, y = (-50).dp)
            .background(Brush.radialGradient(colors = listOf(secondaryColor.copy(alpha = 0.08f), Color.Transparent)), CircleShape))
        Box(modifier = Modifier.size(250.dp).align(Alignment.BottomStart).offset(x = (-50).dp, y = 50.dp)
            .background(Brush.radialGradient(colors = listOf(primaryColor.copy(alpha = 0.08f), Color.Transparent)), CircleShape))

        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // Back button
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Card(modifier = Modifier.size(40.dp).shadow(4.dp, CircleShape), shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = surfaceColor)) {
                    IconButton(onClick = onBack, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = primaryColor, modifier = Modifier.size(20.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Icon card (scale in)
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)) + fadeIn()
            ) {
                Card(modifier = Modifier.size(120.dp).shadow(20.dp, CircleShape, spotColor = secondaryColor.copy(alpha = 0.3f)),
                    shape = CircleShape, colors = CardDefaults.cardColors(containerColor = secondaryColor)) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) { Text("🔐", fontSize = 60.sp) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title (fade in)
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(400, delayMillis = 100)) + slideInVertically(initialOffsetY = { -it })
            ) {
                Text("Enter Verification Code", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = primaryColor, textAlign = TextAlign.Center)
            }
            Text("We've sent a 6-digit code to", fontSize = 14.sp, color = textSecondary, modifier = Modifier.padding(top = 8.dp))
            Card(modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.1f)), shape = RoundedCornerShape(30.dp)) {
                Text("+254 $phoneNumber", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryColor, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            }

            // Error message (animated)
            AnimatedVisibility(visible = errorMessage != null, enter = fadeIn() + slideInVertically(), exit = fadeOut() + slideOutVertically()) {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = errorBg), shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("⚠️", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        // FIXED: Use safe access
                        Text(errorMessage ?: "", color = errorText, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    }
                }
            }

            // OTP input card (fade in with slide)
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(400, delayMillis = 200)) + slideInVertically(initialOffsetY = { it })
            ) {
                Card(modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = surfaceColor)) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        OutlinedTextField(
                            value = otp,
                            onValueChange = { newValue ->
                                if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                                    otp = newValue
                                    errorMessage = null
                                    if (newValue.length == 6) authViewModel.verifyCode(newValue, phoneNumber)
                                }
                            },
                            label = { Text("6-digit OTP") },
                            placeholder = { Text("Enter code") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = secondaryColor,
                                unfocusedBorderColor = dividerColor,
                                focusedLabelColor = secondaryColor,
                                cursorColor = secondaryColor
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = if (timeLeft > 0) primaryColor.copy(alpha = 0.1f) else errorBg),
                            shape = RoundedCornerShape(30.dp)) {
                            Text(if (timeLeft > 0) "⏳ Code expires in ${timeLeft}s" else "⌛ Code expired",
                                fontSize = 14.sp, color = if (timeLeft > 0) secondaryColor else errorText,
                                fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Verify button (pulse when OTP valid)
            Button(
                onClick = {
                    if (otp.length == 6) {
                        authViewModel.verifyCode(otp, phoneNumber)
                    } else {
                        errorMessage = "Please enter 6-digit OTP"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scale(if (otpValid) buttonScale else 1f)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                enabled = !authState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = secondaryColor, disabledContainerColor = dividerColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("VERIFY & LOGIN", fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.5.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Resend button (fade in)
            AnimatedVisibility(visible = true, enter = fadeIn(animationSpec = tween(400, delayMillis = 300))) {
                OutlinedButton(
                    onClick = {
                        if (timeLeft == 0 && !isResending) {
                            isResending = true
                            authViewModel.sendVerificationCode(phoneNumber)
                            timeLeft = 60
                            isResending = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = timeLeft == 0 && !isResending,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
                ) {
                    if (isResending) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = primaryColor)
                    } else {
                        Text("RESEND OTP")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer text (fade in)
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(400, delayMillis = 400))
            ) {
                Text("Didn't receive the code? Check your SMS or request a new one", fontSize = 12.sp, color = textSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
            }
        }
    }
}