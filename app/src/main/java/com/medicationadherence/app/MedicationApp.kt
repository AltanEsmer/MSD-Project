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
        
        // Set up uncaught exception handler for Google Play Services SecurityExceptions
        // This prevents the app from crashing when Google Play Services throws SecurityException
        // The error can occur in background threads (like GoogleApiManager), so we catch it globally
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            // Check if this is a Google Play Services SecurityException
            val isGooglePlayServicesError = exception is SecurityException && 
                (exception.message?.contains("com.google.android.gms") == true ||
                 exception.message?.contains("Unknown calling package") == true ||
                 exception.message?.contains("Failed to get service from broker") == true)
            
            if (isGooglePlayServicesError) {
                Log.w("MedicationApp", "Caught SecurityException from Google Play Services: ${exception.message}. " +
                        "This usually means SHA-1/SHA-256 fingerprints are not registered in Firebase Console. " +
                        "The app will continue without Google Play Services features.", exception)
                // Don't crash the app, just log the error
                return@setDefaultUncaughtExceptionHandler
            }
            
            // Check stack trace for GoogleApiManager errors
            val stackTrace = exception.stackTraceToString()
            if (stackTrace.contains("GoogleApiManager") || 
                stackTrace.contains("com.google.android.gms")) {
                Log.w("MedicationApp", "Caught Google Play Services error in background thread: ${exception.message}. " +
                        "The app will continue without Google Play Services features.", exception)
                return@setDefaultUncaughtExceptionHandler
            }
            
            // For other exceptions, use the default handler
            defaultHandler?.uncaughtException(thread, exception)
        }
        
        // Initialize FCM with error handling
        // We don't check GoogleApiAvailability first because that can also throw SecurityException
        // Instead, we just try to initialize and catch any errors
        initializeFCM()
        
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
     * Initialize Firebase Cloud Messaging
     * Handles errors gracefully if Google Play Services is not available
     * We don't check GoogleApiAvailability first because that can also throw SecurityException
     */
    private fun initializeFCM() {
        try {
            // Wrap in try-catch to handle SecurityException from Google Play Services
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    val exception = task.exception
                    if (exception != null) {
                        val errorMessage = exception.message ?: ""
                        Log.w("MedicationApp", "Failed to get FCM token: $errorMessage", exception)
                        
                        // Check if it's a Google Play Services issue
                        if (errorMessage.contains("Google Play Services") ||
                            errorMessage.contains("com.google.android.gms") ||
                            errorMessage.contains("Unknown calling package") ||
                            errorMessage.contains("Failed to get service from broker") ||
                            exception is SecurityException) {
                            Log.w("MedicationApp", "Google Play Services issue detected. FCM will be disabled. " +
                                    "Please register SHA-1/SHA-256 fingerprints in Firebase Console if you need FCM.")
                        }
                    }
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.d("MedicationApp", "FCM token retrieved successfully: ${token.take(20)}...")
                saveFcmToken(token)
            }
        } catch (e: SecurityException) {
            Log.w("MedicationApp", "SecurityException when initializing FCM: ${e.message}. " +
                    "FCM will be disabled. Please register SHA-1/SHA-256 fingerprints in Firebase Console.", e)
            // App can continue without FCM
        } catch (e: Exception) {
            Log.w("MedicationApp", "Exception when initializing FCM: ${e.message}. FCM will be disabled.", e)
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
