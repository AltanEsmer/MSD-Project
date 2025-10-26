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
 * Family Messages Screen - View conversations with patients
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMessagesScreen(
    onBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onNavigateToReports: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Mock conversations data
    val mockConversations = listOf(
        ConversationItem(
            id = "1",
            patientName = "John Smith",
            patientInitials = "JS",
            lastMessage = "I took my medication on time today",
            timestamp = "2 hours ago",
            unreadCount = 2,
            isOnline = true
        ),
        ConversationItem(
            id = "2",
            patientName = "Emily Johnson",
            patientInitials = "EJ",
            lastMessage = "Thank you for checking in!",
            timestamp = "1 day ago",
            unreadCount = 0,
            isOnline = false
        ),
        ConversationItem(
            id = "3",
            patientName = "Michael Brown",
            patientInitials = "MB",
            lastMessage = "I missed the morning dose, sorry",
            timestamp = "2 days ago",
            unreadCount = 1,
            isOnline = true
        ),
        ConversationItem(
            id = "4",
            patientName = "Sarah Williams",
            patientInitials = "SW",
            lastMessage = "All medications taken successfully",
            timestamp = "3 days ago",
            unreadCount = 0,
            isOnline = false
        )
    )
    
    val filteredConversations = if (searchQuery.isBlank()) {
        mockConversations
    } else {
        mockConversations.filter { 
            it.patientName.contains(searchQuery, ignoreCase = true) ||
            it.lastMessage.contains(searchQuery, ignoreCase = true)
        }
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
                    Text(
                        text = "Messages",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${filteredConversations.size} conversations",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Purple100
                    )
                }
            }
            
            // Search Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    placeholder = { Text("Search conversations...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
            
            // Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredConversations.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.Message,
                            title = "No conversations",
                            message = if (searchQuery.isBlank()) 
                                "Start a conversation with a patient" 
                            else 
                                "No conversations match your search",
                            actionText = null,
                            onActionClick = null
                        )
                    }
                } else {
                    items(filteredConversations) { conversation ->
                        ConversationItemCard(
                            conversation = conversation,
                            onClick = { /* Navigate to conversation detail */ }
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
            selectedItem = 2,
            onItemSelected = { index ->
                when (index) {
                    0 -> onNavigateToHome()
                    1 -> onNavigateToAlerts()
                    2 -> {} // Already on messages
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
private fun ConversationItemCard(
    conversation: ConversationItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Box {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Purple100),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = conversation.patientInitials,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Purple600
                    )
                }
                
                // Online indicator
                if (conversation.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .align(Alignment.BottomEnd)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Green600)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.patientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = conversation.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray500
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = conversation.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
                    maxLines = 1
                )
            }
            
            // Unread badge
            if (conversation.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Red600),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (conversation.unreadCount > 9) "9+" else conversation.unreadCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Mock data classes
data class ConversationItem(
    val id: String,
    val patientName: String,
    val patientInitials: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int,
    val isOnline: Boolean
)

