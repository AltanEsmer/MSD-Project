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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medicationadherence.app.presentation.common.components.BottomNavBar
import com.medicationadherence.app.presentation.common.components.BottomNavItem
import com.medicationadherence.app.presentation.common.components.EmptyState
import com.medicationadherence.app.presentation.theme.*

/**
 * Family Alerts Screen - View and manage alerts for monitored patients
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyAlertsScreen(
    onBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToReports: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    // Mock alerts data
    val mockAlerts = listOf(
        AlertItem(
            id = "1",
            type = "critical",
            patientName = "John Smith",
            message = "Missed evening medication dose",
            timestamp = "2 hours ago",
            medication = "Insulin"
        ),
        AlertItem(
            id = "2",
            type = "warning",
            patientName = "John Smith",
            message = "Low adherence rate - 65% this week",
            timestamp = "5 hours ago",
            medication = "Multiple medications"
        ),
        AlertItem(
            id = "3",
            type = "info",
            patientName = "John Smith",
            message = "Medication refill due in 3 days",
            timestamp = "1 day ago",
            medication = "Blood pressure medication"
        ),
        AlertItem(
            id = "4",
            type = "warning",
            patientName = "John Smith",
            message = "Missed morning dose detected",
            timestamp = "2 days ago",
            medication = "Cholesterol medication"
        ),
        AlertItem(
            id = "5",
            type = "info",
            patientName = "John Smith",
            message = "Regular check-in reminder",
            timestamp = "3 days ago",
            medication = "General reminder"
        )
    )
    
    val filteredAlerts = when (selectedTab) {
        1 -> mockAlerts.filter { it.type == "critical" }
        2 -> mockAlerts.filter { it.type == "warning" || it.type == "critical" }
        3 -> mockAlerts.filter { it.type == "warning" || it.type == "critical" }
        else -> mockAlerts
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
                if (filteredAlerts.isEmpty()) {
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
                        AlertCard(alert = alert)
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

