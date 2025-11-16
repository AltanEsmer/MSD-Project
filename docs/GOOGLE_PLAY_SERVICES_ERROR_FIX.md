# Google Play Services Error Fix

## Problem

When running the app, you may encounter the following error:

```
Failed to get service from broker. (Ask Gemini)
java.lang.SecurityException: Unknown calling package name 'com.google.android.gms'.
```

This error occurs when the app tries to initialize Firebase Cloud Messaging (FCM) but Google Play Services is not available or properly configured on the device/emulator.

## Root Cause

The error happens in `MedicationApp.kt` when calling `FirebaseMessaging.getInstance().token`. This requires Google Play Services to be:
1. Installed on the device/emulator
2. Up to date
3. Properly configured with the app's package name and SHA-1 fingerprint

Common scenarios:
- Running on an emulator without Google Play Services
- Google Play Services not installed or outdated on physical device
- SHA-1 fingerprint not registered in Firebase Console
- Package name mismatch between app and Firebase configuration

## Solution Implemented

The fix makes FCM initialization **graceful and non-blocking**:

1. **Google Play Services Check**: Before initializing FCM, the app checks if Google Play Services is available
2. **Error Handling**: Wraps FCM initialization in try-catch blocks to handle SecurityException and other exceptions
3. **Non-Blocking**: The app continues to function normally even if FCM fails to initialize
4. **Logging**: Logs warnings instead of crashing, so developers can diagnose issues

### Code Changes

**MedicationApp.kt**:
- Added `isGooglePlayServicesAvailable()` method to check availability
- Modified `initializeFCM()` to handle exceptions gracefully
- Only initializes FCM if Google Play Services is available

**build.gradle.kts**:
- Added `play-services-base` dependency for GoogleApiAvailability

## How It Works

1. On app startup, `onCreate()` checks if Google Play Services is available
2. If available, FCM initialization proceeds
3. If unavailable, a warning is logged and the app continues
4. If FCM initialization fails (SecurityException, etc.), it's caught and logged
5. The app continues to function normally - only push notifications will be unavailable

## Testing

### On Emulator Without Google Play Services
- App should start without crashing
- Logcat should show: "Google Play Services not available. FCM will be disabled."
- All other features should work normally

### On Device/Emulator With Google Play Services
- App should start normally
- FCM token should be retrieved and saved
- Logcat should show: "FCM token: [token]"

### With Network Issues
- App should handle network errors gracefully
- Should not crash if Firebase is unreachable

## Additional Troubleshooting

If you still encounter issues:

### 1. Verify Package Name
Ensure `applicationId` in `app/build.gradle.kts` matches the package name in Firebase Console:
```kotlin
applicationId = "com.medicationadherence.app"
```

### 2. Check google-services.json
Verify `app/google-services.json` has the correct package name:
```json
"package_name": "com.medicationadherence.app"
```

### 3. Add SHA-1 Fingerprint (Optional)
For additional Firebase features, add your app's SHA-1 fingerprint to Firebase Console:

**Get SHA-1:**
```bash
# Debug keystore
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# Release keystore (if applicable)
keytool -list -v -keystore [path-to-keystore] -alias [alias]
```

Then add it in Firebase Console → Project Settings → Your Android App → Add fingerprint

### 4. Update Google Play Services
On physical devices, ensure Google Play Services is up to date:
- Settings → Apps → Google Play Services → Update

### 5. Use Emulator With Google Play
When testing FCM, use an emulator with Google Play Services:
- Create AVD with "Google APIs" or "Google Play" system image
- Not "AOSP" system image (doesn't include Google Play Services)

## Impact

**What Still Works:**
- ✅ Firebase Authentication
- ✅ Firestore database operations
- ✅ All app features except push notifications
- ✅ Local notifications (medication reminders)

**What Doesn't Work:**
- ❌ Firebase Cloud Messaging (push notifications)
- ❌ Remote push notifications from server

## Future Improvements

1. **Retry Logic**: Implement retry mechanism for FCM token retrieval
2. **User Notification**: Show user-friendly message if FCM is unavailable
3. **Fallback**: Use local notifications as fallback for push notifications
4. **Settings**: Add toggle in settings to enable/disable FCM

## Related Files

- `app/src/main/java/com/medicationadherence/app/MedicationApp.kt`
- `app/build.gradle.kts`
- `app/google-services.json`
- `docs/FIREBASE_SETUP_GUIDE.md`

## References

- [Firebase Cloud Messaging Setup](https://firebase.google.com/docs/cloud-messaging/android/client)
- [Google Play Services](https://developers.google.com/android/guides/overview)
- [Troubleshooting FCM](https://firebase.google.com/docs/cloud-messaging/android/troubleshooting)

