# Firebase Cloud Messaging Remote Notifications Setup Guide

This guide explains how to set up remote push notifications using Firebase Cloud Messaging (FCM) for the Medication Adherence App. Remote notifications complement local WorkManager notifications by enabling cross-device sync and server-triggered notifications.

## Overview

The app uses a dual notification system:
- **Local Notifications (WorkManager)**: Reliable device-specific reminders scheduled 5 minutes before medication times
- **Remote Notifications (FCM)**: Cross-device sync and server-triggered notifications

## Prerequisites

1. Firebase project created and configured
2. `google-services.json` added to `app/` directory
3. Firebase Cloud Messaging API enabled in Firebase Console
4. Android app registered in Firebase Console

## Firebase Console Setup

### Step 1: Enable Cloud Messaging API

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Navigate to **Project Settings** → **Cloud Messaging**
4. Ensure **Cloud Messaging API (V1)** is enabled (✅ You have this enabled)
5. **Cloud Messaging API (Legacy)** is not required - V1 API is the recommended approach
6. For backend/Cloud Functions, you'll use OAuth2 tokens (not server keys) - see implementation options below

### Step 2: Configure Android App

1. Verify `google-services.json` is in `app/` directory
2. Ensure package name matches: `com.medicationadherence.app`
3. Add SHA-1/SHA-256 certificates (for FCM features):
   - Go to **Project Settings** → **Your Apps** → **Android App**
   - Click **Add fingerprint**
   - Get SHA-1: `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`
   - Get SHA-256: Same command, look for SHA-256 value

## Remote Notification Implementation Options

### Option 1: Cloud Functions (Recommended)

Use Firebase Cloud Functions to schedule and send notifications. The Admin SDK automatically uses FCM V1 API.

#### Setup Cloud Functions

1. Install Firebase CLI:
   ```bash
   npm install -g firebase-tools
   ```

2. Initialize Functions:
   ```bash
   firebase init functions
   ```

3. Create a Cloud Function to schedule notifications:

```javascript
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

// Schedule notification 5 minutes before medication time
exports.scheduleMedicationReminder = functions.firestore
  .document('medications/{medicationId}')
  .onCreate(async (snap, context) => {
    const medication = snap.data();
    const userId = medication.userId;
    
    // Get user's FCM token
    const userDoc = await admin.firestore()
      .collection('patients')
      .doc(userId)
      .get();
    const fcmToken = userDoc.data()?.fcmToken;
    
    if (!fcmToken) return;
    
    // Schedule notifications for each frequency
    medication.frequency.forEach(async (time) => {
      const [hours, minutes] = time.split(':').map(Number);
      const reminderTime = new Date();
      reminderTime.setHours(hours, minutes - 5, 0, 0); // 5 minutes before
      
      // If time has passed today, schedule for tomorrow
      if (reminderTime < new Date()) {
        reminderTime.setDate(reminderTime.getDate() + 1);
      }
      
      const delay = reminderTime.getTime() - Date.now();
      
      // Schedule notification using Cloud Tasks or setTimeout
      setTimeout(async () => {
        await admin.messaging().send({
          token: fcmToken,
          notification: {
            title: 'Time to take medication',
            body: `${medication.name} (${medication.dosage})`
          },
          data: {
            type: 'medication_reminder',
            medicationId: medication.id,
            medicationName: medication.name,
            dosage: medication.dosage,
            scheduledTime: time
          },
          android: {
            priority: 'high',
            notification: {
              channelId: 'medication_reminder_channel',
              sound: 'default',
              vibrateTimingsMillis: [0, 500, 250, 500]
            }
          }
        });
      }, delay);
    });
  });
```

### Option 2: Backend Service

Use a backend service (Node.js, Python, etc.) with FCM Admin SDK. The Admin SDK automatically uses FCM V1 API and handles OAuth2 authentication.

#### Node.js Example

```javascript
const admin = require('firebase-admin');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

async function sendMedicationReminder(userId, medication) {
  // Get FCM token from Firestore
  const userDoc = await admin.firestore()
    .collection('patients')
    .doc(userId)
    .get();
  const fcmToken = userDoc.data()?.fcmToken;
  
  if (!fcmToken) return;
  
  const message = {
    token: fcmToken,
    notification: {
      title: 'Time to take medication',
      body: `${medication.name} (${medication.dosage})`
    },
    data: {
      type: 'medication_reminder',
      medicationId: medication.id,
      medicationName: medication.name,
      dosage: medication.dosage,
      scheduledTime: medication.scheduledTime
    },
    android: {
      priority: 'high',
      notification: {
        channelId: 'medication_reminder_channel'
      }
    }
  };
  
  await admin.messaging().send(message);
}
```

### Option 3: Firebase Console (Testing Only)

For testing purposes, you can send notifications manually:

1. Go to Firebase Console → **Cloud Messaging**
2. Click **Send your first message**
3. Enter notification title and text
4. Click **Send test message**
5. Enter FCM token from app logs
6. Click **Test**

## Notification Payload Structure

### Data Message (Recommended)

```json
{
  "type": "medication_reminder",
  "medicationId": "med123",
  "medicationName": "Aspirin",
  "dosage": "100mg",
  "scheduledTime": "08:00"
}
```

### Notification Types

1. **medication_reminder**: Reminder 5 minutes before medication time
2. **medication_taken**: Medication was taken on another device
3. **medication_updated**: Medication schedule was updated

## Cross-Device Sync

When a medication is taken on one device:

1. Update Firestore adherence record
2. Send FCM message to other devices:
   ```json
   {
     "type": "medication_taken",
     "medicationId": "med123",
     "scheduledTime": "08:00"
   }
   ```
3. Other devices receive notification and cancel local reminders

## Testing

### Get FCM Token

The FCM token is logged when the app starts. Check Logcat:
```
MedicationApp: FCM token: <token>
```

### Send Test Notification

**Recommended: Use Firebase Console**
1. Go to Firebase Console → **Cloud Messaging** → **Send your first message**
2. Enter notification title and text
3. Click **Send test message**
4. Enter FCM token from app logs
5. Click **Test**

**Alternative: Use FCM V1 API (requires OAuth2 token)**
For programmatic testing with V1 API, you'll need to:
1. Generate an OAuth2 access token (using service account)
2. Use the V1 endpoint: `https://fcm.googleapis.com/v1/projects/{project-id}/messages:send`
3. See [FCM V1 API documentation](https://firebase.google.com/docs/cloud-messaging/migrate-v1) for details

**Note:** For production, use Cloud Functions or backend service with Admin SDK (handles authentication automatically).

## Troubleshooting

### Notifications Not Received

1. Check FCM token is saved in Firestore (`patients/{userId}/fcmToken`)
2. Verify notification permissions are granted (Android 13+)
3. Check notification channel is created
4. Verify `google-services.json` is correct
5. Check Logcat for FCM errors

### Token Refresh

FCM tokens refresh automatically. The app handles this in `MedicationMessagingService.onNewToken()`.

### Background Restrictions

On some Android devices, background restrictions may prevent notifications. Users should:
1. Disable battery optimization for the app
2. Allow background activity
3. Grant notification permissions

## Security Considerations

1. **OAuth2 Tokens**: With V1 API, use OAuth2 tokens (not server keys). Keep service account credentials secure
2. **Token Storage**: FCM tokens are stored in Firestore with user authentication
3. **Data Validation**: Validate all notification data on the server side
4. **Rate Limiting**: Implement rate limiting for notification sending
5. **Service Account**: Use service account JSON files securely, never commit to version control

## Next Steps

1. Set up Cloud Functions or backend service
2. Implement notification scheduling logic
3. Test cross-device sync
4. Monitor notification delivery rates
5. Implement analytics for notification effectiveness

## References

- [Firebase Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging)
- [FCM V1 API Migration Guide](https://firebase.google.com/docs/cloud-messaging/migrate-v1)
- [FCM Admin SDK](https://firebase.google.com/docs/cloud-messaging/admin/send-messages)
- [Cloud Functions Documentation](https://firebase.google.com/docs/functions)

