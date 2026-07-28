
package com.kiminini.hospital.ui.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kiminini.hospital.data.model.MedicalRecord
import com.kiminini.hospital.ui.viewmodel.MedicalRecordViewModel
import com.kiminini.hospital.ui.viewmodel.MedicalRecordViewModelFactory
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalRecordsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: MedicalRecordViewModel = viewModel(
        factory = MedicalRecordViewModelFactory(context)
    )

    val uiState by viewModel.uiState.collectAsState()
    val tabs = listOf("Lab Results", "Prescriptions", "Vaccinations", "Reports")

    LaunchedEffect(key1 = Unit) {
        if (uiState.error != null) {
            delay(3000)
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A73E8))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Medical Records",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        // Error Message
        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFCE8E6)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚠️",
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = Color(0xFFEA4335),
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.clearError() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("✕", fontSize = 14.sp)
                    }
                }
            }
        }

        // Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F0FE)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFF1A73E8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📋",
                        fontSize = 30.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Your Medical History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1A73E8)
                    )
                    Text(
                        text = "Access your test results, prescriptions and more",
                        fontSize = 14.sp,
                        color = Color(0xFF5F6368),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = uiState.selectedTabIndex,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            containerColor = Color.Transparent,
            edgePadding = 0.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = uiState.selectedTabIndex == index,
                    onClick = { viewModel.selectTab(index) },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (uiState.selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        // Content based on selected tab
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val records = viewModel.getRecordsForTab(uiState.selectedTabIndex)

            if (uiState.isLoading && records.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (records.isEmpty()) {
                // IMPROVED EMPTY STATE WITH HELPFUL MESSAGES
                EmptyState(
                    tabIndex = uiState.selectedTabIndex
                )
            } else {
                MedicalRecordsList(records = records)
            }
        }

        // Quick Actions - NOW CLICKABLE WITH TOASTS
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Quick Actions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1A73E8),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickActionButton(
                        icon = "⬇️",
                        label = "Download All",
                        color = Color(0xFF34A853),
                        onClick = {
                            android.widget.Toast.makeText(
                                context,
                                "Download started - Your records will be saved to your device",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                    QuickActionButton(
                        icon = "📤",
                        label = "Share",
                        color = Color(0xFF4285F4),
                        onClick = {
                            android.widget.Toast.makeText(
                                context,
                                "Share feature - You can share your records via email or WhatsApp",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                    QuickActionButton(
                        icon = "🖨️",
                        label = "Print",
                        color = Color(0xFFEA4335),
                        onClick = {
                            android.widget.Toast.makeText(
                                context,
                                "Print feature - Connect to a printer to print your records",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MedicalRecordsList(records: List<MedicalRecord>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(records) { record ->
            MedicalRecordCard(record = record)
        }
    }
}

@Composable
fun MedicalRecordCard(record: MedicalRecord) {
    val iconColor = when(record.type) {
        "Lab Results" -> Color(0xFF4285F4)
        "Prescription" -> Color(0xFF34A853)
        "Vaccination" -> Color(0xFFFBBC05)
        else -> Color(0xFFEA4335)
    }

    val icon = when(record.type) {
        "Lab Results" -> "🩸"
        "Prescription" -> "💊"
        "Vaccination" -> "💉"
        else -> "📄"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(iconColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = record.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "By ${record.doctor}",
                    fontSize = 14.sp,
                    color = Color(0xFF5F6368),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "Date: ${record.date}",
                    fontSize = 13.sp,
                    color = Color(0xFF9AA0A6),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Status and Actions
            Column(
                horizontalAlignment = Alignment.End
            ) {
                StatusBadge(status = record.status)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { /* View details */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            text = "👁️",
                            fontSize = 18.sp
                        )
                    }
                    IconButton(
                        onClick = { /* Download */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            text = "⬇️",
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val backgroundColor = when(status) {
        "Available", "Active" -> Color(0xFF34A853)
        "Pending" -> Color(0xFFFBBC05)
        "Completed" -> Color(0xFF1A73E8)
        else -> Color(0xFFEA4335)
    }

    Text(
        text = status,
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
fun QuickActionButton(
    icon: String,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF5F6368)
        )
    }
}

@Composable
fun EmptyState(tabIndex: Int) {
    val (title, message, tip) = when(tabIndex) {
        0 -> Triple(
            "🔬 No Lab Results Yet",
            "Your lab results will appear here after your appointment.",
            "Results are usually available 24-48 hours after your visit."
        )
        1 -> Triple(
            "💊 No Prescriptions Yet",
            "Prescriptions from your doctor will appear here.",
            "You'll receive prescriptions after your consultation."
        )
        2 -> Triple(
            "💉 No Vaccination Records",
            "Your vaccination history will be shown here.",
            "Records appear after you receive a vaccination."
        )
        3 -> Triple(
            "📋 No Medical Reports",
            "Medical reports from your visits will appear here.",
            "Check back after your appointment for updates."
        )
        else -> Triple(
            "📄 No Records",
            "No medical records available.",
            "Records appear after your hospital visits."
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title.split(" ")[0], // Just the emoji
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = title.substringAfter(" "), // The text after emoji
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A73E8)
        )
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color(0xFF5F6368),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        // Educational Tip Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F0FE)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💡",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = tip,
                    fontSize = 13.sp,
                    color = Color(0xFF1A73E8),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}