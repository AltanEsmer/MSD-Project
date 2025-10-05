package com.medicationadherence.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class for Medication Adherence App
 */
@HiltAndroidApp
class MedicationApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    /**
     * Create notification channels for medication reminders
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Medication reminder channel
            val medicationChannel = NotificationChannel(
                MEDICATION_REMINDER_CHANNEL_ID,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for medication reminders"
                enableVibration(true)
                enableLights(true)
            }

            // Missed dose alert channel
            val missedDoseChannel = NotificationChannel(
                MISSED_DOSE_CHANNEL_ID,
                "Missed Dose Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for missed medication doses"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(medicationChannel)
            notificationManager.createNotificationChannel(missedDoseChannel)
        }
    }

    companion object {
        const val MEDICATION_REMINDER_CHANNEL_ID = "medication_reminder_channel"
        const val MISSED_DOSE_CHANNEL_ID = "missed_dose_channel"
    }
}
