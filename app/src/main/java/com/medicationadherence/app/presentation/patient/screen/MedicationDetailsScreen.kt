package com.medicationadherence.app.presentation.patient.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.medicationadherence.app.domain.model.AdherenceRecord
import com.medicationadherence.app.domain.model.AdherenceStatus
import com.medicationadherence.app.domain.model.Medication
import com.medicationadherence.app.presentation.patient.viewmodel.MedicationViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone

/**
 * Medication Details Screen - Shows detailed information about a medication
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationDetailsScreen(
    medicationId: String,
    viewModel: MedicationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onEditMedication: (String) -> Unit = {}
) {
    val todayMedications by viewModel.todayMedications.collectAsState()
    val adherenceHistory by viewModel.adherenceHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val medicationWithSchedule = todayMedications.find { it.medication.id == medicationId }
    val medication = medicationWithSchedule?.medication
    
    // Load adherence history when screen opens
    LaunchedEffect(medicationId) {
        viewModel.loadAdherenceHistory(medicationId, days = 7)
    }
    
    // Clear adherence history when screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearAdherenceHistory()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Medication Details") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Back")
                    }
                },
                actions = {
                    TextButton(onClick = { medication?.let { onEditMedication(it.id) } }) {
                        Text("Edit")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (medication == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Medication not found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Medication Info Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = medication.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Dosage: ${medication.dosage}",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Instructions: ${medication.instructions}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // Schedule Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Today's Schedule",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            medicationWithSchedule?.schedules?.forEach { schedule ->
                                ScheduleItem(
                                    schedule = schedule,
                                    onTakeClick = { scheduleId ->
                                        viewModel.takeMedication(scheduleId, medication.id)
                                    },
                                    onSkipClick = { scheduleId ->
                                        viewModel.skipMedication(scheduleId, medication.id)
                                    }
                                )
                            } ?: run {
                                Text(
                                    text = "No schedules for today",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Adherence History Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Recent Adherence",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Show last 7 days of adherence records
                            if (adherenceHistory.isEmpty()) {
                                Text(
                                    text = "No adherence history yet. Start taking your medications to build your history!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                adherenceHistory.forEach { record ->
                                    AdherenceHistoryItem(
                                        date = record.date,
                                        status = record.status,
                                        time = record.timestamp?.let { 
                                            "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}"
                                        } ?: "N/A"
                                    )
                                }
                            }
                        }
                    }
                }

                // Actions Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Actions",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { /* TODO: Show edit dialog */ },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Edit Medication")
                                }
                                Button(
                                    onClick = { 
                                        viewModel.deleteMedication(medication.id)
                                        onNavigateBack()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleItem(
    schedule: com.medicationadherence.app.domain.model.MedicationSchedule,
    onTakeClick: (String) -> Unit,
    onSkipClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Time: ${schedule.scheduledTime}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Status: ${schedule.status.name}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (schedule.status == AdherenceStatus.PENDING) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = { onSkipClick(schedule.id) }) {
                    Text("Skip")
                }
                Button(onClick = { onTakeClick(schedule.id) }) {
                    Text("Take")
                }
            }
        }
    }
}

@Composable
fun AdherenceHistoryItem(
    date: LocalDate,
    status: AdherenceStatus,
    time: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall
            )
        }

        val (color, text) = when (status) {
            AdherenceStatus.TAKEN -> MaterialTheme.colorScheme.primary to "Taken"
            AdherenceStatus.SKIPPED -> MaterialTheme.colorScheme.error to "Skipped"
            else -> MaterialTheme.colorScheme.outline to "Missed"
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
