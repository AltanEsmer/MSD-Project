package com.medicationadherence.app.presentation.family

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medicationadherence.app.domain.model.MedicationAdherence
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
    onNavigateToMessages: () -> Unit = {},
    viewModel: FamilyReportsViewModel = hiltViewModel()
) {
    val patients by viewModel.patients.collectAsState()
    val selectedPatient by viewModel.selectedPatient.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val report by viewModel.report.collectAsState()
    val summaryStats by viewModel.summaryStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    var showPatientSelector by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    
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
                                text = selectedPatient?.name ?: "Select a patient",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Purple100
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { showPatientSelector = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Select Patient",
                                    tint = Color.White
                                )
                            }
                            
                            IconButton(
                                onClick = { showExportDialog = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.2f)),
                                enabled = report != null
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Export",
                                    tint = Color.White
                                )
                            }
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
                    ReportPeriod.values().forEach { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { viewModel.selectPeriod(period) },
                            label = { Text(getPeriodLabel(period)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Purple600,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Error message
            errorMessage?.let { error ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Red100
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Red600)
                        Text(error, color = Red600, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Red600)
                        }
                    }
                }
            }
            
            // Content
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (selectedPatient == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Default.Person,
                        title = "No Patient Selected",
                        message = "Select a patient to view their adherence report",
                        actionText = "Select Patient",
                        onActionClick = { showPatientSelector = true }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Overall Summary Stats (All Patients)
                    summaryStats?.let { stats ->
                        item {
                            Text(
                                text = "Overall Statistics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
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
                                    value = "${stats.overallAdherence.toInt()}%",
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    icon = Icons.Default.Person,
                                    iconTint = Blue600,
                                    iconBackground = Blue100,
                                    label = "Active Patients",
                                    value = stats.activePatients.toString(),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Patient-Specific Report
                    report?.let { reportData ->
                        item {
                            Text(
                                text = "${reportData.patientName}'s Report",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        // Summary Cards
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    icon = Icons.Default.Medication,
                                    iconTint = Purple600,
                                    iconBackground = Purple100,
                                    label = "Adherence Rate",
                                    value = "${reportData.overallAdherenceRate.toInt()}%",
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    icon = Icons.Default.LocalFireDepartment,
                                    iconTint = Orange600,
                                    iconBackground = Orange100,
                                    label = "Current Streak",
                                    value = "${reportData.currentStreak}d",
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
                                    icon = Icons.Default.CheckCircle,
                                    iconTint = Green600,
                                    iconBackground = Green100,
                                    label = "Doses Taken",
                                    value = "${reportData.takenDoses}/${reportData.totalDoses}",
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    icon = Icons.Default.Warning,
                                    iconTint = Red600,
                                    iconBackground = Red100,
                                    label = "Missed Doses",
                                    value = reportData.missedDoses.toString(),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Medication Breakdown
                        if (reportData.medicationBreakdown.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Medication Breakdown",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            items(reportData.medicationBreakdown) { medication ->
                                MedicationAdherenceCard(medication = medication)
                            }
                        }

                        // Weekly Trend Chart
                        if (reportData.weeklyData.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Weekly Adherence Trend",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            item {
                                WeeklyAdherenceChart(weeklyData = reportData.weeklyData)
                            }
                        }

                        // Insights
                        if (reportData.insights.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Insights & Recommendations",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            items(reportData.insights) { insight ->
                                InsightCard(insight = insight)
                            }
                        }
                    }
                    
                    // Bottom padding for nav bar
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
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

    // Patient Selector Dialog
    if (showPatientSelector && patients.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showPatientSelector = false },
            title = { Text("Select Patient") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    patients.forEach { patient ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectPatient(patient)
                                    showPatientSelector = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (patient.id == selectedPatient?.id) Purple100 else Color.White
                            )
                        ) {
                            Text(
                                text = patient.name,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (patient.id == selectedPatient?.id) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPatientSelector = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Report") },
            text = { Text("Share this report via:") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val reportText = viewModel.exportReportAsText()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, reportText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Report"))
                        showExportDialog = false
                    }
                ) {
                    Text("Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MedicationAdherenceCard(medication: MedicationAdherence) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = medication.medicationName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${medication.adherenceRate.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = getAdherenceColor(medication.adherenceRate)
                )
            }

            LinearProgressIndicator(
                progress = medication.adherenceRate / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = getAdherenceColor(medication.adherenceRate),
                trackColor = Gray200
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Taken: ${medication.takenDoses}/${medication.totalDoses}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
                Text(
                    text = "Missed: ${medication.missedDoses}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Red600
                )
            }
        }
    }
}

@Composable
private fun WeeklyAdherenceChart(weeklyData: List<com.medicationadherence.app.domain.model.WeeklyAdherenceData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Last ${weeklyData.size} Weeks",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.take(7).forEachIndexed { index, week ->
                    WeekBar(
                        label = "W${index + 1}",
                        percentage = week.adherenceRate.toInt()
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightCard(insight: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Blue50),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = Blue600
            )
            Text(
                text = insight,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray900
            )
        }
    }
}

@Composable
private fun WeekBar(label: String, percentage: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.width(40.dp)
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height((percentage * 1.2f).dp.coerceAtLeast(20.dp).coerceAtMost(120.dp))
                .clip(RoundedCornerShape(4.dp))
                .background(getAdherenceColor(percentage.toFloat()))
        )
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelSmall,
            color = Gray500,
            fontSize = 10.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Gray600
        )
    }
}

private fun getAdherenceColor(adherenceRate: Float): Color {
    return when {
        adherenceRate >= 90 -> Green600
        adherenceRate >= 75 -> Blue600
        adherenceRate >= 50 -> Orange600
        else -> Red600
    }
}

private fun getPeriodLabel(period: ReportPeriod): String {
    return when (period) {
        ReportPeriod.WEEK -> "Week"
        ReportPeriod.MONTH -> "Month"
        ReportPeriod.THREE_MONTHS -> "3 Months"
    }
}