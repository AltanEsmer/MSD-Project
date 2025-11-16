package com.medicationadherence.app.presentation.family

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast
import android.content.Intent
import android.net.Uri
import com.medicationadherence.app.domain.model.Alert
import com.medicationadherence.app.domain.model.AlertType
import com.medicationadherence.app.presentation.common.components.BottomNavBar
import com.medicationadherence.app.presentation.common.components.BottomNavItem
import com.medicationadherence.app.presentation.common.components.EmptyState
import com.medicationadherence.app.presentation.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Family Alerts Screen - View and manage alerts for monitored patients
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyAlertsScreen(
    viewModel: FamilyAlertsViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToPatientDetails: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val alerts by viewModel.alerts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current
    
    // Show error toast if there's an error
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }
    
    val filteredAlerts = when (selectedTab) {
        0 -> viewModel.getFilteredAlerts(AlertFilter.ALL)
        1 -> viewModel.getFilteredAlerts(AlertFilter.CRITICAL)
        2 -> viewModel.getFilteredAlerts(AlertFilter.MISSED_DOSES)
        3 -> viewModel.getFilteredAlerts(AlertFilter.LOW_ADHERENCE)
        else -> alerts
    }
    
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Alerts",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${filteredAlerts.size} active alerts",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Purple100
                            )
                        }
                    }
                }
            }
            
            // Filter Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Purple600,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("All") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Critical") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Missed Doses") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Low Adherence") }
                )
            }
            
            // Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isLoading && filteredAlerts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Purple600)
                        }
                    }
                } else if (filteredAlerts.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.NotificationsOff,
                            title = "No alerts",
                            message = "All clear! No alerts at this time.",
                            actionText = null,
                            onActionClick = null
                        )
                    }
                } else {
                    items(filteredAlerts) { alert ->
                        AlertCardReal(
                            alert = alert,
                            onDismiss = { viewModel.dismissAlert(alert.id) },
                            onResolve = { viewModel.resolveAlert(alert.id) },
                            onViewPatient = { onNavigateToPatientDetails(alert.patientId) },
                            viewModel = viewModel
                        )
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
            selectedItem = 1,
            onItemSelected = { index ->
                when (index) {
                    0 -> onNavigateToHome()
                    1 -> {} // Already on alerts
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

@Composable
private fun AlertCardReal(
    alert: Alert,
    onDismiss: () -> Unit,
    onResolve: () -> Unit,
    onViewPatient: () -> Unit,
    viewModel: FamilyAlertsViewModel
) {
    val context = LocalContext.current
    val iconColor = when (alert.type) {
        AlertType.CRITICAL -> Red600
        AlertType.WARNING -> Orange600
        AlertType.INFO -> Blue600
    }
    
    val iconBackground = when (alert.type) {
        AlertType.CRITICAL -> Red100
        AlertType.WARNING -> Orange100
        AlertType.INFO -> Blue100
    }
    
    val icon = when (alert.type) {
        AlertType.CRITICAL -> Icons.Default.Error
        AlertType.WARNING -> Icons.Default.Warning
        AlertType.INFO -> Icons.Default.Info
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.patientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = alert.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600
                    )
                    alert.medicationName?.let { medication ->
                        Text(
                            text = medication,
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatTimestamp(alert.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray400
                    )
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewPatient,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View")
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dismiss")
                }
                OutlinedButton(
                    onClick = {
                        viewModel.getPatientPhoneNumber(alert.patientId)?.let { phoneNumber ->
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$phoneNumber")
                            }
                            context.startActivity(intent)
                        } ?: Toast.makeText(
                            context,
                            "No contact number available",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Format timestamp to relative time
 */
private fun formatTimestamp(timestamp: kotlinx.datetime.LocalDateTime): String {
    val now = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
    
    val daysDiff = now.date.toEpochDays() - timestamp.date.toEpochDays()
    
    return when {
        daysDiff == 0 -> {
            val hoursDiff = now.hour - timestamp.hour
            when {
                hoursDiff == 0 -> "Just now"
                hoursDiff == 1 -> "1 hour ago"
                hoursDiff < 24 -> "$hoursDiff hours ago"
                else -> "Today"
            }
        }
        daysDiff == 1 -> "Yesterday"
        daysDiff < 7 -> "$daysDiff days ago"
        else -> "${daysDiff / 7} weeks ago"
    }
}

@Composable
private fun AlertCard(alert: AlertItem) {
    val iconColor = when (alert.type) {
        "critical" -> Red600
        "warning" -> Orange600
        else -> Blue600
    }
    
    val iconBackground = when (alert.type) {
        "critical" -> Red100
        "warning" -> Orange100
        else -> Blue100
    }
    
    val icon = when (alert.type) {
        "critical" -> Icons.Default.Error
        "warning" -> Icons.Default.Warning
        else -> Icons.Default.Info
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.patientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = alert.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600
                    )
                    Text(
                        text = alert.medication,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alert.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray400
                    )
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { /* View patient */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View")
                }
                OutlinedButton(
                    onClick = { /* Dismiss */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dismiss")
                }
                OutlinedButton(
                    onClick = { /* Contact */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Mock data classes
data class AlertItem(
    val id: String,
    val type: String, // "critical", "warning", "info"
    val patientName: String,
    val message: String,
    val timestamp: String,
    val medication: String
)

