package com.medicationadherence.app.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.medicationadherence.app.data.local.LocalMedicationDataSource
import com.medicationadherence.app.data.notification.NotificationHelper
import com.medicationadherence.app.domain.model.AdherenceStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for medication reminders
 */
@HiltWorker
class MedicationReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val localDataSource: LocalMedicationDataSource
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val medicationId = inputData.getString(KEY_MEDICATION_ID)
            val medicationName = inputData.getString(KEY_MEDICATION_NAME)
            val scheduledTime = inputData.getString(KEY_SCHEDULED_TIME)
            val dosage = inputData.getString(KEY_DOSAGE) ?: ""

            if (medicationId == null || medicationName == null || scheduledTime == null) {
                android.util.Log.e("MedicationReminder", "Missing required data for reminder")
                return@withContext Result.failure()
            }

            // Check if medication was already taken today
            val today = kotlinx.datetime.Clock.System.now()
                .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
            val schedulesFlow = localDataSource.getMedicationSchedules(today)
            val schedules = schedulesFlow.first()
            
            // Check if this specific schedule is still pending
            val schedule = schedules.firstOrNull { 
                it.medicationId == medicationId && 
                it.scheduledTime == scheduledTime &&
                it.status == AdherenceStatus.PENDING
            }
            
            if (schedule == null) {
                // Medication already taken or schedule doesn't exist
                android.util.Log.d("MedicationReminder", "Medication $medicationName already taken or schedule not found")
                return@withContext Result.success()
            }
            
            // Show notification
            val notificationId = generateNotificationId(medicationId, scheduledTime)
            NotificationHelper.showMedicationReminder(
                context = applicationContext,
                medicationId = medicationId,
                medicationName = medicationName,
                dosage = dosage,
                scheduledTime = scheduledTime,
                notificationId = notificationId
            )
            
            android.util.Log.d("MedicationReminder", "Reminder notification shown for $medicationName at $scheduledTime")

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("MedicationReminder", "Error in reminder worker", e)
            Result.failure()
        }
    }
    
    private fun generateNotificationId(medicationId: String, scheduledTime: String): Int {
        val hash = (medicationId + scheduledTime).hashCode()
        return 1000 + (hash % 10000).absoluteValue()
    }
    
    private fun Int.absoluteValue(): Int = if (this < 0) -this else this

    companion object {
        const val KEY_MEDICATION_ID = "medication_id"
        const val KEY_MEDICATION_NAME = "medication_name"
        const val KEY_SCHEDULED_TIME = "scheduled_time"
        const val KEY_DOSAGE = "dosage"

        /**
         * Schedule a medication reminder with a specific delay in minutes
         */
        fun scheduleReminderWithDelay(
            context: Context,
            medicationId: String,
            medicationName: String,
            scheduledTime: String,
            dosage: String,
            delayMinutes: Int
        ) {
            val workManager = WorkManager.getInstance(context)
            
            android.util.Log.d("MedicationReminderWorker", 
                "Scheduling reminder: medication=$medicationName, time=$scheduledTime, delay=$delayMinutes minutes")
            
            val data = Data.Builder()
                .putString(KEY_MEDICATION_ID, medicationId)
                .putString(KEY_MEDICATION_NAME, medicationName)
                .putString(KEY_SCHEDULED_TIME, scheduledTime)
                .putString(KEY_DOSAGE, dosage)
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            // For delays less than 15 minutes, WorkManager might not be reliable
            // Use seconds for very short delays (less than 15 minutes)
            val request = when {
                delayMinutes == 0 -> {
                    // Show immediately - use 1 second delay to ensure it triggers
                    android.util.Log.d("MedicationReminderWorker", 
                        "Delay is 0, using 1 second delay for immediate notification")
                    OneTimeWorkRequestBuilder<MedicationReminderWorker>()
                        .setInputData(data)
                        .setConstraints(constraints)
                        .setInitialDelay(1, TimeUnit.SECONDS)
                        .addTag("medication_reminder_$medicationId")
                        .addTag("medication_reminder_${medicationId}_$scheduledTime")
                        .build()
                }
                delayMinutes < 15 && delayMinutes > 0 -> {
                    // Use seconds for short delays (more reliable)
                    val delaySeconds = delayMinutes * 60L
                    android.util.Log.d("MedicationReminderWorker", 
                        "Using seconds delay: $delaySeconds seconds")
                    OneTimeWorkRequestBuilder<MedicationReminderWorker>()
                        .setInputData(data)
                        .setConstraints(constraints)
                        .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
                        .addTag("medication_reminder_$medicationId")
                        .addTag("medication_reminder_${medicationId}_$scheduledTime")
                        .build()
                }
                else -> {
                    // Use minutes for longer delays
                    OneTimeWorkRequestBuilder<MedicationReminderWorker>()
                        .setInputData(data)
                        .setConstraints(constraints)
                        .setInitialDelay(delayMinutes.toLong(), TimeUnit.MINUTES)
                        .addTag("medication_reminder_$medicationId")
                        .addTag("medication_reminder_${medicationId}_$scheduledTime")
                        .build()
                }
            }

            workManager.enqueueUniqueWork(
                "medication_reminder_${medicationId}_${scheduledTime}",
                ExistingWorkPolicy.REPLACE,
                request
            )
            
            android.util.Log.d("MedicationReminderWorker", 
                "Reminder scheduled successfully with WorkManager")
        }

        /**
         * Schedule a medication reminder (immediate - for backward compatibility)
         */
        fun scheduleReminder(
            context: Context,
            medicationId: String,
            medicationName: String,
            scheduledTime: String,
            dosage: String
        ) {
            scheduleReminderWithDelay(context, medicationId, medicationName, scheduledTime, dosage, 0)
        }

        /**
         * Cancel a medication reminder
         */
        fun cancelReminder(context: Context, medicationId: String) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelAllWorkByTag("medication_reminder_$medicationId")
        }
    }
}

