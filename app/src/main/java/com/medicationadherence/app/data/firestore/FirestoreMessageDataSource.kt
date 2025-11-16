package com.medicationadherence.app.data.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.medicationadherence.app.domain.model.Conversation
import com.medicationadherence.app.domain.model.Message
import com.medicationadherence.app.domain.model.SenderType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore data source for message operations
 */
@Singleton
class FirestoreMessageDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_CONVERSATIONS = "conversations"
        private const val COLLECTION_MESSAGES = "messages"
    }

    /**
     * Get all conversations for a caregiver
     */
    suspend fun getConversations(caregiverId: String): List<Conversation> {
        return try {
            val snapshot = firestore.collection(COLLECTION_CONVERSATIONS)
                .whereEqualTo("caregiverId", caregiverId)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    Conversation(
                        id = doc.id,
                        patientId = data["patientId"] as? String ?: "",
                        patientName = data["patientName"] as? String ?: "",
                        caregiverId = data["caregiverId"] as? String ?: "",
                        lastMessage = data["lastMessage"] as? String ?: "",
                        lastMessageTimestamp = (data["lastMessageTimestamp"] as? Timestamp)?.toLocalDateTime(),
                        lastMessageSenderId = data["lastMessageSenderId"] as? String ?: "",
                        unreadCount = (data["unreadCount"] as? Long)?.toInt() ?: 0,
                        createdAt = (data["createdAt"] as? Timestamp)?.toLocalDateTime() 
                            ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                        updatedAt = (data["updatedAt"] as? Timestamp)?.toLocalDateTime()
                            ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch conversations: ${e.message}", e)
        }
    }

    /**
     * Get all conversations for a patient
     */
    suspend fun getConversationsForPatient(patientId: String): List<Conversation> {
        return try {
            val snapshot = firestore.collection(COLLECTION_CONVERSATIONS)
                .whereEqualTo("patientId", patientId)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    Conversation(
                        id = doc.id,
                        patientId = data["patientId"] as? String ?: "",
                        patientName = data["patientName"] as? String ?: "",
                        caregiverId = data["caregiverId"] as? String ?: "",
                        lastMessage = data["lastMessage"] as? String ?: "",
                        lastMessageTimestamp = (data["lastMessageTimestamp"] as? Timestamp)?.toLocalDateTime(),
                        lastMessageSenderId = data["lastMessageSenderId"] as? String ?: "",
                        unreadCount = (data["unreadCount"] as? Long)?.toInt() ?: 0,
                        createdAt = (data["createdAt"] as? Timestamp)?.toLocalDateTime() 
                            ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                        updatedAt = (data["updatedAt"] as? Timestamp)?.toLocalDateTime()
                            ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch conversations: ${e.message}", e)
        }
    }

    /**
     * Setup real-time listener for conversations (works for both caregiver and patient)
     */
    fun listenToConversations(userId: String, onConversationsChanged: (List<Conversation>) -> Unit): () -> Unit {
        // Try both caregiver and patient queries
        val caregiverListener = firestore.collection(COLLECTION_CONVERSATIONS)
            .whereEqualTo("caregiverId", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                processConversationSnapshot(snapshot, onConversationsChanged)
            }

        val patientListener = firestore.collection(COLLECTION_CONVERSATIONS)
            .whereEqualTo("patientId", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                processConversationSnapshot(snapshot, onConversationsChanged)
            }

        return {
            caregiverListener.remove()
            patientListener.remove()
        }
    }

    private fun processConversationSnapshot(
        snapshot: com.google.firebase.firestore.QuerySnapshot,
        onConversationsChanged: (List<Conversation>) -> Unit
    ) {
        val conversations = snapshot.documents.mapNotNull { doc ->
            try {
                val data = doc.data ?: return@mapNotNull null
                Conversation(
                    id = doc.id,
                    patientId = data["patientId"] as? String ?: "",
                    patientName = data["patientName"] as? String ?: "",
                    caregiverId = data["caregiverId"] as? String ?: "",
                    lastMessage = data["lastMessage"] as? String ?: "",
                    lastMessageTimestamp = (data["lastMessageTimestamp"] as? Timestamp)?.toLocalDateTime(),
                    lastMessageSenderId = data["lastMessageSenderId"] as? String ?: "",
                    unreadCount = (data["unreadCount"] as? Long)?.toInt() ?: 0,
                    createdAt = (data["createdAt"] as? Timestamp)?.toLocalDateTime()
                        ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                    updatedAt = (data["updatedAt"] as? Timestamp)?.toLocalDateTime()
                        ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                )
            } catch (e: Exception) {
                null
            }
        }
        onConversationsChanged(conversations)
    }

    /**
     * Setup real-time listener for messages in a conversation
     */
    fun listenToMessages(conversationId: String, onMessagesChanged: (List<Message>) -> Unit): () -> Unit {
        val registration = firestore.collection(COLLECTION_MESSAGES)
            .whereEqualTo("conversationId", conversationId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                
                val messages = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        Message(
                            id = doc.id,
                            conversationId = data["conversationId"] as? String ?: "",
                            senderId = data["senderId"] as? String ?: "",
                            senderType = when (data["senderType"] as? String) {
                                "PATIENT" -> SenderType.PATIENT
                                else -> SenderType.CAREGIVER
                            },
                            content = data["content"] as? String ?: "",
                            timestamp = (data["timestamp"] as? Timestamp)?.toLocalDateTime()
                                ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                            read = data["read"] as? Boolean ?: false,
                            readAt = (data["readAt"] as? Timestamp)?.toLocalDateTime()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onMessagesChanged(messages)
            }
        
        return { registration.remove() }
    }

    /**
     * Get real-time conversations flow for a caregiver
     */
    fun getConversationsFlow(caregiverId: String): Flow<List<Conversation>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_CONVERSATIONS)
            .whereEqualTo("caregiverId", caregiverId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val conversations = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        Conversation(
                            id = doc.id,
                            patientId = data["patientId"] as? String ?: "",
                            patientName = data["patientName"] as? String ?: "",
                            caregiverId = data["caregiverId"] as? String ?: "",
                            lastMessage = data["lastMessage"] as? String ?: "",
                            lastMessageTimestamp = (data["lastMessageTimestamp"] as? Timestamp)?.toLocalDateTime(),
                            lastMessageSenderId = data["lastMessageSenderId"] as? String ?: "",
                            unreadCount = (data["unreadCount"] as? Long)?.toInt() ?: 0,
                            createdAt = (data["createdAt"] as? Timestamp)?.toLocalDateTime()
                                ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                            updatedAt = (data["updatedAt"] as? Timestamp)?.toLocalDateTime()
                                ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(conversations)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get messages for a specific conversation
     */
    suspend fun getMessages(conversationId: String): List<Message> {
        return try {
            val snapshot = firestore.collection(COLLECTION_MESSAGES)
                .whereEqualTo("conversationId", conversationId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    Message(
                        id = doc.id,
                        conversationId = data["conversationId"] as? String ?: "",
                        senderId = data["senderId"] as? String ?: "",
                        senderType = when (data["senderType"] as? String) {
                            "PATIENT" -> SenderType.PATIENT
                            else -> SenderType.CAREGIVER
                        },
                        content = data["content"] as? String ?: "",
                        timestamp = (data["timestamp"] as? Timestamp)?.toLocalDateTime()
                            ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                        read = data["read"] as? Boolean ?: false,
                        readAt = (data["readAt"] as? Timestamp)?.toLocalDateTime()
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch messages: ${e.message}", e)
        }
    }

    /**
     * Get real-time messages flow for a conversation
     */
    fun getMessagesFlow(conversationId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_MESSAGES)
            .whereEqualTo("conversationId", conversationId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        Message(
                            id = doc.id,
                            conversationId = data["conversationId"] as? String ?: "",
                            senderId = data["senderId"] as? String ?: "",
                            senderType = when (data["senderType"] as? String) {
                                "PATIENT" -> SenderType.PATIENT
                                else -> SenderType.CAREGIVER
                            },
                            content = data["content"] as? String ?: "",
                            timestamp = (data["timestamp"] as? Timestamp)?.toLocalDateTime()
                                ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                            read = data["read"] as? Boolean ?: false,
                            readAt = (data["readAt"] as? Timestamp)?.toLocalDateTime()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Send a new message
     */
    suspend fun sendMessage(conversationId: String, senderId: String, senderType: SenderType, content: String): Result<Message> {
        return try {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val messageId = UUID.randomUUID().toString()
            val message = Message(
                id = messageId,
                conversationId = conversationId,
                senderId = senderId,
                senderType = senderType,
                content = content,
                timestamp = now,
                read = false
            )

            val messageData = mapOf(
                "conversationId" to conversationId,
                "senderId" to senderId,
                "senderType" to senderType.name,
                "content" to content,
                "timestamp" to Timestamp.now(),
                "read" to false
            )

            firestore.collection(COLLECTION_MESSAGES)
                .document(messageId)
                .set(messageData)
                .await()

            // Update conversation with last message info
            val conversationUpdate = mapOf(
                "lastMessage" to content,
                "lastMessageTimestamp" to Timestamp.now(),
                "lastMessageSenderId" to senderId,
                "updatedAt" to Timestamp.now(),
                "unreadCount" to com.google.firebase.firestore.FieldValue.increment(
                    if (senderType == SenderType.PATIENT) 1 else 0
                )
            )

            firestore.collection(COLLECTION_CONVERSATIONS)
                .document(conversationId)
                .update(conversationUpdate)
                .await()

            Result.success(message)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to send message: ${e.message}", e))
        }
    }

    /**
     * Send a new message using Message object
     */
    suspend fun sendMessage(message: Message): Result<Unit> {
        return try {
            val messageData = mapOf(
                "conversationId" to message.conversationId,
                "senderId" to message.senderId,
                "senderType" to message.senderType.name,
                "content" to message.content,
                "timestamp" to Timestamp.now(),
                "read" to false
            )

            firestore.collection(COLLECTION_MESSAGES)
                .document(message.id)
                .set(messageData)
                .await()

            // Update conversation with last message info
            val conversationUpdate = mapOf(
                "lastMessage" to message.content,
                "lastMessageTimestamp" to Timestamp.now(),
                "lastMessageSenderId" to message.senderId,
                "updatedAt" to Timestamp.now(),
                "unreadCount" to com.google.firebase.firestore.FieldValue.increment(
                    if (message.senderType == SenderType.PATIENT) 1 else 0
                )
            )

            firestore.collection(COLLECTION_CONVERSATIONS)
                .document(message.conversationId)
                .update(conversationUpdate)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to send message: ${e.message}", e))
        }
    }

    /**
     * Mark a single message as read
     */
    suspend fun markMessageAsRead(messageId: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_MESSAGES)
                .document(messageId)
                .update(mapOf(
                    "read" to true,
                    "readAt" to Timestamp.now()
                ))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to mark message as read: ${e.message}", e))
        }
    }

    /**
     * Mark all messages in a conversation as read
     */
    suspend fun markMessagesAsRead(conversationId: String, caregiverId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection(COLLECTION_MESSAGES)
                .whereEqualTo("conversationId", conversationId)
                .whereEqualTo("read", false)
                .whereEqualTo("senderType", "PATIENT")
                .get()
                .await()

            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, mapOf(
                    "read" to true,
                    "readAt" to Timestamp.now()
                ))
            }

            // Reset unread count in conversation
            val conversationRef = firestore.collection(COLLECTION_CONVERSATIONS)
                .document(conversationId)
            batch.update(conversationRef, "unreadCount", 0)

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to mark messages as read: ${e.message}", e))
        }
    }

    /**
     * Get total unread message count for caregiver
     */
    suspend fun getUnreadCount(caregiverId: String): Int {
        return try {
            val snapshot = firestore.collection(COLLECTION_CONVERSATIONS)
                .whereEqualTo("caregiverId", caregiverId)
                .get()
                .await()

            snapshot.documents.sumOf { doc ->
                (doc.data?.get("unreadCount") as? Long)?.toInt() ?: 0
            }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Create a new conversation if it doesn't exist
     */
    suspend fun createConversation(patientId: String, patientName: String, caregiverId: String): Result<String> {
        return try {
            // Check if conversation already exists
            val existing = firestore.collection(COLLECTION_CONVERSATIONS)
                .whereEqualTo("patientId", patientId)
                .whereEqualTo("caregiverId", caregiverId)
                .get()
                .await()

            if (!existing.isEmpty) {
                return Result.success(existing.documents[0].id)
            }

            // Create new conversation
            val now = Timestamp.now()
            val conversationData = mapOf(
                "patientId" to patientId,
                "patientName" to patientName,
                "caregiverId" to caregiverId,
                "lastMessage" to "",
                "lastMessageTimestamp" to now,
                "lastMessageSenderId" to "",
                "unreadCount" to 0,
                "createdAt" to now,
                "updatedAt" to now
            )

            val docRef = firestore.collection(COLLECTION_CONVERSATIONS)
                .add(conversationData)
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create conversation: ${e.message}", e))
        }
    }

    /**
     * Extension function to convert Timestamp to LocalDateTime
     */
    private fun Timestamp.toLocalDateTime(): LocalDateTime {
        val instant = Instant.fromEpochSeconds(seconds, nanoseconds)
        return instant.toLocalDateTime(TimeZone.currentSystemDefault())
    }
}
