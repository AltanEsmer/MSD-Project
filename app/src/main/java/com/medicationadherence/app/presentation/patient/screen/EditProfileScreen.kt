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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medicationadherence.app.presentation.common.components.HealthConditionBadge
import com.medicationadherence.app.presentation.patient.viewmodel.ProfileViewModel
import com.medicationadherence.app.presentation.theme.*

/**
 * Edit Profile Screen - Allows editing of patient profile information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    initialName: String = "",
    initialAge: String = "",
    initialConditions: List<String> = emptyList(),
    initialEmergencyContact: String = "",
    initialBloodType: String? = null,
    onSave: (name: String, age: String, conditions: List<String>, emergencyContact: String, bloodType: String?) -> Unit,
    onCancel: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf(initialName) }
    var age by remember { mutableStateOf(initialAge) }
    var emergencyContact by remember { mutableStateOf(initialEmergencyContact) }
    var bloodType by remember { mutableStateOf(initialBloodType ?: "") }
    var selectedConditions by remember { mutableStateOf(initialConditions.toSet()) }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()

    val commonConditions = listOf(
        "Diabetes",
        "Hypertension",
        "Heart Disease",
        "Asthma",
        "Arthritis",
        "High Cholesterol",
        "Thyroid",
        "Depression"
    )

    val bloodTypes = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "")

    val isValid = name.isNotBlank() && age.isNotBlank()

    // Show success message and navigate back
    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            onSave(name, age, selectedConditions.toList(), emergencyContact, bloodType.ifBlank { null })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue600,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Gray50)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Full Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                }
            )

            // Age
            OutlinedTextField(
                value = age,
                onValueChange = { if (it.all { char -> char.isDigit() }) age = it },
                label = { Text("Age *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                }
            )

            // Blood Type
            var bloodTypeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = bloodTypeExpanded,
                onExpandedChange = { bloodTypeExpanded = !bloodTypeExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = bloodType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Blood Type (Optional)") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodTypeExpanded)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Favorite, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Select blood type") }
                )
                ExposedDropdownMenu(
                    expanded = bloodTypeExpanded,
                    onDismissRequest = { bloodTypeExpanded = false }
                ) {
                    bloodTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(if (type.isBlank()) "Not specified" else type) },
                            onClick = {
                                bloodType = type
                                bloodTypeExpanded = false
                            }
                        )
                    }
                }
            }

            // Health Conditions
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Health Conditions (Optional)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Select any that apply",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )

                // Condition chips
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    commonConditions.chunked(2).forEach { rowConditions ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowConditions.forEach { condition ->
                                HealthConditionBadge(
                                    condition = condition,
                                    selected = condition in selectedConditions,
                                    onClick = {
                                        selectedConditions = if (condition in selectedConditions) {
                                            selectedConditions - condition
                                        } else {
                                            selectedConditions + condition
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Emergency Contact
            OutlinedTextField(
                value = emergencyContact,
                onValueChange = { emergencyContact = it },
                label = { Text("Emergency Contact (Optional)") },
                placeholder = { Text("Phone number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null)
                }
            )

            // Error Message
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Red100),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Red600,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Red700
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    viewModel.updateProfile(
                        name = name,
                        age = age,
                        conditions = selectedConditions.toList(),
                        emergencyContact = emergencyContact,
                        bloodType = bloodType.ifBlank { null }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                enabled = !isLoading && isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue600
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Save Changes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Cancel Button
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
