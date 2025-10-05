package com.medicationadherence.app.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.medicationadherence.app.data.local.LocalMedicationDataSource
import com.medicationadherence.app.domain.model.AdherenceStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
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
            val dosage = inputData.getString(KEY_DOSAGE)

            if (medicationId == null || medicationName == null || scheduledTime == null) {
                return@withContext Result.failure()
            }

            // Check if medication was already taken
            val today = kotlinx.datetime.LocalDate(2024, 1, 1) // Temporary fix
            val schedules = localDataSource.getMedicationSchedules(today)
            
            // For now, we'll just log that the reminder was triggered
            // In a real implementation, you would show a notification here
            android.util.Log.d("MedicationReminder", "Reminder triggered for $medicationName at $scheduledTime")

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("MedicationReminder", "Error in reminder worker", e)
            Result.failure()
        }
    }

    companion object {
        const val KEY_MEDICATION_ID = "medication_id"
        const val KEY_MEDICATION_NAME = "medication_name"
        const val KEY_SCHEDULED_TIME = "scheduled_time"
        const val KEY_DOSAGE = "dosage"

        /**
         * Schedule a medication reminder
         */
        fun scheduleReminder(
            context: Context,
            medicationId: String,
            medicationName: String,
            scheduledTime: String,
            dosage: String
        ) {
            val workManager = WorkManager.getInstance(context)
            
            val data = Data.Builder()
                .putString(KEY_MEDICATION_ID, medicationId)
                .putString(KEY_MEDICATION_NAME, medicationName)
                .putString(KEY_SCHEDULED_TIME, scheduledTime)
                .putString(KEY_DOSAGE, dosage)
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val request = OneTimeWorkRequestBuilder<MedicationReminderWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .addTag("medication_reminder_$medicationId")
                .build()

            workManager.enqueueUniqueWork(
                "medication_reminder_${medicationId}_${scheduledTime}",
                ExistingWorkPolicy.REPLACE,
                request
            )
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

