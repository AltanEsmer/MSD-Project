package com.medicationadherence.app.presentation.patient.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medicationadherence.app.presentation.patient.viewmodel.MedicationViewModel

/**
 * Add Medication Screen - Screen for adding new medications
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    viewModel: MedicationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    var medicationName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var selectedTimes by remember { mutableStateOf(setOf<String>()) }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val medicationAdded by viewModel.medicationAdded.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Add Medication") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Cancel")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (medicationName.isNotBlank() && dosage.isNotBlank() && selectedTimes.isNotEmpty()) {
                                viewModel.addMedication(
                                    name = medicationName,
                                    dosage = dosage,
                                    frequency = selectedTimes.toList(),
                                    instructions = instructions
                                )
                            }
                        },
                        enabled = medicationName.isNotBlank() && dosage.isNotBlank() && selectedTimes.isNotEmpty() && !isLoading
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Navigate back when medication is added
            LaunchedEffect(medicationAdded) {
                if (medicationAdded) {
                    viewModel.clearMedicationAdded()
                    onNavigateBack()
                }
            }
            // Error message
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Medication Name
            OutlinedTextField(
                value = medicationName,
                onValueChange = { medicationName = it },
                label = { Text("Medication Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Dosage
            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dosage (e.g., 10mg, 1 tablet)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Instructions
            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Instructions (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Frequency Selection
            Text(
                text = "Frequency",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val timeOptions = listOf(
                "08:00" to "Morning",
                "12:00" to "Afternoon", 
                "18:00" to "Evening",
                "22:00" to "Night"
            )

            Column(
                modifier = Modifier.selectableGroup()
            ) {
                timeOptions.forEach { (time, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedTimes.contains(time),
                                onClick = {
                                    selectedTimes = if (selectedTimes.contains(time)) {
                                        selectedTimes - time
                                    } else {
                                        selectedTimes + time
                                    }
                                },
                                role = Role.Checkbox
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTimes.contains(time),
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$label ($time)",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
