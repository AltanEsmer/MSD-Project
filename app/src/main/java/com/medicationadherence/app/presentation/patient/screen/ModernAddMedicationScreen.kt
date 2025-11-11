package com.medicationadherence.app.presentation.patient.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medicationadherence.app.presentation.common.components.*
import com.medicationadherence.app.presentation.patient.viewmodel.MedicationViewModel
import com.medicationadherence.app.presentation.theme.*

/**
 * Modern Add/Edit Medication Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernAddMedicationScreen(
    medicationId: String? = null,
    viewModel: MedicationViewModel = hiltViewModel(),
    onCancel: () -> Unit,
    onSaved: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val medicationAdded by viewModel.medicationAdded.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val medicationToEdit by viewModel.medicationToEdit.collectAsState()
    
    val isEdit = medicationId != null
    val scrollState = rememberScrollState()
    
    // Load medication data if editing
    LaunchedEffect(medicationId) {
        if (medicationId != null) {
            android.util.Log.d("ModernAddMedicationScreen", "Loading medication for edit: $medicationId")
            viewModel.loadMedicationForEdit(medicationId)
        }
    }
    
    // Initialize state from medicationToEdit or defaults
    var medicationName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Once daily") }
    var times by remember { mutableStateOf(listOf("08:00")) }
    var instructions by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(MedicationIcon.PILL) }
    var importance by remember { mutableStateOf("medium") }
    
    var nameError by remember { mutableStateOf(false) }
    var dosageError by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }
    
    // Populate fields when medication data loads
    LaunchedEffect(medicationToEdit) {
        medicationToEdit?.let { med ->
            android.util.Log.d("ModernAddMedicationScreen", "Populating fields with: name=${med.name}, dosage=${med.dosage}, importance=${med.importance}")
            medicationName = med.name
            dosage = med.dosage
            times = med.frequency
            instructions = med.instructions
            importance = med.importance
        }
    }
    
    // Clean up on dispose
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearMedicationToEdit()
        }
    }
    
    // Handle successful save
    LaunchedEffect(medicationAdded) {
        if (medicationAdded) {
            viewModel.clearMedicationAdded()
            onSaved()
        }
    }
    
    // Scroll to top when validation fails
    LaunchedEffect(showValidationError) {
        if (showValidationError) {
            scrollState.animateScrollTo(0)
        }
    }
    
    // Show error message
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            android.util.Log.e("AddMedicationScreen", "Error: $errorMessage")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Medication" else "Add Medication") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (showValidationError) {
                    Snackbar(
                        action = {
                            TextButton(onClick = { showValidationError = false }) {
                                Text("OK")
                            }
                        }
                    ) {
                        Text("Please fill in all required fields (marked with *)")
                    }
                }
                if (errorMessage != null) {
                    Snackbar(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        action = {
                            TextButton(onClick = { viewModel.clearError() }) {
                                Text("OK")
                            }
                        }
                    ) {
                        Text("Error: $errorMessage")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Gray50)
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Medication Type / Icon Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Medication Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconSelector(
                    selectedIcon = selectedIcon,
                    onIconSelected = { selectedIcon = it }
                )
            }
            
            // Basic Information Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SectionHeader(title = "Basic Information")
                    
                    // Medication Name
                    OutlinedTextField(
                        value = medicationName,
                        onValueChange = {
                            medicationName = it
                            nameError = false
                        },
                        label = { Text("Medication Name *") },
                        placeholder = { Text("e.g., Aspirin") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameError,
                        supportingText = if (nameError) {
                            { Text("Medication name is required") }
                        } else null,
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    
                    // Dosage
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = {
                            dosage = it
                            dosageError = false
                        },
                        label = { Text("Dosage *") },
                        placeholder = { Text("e.g., 100mg, 2 tablets") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = dosageError,
                        supportingText = if (dosageError) {
                            { Text("Dosage is required") }
                        } else null,
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    
                    // Frequency
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = frequency,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Frequency *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf(
                                "Once daily",
                                "Twice daily",
                                "Three times daily",
                                "Four times daily",
                                "Every 4 hours",
                                "Every 6 hours",
                                "Every 8 hours",
                                "As needed",
                                "Weekly"
                            ).forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        frequency = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Schedule Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SectionHeader(title = "Schedule")
                    
                    Text(
                        text = "Times to Take *",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    times.forEachIndexed { index, time ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = time,
                                onValueChange = { newTime ->
                                    times = times.toMutableList().apply {
                                        set(index, newTime)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("HH:MM") },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            
                            if (times.size > 1) {
                                IconButton(
                                    onClick = {
                                        times = times.filterIndexed { i, _ -> i != index }
                                    }
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove")
                                }
                            }
                        }
                    }
                    
                    OutlinedButton(
                        onClick = { times = times + "12:00" },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Another Time")
                    }
                }
            }
            
            // Additional Details Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SectionHeader(title = "Additional Details")
                    
                    // Instructions
                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Instructions") },
                        placeholder = { Text("e.g., Take with food, Avoid alcohol") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )
                    
                    // Importance Level
                    var importanceExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = importanceExpanded,
                        onExpandedChange = { importanceExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = when (importance) {
                                "high" -> "🔴 High - Critical medication"
                                "low" -> "🟢 Low - As needed"
                                else -> "🟡 Medium - Important"
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Importance Level") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = importanceExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = importanceExpanded,
                            onDismissRequest = { importanceExpanded = false }
                        ) {
                            listOf(
                                "high" to "🔴 High - Critical medication",
                                "medium" to "🟡 Medium - Important",
                                "low" to "🟢 Low - As needed"
                            ).forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        importance = value
                                        importanceExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = {
                        // Validate
                        nameError = medicationName.isBlank()
                        dosageError = dosage.isBlank()
                        
                        if (!nameError && !dosageError && times.isNotEmpty()) {
                            if (isEdit && medicationToEdit != null) {
                                // Update existing medication
                                val updatedMedication = medicationToEdit!!.copy(
                                    name = medicationName.trim(),
                                    dosage = dosage.trim(),
                                    frequency = times,
                                    instructions = instructions.trim(),
                                    importance = importance
                                )
                                viewModel.updateMedication(updatedMedication)
                            } else {
                                // Add new medication
                                viewModel.addMedication(
                                    name = medicationName.trim(),
                                    dosage = dosage.trim(),
                                    frequency = times,
                                    instructions = instructions.trim(),
                                    importance = importance
                                )
                            }
                        } else {
                            showValidationError = true
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue600
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text(if (isEdit) "Save Changes" else "Add Medication")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

