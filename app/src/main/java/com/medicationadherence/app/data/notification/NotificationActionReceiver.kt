package com.medicationadherence.app.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.medicationadherence.app.data.local.LocalMedicationDataSource
import com.medicationadherence.app.data.work.MedicationReminderManager
import com.medicationadherence.app.domain.model.AdherenceStatus
import com.medicationadherence.app.domain.model.MedicationSchedule
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * BroadcastReceiver for handling notification actions (Mark as Taken, Snooze)
 */
@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var localDataSource: LocalMedicationDataSource
    
    @Inject
    lateinit var reminderManager: MedicationReminderManager
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        const val ACTION_MARK_TAKEN = "com.medicationadherence.app.ACTION_MARK_TAKEN"
        const val ACTION_SNOOZE = "com.medicationadherence.app.ACTION_SNOOZE"
        
        const val EXTRA_MEDICATION_ID = "medication_id"
        const val EXTRA_SCHEDULE_ID = "schedule_id"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_SCHEDULED_TIME = "scheduled_time"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_MARK_TAKEN -> handleMarkAsTaken(context, intent)
            ACTION_SNOOZE -> handleSnooze(context, intent)
        }
    }
    
    private fun handleMarkAsTaken(context: Context, intent: Intent) {
        val medicationId = intent.getStringExtra(EXTRA_MEDICATION_ID) ?: return
        val scheduleId = intent.getStringExtra(EXTRA_SCHEDULE_ID)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val scheduledTime = intent.getStringExtra(EXTRA_SCHEDULED_TIME) ?: return
        
        scope.launch {
            try {
                // Cancel the notification
                if (notificationId != -1) {
                    NotificationHelper.cancelNotification(context, notificationId)
                }
                
                // Find the schedule for today with this medication and time
                val today = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                
                val schedulesFlow = localDataSource.getMedicationSchedules(today)
                val scheduleList: List<MedicationSchedule> = schedulesFlow.first()
                
                val targetSchedule = scheduleList.firstOrNull { schedule ->
                    schedule.medicationId == medicationId &&
                    schedule.scheduledTime == scheduledTime &&
                    schedule.status == AdherenceStatus.PENDING
                }
                
                if (targetSchedule != null) {
                    // Update schedule status to TAKEN
                    localDataSource.updateScheduleStatus(
                        targetSchedule.id,
                        AdherenceStatus.TAKEN
                    )
                    
                    // Log the dose
                    localDataSource.logDose(medicationId, AdherenceStatus.TAKEN)
                    
                    // Auto-reschedule next reminder
                    val medication = localDataSource.getMedicationById(medicationId)
                    if (medication != null) {
                        reminderManager.scheduleNextReminder(medication, scheduledTime)
                    }
                    
                    Log.d("NotificationAction", "Medication $medicationId marked as taken")
                } else {
                    // If schedule not found, just log the dose and reschedule
                    localDataSource.logDose(medicationId, AdherenceStatus.TAKEN)
                    
                    val medication = localDataSource.getMedicationById(medicationId)
                    if (medication != null) {
                        reminderManager.scheduleNextReminder(medication, scheduledTime)
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationAction", "Error marking medication as taken", e)
            }
        }
    }
    
    private fun handleSnooze(context: Context, intent: Intent) {
        val medicationId = intent.getStringExtra(EXTRA_MEDICATION_ID) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val scheduledTime = intent.getStringExtra(EXTRA_SCHEDULED_TIME) ?: return
        
        scope.launch {
            try {
                // Cancel the current notification
                if (notificationId != -1) {
                    NotificationHelper.cancelNotification(context, notificationId)
                }
                
                // Get medication
                val medication = localDataSource.getMedicationById(medicationId)
                if (medication != null) {
                    // Parse scheduled time
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                    val originalTime = LocalTime.parse(scheduledTime, timeFormatter)
                    
                    // Add 15 minutes
                    val snoozedTime = originalTime.plusMinutes(15)
                    val snoozedTimeString = snoozedTime.format(timeFormatter)
                    
                    // Schedule reminder for 15 minutes from now
                    reminderManager.scheduleReminderForTime(
                        medication,
                        snoozedTimeString,
                        delayMinutes = 0 // Show immediately (or 5 minutes before if we want to keep the pattern)
                    )
                    
                    Log.d("NotificationAction", "Medication $medicationId snoozed until $snoozedTimeString")
                }
            } catch (e: Exception) {
                Log.e("NotificationAction", "Error snoozing medication", e)
            }
        }
    }
}

