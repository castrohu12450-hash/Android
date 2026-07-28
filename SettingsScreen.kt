package com.kiminini.hospital.ui.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kiminini.hospital.ui.theme.LocalDarkMode
import com.kiminini.hospital.ui.theme.LocalSetDarkMode

data class SettingsSection(
    val title: String,
    val items: List<SettingsItem>
)

data class SettingsItem(
    val title: String,
    val description: String,
    val icon: String,
    val onClick: () -> Unit,
    val isSwitch: Boolean = false,
    val switchState: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onLogout: () -> Unit
) {
    val isDarkMode = LocalDarkMode.current
    val setDarkMode = LocalSetDarkMode.current

    var notificationsEnabled by remember { mutableStateOf(true) }
    var emailUpdates by remember { mutableStateOf(true) }
    var biometricEnabled by remember { mutableStateOf(false) }
    var autoSyncEnabled by remember { mutableStateOf(true) }

    val settingsSections = listOf(
        SettingsSection(
            title = "Preferences",
            items = listOf(
                SettingsItem(
                    title = "Push Notifications",
                    description = "Receive appointment reminders and health tips",
                    icon = "🔔",
                    onClick = { notificationsEnabled = !notificationsEnabled },
                    isSwitch = true,
                    switchState = notificationsEnabled
                ),
                SettingsItem(
                    title = "Email Updates",
                    description = "Get medical reports and newsletters",
                    icon = "📧",
                    onClick = { emailUpdates = !emailUpdates },
                    isSwitch = true,
                    switchState = emailUpdates
                ),
                SettingsItem(
                    title = "Dark Mode",
                    description = "Switch between light and dark theme",
                    icon = "🌙",
                    onClick = { setDarkMode(!isDarkMode) },
                    isSwitch = true,
                    switchState = isDarkMode
                ),
                SettingsItem(
                    title = "Biometric Login",
                    description = "Use fingerprint or face recognition",
                    icon = "🔐",
                    onClick = { biometricEnabled = !biometricEnabled },
                    isSwitch = true,
                    switchState = biometricEnabled
                ),
                SettingsItem(
                    title = "Auto‑Sync Data",
                    description = "Automatically sync health data with cloud",
                    icon = "🔄",
                    onClick = { autoSyncEnabled = !autoSyncEnabled },
                    isSwitch = true,
                    switchState = autoSyncEnabled
                )
            )
        ),
        SettingsSection(
            title = "Account",
            items = listOf(
                SettingsItem(
                    title = "Personal Information",
                    description = "Update your name, phone, email",
                    icon = "👤",
                    onClick = { navController.navigate("profile") }
                ),
                SettingsItem(
                    title = "Change Password",
                    description = "Update your account password",
                    icon = "🔑",
                    onClick = { /* TODO: navigate to change password screen */ }
                ),
                SettingsItem(
                    title = "Emergency Contact",
                    description = "Manage your emergency contact",
                    icon = "🚨",
                    onClick = { /* TODO: navigate to emergency contact screen */ }
                ),
                SettingsItem(
                    title = "Linked Devices",
                    description = "Manage devices connected to your account",
                    icon = "📱",
                    onClick = { /* TODO: navigate to linked devices */ }
                )
            )
        ),
        SettingsSection(
            title = "Data & Privacy",
            items = listOf(
                SettingsItem(
                    title = "Clear Cache",
                    description = "Free up storage space",
                    icon = "🗑️",
                    onClick = { /* TODO: show confirmation dialog and clear cache */ }
                ),
                SettingsItem(
                    title = "Privacy Policy",
                    description = "Read our privacy policy",
                    icon = "📜",
                    onClick = { /* TODO: open privacy policy in browser */ }
                ),
                SettingsItem(
                    title = "Terms of Service",
                    description = "Read our terms and conditions",
                    icon = "⚖️",
                    onClick = { /* TODO: open terms in browser */ }
                )
            )
        ),
        SettingsSection(
            title = "Support",
            items = listOf(
                SettingsItem(
                    title = "Help Center",
                    description = "FAQs and troubleshooting",
                    icon = "❓",
                    onClick = { /* TODO: open help center */ }
                ),
                SettingsItem(
                    title = "Contact Us",
                    description = "Reach out to customer support",
                    icon = "📞",
                    onClick = { /* TODO: open contact screen / email */ }
                ),
                SettingsItem(
                    title = "Rate the App",
                    description = "Leave a review on Play Store",
                    icon = "⭐",
                    onClick = { /* TODO: open Play Store rating */ }
                ),
                SettingsItem(
                    title = "About",
                    description = "App version, licenses, and credits",
                    icon = "ℹ️",
                    onClick = { /* TODO: show about dialog */ }
                )
            )
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Subtle decorative circles
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
                .padding(16.dp)
        ) {
            // Top Bar (consistent with other screens)
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
                    .padding(16.dp)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
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
                    text = "Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Settings List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(settingsSections) { section ->
                    SettingsSectionCard(section = section)
                }

                item {
                    // Logout Button
                    Button(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onError
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "LOGOUT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }

                    // App Version
                    Text(
                        text = "Kiminini Hospital Portal v1.0.0",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSectionCard(
    section: SettingsSection
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = section.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )

            section.items.forEachIndexed { index, item ->
                SettingsItemRow(item = item)
                if (index < section.items.size - 1) {
                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItemRow(
    item: SettingsItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!item.isSwitch) item.onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.icon,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = item.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (item.isSwitch) {
            Switch(
                checked = item.switchState,
                onCheckedChange = { item.onClick() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onSecondary,
                    checkedTrackColor = MaterialTheme.colorScheme.secondary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        } else {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}