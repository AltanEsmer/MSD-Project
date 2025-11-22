package com.medicationadherence.app.presentation.patient.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.medicationadherence.app.domain.model.Message
import com.medicationadherence.app.domain.model.SenderType
import com.medicationadherence.app.presentation.common.components.BottomNavBar
import com.medicationadherence.app.presentation.common.components.BottomNavItem
import com.medicationadherence.app.presentation.common.components.EmptyState
import com.medicationadherence.app.presentation.patient.viewmodel.PatientMessagesViewModel
import com.medicationadherence.app.presentation.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Patient Messages Screen - View and respond to caregiver messages
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientMessagesScreen(
    viewModel: PatientMessagesViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToMedications: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val conversations by viewModel.conversations.collectAsState()
    val selectedConversation by viewModel.selectedConversation.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current
    
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    // Auto-scroll to latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (selectedConversation != null) {
                            IconButton(
                                onClick = { viewModel.clearSelection() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = selectedConversation?.patientName ?: "Caregiver",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Messages",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${conversations.size} conversations",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Purple100
                            )
                        }
                    }
                }
            }
            
            // Content
            if (selectedConversation == null) {
                // Conversation List
                if (isLoading && conversations.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Purple600)
                    }
                } else if (conversations.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Message,
                        title = "No Messages",
                        message = "You don't have any messages yet. Your caregiver can send you messages here.",
                        actionText = null,
                        onActionClick = null
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(conversations) { conversation ->
                            ConversationCard(
                                conversation = conversation,
                                onClick = { viewModel.selectConversation(conversation) }
                            )
                        }
                        
                        // Bottom padding for nav bar
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            } else {
                // Chat View
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Messages
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(messages) { message ->
                            MessageBubble(message = message)
                        }
                        
                        // Bottom padding for input
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    
                    // Message Input
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Type a message...") },
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Purple600,
                                    unfocusedBorderColor = Gray300
                                )
                            )
                            
                            IconButton(
                                onClick = {
                                    if (messageText.isNotBlank()) {
                                        viewModel.sendMessage(messageText)
                                        messageText = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Purple600),
                                enabled = messageText.isNotBlank()
                            ) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        
        // Bottom Navigation (only show when not in chat)
        if (selectedConversation == null) {
            BottomNavBar(
                selectedItem = 3,
                onItemSelected = { index ->
                    when (index) {
                        0 -> onNavigateToHome()
                        1 -> onNavigateToMedications()
                        2 -> onNavigateToHistory()
                        3 -> {} // Already on messages
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
}

@Composable
private fun ConversationCard(
    conversation: com.medicationadherence.app.domain.model.Conversation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    Icons.Default.Person,
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
                    Text(
                        text = conversation.patientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    conversation.lastMessageTimestamp?.let { timestamp ->
                        Text(
                            text = formatTimestamp(timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Gray400
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (conversation.unreadCount > 0) Gray900 else Gray600,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (conversation.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Purple600),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = conversation.unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Message) {
    val isFromPatient = message.senderType == SenderType.PATIENT
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromPatient) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isFromPatient) Purple600 else Color.White
            ),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isFromPatient) 12.dp else 0.dp,
                bottomEnd = if (isFromPatient) 0.dp else 12.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isFromPatient) Color.White else Gray900
                )
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isFromPatient) Color.White.copy(alpha = 0.7f) else Gray400
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: kotlinx.datetime.LocalDateTime): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    
    val daysDiff = now.date.toEpochDays() - timestamp.date.toEpochDays()
    
    return when {
        daysDiff == 0 -> {
            val hoursDiff = now.hour - timestamp.hour
            when {
                hoursDiff == 0 -> {
                    val minDiff = now.minute - timestamp.minute
                    if (minDiff < 1) "Just now" else "${minDiff}m ago"
                }
                hoursDiff < 24 -> "${hoursDiff}h ago"
                else -> "Today"
            }
        }
        daysDiff == 1 -> "Yesterday"
        daysDiff < 7 -> "$daysDiff days ago"
        else -> "${daysDiff / 7}w ago"
    }
}
