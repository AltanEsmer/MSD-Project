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
    onNavigateToProfile: () -> Unit,
    onNavigateToMessages: () -> Unit = {}
) {
    val todayMedications by viewModel.todayMedications.collectAsState()
    val adherenceStats by viewModel.adherenceStats.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val weeklyAdherence by viewModel.weeklyAdherence.collectAsState()
    val monthlyAdherence by viewModel.monthlyAdherence.collectAsState()
    var viewMode by remember { mutableStateOf("week") }
    
    // Load data when screen is first composed or view mode changes
    LaunchedEffect(viewMode) {
        if (viewMode == "week") {
            viewModel.loadWeeklyAdherence()
        } else {
            viewModel.loadMonthlyAdherence()
        }
    }
    
    // Get adherence percentage
    val adherencePercentage = adherenceStats?.adherencePercentage ?: 0
    
    // Per-medication adherence - calculate from today's schedules
    val medicationAdherence = todayMedications.map { medWithSchedule ->
        val percentage = (medWithSchedule.adherenceRate * 100).toInt()
        Triple(medWithSchedule.medication.name, percentage, medWithSchedule.schedules.size)
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
                            
                            // Bar chart visualization with real data
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val adherenceData = if (viewMode == "week") weeklyAdherence else monthlyAdherence
                                val days = if (viewMode == "week") {
                                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                                } else {
                                    listOf("W1", "W2", "W3", "W4")
                                }
                                
                                days.forEachIndexed { index, day ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        val value = adherenceData.getOrNull(index)?.percentage ?: 0
                                        Box(
                                            modifier = Modifier
                                                .width(32.dp)
                                                .height((value * 1.5f).dp)
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
                    var calendarData by remember { mutableStateOf<Map<Int, Boolean>>(emptyMap()) }
                    
                    // Load calendar data
                    LaunchedEffect(Unit) {
                        val now = java.time.LocalDateTime.now()
                        val javaToday = java.time.LocalDate.of(now.year, now.monthValue, now.dayOfMonth)
                        val lastDayOfMonth = javaToday.withDayOfMonth(javaToday.lengthOfMonth())
                        
                        val dataMap = mutableMapOf<Int, Boolean>()
                        for (day in 1..lastDayOfMonth.dayOfMonth) {
                            val javaDate = javaToday.withDayOfMonth(day)
                            val kotlinDate = kotlinx.datetime.LocalDate(javaDate.year, javaDate.monthValue, javaDate.dayOfMonth)
                            val adherence = viewModel.getAdherenceForDate(kotlinDate)
                            dataMap[day] = adherence
                        }
                        calendarData = dataMap
                    }
                    
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
                                    val now = java.time.LocalDateTime.now()
                                    val monthName = java.time.Month.of(now.monthValue).toString()
                                        .lowercase().replaceFirstChar { it.uppercase() }
                                    Text("$monthName ${now.year}")
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
                                
                                // Calendar days with real data
                                val now = java.time.LocalDateTime.now()
                                val javaToday = java.time.LocalDate.of(now.year, now.monthValue, now.dayOfMonth)
                                val firstDayOfMonth = javaToday.withDayOfMonth(1)
                                val lastDayOfMonth = javaToday.withDayOfMonth(javaToday.lengthOfMonth())
                                val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Sunday
                                val totalDays = lastDayOfMonth.dayOfMonth
                                val weeksNeeded = kotlin.math.ceil((startDayOfWeek + totalDays) / 7.0).toInt()
                                
                                repeat(weeksNeeded) { week ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        repeat(7) { day ->
                                            val cellIndex = week * 7 + day
                                            val dayNum = cellIndex - startDayOfWeek + 1
                                            
                                            if (dayNum in 1..totalDays) {
                                                val adherence = calendarData[dayNum] ?: false
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
                                                        text = dayNum.toString(),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (adherence) Green700 else Red700,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .aspectRatio(1f)
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
                
                // Insights (removed dummy data - can be enhanced with ML insights later)
                item {
                    if (adherencePercentage >= 80) {
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
                                    text = "💡 Great Job!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Blue900
                                )
                                Text(
                                    text = "You're maintaining excellent adherence! Keep up the good work.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Blue800
                                )
                            }
                        }
                    } else if (adherencePercentage >= 50) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Orange50
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "💡 Reminder",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Orange600
                                )
                                Text(
                                    text = "Try to take your medications consistently to improve your health outcomes.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Orange600
                                )
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Red100
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "💡 Important",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Red700
                                )
                                Text(
                                    text = "Consider setting up reminders to help maintain your medication schedule.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Red700
                                )
                            }
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
                    3 -> onNavigateToMessages()
                    4 -> onNavigateToProfile()
                }
            },
            items = listOf(
                BottomNavItem(Icons.Default.Home, "Home"),
                BottomNavItem(Icons.Default.Medication, "Medications"),
                BottomNavItem(Icons.Default.History, "History"),
                BottomNavItem(Icons.Default.Message, "Messages"),
                BottomNavItem(Icons.Default.Person, "Profile")
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

