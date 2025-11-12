package com.medicationadherence.app.presentation.patient.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.medicationadherence.app.presentation.common.components.HealthConditionBadge
import com.medicationadherence.app.presentation.patient.viewmodel.ProfileViewModel
import com.medicationadherence.app.presentation.theme.*

/**
 * Profile Setup Screen - Collect basic user information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }
    var selectedConditions by remember { mutableStateOf(setOf<String>()) }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    
    // Navigate to dashboard when profile is successfully created
    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            viewModel.clearUpdateSuccess()
            onComplete()
        }
    }
    
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
    
    val isValid = name.isNotBlank() && age.isNotBlank()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue600,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Blue600)
                    .padding(bottom = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "Your Profile",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tell us a bit about yourself",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Blue100
                    )
                }
            }
            
            // Form Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White)
                    .offset(y = (-24).dp)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Profile Icon
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .offset(y = (-48).dp)
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Blue600),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                // Full Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Age
                OutlinedTextField(
                    value = age,
                    onValueChange = { if (it.all { char -> char.isDigit() }) age = it },
                    label = { Text("Age *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
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
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        commonConditions.forEach { condition ->
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
                
                // Emergency Contact
                OutlinedTextField(
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    label = { Text("Emergency Contact (Optional)") },
                    placeholder = { Text("Phone number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Continue Button
                Button(
                    onClick = {
                        viewModel.updateProfile(
                            name = name,
                            age = age,
                            conditions = selectedConditions.toList(),
                            emergencyContact = emergencyContact,
                            bloodType = null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = isValid && !isLoading,
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
                            text = "Continue",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                // Progress Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(2) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (index == 0) Blue600 else Gray300)
                        )
                        if (index < 1) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // Simple implementation - in production would use FlowRow from accompanist
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}

