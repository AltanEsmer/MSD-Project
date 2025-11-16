package com.medicationadherence.app.presentation.family

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
import com.medicationadherence.app.domain.model.Conversation
import com.medicationadherence.app.domain.model.Message
import com.medicationadherence.app.domain.model.Patient
import com.medicationadherence.app.domain.model.SenderType
import com.medicationadherence.app.presentation.common.components.BottomNavBar
import com.medicationadherence.app.presentation.common.components.BottomNavItem
import com.medicationadherence.app.presentation.common.components.EmptyState
import com.medicationadherence.app.presentation.theme.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * Family Messages Screen - View conversations with patients
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMessagesScreen(
    onBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    viewModel: FamilyMessagesViewModel = hiltViewModel()
) {
    val selectedConversation by viewModel.selectedConversation.collectAsState()

    // Show chat view if conversation is selected, otherwise show conversation list
    if (selectedConversation != null) {
        ChatView(
            viewModel = viewModel,
            onBack = { viewModel.clearSelectedConversation() }
        )
    } else {
        ConversationListView(
            viewModel = viewModel,
            onNavigateToHome = onNavigateToHome,
            onNavigateToAlerts = onNavigateToAlerts,
            onNavigateToReports = onNavigateToReports
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationListView(
    viewModel: FamilyMessagesViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    val filteredConversations by viewModel.filteredConversations.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val availablePatients by viewModel.availablePatients.collectAsState()
    var showNewConversationDialog by remember { mutableStateOf(false) }
    
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
                    IconButton(
                        onClick = { showNewConversationDialog = true },
                        modifier = Modifier.background(Purple500, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "New Conversation",
                            tint = Color.White
                        )
                    }
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
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    placeholder = { Text("Search conversations...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Error message
            errorMessage?.let { error ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Red100
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Red600)
                        Text(error, color = Red600, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Red600)
                        }
                    }
                }
            }
            
            // Content
            if (isLoading && filteredConversations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
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
                                onClick = { viewModel.selectConversation(conversation) }
                            )
                        }
                    }
                    
                    // Bottom padding for nav bar
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
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

    // New Conversation Dialog
    if (showNewConversationDialog) {
        NewConversationDialog(
            patients = availablePatients,
            existingConversationPatientIds = filteredConversations.map { it.patientId },
            onPatientSelected = { patient ->
                viewModel.createConversation(patient.id, patient.name)
                showNewConversationDialog = false
            },
            onDismiss = { showNewConversationDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatView(
    viewModel: FamilyMessagesViewModel,
    onBack: () -> Unit
) {
    val conversation by viewModel.selectedConversation.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var messageText by remember { mutableStateOf("") }
    var showTemplates by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Auto-scroll to bottom when new messages arrive
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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Purple600,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Purple100),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = conversation?.patientName?.take(2)?.uppercase() ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Purple600
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = conversation?.patientName ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "Patient",
                            style = MaterialTheme.typography.bodySmall,
                            color = Purple100
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            // Initiate phone call
                            val intent = Intent(Intent.ACTION_DIAL)
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = "Call",
                            tint = Color.White
                        )
                    }
                }
            }
            
            // Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isLoading && messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                
                items(messages) { message ->
                    MessageBubble(message = message)
                }
            }
            
            // Message Input
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        IconButton(
                            onClick = { showTemplates = true }
                        ) {
                            Icon(
                                Icons.Default.InsertComment,
                                contentDescription = "Templates",
                                tint = Purple600
                            )
                        }
                        
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Type a message...") },
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4
                        )
                        
                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    viewModel.sendMessage(messageText)
                                    messageText = ""
                                }
                            },
                            enabled = messageText.isNotBlank()
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (messageText.isNotBlank()) Purple600 else Gray400
                            )
                        }
                    }
                }
            }
        }
    }

    // Message Templates Dialog
    if (showTemplates) {
        MessageTemplatesDialog(
            patientName = conversation?.patientName ?: "",
            onTemplateSelected = { template ->
                messageText = viewModel.getMessageTemplate(template, conversation?.patientName ?: "")
                showTemplates = false
            },
            onDismiss = { showTemplates = false }
        )
    }
}

@Composable
private fun MessageBubble(message: Message) {
    val isCaregiver = message.senderType == SenderType.CAREGIVER
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCaregiver) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isCaregiver) Purple600 else Color.White
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCaregiver) 16.dp else 4.dp,
                bottomEnd = if (isCaregiver) 4.dp else 16.dp
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCaregiver) Color.White else Gray900
                )
                Text(
                    text = formatMessageTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCaregiver) Purple100 else Gray500
                )
            }
        }
    }
}

@Composable
private fun MessageTemplatesDialog(
    patientName: String,
    onTemplateSelected: (MessageTemplateType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Message Templates") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MessageTemplateType.values().forEach { template ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTemplateSelected(template) },
                        colors = CardDefaults.cardColors(containerColor = Purple50)
                    ) {
                        Text(
                            text = getTemplateLabel(template),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getTemplateLabel(type: MessageTemplateType): String {
    return when (type) {
        MessageTemplateType.MORNING_CHECKUP -> "Morning Check-in"
        MessageTemplateType.EVENING_CHECKUP -> "Evening Check-in"
        MessageTemplateType.ENCOURAGEMENT -> "Encouragement"
        MessageTemplateType.MISSED_REMINDER -> "Missed Dose Reminder"
        MessageTemplateType.GENERAL_SUPPORT -> "General Support"
    }
}

private fun formatMessageTime(timestamp: LocalDateTime): String {
    val now = kotlinx.datetime.Clock.System.now()
    val messageInstant = timestamp.toInstant(TimeZone.currentSystemDefault())
    val duration = now - messageInstant
    
    return when {
        duration < 1.hours -> "Just now"
        duration < 24.hours -> "${duration.inWholeHours}h ago"
        duration < 7.days -> "${duration.inWholeDays}d ago"
        else -> "${timestamp.date.dayOfMonth}/${timestamp.date.monthNumber}"
    }
}

@Composable
private fun ConversationItemCard(
    conversation: Conversation,
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
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Purple100),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.patientName.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Purple600
                )
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
                    conversation.lastMessageTimestamp?.let { timestamp ->
                        Text(
                            text = formatMessageTime(timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Gray500
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = conversation.lastMessage.ifEmpty { "No messages yet" },
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

@Composable
private fun NewConversationDialog(
    patients: List<Patient>,
    existingConversationPatientIds: List<String>,
    onPatientSelected: (Patient) -> Unit,
    onDismiss: () -> Unit
) {
    val availablePatients = patients.filter { it.id !in existingConversationPatientIds }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start New Conversation") },
        text = {
            if (availablePatients.isEmpty()) {
                Text("All patients already have conversations. Start messaging from an existing conversation.")
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availablePatients) { patient ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPatientSelected(patient) },
                            colors = CardDefaults.cardColors(containerColor = Purple50)
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
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Purple100),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = patient.name.take(2).uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Purple600
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = patient.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (patient.email.isNotBlank()) {
                                        Text(
                                            text = patient.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Gray600
                                        )
                                    }
                                }
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Gray400
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}