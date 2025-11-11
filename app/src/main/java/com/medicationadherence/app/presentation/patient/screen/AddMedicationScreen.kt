package com.medicationadherence.app.presentation.patient.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var importance by remember { mutableStateOf("medium") }
    
    // Error states for validation
    var nameError by remember { mutableStateOf<String?>(null) }
    var dosageError by remember { mutableStateOf<String?>(null) }
    var frequencyError by remember { mutableStateOf<String?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val medicationAdded by viewModel.medicationAdded.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Add Medication",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            // Clear previous errors
                            nameError = null
                            dosageError = null
                            frequencyError = null
                            
                            // Validate inputs
                            var hasError = false
                            
                            if (medicationName.trim().isBlank()) {
                                nameError = "Medication name is required"
                                hasError = true
                            }
                            
                            if (dosage.trim().isBlank()) {
                                dosageError = "Dosage is required"
                                hasError = true
                            }
                            
                            if (selectedTimes.isEmpty()) {
                                frequencyError = "Please select at least one time"
                                hasError = true
                            }
                            
                            // If no errors, submit
                            if (!hasError) {
                                viewModel.addMedication(
                                    name = medicationName.trim(),
                                    dosage = dosage.trim(),
                                    frequency = selectedTimes.toList().sorted(),
                                    instructions = instructions.trim(),
                                    importance = importance
                                )
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                    snackbarHostState.showSnackbar(
                        message = "Medication added successfully",
                        duration = SnackbarDuration.Short
                    )
                    viewModel.clearMedicationAdded()
                    onNavigateBack()
                }
            }
            
            // Show error message from ViewModel
            LaunchedEffect(errorMessage) {
                errorMessage?.let { error ->
                    snackbarHostState.showSnackbar(
                        message = error,
                        duration = SnackbarDuration.Long
                    )
                    viewModel.clearError()
                }
            }

            // Medication Name
            OutlinedTextField(
                value = medicationName,
                onValueChange = { 
                    medicationName = it
                    nameError = null // Clear error on change
                },
                label = { Text("Medication Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } }
            )

            // Dosage
            OutlinedTextField(
                value = dosage,
                onValueChange = { 
                    dosage = it
                    dosageError = null // Clear error on change
                },
                label = { Text("Dosage (e.g., 10mg, 1 tablet)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = dosageError != null,
                supportingText = dosageError?.let { { Text(it) } }
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
            Column {
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                frequencyError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }

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
                                    frequencyError = null // Clear error on selection
                                },
                                role = Role.Checkbox
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedTimes.contains(time),
                            onCheckedChange = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$label ($time)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 18.sp
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
