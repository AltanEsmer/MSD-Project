package com.medicationadherence.app.presentation.patient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medicationadherence.app.data.firestore.FirestoreMessageDataSource
import com.medicationadherence.app.domain.model.Conversation
import com.medicationadherence.app.domain.model.Message
import com.medicationadherence.app.domain.model.SenderType
import com.medicationadherence.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for Patient Messages Screen
 */
@HiltViewModel
class PatientMessagesViewModel @Inject constructor(
    private val messageDataSource: FirestoreMessageDataSource,
    private val authRepository: AuthRepository
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

    private var currentUserId: String? = null
    private var conversationsListener: (() -> Unit)? = null
    private var messagesListener: (() -> Unit)? = null

    init {
        viewModelScope.launch {
            try {
                // Get current user ID
                authRepository.authState.collect { state ->
                    if (state is com.medicationadherence.app.domain.model.AuthState.Authenticated) {
                        currentUserId = state.userId
                        loadConversations()
                        setupConversationsListener()
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to initialize: ${e.message}"
            }
        }
    }

    /**
     * Load all conversations for the current patient
     */
    private fun loadConversations() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                currentUserId?.let { userId ->
                    val convos = messageDataSource.getConversationsForPatient(userId)
                    _conversations.value = convos.sortedByDescending { it.lastMessageTimestamp }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load conversations: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Setup real-time listener for conversations
     */
    private fun setupConversationsListener() {
        viewModelScope.launch {
            try {
                currentUserId?.let { userId ->
                    conversationsListener = messageDataSource.listenToConversations(userId) { convos ->
                        _conversations.value = convos.sortedByDescending { it.lastMessageTimestamp }
                    }
                }
            } catch (e: Exception) {
                // Real-time updates not available
            }
        }
    }

    /**
     * Setup real-time listener for messages in a conversation
     */
    private fun setupMessagesListener(conversationId: String) {
        viewModelScope.launch {
            try {
                messagesListener?.invoke() // Cancel previous listener
                messagesListener = messageDataSource.listenToMessages(conversationId) { msgs ->
                    _messages.value = msgs.sortedBy { it.timestamp }
                    // Mark messages as read
                    markMessagesAsRead(conversationId, msgs)
                }
            } catch (e: Exception) {
                // Real-time updates not available
            }
        }
    }

    /**
     * Select a conversation to view
     */
    fun selectConversation(conversation: Conversation) {
        _selectedConversation.value = conversation
        loadMessages(conversation.id)
        setupMessagesListener(conversation.id)
    }

    /**
     * Clear conversation selection
     */
    fun clearSelection() {
        _selectedConversation.value = null
        _messages.value = emptyList()
        messagesListener?.invoke()
        messagesListener = null
    }

    /**
     * Load messages for a conversation
     */
    private fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val msgs = messageDataSource.getMessages(conversationId)
                _messages.value = msgs.sortedBy { it.timestamp }
                markMessagesAsRead(conversationId, msgs)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load messages: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Send a message to the caregiver
     */
    fun sendMessage(content: String) {
        viewModelScope.launch {
            try {
                val conversation = _selectedConversation.value ?: return@launch
                currentUserId?.let { userId ->
                    val message = Message(
                        id = UUID.randomUUID().toString(),
                        conversationId = conversation.id,
                        senderId = userId,
                        senderType = SenderType.PATIENT,
                        content = content,
                        timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                        read = false
                    )
                    
                    val result = messageDataSource.sendMessage(message)
                    if (result.isFailure) {
                        _errorMessage.value = "Failed to send message"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to send message: ${e.message}"
            }
        }
    }

    /**
     * Mark all unread messages as read
     */
    private fun markMessagesAsRead(conversationId: String, messages: List<Message>) {
        viewModelScope.launch {
            try {
                currentUserId?.let { userId ->
                    messages
                        .filter { !it.read && it.senderId != userId }
                        .forEach { message ->
                            messageDataSource.markMessageAsRead(message.id)
                        }
                }
            } catch (e: Exception) {
                // Silently fail
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        conversationsListener?.invoke()
        messagesListener?.invoke()
    }
}
