# SHA-1/SHA-256 Fingerprint Setup Guide

## Problem

If you're seeing this error:
```
Failed to get service from broker. (Ask Gemini)
java.lang.SecurityException: Unknown calling package name 'com.google.android.gms'.
```

This means your app's SHA-1/SHA-256 fingerprint is not registered in Firebase Console, or there's a mismatch between your debug/release keystores.

## Solution

### Step 1: Get Your SHA-1 Fingerprint

#### Option A: Using Gradle (Easiest)

Run this command in your project root:

**Windows (PowerShell):**
```powershell
.\gradlew signingReport
```

**Windows (CMD) or Mac/Linux:**
```bash
./gradlew signingReport
```

Look for the output under `Variant: debug`:
```
SHA1: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
SHA256: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
```

#### Option B: Using Keytool (Manual)

**Debug Keystore (Default):**
```bash
# Windows
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android

# Mac/Linux
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Release Keystore (If you have one):**
```bash
keytool -list -v -keystore [path-to-your-release-keystore] -alias [your-alias-name]
```

### Step 2: Register Fingerprint in Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **medication-adherence-app-d2cc2**
3. Click the gear icon ⚙️ → **Project Settings**
4. Scroll down to **Your apps** section
5. Find your Android app: **com.medicationadherence.app**
6. Click **Add fingerprint** button
7. Paste your **SHA-1** fingerprint (the one starting with `XX:XX:XX...`)
8. Click **Save**
9. **IMPORTANT:** Also add your **SHA-256** fingerprint if available
10. Wait 1-2 minutes for Firebase to process

### Step 3: Download Updated google-services.json (Optional)

After adding fingerprints, Firebase may ask you to download a new `google-services.json`. If prompted:

1. Click **Download google-services.json**
2. Replace `app/google-services.json` with the new file
3. Rebuild your app

### Step 4: Verify Package Name

Make sure your package name matches exactly:

**In `app/build.gradle.kts`:**
```kotlin
applicationId = "com.medicationadherence.app"
```

**In `app/google-services.json`:**
```json
"package_name": "com.medicationadherence.app"
```

**In `AndroidManifest.xml`:**
```xml
<manifest xmlns:android="..." package="com.medicationadherence.app">
```

All three must match exactly!

### Step 5: Clean and Rebuild

After registering fingerprints:

```bash
# Clean build
./gradlew clean

# Rebuild
./gradlew build
```

Or in Android Studio:
- **Build** → **Clean Project**
- **Build** → **Rebuild Project**

### Step 6: Test

Run the app again. The error should be gone. Check Logcat for:
- ✅ `FCM token retrieved successfully` - Success!
- ⚠️ `Google Play Services issue detected` - Fingerprint still not registered

## Common Issues

### Issue 1: Multiple Keystores

If you have multiple keystores (debug, release, staging), you need to register **all** of them in Firebase Console.

**Solution:** Add fingerprints for all keystores you use.

### Issue 2: Emulator Without Google Play Services

If you're testing on an emulator without Google Play Services, the error is expected.

**Solution:** 
- Use an emulator with Google Play Services (Google APIs or Google Play system image)
- Or test on a physical device

### Issue 3: Fingerprint Format

Make sure you copy the fingerprint correctly:
- ✅ Correct: `A1:B2:C3:D4:E5:F6:...`
- ❌ Wrong: `A1B2C3D4E5F6...` (no colons)
- ❌ Wrong: `SHA1: A1:B2:C3:...` (includes "SHA1:" prefix)

### Issue 4: Still Getting Error After Registration

1. **Wait 2-3 minutes** - Firebase needs time to propagate
2. **Uninstall and reinstall** the app on your device
3. **Clear app data** if reinstalling doesn't work
4. **Verify fingerprint** - Double-check you copied it correctly
5. **Check package name** - Ensure it matches exactly

## Quick Reference

### Get SHA-1 via Gradle
```bash
./gradlew signingReport
```

### Firebase Console
- URL: https://console.firebase.google.com/
- Project: medication-adherence-app-d2cc2
- Path: Project Settings → Your apps → Add fingerprint

### Package Name
- Must be: `com.medicationadherence.app`
- Check in: `build.gradle.kts`, `google-services.json`, `AndroidManifest.xml`

## Need Help?

If you're still having issues:

1. Check Logcat for the exact error message
2. Verify your fingerprint format (with colons)
3. Make sure you're using the correct keystore (debug vs release)
4. Try uninstalling and reinstalling the app
5. Wait a few minutes after adding fingerprint in Firebase

## Notes

- The app will continue to work even without FCM (push notifications just won't work)
- This error is non-critical for most app features
- Only affects Firebase Cloud Messaging (FCM) functionality
- Firestore, Auth, and other Firebase features work without SHA-1 registration



