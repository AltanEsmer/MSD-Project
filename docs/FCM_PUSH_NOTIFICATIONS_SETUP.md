# Firebase Cloud Messaging Push Notifications Setup Guide

This guide provides step-by-step instructions to set up FCM push notifications for medication reminders in your Medication Adherence App.

## Overview

This setup will enable:
- **Push notifications** sent from Firebase Cloud Functions
- **Automatic scheduling** of notifications 5 minutes before medication times
- **Cross-device sync** when medications are taken
- **Reliable delivery** even when the app is closed

## Prerequisites

- Firebase project created
- `google-services.json` added to `app/` directory
- FCM V1 API enabled in Firebase Console
- Node.js installed (for Cloud Functions)
- Firebase CLI installed

## Step 1: Verify Firebase Console Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Navigate to **Project Settings** → **Cloud Messaging**
4. Verify **Cloud Messaging API (V1)** is enabled ✅
5. Note your **Project ID** (you'll need this later)

## Step 2: Install Firebase CLI

```bash
npm install -g firebase-tools
```

Login to Firebase:
```bash
firebase login
```

## Step 3: Initialize Cloud Functions

1. Navigate to your project root directory:
   ```bash
   cd "C:\Users\esmer\Desktop\Projects\MSD Project\MSD-Project"
   ```

2. Initialize Firebase Functions:
   ```bash
   firebase init functions
   ```

3. When prompted, select:
   - ✅ Functions: Configure and deploy Cloud Functions
   - Use an existing project (select your Firebase project)
   - Language: **JavaScript** (or TypeScript if preferred)
   - ESLint: Yes (recommended)
   - Install dependencies: Yes

## Step 4: Create Cloud Function for Medication Reminders

1. Navigate to the functions directory:
   ```bash
   cd functions
   ```

2. Install required dependencies:
   ```bash
   npm install firebase-admin firebase-functions
   ```

3. Create/edit `functions/index.js`:

```javascript
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

/**
 * Cloud Function to schedule medication reminder notifications
 * Triggered when a medication is created or updated
 */
exports.scheduleMedicationReminders = functions.firestore
  .document('medications/{medicationId}')
  .onWrite(async (change, context) => {
    const medication = change.after.exists ? change.after.data() : null;
    const medicationId = context.params.medicationId;
    
    if (!medication || !medication.isActive) {
      // Medication deleted or deactivated, cancel reminders
      return null;
    }
    
    const userId = medication.userId;
    if (!userId) return null;
    
    // Get user's FCM token
    const userDoc = await admin.firestore()
      .collection('patients')
      .doc(userId)
      .get();
    
    const fcmToken = userDoc.data()?.fcmToken;
    if (!fcmToken) {
      console.log(`No FCM token found for user ${userId}`);
      return null;
    }
    
    // Schedule notifications for each medication time
    const promises = medication.frequency.map(async (scheduledTime) => {
      return scheduleReminderForTime(
        fcmToken,
        medicationId,
        medication.name,
        medication.dosage,
        scheduledTime
      );
    });
    
    await Promise.all(promises);
    console.log(`Scheduled reminders for medication ${medicationId}`);
    
    return null;
  });

/**
 * Schedule a single reminder notification
 */
async function scheduleReminderForTime(
  fcmToken,
  medicationId,
  medicationName,
  dosage,
  scheduledTime
) {
  // Parse scheduled time (format: "HH:mm")
  const [hours, minutes] = scheduledTime.split(':').map(Number);
  
  // Calculate reminder time (5 minutes before scheduled time)
  const now = new Date();
  const reminderTime = new Date();
  reminderTime.setHours(hours, minutes - 5, 0, 0);
  
  // If reminder time has passed today, schedule for tomorrow
  if (reminderTime <= now) {
    reminderTime.setDate(reminderTime.getDate() + 1);
  }
  
  const delayMs = reminderTime.getTime() - now.getTime();
  
  // Use Cloud Tasks for reliable scheduling (recommended)
  // For now, we'll use a scheduled Cloud Function
  const scheduledFunction = functions.pubsub
    .schedule(`${medicationId}_${scheduledTime.replace(':', '_')}`)
    .timeZone('America/New_York') // Change to your timezone
    .onRun(async (context) => {
      return sendMedicationReminder(
        fcmToken,
        medicationId,
        medicationName,
        dosage,
        scheduledTime
      );
    });
  
  // Alternative: Use setTimeout for immediate scheduling (less reliable)
  // This is a simplified approach - for production, use Cloud Tasks
  if (delayMs > 0 && delayMs < 24 * 60 * 60 * 1000) { // Within 24 hours
    setTimeout(async () => {
      await sendMedicationReminder(
        fcmToken,
        medicationId,
        medicationName,
        dosage,
        scheduledTime
      );
    }, delayMs);
  }
  
  return scheduledFunction;
}

/**
 * Send medication reminder notification via FCM
 */
async function sendMedicationReminder(
  fcmToken,
  medicationId,
  medicationName,
  dosage,
  scheduledTime
) {
  const message = {
    token: fcmToken,
    notification: {
      title: 'Time to take medication',
      body: `${medicationName} (${dosage})`
    },
    data: {
      type: 'medication_reminder',
      medicationId: medicationId,
      medicationName: medicationName,
      dosage: dosage || '',
      scheduledTime: scheduledTime
    },
    android: {
      priority: 'high',
      notification: {
        channelId: 'medication_reminder_channel',
        sound: 'default',
        vibrateTimingsMillis: [0, 500, 250, 500],
        priority: 'high'
      }
    },
    apns: {
      headers: {
        'apns-priority': '10'
      },
      payload: {
        aps: {
          sound: 'default',
          badge: 1
        }
      }
    }
  };
  
  try {
    const response = await admin.messaging().send(message);
    console.log('Successfully sent message:', response);
    return response;
  } catch (error) {
    console.error('Error sending message:', error);
    throw error;
  }
}

/**
 * Handle medication taken event - cancel reminders on other devices
 */
exports.onMedicationTaken = functions.firestore
  .document('adherence_records/{recordId}')
  .onCreate(async (snap, context) => {
    const record = snap.data();
    
    if (record.status !== 'TAKEN') return null;
    
    const userId = record.userId || record.patientId;
    if (!userId) return null;
    
    // Get user's FCM token
    const userDoc = await admin.firestore()
      .collection('patients')
      .doc(userId)
      .get();
    
    const fcmToken = userDoc.data()?.fcmToken;
    if (!fcmToken) return null;
    
    // Send notification to cancel reminders on other devices
    const message = {
      token: fcmToken,
      data: {
        type: 'medication_taken',
        medicationId: record.medicationId,
        scheduledTime: record.date || ''
      },
      android: {
        priority: 'high'
      }
    };
    
    try {
      await admin.messaging().send(message);
      console.log('Sent medication_taken notification');
    } catch (error) {
      console.error('Error sending medication_taken notification:', error);
    }
    
    return null;
  });
```

## Step 5: Deploy Cloud Functions

1. Deploy all functions:
   ```bash
   firebase deploy --only functions
   ```

2. Or deploy specific function:
   ```bash
   firebase deploy --only functions:scheduleMedicationReminders
   ```

## Step 6: Alternative - Simple HTTP Function (Easier to Test)

If you want a simpler approach for testing, create an HTTP-triggered function:

Add to `functions/index.js`:

```javascript
/**
 * HTTP function to send test notification (for testing)
 * Call: https://us-central1-YOUR-PROJECT-ID.cloudfunctions.net/sendTestNotification
 */
exports.sendTestNotification = functions.https.onRequest(async (req, res) => {
  const { userId, medicationId, medicationName, dosage, scheduledTime } = req.body;
  
  if (!userId) {
    res.status(400).send('userId is required');
    return;
  }
  
  // Get user's FCM token
  const userDoc = await admin.firestore()
    .collection('patients')
    .doc(userId)
    .get();
  
  const fcmToken = userDoc.data()?.fcmToken;
  if (!fcmToken) {
    res.status(404).send('FCM token not found for user');
    return;
  }
  
  try {
    const response = await sendMedicationReminder(
      fcmToken,
      medicationId || 'test123',
      medicationName || 'Test Medication',
      dosage || '100mg',
      scheduledTime || '08:00'
    );
    
    res.status(200).send({ success: true, messageId: response });
  } catch (error) {
    res.status(500).send({ success: false, error: error.message });
  }
});
```

Deploy:
```bash
firebase deploy --only functions:sendTestNotification
```

## Step 7: Test the Setup

### Method 1: Using Firebase Console

1. Go to Firebase Console → **Cloud Messaging**
2. Click **Send your first message**
3. Enter:
   - **Notification title**: "Time to take medication"
   - **Notification text**: "Aspirin (100mg)"
4. Click **Send test message**
5. Get FCM token from app logs (check Logcat for: `MedicationApp: FCM token: <token>`)
6. Paste token and click **Test**

### Method 2: Using HTTP Function (if created)

```bash
curl -X POST https://us-central1-YOUR-PROJECT-ID.cloudfunctions.net/sendTestNotification \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "YOUR_USER_ID",
    "medicationName": "Aspirin",
    "dosage": "100mg",
    "scheduledTime": "08:00"
  }'
```

### Method 3: Create Test Medication in Firestore

1. Go to Firebase Console → **Firestore Database**
2. Create a document in `medications` collection:
   ```json
   {
     "id": "test-med-123",
     "name": "Aspirin",
     "dosage": "100mg",
     "frequency": ["08:00", "20:00"],
     "isActive": true,
     "userId": "YOUR_USER_ID"
   }
   ```
3. This will trigger the `scheduleMedicationReminders` function

## Step 8: Verify Notifications Are Received

1. Open your app on an Android device
2. Check Logcat for:
   ```
   MedicationMessaging: Message received from: ...
   MedicationMessaging: Message data payload: {...}
   ```
3. You should see a notification appear 5 minutes before the scheduled time

## Step 9: Monitor Function Execution

1. Go to Firebase Console → **Functions**
2. Click on your function name
3. View **Logs** tab to see execution logs
4. Check for any errors

## Troubleshooting

### Notifications Not Received

1. **Check FCM token is saved:**
   - Go to Firestore → `patients/{userId}`
   - Verify `fcmToken` field exists

2. **Check function logs:**
   - Firebase Console → Functions → Logs
   - Look for errors

3. **Verify notification permissions:**
   - Android 13+ requires runtime permission
   - Check app settings → Notifications

4. **Check device timezone:**
   - Ensure device timezone matches function timezone
   - Update timezone in function code if needed

### Function Not Triggering

1. **Check Firestore rules:**
   - Ensure Cloud Functions can read/write Firestore
   - Go to Firestore → Rules

2. **Verify function is deployed:**
   ```bash
   firebase functions:list
   ```

3. **Check function triggers:**
   - Verify document path matches: `medications/{medicationId}`

### Token Issues

1. **Token not saved:**
   - Check `MedicationApp.kt` logs
   - Verify user is logged in when token is generated

2. **Token expired:**
   - FCM tokens refresh automatically
   - Check `onNewToken` is being called

## Advanced: Using Cloud Tasks for Reliable Scheduling

For production, use Cloud Tasks instead of setTimeout:

1. Enable Cloud Tasks API:
   ```bash
   gcloud services enable cloudtasks.googleapis.com
   ```

2. Install Cloud Tasks library:
   ```bash
   npm install @google-cloud/tasks
   ```

3. Update function to use Cloud Tasks (see Firebase documentation)

## Next Steps

1. ✅ Test with a real medication
2. ✅ Monitor function execution
3. ✅ Set up error alerts
4. ✅ Configure timezone handling
5. ✅ Implement retry logic for failed notifications

## Quick Reference

- **Function logs**: Firebase Console → Functions → Logs
- **FCM tokens**: Firestore → `patients/{userId}/fcmToken`
- **Test function**: `firebase functions:shell`
- **Deploy**: `firebase deploy --only functions`
- **View functions**: `firebase functions:list`

## Support

- [Firebase Cloud Functions Docs](https://firebase.google.com/docs/functions)
- [FCM Admin SDK](https://firebase.google.com/docs/cloud-messaging/admin/send-messages)
- [Cloud Tasks Documentation](https://cloud.google.com/tasks/docs)

