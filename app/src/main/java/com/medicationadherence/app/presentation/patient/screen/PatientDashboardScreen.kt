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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.medicationadherence.app.domain.model.AdherenceStatus
import com.medicationadherence.app.domain.model.MedicationWithSchedule
import com.medicationadherence.app.presentation.common.components.*
import com.medicationadherence.app.presentation.patient.viewmodel.MedicationViewModel

/**
 * Patient Dashboard Screen - Main screen for medication management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDashboardScreen(
    viewModel: MedicationViewModel = hiltViewModel(),
    onAddMedication: () -> Unit = {},
    onMedicationDetails: (String) -> Unit = {}
) {
    // Connect to ViewModel properly
    val todayMedications by viewModel.todayMedications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Medication Adherence") },
                actions = {
                    TextButton(onClick = onAddMedication) {
                        Text("Add Medication")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Today's Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Medications: ${todayMedications.size}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Taken: ${todayMedications.sumOf { medicationWithSchedule -> medicationWithSchedule.schedules.count { schedule -> schedule.status == AdherenceStatus.TAKEN } }}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Medications List
            if (todayMedications.isEmpty()) {
                // Empty State
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Medications Yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add your first medication to get started with tracking your health journey.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onAddMedication) {
                            Text("Add Medication")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(todayMedications) { medicationWithSchedule ->
                        MedicationCardItem(
                            medicationWithSchedule = medicationWithSchedule,
                            onTakeClick = { scheduleId ->
                                viewModel.takeMedication(scheduleId, medicationWithSchedule.medication.id)
                            },
                            onSkipClick = { scheduleId ->
                                viewModel.skipMedication(scheduleId, medicationWithSchedule.medication.id)
                            },
                            onDetailsClick = {
                                onMedicationDetails(medicationWithSchedule.medication.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
