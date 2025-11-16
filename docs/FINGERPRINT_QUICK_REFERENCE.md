# Quick Reference: Your SHA-1/SHA-256 Fingerprints

## Your Current Fingerprints

**SHA-1:**
```
B2:EC:A0:4D:E2:10:95:2C:2F:FB:93:AC:9B:4D:8C:65:98:B3:51:34
```

**SHA-256:**
```
DC:99:99:4A:7C:B9:F7:CC:3D:4B:F2:AE:F6:7C:46:3A:4C:D0:91:E1:EC:A6:92:40:67:B6:22:96:95:8B:02:6D
```

## Steps to Fix the Error

### 1. Go to Firebase Console
- URL: https://console.firebase.google.com/
- Project: **medication-adherence-app-d2cc2**

### 2. Navigate to Project Settings
- Click the gear icon ⚙️ (top left)
- Click **Project Settings**

### 3. Find Your Android App
- Scroll down to **Your apps** section
- Find: **com.medicationadherence.app**

### 4. Add Fingerprints
- Click **Add fingerprint** button
- Paste SHA-1: `B2:EC:A0:4D:E2:10:95:2C:2F:FB:93:AC:9B:4D:8C:65:98:B3:51:34`
- Click **Save**
- Click **Add fingerprint** again
- Paste SHA-256: `DC:99:99:4A:7C:B9:F7:CC:3D:4B:F2:AE:F6:7C:46:3A:4C:D0:91:E1:EC:A6:92:40:67:B6:22:96:95:8B:02:6D`
- Click **Save**

### 5. Wait 2-3 Minutes
Firebase needs time to process the fingerprints.

### 6. Rebuild Your App
```bash
./gradlew clean
./gradlew build
```

Or in Android Studio:
- **Build** → **Clean Project**
- **Build** → **Rebuild Project**

### 7. Uninstall and Reinstall
- Uninstall the app from your device/emulator
- Reinstall it
- The error should be gone!

## Verification

After completing the steps, check Logcat. You should see:
- ✅ `FCM token retrieved successfully` - Success!
- ❌ `Google Play Services issue detected` - Fingerprint not registered yet (wait longer or check again)

## Package Name Verification

Make sure this matches everywhere:
- **Package Name:** `com.medicationadherence.app`
- **File:** `app/build.gradle.kts` → `applicationId`
- **File:** `app/google-services.json` → `package_name`
- **File:** `app/src/main/AndroidManifest.xml` → `package` attribute

## Still Having Issues?

See the detailed guide: [SHA1_FINGERPRINT_SETUP.md](./SHA1_FINGERPRINT_SETUP.md)

