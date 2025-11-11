package com.medicationadherence.app.data.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.medicationadherence.app.data.firestore.FirestoreTokenDataSource
import com.medicationadherence.app.data.notification.NotificationHelper
import com.medicationadherence.app.data.work.MedicationReminderManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Firebase Cloud Messaging service for handling push notifications
 */
@AndroidEntryPoint
class MedicationMessagingService : FirebaseMessagingService() {
    
    @Inject
    lateinit var tokenDataSource: FirestoreTokenDataSource
    
    @Inject
    lateinit var reminderManager: MedicationReminderManager
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        private const val TAG = "MedicationMessaging"
    }
    
    override fun onNewToken(token: String) {
        Log.d(TAG, "FCM token refreshed: $token")
        
        // Save token to Firestore
        scope.launch {
            try {
                // Get current user ID - you may need to inject FirebaseAuth here
                // For now, we'll handle this in the app initialization
                // The token will be saved when user is logged in
                Log.d(TAG, "New FCM token received, will be saved on next login")
            } catch (e: Exception) {
                Log.e(TAG, "Error handling new token", e)
            }
        }
        
        // Notify app to save token
        sendBroadcast(android.content.Intent("com.medicationadherence.app.FCM_TOKEN_REFRESHED").apply {
            putExtra("token", token)
        })
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        
        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
        
        // Check if message contains notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Message notification body: ${notification.body}")
            handleNotificationMessage(remoteMessage)
        }
    }
    
    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]
        
        when (type) {
            "medication_reminder" -> {
                val medicationId = data["medicationId"] ?: return
                val medicationName = data["medicationName"] ?: return
                val dosage = data["dosage"] ?: ""
                val scheduledTime = data["scheduledTime"] ?: return
                
                // Display notification
                NotificationHelper.showMedicationReminder(
                    context = this,
                    medicationId = medicationId,
                    medicationName = medicationName,
                    dosage = dosage,
                    scheduledTime = scheduledTime
                )
                
                // Schedule local reminder as backup
                scope.launch {
                    try {
                        // This would require fetching medication from database
                        // For now, the notification is already shown
                        Log.d(TAG, "Remote reminder notification displayed")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error handling remote reminder", e)
                    }
                }
            }
            "medication_taken" -> {
                // Another device marked medication as taken
                val medicationId = data["medicationId"] ?: return
                val scheduledTime = data["scheduledTime"] ?: return
                
                // Cancel local reminder if it exists
                NotificationHelper.cancelMedicationNotifications(this, medicationId)
                
                Log.d(TAG, "Medication taken on another device, cancelling local reminders")
            }
            "medication_updated" -> {
                // Medication schedule was updated, reschedule reminders
                scope.launch {
                    try {
                        reminderManager.scheduleAllReminders()
                        Log.d(TAG, "Medication updated, rescheduling all reminders")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error rescheduling reminders", e)
                    }
                }
            }
        }
    }
    
    private fun handleNotificationMessage(remoteMessage: RemoteMessage) {
        val notification = remoteMessage.notification ?: return
        
        // Extract data from notification
        val medicationId = remoteMessage.data["medicationId"] ?: ""
        val medicationName = notification.title ?: "Medication Reminder"
        val dosage = remoteMessage.data["dosage"] ?: ""
        val scheduledTime = remoteMessage.data["scheduledTime"] ?: ""
        
        // Display notification using our helper
        if (medicationId.isNotEmpty() && scheduledTime.isNotEmpty()) {
            NotificationHelper.showMedicationReminder(
                context = this,
                medicationId = medicationId,
                medicationName = medicationName,
                dosage = dosage,
                scheduledTime = scheduledTime
            )
        }
    }
    
    /**
     * Save FCM token for current user
     * Call this method when user is logged in
     */
    suspend fun saveTokenForUser(userId: String, token: String) {
        tokenDataSource.saveFcmToken(userId, token)
    }
}

