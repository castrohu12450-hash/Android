package com.kiminini.hospital.ui.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Phone
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
import com.kiminini.hospital.ui.theme.LocalDarkMode
import com.kiminini.hospital.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToOTP: (String) -> Unit
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val isDarkMode = LocalDarkMode.current

    LaunchedEffect(Unit) {
        authViewModel.clearAuthState()
        (context as? Activity)?.let { authViewModel.setActivity(it) }
    }

    val authState by authViewModel.authState.collectAsState()

    var phoneNumber by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(authState.verificationId) {
        authState.verificationId?.let {
            onNavigateToOTP(phoneNumber)
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

    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textSecondary = if (isDarkMode) Color(0xFF9AA0A6) else Color(0xFF5F6368)
    val primaryColor = if (isDarkMode) Color(0xFF8AB4F8) else Color(0xFF1A73E8)
    val secondaryColor = if (isDarkMode) Color(0xFF81C995) else Color(0xFF34A853)
    val errorBg = if (isDarkMode) Color(0xFFB00020).copy(alpha = 0.2f) else Color(0xFFFCE8E6)
    val errorText = if (isDarkMode) Color(0xFFFF8A80) else Color(0xFFEA4335)
    val dividerColor = if (isDarkMode) Color(0xFF3C4043) else Color(0xFFDADCE0)

    val backgroundGradient = Brush.verticalGradient(
        colors = if (isDarkMode) listOf(Color(0xFF121212), Color(0xFF1A1A1A))
        else listOf(Color(0xFFF8F9FA), Color(0xFFE8F0FE))
    )

    val phoneValid = phoneNumber.length >= 9
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
        Box(
            modifier = Modifier.size(250.dp).offset(x = (-50).dp, y = (-100).dp)
                .background(Brush.radialGradient(colors = listOf(primaryColor.copy(alpha = 0.08f), Color.Transparent)), CircleShape)
        )
        Box(
            modifier = Modifier.size(300.dp).align(Alignment.BottomEnd).offset(x = 100.dp, y = 100.dp)
                .background(Brush.radialGradient(colors = listOf(secondaryColor.copy(alpha = 0.08f), Color.Transparent)), CircleShape)
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            AnimatedVisibility(
                visible = true,
                enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)) + fadeIn()
            ) {
                Card(
                    modifier = Modifier.size(120.dp).shadow(20.dp, CircleShape, spotColor = primaryColor.copy(alpha = 0.3f)),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = primaryColor)
                ) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) { Text("🏥", fontSize = 60.sp) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(400, delayMillis = 100)) + slideInVertically(initialOffsetY = { -it })
            ) {
                Text(
                    "Kiminini Hospital",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                "Hospital Portal",
                fontSize = 18.sp,
                color = secondaryColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(400, delayMillis = 200)) + slideInVertically(initialOffsetY = { it })
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Welcome Back", fontSize = 16.sp, color = textSecondary)
                        Text(
                            "Hospital Login",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        AnimatedVisibility(
                            visible = errorMessage != null,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = errorBg),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("⚠️", fontSize = 20.sp)
                                    Spacer(Modifier.width(8.dp))
                                    // FIXED: Use safe access instead of !!
                                    Text(errorMessage ?: "", color = errorText, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { newValue ->
                                    if (newValue.all { it.isDigit() }) phoneNumber = newValue.take(10)
                                },
                                label = { Text("Phone Number") },
                                placeholder = { Text("") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone", tint = primaryColor) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = dividerColor,
                                    focusedLabelColor = primaryColor
                                )
                            )
                            IconButton(onClick = { phoneNumber = "" }, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = textSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Enter your Kenyan phone number (no country code)",
                            fontSize = 12.sp,
                            color = textSecondary,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                        )

                        Button(
                            onClick = {
                                if (phoneNumber.length >= 9) authViewModel.sendVerificationCode(phoneNumber)
                                else errorMessage = "Please enter a valid phone number (9 digits)"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .scale(if (phoneValid) buttonScale else 1f)
                                .shadow(4.dp, RoundedCornerShape(16.dp)),
                            enabled = phoneNumber.length >= 9 && !authState.isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor, disabledContainerColor = dividerColor),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (authState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("SEND OTP", fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.5.sp, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(visible = true, enter = fadeIn(animationSpec = tween(400, delayMillis = 300))) {
                Box(Modifier.fillMaxWidth().padding(horizontal = 40.dp)) {
                    Divider(color = dividerColor, thickness = 1.dp, modifier = Modifier.align(Alignment.Center))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(400, delayMillis = 400))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 24.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+254700123456")))
                        }
                    ) {
                        Text("📞", fontSize = 14.sp, modifier = Modifier.padding(end = 4.dp))
                        Text("Emergency: +254 700 123 456", fontSize = 14.sp, color = primaryColor, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("© 2026 Kiminini Hospital. All rights reserved.", fontSize = 12.sp, color = textSecondary, textAlign = TextAlign.Center)
                }
            }
        }
    }
}