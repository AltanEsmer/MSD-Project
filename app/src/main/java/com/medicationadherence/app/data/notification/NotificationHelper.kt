package com.medicationadherence.app.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.medicationadherence.app.MedicationApp
import com.medicationadherence.app.R
import com.medicationadherence.app.presentation.MainActivity
import kotlin.math.abs

/**
 * Helper class for creating and displaying medication reminder notifications
 */
object NotificationHelper {
    private const val NOTIFICATION_ID_BASE = 1000
    
    /**
     * Display a medication reminder notification
     */
    fun showMedicationReminder(
        context: Context,
        medicationId: String,
        medicationName: String,
        dosage: String,
        scheduledTime: String,
        notificationId: Int = generateNotificationId(medicationId, scheduledTime)
    ) {
        val notificationManager = NotificationManagerCompat.from(context)
        
        // Ensure notification channel exists
        createNotificationChannel(context)
        
        // Create intent for when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("medicationId", medicationId)
            putExtra("action", "show_medication")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create "Mark as Taken" action
        val takenIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MARK_TAKEN
            putExtra(NotificationActionReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(NotificationActionReceiver.EXTRA_SCHEDULE_ID, "")
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_SCHEDULED_TIME, scheduledTime)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create "Snooze" action
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(NotificationActionReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_SCHEDULED_TIME, scheduledTime)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(context, MedicationApp.MEDICATION_REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Time to take medication")
            .setContentText("$medicationName ($dosage)")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("It's time to take your medication: $medicationName ($dosage)\nScheduled time: $scheduledTime"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_edit,
                "Mark as Taken",
                takenPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_revert,
                "Snooze (15 min)",
                snoozePendingIntent
            )
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .build()
        
        // Show notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check permission for Android 13+
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(notificationId, notification)
            }
        } else {
            notificationManager.notify(notificationId, notification)
        }
    }
    
    /**
     * Cancel a specific notification
     */
    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
    
    /**
     * Cancel all notifications for a medication
     */
    fun cancelMedicationNotifications(context: Context, medicationId: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        // Cancel notifications for all possible times (0-23 hours)
        for (hour in 0..23) {
            for (minute in 0..59 step 5) {
                val timeString = String.format("%02d:%02d", hour, minute)
                val notificationId = generateNotificationId(medicationId, timeString)
                notificationManager.cancel(notificationId)
            }
        }
    }
    
    /**
     * Generate a unique notification ID based on medication ID and scheduled time
     */
    private fun generateNotificationId(medicationId: String, scheduledTime: String): Int {
        val hash = (medicationId + scheduledTime).hashCode()
        return NOTIFICATION_ID_BASE + abs(hash % 10000)
    }
    
    /**
     * Ensure notification channel exists (for Android 8.0+)
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Channel should already be created in MedicationApp, but ensure it exists
            val channel = NotificationChannel(
                MedicationApp.MEDICATION_REMINDER_CHANNEL_ID,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for medication reminders"
                enableVibration(true)
                enableLights(true)
            }
            
            notificationManager.createNotificationChannel(channel)
        }
    }
}
