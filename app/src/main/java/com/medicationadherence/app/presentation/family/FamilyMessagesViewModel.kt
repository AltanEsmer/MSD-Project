package com.medicationadherence.app.presentation.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.medicationadherence.app.data.firestore.FirestoreMessageDataSource
import com.medicationadherence.app.data.firestore.FirestorePatientDataSource
import com.medicationadherence.app.domain.model.Conversation
import com.medicationadherence.app.domain.model.Message
import com.medicationadherence.app.domain.model.Patient
import com.medicationadherence.app.domain.model.SenderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Family Messages Screen
 */
@HiltViewModel
class FamilyMessagesViewModel @Inject constructor(
    private val messageDataSource: FirestoreMessageDataSource,
    private val patientDataSource: FirestorePatientDataSource,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _selectedConversation = MutableStateFlow<Conversation?>(null)
    val selectedConversation: StateFlow<Conversation?> = _selectedConversation.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _availablePatients = MutableStateFlow<List<Patient>>(emptyList())
    val availablePatients: StateFlow<List<Patient>> = _availablePatients.asStateFlow()

    val filteredConversations: StateFlow<List<Conversation>> = combine(
        _conversations,
        _searchQuery
    ) { conversations, query ->
        if (query.isBlank()) {
            conversations
        } else {
            conversations.filter {
                it.patientName.contains(query, ignoreCase = true) ||
                        it.lastMessage.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadConversations()
        loadAvailablePatients()
    }

    /**
     * Load all conversations for the current caregiver
     */
    fun loadConversations() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                // Use real-time updates
                messageDataSource.getConversationsFlow(userId)
                    .catch { e ->
                        _errorMessage.value = "Failed to load conversations: ${e.message}"
                    }
                    .collect { conversationList ->
                        _conversations.value = conversationList
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load conversations: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Select a conversation and load its messages
     */
    fun selectConversation(conversation: Conversation) {
        _selectedConversation.value = conversation
        loadMessages(conversation.id)
        markAsRead(conversation.id)
    }

    /**
     * Load messages for the selected conversation
     */
    private fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                // Use real-time updates
                messageDataSource.getMessagesFlow(conversationId)
                    .catch { e ->
                        _errorMessage.value = "Failed to load messages: ${e.message}"
                    }
                    .collect { messageList ->
                        _messages.value = messageList
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load messages: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Send a message in the current conversation
     */
    fun sendMessage(content: String) {
        val conversation = _selectedConversation.value ?: return
        val userId = firebaseAuth.currentUser?.uid ?: return
        
        if (content.isBlank()) return

        viewModelScope.launch {
            try {
                val result = messageDataSource.sendMessage(
                    conversationId = conversation.id,
                    senderId = userId,
                    senderType = SenderType.CAREGIVER,
                    content = content
                )
                
                if (result.isFailure) {
                    _errorMessage.value = "Failed to send message: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to send message: ${e.message}"
            }
        }
    }

    /**
     * Mark conversation messages as read
     */
    fun markAsRead(conversationId: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                messageDataSource.markMessagesAsRead(conversationId, userId)
            } catch (e: Exception) {
                // Silently fail - not critical
            }
        }
    }

    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Refresh conversations
     */
    fun refresh() {
        loadConversations()
    }

    /**
     * Clear selected conversation
     */
    fun clearSelectedConversation() {
        _selectedConversation.value = null
        _messages.value = emptyList()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Get message template by type
     */
    fun getMessageTemplate(type: MessageTemplateType, patientName: String = ""): String {
        return when (type) {
            MessageTemplateType.MORNING_CHECKUP -> 
                "Good morning $patientName! Just checking in - have you taken your morning medications?"
            MessageTemplateType.EVENING_CHECKUP -> 
                "Good evening $patientName! Hope you had a great day. Don't forget your evening medications."
            MessageTemplateType.ENCOURAGEMENT -> 
                "Great job on staying consistent with your medications, $patientName! Keep it up! 🌟"
            MessageTemplateType.MISSED_REMINDER -> 
                "Hi $patientName, I noticed you missed a dose today. Is everything okay? Let me know if you need any help."
            MessageTemplateType.GENERAL_SUPPORT -> 
                "Hi $patientName, I'm here if you need any support with your medications or have any questions."
        }
    }

    /**
     * Load available patients for creating conversations
     */
    fun loadAvailablePatients() {
        viewModelScope.launch {
            try {
                val patients = patientDataSource.getAllPatientsWithSharingEnabled()
                _availablePatients.value = patients
            } catch (e: Exception) {
                // Silently fail - not critical
            }
        }
    }

    /**
     * Create a new conversation with a patient
     */
    fun createConversation(patientId: String, patientName: String) {
        val caregiverId = firebaseAuth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = messageDataSource.createConversation(patientId, patientName, caregiverId)
                
                if (result.isSuccess) {
                    val conversationId = result.getOrNull() ?: return@launch
                    // Wait a bit for the real-time listener to pick up the new conversation
                    kotlinx.coroutines.delay(500)
                    // Find and select the new conversation
                    _conversations.value.firstOrNull { it.id == conversationId }?.let {
                        selectConversation(it)
                    } ?: run {
                        // If not found yet, try loading conversations directly
                        val conversations = messageDataSource.getConversations(caregiverId)
                        _conversations.value = conversations
                        conversations.firstOrNull { it.id == conversationId }?.let {
                            selectConversation(it)
                        }
                    }
                } else {
                    _errorMessage.value = "Failed to create conversation: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create conversation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

/**
 * Message template types
 */
enum class MessageTemplateType {
    MORNING_CHECKUP,
    EVENING_CHECKUP,
    ENCOURAGEMENT,
    MISSED_REMINDER,
    GENERAL_SUPPORT
}
