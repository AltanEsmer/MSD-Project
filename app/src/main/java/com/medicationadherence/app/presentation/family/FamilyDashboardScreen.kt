package com.medicationadherence.app.presentation.family

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast
import com.medicationadherence.app.presentation.common.components.*
import com.medicationadherence.app.presentation.theme.*

/**
 * Family Dashboard Screen - Monitor patients' medication adherence
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyDashboardScreen(
    viewModel: FamilyDashboardViewModel = hiltViewModel(),
    onNavigateToAlerts: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onAddPatient: () -> Unit = {},
    onSwitchToPatientMode: () -> Unit = {}
) {
    val patients by viewModel.patients.collectAsState()
    val recentActivity by viewModel.recentActivity.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    // Show error toast if there's an error
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    // Show loading state
    if (isLoading && patients.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Purple600)
        }
        return
    }

    val avgAdherence = viewModel.getAverageAdherence()
    val activeAlerts = viewModel.getActiveAlertsCount()
    
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
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "Family Dashboard",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Monitoring ${patients.size} ${if (patients.size == 1) "patient" else "patients"}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Purple100
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = onSwitchToPatientMode,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Switch to Patient Mode",
                                    tint = Color.White
                                )
                            }
                            IconButton(
                                onClick = onAddPatient,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Patient",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Quick Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Active Alerts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Purple100
                                )
                                Text(
                                    text = "$activeAlerts",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Avg. Adherence",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Purple100
                                )
                                Text(
                                    text = "$avgAdherence%",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
            
            // Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Patients",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = onAddPatient) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Patient")
                        }
                    }
                }
                
                // Patient Cards
                if (patients.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.Person,
                            title = "No patients connected",
                            message = "Connect to a patient to start monitoring their medication adherence",
                            actionText = "Connect to Patient",
                            onActionClick = onAddPatient
                        )
                    }
                } else {
                    items(patients) { patientWithStats ->
                        PatientCard(patientWithStats = patientWithStats)
                    }
                }
                
                // Recent Activity
                if (patients.isNotEmpty() && recentActivity.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recent Activity",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
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
                                recentActivity.forEach { activity ->
                                    ActivityItemReal(
                                        activity = activity,
                                        viewModel = viewModel
                                    )
                                }
                            }
                        }
                    }
                    
                    // Quick Actions
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickActionCard(
                                icon = Icons.Default.Notifications,
                                iconBackground = Red100,
                                iconTint = Red600,
                                title = "Alerts",
                                subtitle = "$activeAlerts new alerts",
                                onClick = onNavigateToAlerts,
                                modifier = Modifier.weight(1f)
                            )
                            
                            QuickActionCard(
                                icon = Icons.Default.BarChart,
                                iconBackground = Blue100,
                                iconTint = Blue600,
                                title = "Reports",
                                subtitle = "View insights",
                                onClick = onNavigateToReports,
                                modifier = Modifier.weight(1f)
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
                    1 -> onNavigateToAlerts()
                    2 -> onNavigateToMessages()
                    3 -> onNavigateToReports()
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PatientCard(patientWithStats: FamilyDashboardViewModel.PatientWithStats) {
    val patient = patientWithStats.patient
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Purple100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Purple600,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = patient.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${patient.age} years • ${patient.conditions.firstOrNull() ?: "No conditions"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                    Badge(
                        containerColor = if (patientWithStats.status == "good") Green600 else Orange600
                    ) {
                        Text(
                            text = if (patientWithStats.status == "good") "✓ Good" else "! Attention",
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Today's Progress",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                        Text(
                            text = "${patientWithStats.todayTaken}/${patientWithStats.todayTotal}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    LinearProgressIndicator(
                        progress = patientWithStats.todayTaken.toFloat() / patientWithStats.todayTotal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Blue600,
                        trackColor = Gray200
                    )
                }
                
                if (patientWithStats.missedDoses > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Orange50)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Orange600,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${patientWithStats.missedDoses} dose(s) missed today",
                            style = MaterialTheme.typography.bodySmall,
                            color = Orange600
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* Message patient */ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Message,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Message")
                    }
                    OutlinedButton(
                        onClick = { /* Call patient */ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call")
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityItemReal(
    activity: com.medicationadherence.app.domain.model.ActivityItem,
    viewModel: FamilyDashboardViewModel
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    when (activity.actionType) {
                        com.medicationadherence.app.domain.model.ActivityType.TOOK_MEDICATION -> Green100
                        com.medicationadherence.app.domain.model.ActivityType.MISSED_DOSE -> Red100
                        com.medicationadherence.app.domain.model.ActivityType.SKIPPED_MEDICATION -> Orange100
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (activity.actionType) {
                    com.medicationadherence.app.domain.model.ActivityType.TOOK_MEDICATION -> Icons.Default.CheckCircle
                    com.medicationadherence.app.domain.model.ActivityType.MISSED_DOSE -> Icons.Default.Error
                    com.medicationadherence.app.domain.model.ActivityType.SKIPPED_MEDICATION -> Icons.Default.Warning
                },
                contentDescription = null,
                tint = when (activity.actionType) {
                    com.medicationadherence.app.domain.model.ActivityType.TOOK_MEDICATION -> Green600
                    com.medicationadherence.app.domain.model.ActivityType.MISSED_DOSE -> Red600
                    com.medicationadherence.app.domain.model.ActivityType.SKIPPED_MEDICATION -> Orange600
                },
                modifier = Modifier.size(16.dp)
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.patientName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = activity.action + (activity.medicationName?.let { " - $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = Gray600
            )
            Text(
                text = viewModel.formatRelativeTime(activity.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = Gray500
            )
        }
    }
}

@Composable
private fun ActivityItem(activity: Activity) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (activity.type == "success") Green100 else Orange100
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (activity.type == "success") 
                    Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (activity.type == "success") Green600 else Orange600,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.patient,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = activity.action,
                style = MaterialTheme.typography.bodySmall,
                color = Gray600
            )
            Text(
                text = activity.time,
                style = MaterialTheme.typography.labelSmall,
                color = Gray500
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
            }
        }
    }
}

data class Activity(
    val patient: String,
    val action: String,
    val time: String,
    val type: String
)

