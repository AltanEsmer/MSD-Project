package com.medicationadherence.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.medicationadherence.app.data.firestore.FirestoreTokenDataSource
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application class for Medication Adherence App
 */
@HiltAndroidApp
class MedicationApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var tokenDataSource: FirestoreTokenDataSource
    
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val tokenRefreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val token = intent?.getStringExtra("token")
            if (token != null) {
                saveFcmToken(token)
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        
        // Only initialize FCM if Google Play Services is available
        if (isGooglePlayServicesAvailable()) {
            initializeFCM()
        } else {
            Log.w("MedicationApp", "Google Play Services not available. FCM will be disabled.")
        }
        
        // Register receiver for FCM token refresh
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                tokenRefreshReceiver,
                IntentFilter("com.medicationadherence.app.FCM_TOKEN_REFRESHED"),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(
                tokenRefreshReceiver,
                IntentFilter("com.medicationadherence.app.FCM_TOKEN_REFRESHED")
            )
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        try {
            unregisterReceiver(tokenRefreshReceiver)
        } catch (e: Exception) {
            // Receiver may not be registered
        }
    }
    
    /**
     * Check if Google Play Services is available
     */
    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        return resultCode == ConnectionResult.SUCCESS
    }
    
    /**
     * Initialize Firebase Cloud Messaging
     * Handles errors gracefully if Google Play Services is not available
     */
    private fun initializeFCM() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    val exception = task.exception
                    if (exception != null) {
                        Log.w("MedicationApp", "Failed to get FCM token: ${exception.message}", exception)
                        // Check if it's a Google Play Services issue
                        if (exception.message?.contains("Google Play Services") == true ||
                            exception.message?.contains("com.google.android.gms") == true) {
                            Log.w("MedicationApp", "Google Play Services issue detected. FCM disabled.")
                        }
                    }
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.d("MedicationApp", "FCM token: $token")
                saveFcmToken(token)
            }
        } catch (e: SecurityException) {
            Log.w("MedicationApp", "SecurityException when initializing FCM: ${e.message}", e)
            // App can continue without FCM
        } catch (e: Exception) {
            Log.w("MedicationApp", "Exception when initializing FCM: ${e.message}", e)
            // App can continue without FCM
        }
    }
    
    /**
     * Save FCM token to Firestore for current user
     */
    private fun saveFcmToken(token: String) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            scope.launch {
                try {
                    tokenDataSource.saveFcmToken(userId, token)
                    Log.d("MedicationApp", "FCM token saved for user: $userId")
                } catch (e: Exception) {
                    Log.e("MedicationApp", "Failed to save FCM token", e)
                }
            }
        } else {
            Log.d("MedicationApp", "User not logged in, FCM token will be saved on login")
        }
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
