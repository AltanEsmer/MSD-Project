package com.medicationadherence.app.presentation.patient.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.medicationadherence.app.domain.model.AdherenceStatus
import com.medicationadherence.app.domain.model.MedicationWithSchedule
import com.medicationadherence.app.presentation.common.components.*
import com.medicationadherence.app.presentation.patient.viewmodel.MedicationViewModel
import com.medicationadherence.app.presentation.theme.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Modern Patient Dashboard Screen - Redesigned to match React prototype
 */
@Composable
fun ModernPatientDashboardScreen(
    patientName: String = "Patient",
    viewModel: MedicationViewModel = hiltViewModel(),
    onNavigateToMedications: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onAddMedication: () -> Unit,
    onSwitchToCaregiverMode: () -> Unit = {}
) {
    val todayMedications by viewModel.todayMedications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // Calculate stats
    val totalDoses = todayMedications.sumOf { it.schedules.size }
    val takenDoses = todayMedications.sumOf { med ->
        med.schedules.count { it.status == AdherenceStatus.TAKEN }
    }
    val percentage = if (totalDoses > 0) (takenDoses * 100) / totalDoses else 0
    
    // Calculate streak (simplified for MVP)
    val streak = 7
    
    // Current time for comparison
    val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    
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
                    .padding(bottom = 16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "Welcome back,",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Blue100
                            )
                            Text(
                                text = patientName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = onSwitchToCaregiverMode,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Switch to Caregiver Mode",
                                    tint = Color.White
                                )
                            }
                            IconButton(
                                onClick = onNavigateToProfile,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Date
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Today", // Simplified for MVP
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (-16).dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Stats Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            icon = Icons.Default.LocalFireDepartment,
                            iconTint = Green600,
                            iconBackground = Green100,
                            label = "Streak",
                            value = "$streak days",
                            modifier = Modifier.weight(1f)
                        )
                        
                        StatCard(
                            icon = Icons.Default.Medication,
                            iconTint = Blue600,
                            iconBackground = Blue100,
                            label = "Today",
                            value = "$takenDoses/$totalDoses",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Progress Card
                item {
                    ProgressCard(
                        title = "Today's Progress",
                        percentage = percentage,
                        taken = takenDoses,
                        total = totalDoses
                    )
                }
                
                // Section Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Medications",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = onNavigateToMedications) {
                            Text("View All")
                        }
                    }
                }
                
                // Medications List
                if (todayMedications.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.Medication,
                            title = "No medications scheduled",
                            message = "Add your first medication to get started",
                            actionText = "Add Medication",
                            onActionClick = onAddMedication
                        )
                    }
                } else {
                    items(todayMedications) { medicationWithSchedule ->
                        val nextSchedule = medicationWithSchedule.schedules
                            .filter { it.status == AdherenceStatus.PENDING }
                            .minByOrNull { it.scheduledTime }
                        
                        nextSchedule?.let { schedule ->
                            // Format time from LocalDateTime
                            val hour = schedule.scheduledTime.toString().substringAfter("T").substringBefore(":").toIntOrNull() ?: 0
                            val minute = schedule.scheduledTime.toString().substringAfter("T").substringAfter(":").substringBefore(":").toIntOrNull() ?: 0
                            val timeStr = String.format("%02d:%02d", hour, minute)
                            val isPast = false // Simplified for MVP
                            
                            ModernMedicationCard(
                                medicationName = medicationWithSchedule.medication.name,
                                dosage = medicationWithSchedule.medication.dosage,
                                time = timeStr,
                                instructions = medicationWithSchedule.medication.instructions,
                                icon = Icons.Default.Medication,
                                isTaken = schedule.status == AdherenceStatus.TAKEN,
                                isPast = isPast,
                                onTakeClick = {
                                    viewModel.takeMedication(
                                        schedule.id,
                                        medicationWithSchedule.medication.id
                                    )
                                }
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
            selectedItem = 0,
            onItemSelected = { index ->
                when (index) {
                    0 -> {} // Already on home
                    1 -> onNavigateToMedications()
                    2 -> onNavigateToHistory()
                    3 -> onNavigateToProfile()
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

