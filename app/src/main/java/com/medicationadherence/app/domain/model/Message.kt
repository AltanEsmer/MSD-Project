package com.medicationadherence.app.domain.model

import kotlinx.datetime.LocalDateTime

/**
 * Domain model for a message in a conversation
 */
data class Message(
    val id: String = "",
    val conversationId: String,
    val senderId: String,
    val senderType: SenderType,
    val content: String,
    val timestamp: LocalDateTime,
    val read: Boolean = false,
    val readAt: LocalDateTime? = null
)

/**
 * Type of message sender
 */
enum class SenderType {
    CAREGIVER,
    PATIENT
}

/**
 * Domain model for a conversation between caregiver and patient
 */
data class Conversation(
    val id: String = "",
    val patientId: String,
    val patientName: String,
    val caregiverId: String,
    val lastMessage: String = "",
    val lastMessageTimestamp: LocalDateTime? = null,
    val lastMessageSenderId: String = "",
    val unreadCount: Int = 0,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
