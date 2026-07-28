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
import java.text.SimpleDateFormat
import java.util.*

data class HealthMetric(
    val id: String,
    val type: String,
    val value: String,
    val unit: String,
    val date: String,
    val time: String,
    val status: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthMetricsScreen(navController: NavController) {
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    )

    val metrics = remember {
        listOf(
            HealthMetric("1", "Blood Pressure", "120/80", "mmHg", "Mar 15, 2026", "9:30 AM", "Normal"),
            HealthMetric("2", "Weight", "72.5", "kg", "Mar 15, 2026", "9:30 AM", null),
            HealthMetric("3", "Temperature", "36.6", "°C", "Mar 15, 2026", "9:30 AM", null),
            HealthMetric("4", "Height", "165", "cm", "Mar 15, 2026", "9:30 AM", null),
            HealthMetric("5", "Blood Pressure", "118/78", "mmHg", "Mar 10, 2026", "2:15 PM", "Normal"),
            HealthMetric("6", "Weight", "73.0", "kg", "Mar 10, 2026", "2:15 PM", null),
            HealthMetric("7", "Blood Pressure", "135/85", "mmHg", "Mar 5, 2026", "11:00 AM", "High")
        )
    }

    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    val tabs = listOf("All", "Vitals", "Body Measurements")

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
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .shadow(elevation = 4.dp, shape = CircleShape)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Health Metrics",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }

            // REMOVED: The Row with three summary cards (BP, Wt, Temp) is deleted.
            // The content now starts directly with tabs.

            // Tabs (now directly below the top bar)
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                containerColor = Color.Transparent,
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Metrics List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(metrics.filter { metric ->
                    when (selectedTab) {
                        1 -> metric.type == "Blood Pressure" || metric.type == "Temperature"
                        2 -> metric.type == "Weight" || metric.type == "Height"
                        else -> true
                    }
                }) { metric ->
                    HealthMetricCard(metric = metric)
                }
            }
        }

        // Add Metric Dialog (unchanged)
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = {
                    Text(
                        text = "Add Health Metric",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Select metric type:",
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MetricTypeButton(
                                icon = "❤️",
                                label = "BP",
                                onClick = { showAddDialog = false }
                            )
                            MetricTypeButton(
                                icon = "⚖️",
                                label = "Wt",
                                onClick = { showAddDialog = false }
                            )
                            MetricTypeButton(
                                icon = "🌡️",
                                label = "Temp",
                                onClick = { showAddDialog = false }
                            )
                            MetricTypeButton(
                                icon = "📏",
                                label = "Ht",
                                onClick = { showAddDialog = false }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun HealthMetricCard(metric: HealthMetric) {
    val icon = when (metric.type) {
        "Blood Pressure" -> "❤️"
        "Weight" -> "⚖️"
        "Temperature" -> "🌡️"
        "Height" -> "📏"
        else -> "📊"
    }

    val color = when (metric.type) {
        "Blood Pressure" -> MaterialTheme.colorScheme.primary
        "Weight" -> MaterialTheme.colorScheme.secondary
        "Temperature" -> MaterialTheme.colorScheme.tertiary
        "Height" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = metric.type,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = color
                    )
                    Text(
                        text = "${metric.value} ${metric.unit}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (metric.status != null) {
                    Text(
                        text = metric.status,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .background(
                                when (metric.status) {
                                    "Normal" -> MaterialTheme.colorScheme.secondary
                                    "High" -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.tertiary
                                },
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${metric.date} • ${metric.time}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MetricTypeButton(icon: String, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(70.dp)
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 20.sp)
        }
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}