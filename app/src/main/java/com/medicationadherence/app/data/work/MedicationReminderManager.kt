package com.medicationadherence.app.data.work

import android.content.Context
import androidx.work.WorkManager
import com.medicationadherence.app.data.local.LocalMedicationDataSource
import com.medicationadherence.app.data.work.MedicationReminderWorker
import com.medicationadherence.app.domain.model.Medication
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager class for handling medication reminders
 */
@Singleton
class MedicationReminderManager @Inject constructor(
    private val context: Context,
    private val localDataSource: LocalMedicationDataSource
) {
    
    /**
     * Schedule reminders for all active medications
     */
    suspend fun scheduleAllReminders() {
        val medications = localDataSource.getAllMedications().first()
        medications.forEach { medication ->
            medication.frequency.forEach { time ->
                scheduleReminderForTime(
                    medication = medication,
                    scheduledTime = time,
                    delayMinutes = null // Will calculate delay to exact scheduled time
                )
            }
        }
    }

    /**
     * Schedule reminder for a specific medication
     */
    suspend fun scheduleReminder(medicationId: String) {
        val medication = localDataSource.getMedicationById(medicationId)
        medication?.let { med ->
            med.frequency.forEach { time ->
                scheduleReminderForTime(
                    medication = med,
                    scheduledTime = time,
                    delayMinutes = null // Will calculate delay to exact scheduled time
                )
            }
        }
    }
    
    /**
     * Schedule reminder for a specific time with optional delay override
     * @param medication The medication to schedule reminder for
     * @param scheduledTime Time in HH:mm format
     * @param delayMinutes Optional delay in minutes. If null, calculates delay to exact scheduled time
     */
    fun scheduleReminderForTime(
        medication: Medication,
        scheduledTime: String,
        delayMinutes: Int? = null
    ) {
        val delay = delayMinutes ?: calculateDelayToReminder(scheduledTime)
        
        android.util.Log.d("MedicationReminderManager", 
            "Scheduling reminder for ${medication.name} at $scheduledTime, delay: $delay minutes")
        
        if (delay < 0) {
            // Time has passed today, schedule for tomorrow
            val delayUntilTomorrow = calculateDelayToTomorrow(scheduledTime)
            android.util.Log.d("MedicationReminderManager", 
                "Time passed, scheduling for tomorrow, delay: $delayUntilTomorrow minutes")
            MedicationReminderWorker.scheduleReminderWithDelay(
                context = context,
                medicationId = medication.id,
                medicationName = medication.name,
                scheduledTime = scheduledTime,
                dosage = medication.dosage,
                delayMinutes = delayUntilTomorrow
            )
        } else if (delay == 0) {
            // Time is exactly now, show immediately
            android.util.Log.d("MedicationReminderManager", 
                "Time is now, showing notification immediately")
            MedicationReminderWorker.scheduleReminderWithDelay(
                context = context,
                medicationId = medication.id,
                medicationName = medication.name,
                scheduledTime = scheduledTime,
                dosage = medication.dosage,
                delayMinutes = 0
            )
        } else {
            // Schedule for future time
            android.util.Log.d("MedicationReminderManager", 
                "Scheduling for $delay minutes from now")
            MedicationReminderWorker.scheduleReminderWithDelay(
                context = context,
                medicationId = medication.id,
                medicationName = medication.name,
                scheduledTime = scheduledTime,
                dosage = medication.dosage,
                delayMinutes = delay
            )
        }
    }
    
    /**
     * Schedule next reminder after medication is taken
     * This is called automatically when medication is marked as taken
     */
    suspend fun scheduleNextReminder(medication: Medication, lastScheduledTime: String) {
        // Schedule for the next occurrence of this time (tomorrow)
        val delayUntilTomorrow = calculateDelayToTomorrow(lastScheduledTime)
        
        MedicationReminderWorker.scheduleReminderWithDelay(
            context = context,
            medicationId = medication.id,
            medicationName = medication.name,
            scheduledTime = lastScheduledTime,
            dosage = medication.dosage,
            delayMinutes = delayUntilTomorrow
        )
    }

    /**
     * Cancel reminders for a specific medication
     */
    fun cancelReminder(medicationId: String) {
        MedicationReminderWorker.cancelReminder(context, medicationId)
    }

    /**
     * Cancel all reminders
     */
    fun cancelAllReminders() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag("medication_reminder")
    }
    
    /**
     * Calculate delay in minutes until the exact scheduled time
     * Returns negative value if time has passed today
     * Returns 0 if time is exactly now (within same minute)
     */
    private fun calculateDelayToReminder(scheduledTime: String): Int {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val scheduled = LocalTime.parse(scheduledTime, timeFormatter)
        
        val now = java.time.LocalDateTime.now()
        val currentTime = now.toLocalTime()
        
        android.util.Log.d("MedicationReminderManager", 
            "Calculating delay: currentTime=$currentTime, scheduled=$scheduled")
        
        // Calculate target time (exact scheduled time)
        val reminderTime = scheduled
        
        // Calculate minutes until reminder time
        val duration = java.time.Duration.between(currentTime, reminderTime)
        val minutesUntilReminder = duration.toMinutes().toInt()
        val secondsUntilReminder = duration.seconds
        
        android.util.Log.d("MedicationReminderManager", 
            "Duration: $secondsUntilReminder seconds, $minutesUntilReminder minutes")
        
        // If time has passed (negative), schedule for tomorrow
        if (minutesUntilReminder < 0) {
            android.util.Log.d("MedicationReminderManager", 
                "Time has passed, will schedule for tomorrow")
            return -1 // Signal to schedule for tomorrow
        }
        
        // If 0 minutes (same minute), return 0 to show immediately
        // If positive, return the delay
        android.util.Log.d("MedicationReminderManager", 
            "Delay calculated: $minutesUntilReminder minutes")
        return minutesUntilReminder
    }
    
    /**
     * Calculate delay in minutes until tomorrow's scheduled time (exact time)
     */
    private fun calculateDelayToTomorrow(scheduledTime: String): Int {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val scheduled = LocalTime.parse(scheduledTime, timeFormatter)
        
        val now = java.time.LocalDateTime.now()
        val currentTime = now.toLocalTime()
        
        // Calculate target time (exact scheduled time tomorrow)
        val reminderTime = scheduled
        
        // Calculate minutes until midnight (end of today)
        val minutesUntilMidnight = java.time.Duration.between(currentTime, java.time.LocalTime.MAX).toMinutes().toInt() + 1
        
        // Add minutes from midnight to reminder time tomorrow
        val minutesFromMidnightToReminder = reminderTime.toSecondOfDay() / 60
        
        // Total delay = time until midnight + time from midnight to reminder
        val totalDelay = minutesUntilMidnight + minutesFromMidnightToReminder
        
        return totalDelay
    }
}
