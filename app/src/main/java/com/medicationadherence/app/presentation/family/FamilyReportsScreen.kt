package com.medicationadherence.app.presentation.family

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medicationadherence.app.presentation.common.components.*
import com.medicationadherence.app.presentation.theme.*

/**
 * Family Reports Screen - View analytics and adherence statistics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyReportsScreen(
    onBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {}
) {
    var selectedPeriod by remember { mutableStateOf(0) } // 0: Week, 1: Month, 2: 3 Months
    
    // Mock report data
    val summaryStats = SummaryStats(
        overallAdherence = 88,
        totalMedications = 15,
        missedDoses = 12,
        activePatients = 2
    )
    
    val patientReports = listOf(
        PatientReport(
            patientName = "John Smith",
            adherenceRate = 85,
            medicationsCount = 8,
            missedCount = 5,
            streakDays = 12
        ),
        PatientReport(
            patientName = "Emily Johnson",
            adherenceRate = 92,
            medicationsCount = 7,
            missedCount = 2,
            streakDays = 28
        )
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Gray50)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Purple600)
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Reports",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Monitor patient adherence",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Purple100
                            )
                        }
                        
                        IconButton(
                            onClick = { /* Export */ },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Export",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
            
            // Period Selector
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Week", "Month", "3 Months").forEachIndexed { index, period ->
                        FilterChip(
                            selected = selectedPeriod == index,
                            onClick = { selectedPeriod = index },
                            label = { Text(period) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Purple600,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
            
            // Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Summary Stats
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            icon = Icons.Default.CheckCircle,
                            iconTint = Green600,
                            iconBackground = Green100,
                            label = "Overall Adherence",
                            value = "${summaryStats.overallAdherence}%",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            icon = Icons.Default.Person,
                            iconTint = Blue600,
                            iconBackground = Blue100,
                            label = "Active Patients",
                            value = summaryStats.activePatients.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            icon = Icons.Default.Medication,
                            iconTint = Purple600,
                            iconBackground = Purple100,
                            label = "Total Medications",
                            value = summaryStats.totalMedications.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            icon = Icons.Default.Warning,
                            iconTint = Orange600,
                            iconBackground = Orange100,
                            label = "Missed Doses",
                            value = summaryStats.missedDoses.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Patient Reports Section
                item {
                    Text(
                        text = "Patient Reports",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                items(patientReports) { report ->
                    PatientReportCard(report = report)
                }
                
                // Chart Section (Simple for MVP)
                item {
                    Text(
                        text = "Weekly Adherence Trend",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Simple bar chart visualization
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                WeekBar("Mon", 75)
                                WeekBar("Tue", 88)
                                WeekBar("Wed", 92)
                                WeekBar("Thu", 85)
                                WeekBar("Fri", 90)
                                WeekBar("Sat", 78)
                                WeekBar("Sun", 82)
                            }
                        }
                    }
                }
                
                // Bottom padding for nav bar
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        
        // Bottom Navigation
        BottomNavBar(
            selectedItem = 3,
            onItemSelected = { index ->
                when (index) {
                    0 -> onNavigateToHome()
                    1 -> onNavigateToAlerts()
                    2 -> onNavigateToMessages()
                    3 -> {} // Already on reports
                }
            },
            items = listOf(
                BottomNavItem(Icons.Default.Home, "Home"),
                BottomNavItem(Icons.Default.Notifications, "Alerts"),
                BottomNavItem(Icons.Default.Message, "Messages"),
                BottomNavItem(Icons.Default.BarChart, "Reports")
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun PatientReportCard(report: PatientReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = report.patientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${report.medicationsCount} medications",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }
                Text(
                    text = "${report.adherenceRate}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Green600
                )
            }
            
            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Adherence Rate",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Text(
                        text = "${report.adherenceRate}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                LinearProgressIndicator(
                    progress = report.adherenceRate / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Green600,
                    trackColor = Gray200
                )
            }
            
            // Additional info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconWithText(
                    icon = Icons.Default.LocalFireDepartment,
                    text = "${report.streakDays} day streak",
                    iconTint = Orange600
                )
                IconWithText(
                    icon = Icons.Default.Warning,
                    text = "${report.missedCount} missed",
                    iconTint = Red600
                )
            }
        }
    }
}

@Composable
private fun WeekBar(day: String, percentage: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height((percentage * 100 / 100f).dp.coerceAtLeast(40.dp))
                .clip(RoundedCornerShape(4.dp))
                .background(Blue600.copy(alpha = percentage / 100f))
        )
        Text(
            text = day,
            style = MaterialTheme.typography.labelSmall,
            color = Gray600
        )
    }
}

@Composable
private fun IconWithText(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, iconTint: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Gray600
        )
    }
}

// Mock data classes
data class SummaryStats(
    val overallAdherence: Int,
    val totalMedications: Int,
    val missedDoses: Int,
    val activePatients: Int
)

data class PatientReport(
    val patientName: String,
    val adherenceRate: Int,
    val medicationsCount: Int,
    val missedCount: Int,
    val streakDays: Int
)

