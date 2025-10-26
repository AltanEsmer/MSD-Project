package com.medicationadherence.app.presentation.patient.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
 * Patient Profile Screen - Settings and profile management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileScreen(
    patientName: String = "Patient",
    patientAge: String = "65",
    healthConditions: List<String> = emptyList(),
    emergencyContact: String = "",
    onBack: () -> Unit,
    onSwitchToFamily: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToMedications: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(true) }
    var sound by remember { mutableStateOf(true) }
    var textSize by remember { mutableStateOf(16f) }
    var highContrast by remember { mutableStateOf(false) }
    var voiceGuidance by remember { mutableStateOf(false) }
    var simplifiedMode by remember { mutableStateOf(false) }
    
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
                    .background(Blue600)
                    .padding(24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Column {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.offset(x = (-12).dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = patientName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "$patientAge years old",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Blue100
                            )
                        }
                        
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
            
            // Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .offset(y = (-24).dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Personal Information
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Personal Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            if (healthConditions.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Health Conditions",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Gray600
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        healthConditions.take(3).forEach { condition ->
                                            Badge(containerColor = Gray200) {
                                                Text(condition, color = Gray700)
                                            }
                                        }
                                    }
                                }
                            }
                            
                            if (emergencyContact.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Emergency Contact",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Gray600
                                    )
                                    Text(
                                        text = emergencyContact,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Notifications Settings
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Blue100),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = Blue600,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = "Notifications",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            SettingItem(
                                title = "Medication Reminders",
                                subtitle = "Push notifications for doses",
                                checked = notifications,
                                onCheckedChange = { notifications = it }
                            )
                            
                            SettingItem(
                                title = "Sound & Vibration",
                                subtitle = "Alert sounds",
                                checked = sound,
                                onCheckedChange = { sound = it }
                            )
                        }
                    }
                }
                
                // Accessibility Settings
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Purple100),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Accessibility,
                                        contentDescription = null,
                                        tint = Purple600,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = "Accessibility",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Text Size",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("A", style = MaterialTheme.typography.bodySmall)
                                    Slider(
                                        value = textSize,
                                        onValueChange = { textSize = it },
                                        valueRange = 12f..24f,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text("A", style = MaterialTheme.typography.headlineSmall)
                                }
                            }
                            
                            SettingItem(
                                title = "High Contrast Mode",
                                subtitle = null,
                                checked = highContrast,
                                onCheckedChange = { highContrast = it }
                            )
                            
                            SettingItem(
                                title = "Voice Guidance",
                                subtitle = null,
                                checked = voiceGuidance,
                                onCheckedChange = { voiceGuidance = it }
                            )
                            
                            SettingItem(
                                title = "Simplified Interface",
                                subtitle = null,
                                checked = simplifiedMode,
                                onCheckedChange = { simplifiedMode = it }
                            )
                        }
                    }
                }
                
                // Family Connection
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onSwitchToFamily),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Green100),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.People,
                                    contentDescription = null,
                                    tint = Green600,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Family Caregivers",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Manage connected family members",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gray600
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Gray400
                            )
                        }
                    }
                }
                
                // App Info
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            ProfileMenuItem(title = "About MediCare", onClick = {})
                            Divider()
                            ProfileMenuItem(title = "Privacy Policy", onClick = {})
                            Divider()
                            ProfileMenuItem(title = "Terms of Service", onClick = {})
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Version 1.0.0",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray600
                            )
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
                    1 -> onNavigateToMedications()
                    2 -> onNavigateToHistory()
                    3 -> {} // Already on profile
                }
            },
            items = listOf(
                BottomNavItem(Icons.Default.Home, "Home"),
                BottomNavItem(Icons.Default.Medication, "Medications"),
                BottomNavItem(Icons.Default.History, "History"),
                BottomNavItem(Icons.Default.Person, "Profile")
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SettingItem(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ProfileMenuItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Gray400,
            modifier = Modifier.size(20.dp)
        )
    }
}

