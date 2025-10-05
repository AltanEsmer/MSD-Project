package com.medicationadherence.app.data.work

import android.content.Context
import androidx.work.WorkManager
import com.medicationadherence.app.data.local.LocalMedicationDataSource
import com.medicationadherence.app.data.work.MedicationReminderWorker
import kotlinx.coroutines.flow.first
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
                MedicationReminderWorker.scheduleReminder(
                    context = context,
                    medicationId = medication.id,
                    medicationName = medication.name,
                    scheduledTime = time,
                    dosage = medication.dosage
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
                MedicationReminderWorker.scheduleReminder(
                    context = context,
                    medicationId = med.id,
                    medicationName = med.name,
                    scheduledTime = time,
                    dosage = med.dosage
                )
            }
        }
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
}
