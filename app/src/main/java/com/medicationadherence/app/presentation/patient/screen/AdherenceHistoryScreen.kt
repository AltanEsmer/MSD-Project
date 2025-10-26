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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medicationadherence.app.domain.model.AdherenceStatus
import com.medicationadherence.app.presentation.common.components.*
import com.medicationadherence.app.presentation.patient.viewmodel.MedicationViewModel
import com.medicationadherence.app.presentation.theme.*

/**
 * Adherence History Screen - View medication adherence history and analytics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdherenceHistoryScreen(
    viewModel: MedicationViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToMedications: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val todayMedications by viewModel.todayMedications.collectAsState()
    var viewMode by remember { mutableStateOf("week") }
    
    // Calculate stats
    val totalDoses = todayMedications.sumOf { it.schedules.size } * 30 // Simplified
    val takenDoses = todayMedications.sumOf { med ->
        med.schedules.count { it.status == AdherenceStatus.TAKEN }
    }
    val adherencePercentage = if (totalDoses > 0) (takenDoses * 100) / totalDoses else 0
    val currentStreak = 7 // Simplified
    
    // Per-medication adherence
    val medicationAdherence = todayMedications.map { medWithSchedule ->
        val taken = medWithSchedule.schedules.count { it.status == AdherenceStatus.TAKEN }
        val total = medWithSchedule.schedules.size
        val percentage = if (total > 0) (taken * 100) / total else 0
        Triple(medWithSchedule.medication.name, percentage, total)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Gray50)
        ) {
            // Header
            TopAppBar(
                title = { Text("Adherence History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Export functionality */ }) {
                        Icon(Icons.Default.Download, contentDescription = "Export")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
            
            // Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            icon = Icons.Default.TrendingUp,
                            iconTint = Green600,
                            iconBackground = Green100,
                            label = "30-Day Rate",
                            value = "$adherencePercentage%",
                            modifier = Modifier.weight(1f)
                        )
                        
                        StatCard(
                            icon = Icons.Default.LocalFireDepartment,
                            iconTint = Orange600,
                            iconBackground = Orange100,
                            label = "Current Streak",
                            value = "$currentStreak days",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // View Toggle
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Gray100),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            listOf("week" to "Week", "month" to "Month").forEach { (mode, label) ->
                                Button(
                                    onClick = { viewMode = mode },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (viewMode == mode) Color.White else Color.Transparent,
                                        contentColor = if (viewMode == mode) Gray900 else Gray600
                                    ),
                                    elevation = if (viewMode == mode) {
                                        ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                    } else {
                                        ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(label)
                                }
                            }
                        }
                    }
                }
                
                // Adherence Chart (Simplified - would use actual charting library)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = if (viewMode == "week") "Weekly Adherence" else "Monthly Adherence",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Simple bar chart visualization
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val days = if (viewMode == "week") {
                                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                                } else {
                                    listOf("W1", "W2", "W3", "W4")
                                }
                                val values = listOf(100, 85, 100, 90, 100, 75, 95)
                                
                                days.forEachIndexed { index, day ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(32.dp)
                                                .height(((values.getOrNull(index) ?: 0) * 1.5f).dp)
                                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                                .background(Blue600)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = day,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Gray600
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Calendar View
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Calendar View",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                TextButton(onClick = { /* Open calendar picker */ }) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Oct 2024")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Calendar grid
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Day headers
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                                        Text(
                                            text = day,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Gray600,
                                            modifier = Modifier.weight(1f),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                                
                                // Calendar days (simplified)
                                repeat(4) { week ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        repeat(7) { day ->
                                            val dayNum = week * 7 + day + 1
                                            val adherence = dayNum % 5 != 0 // Mock data
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .padding(2.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (adherence) Green100 else Red100
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (dayNum <= 28) dayNum.toString() else "",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (adherence) Green700 else Red700,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Legend
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Green100)
                                    )
                                    Text(
                                        text = "All doses taken",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Gray600
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Red100)
                                    )
                                    Text(
                                        text = "Missed doses",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Gray600
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Medication-Specific Rates
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
                            Text(
                                text = "By Medication",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            medicationAdherence.forEach { (name, percentage, _) ->
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Badge(
                                            containerColor = if (percentage >= 80) Green600 else Red600
                                        ) {
                                            Text(
                                                text = "$percentage%",
                                                color = Color.White
                                            )
                                        }
                                    }
                                    LinearProgressIndicator(
                                        progress = percentage / 100f,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = if (percentage >= 80) Green500 else Red500,
                                        trackColor = Gray200
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Insights
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Blue50
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "💡 Insights",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Blue900
                            )
                            Text(
                                text = "• You're most consistent with morning medications",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Blue800
                            )
                            Text(
                                text = "• Evening doses are occasionally missed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Blue800
                            )
                            Text(
                                text = "• Your adherence improved 5% this month!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Blue800
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
            selectedItem = 2,
            onItemSelected = { index ->
                when (index) {
                    0 -> onNavigateToHome()
                    1 -> onNavigateToMedications()
                    2 -> {} // Already on history
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

