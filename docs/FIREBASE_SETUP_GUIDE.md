# Firebase Setup Guide - Medication Adherence App

This guide provides step-by-step instructions for setting up Firebase Authentication and Firestore for the Medication Adherence mobile app.

## Prerequisites

- Google account
- Android Studio installed
- Project already has Firebase dependencies configured in `build.gradle.kts`
- Internet connection

## Step 1: Create Firebase Project

1. **Go to Firebase Console**
   - Open your web browser and navigate to: https://console.firebase.google.com/
   - Sign in with your Google account

2. **Create New Project**
   - Click **"Add project"** or **"Create a project"** button
   - Enter project name: `Medication Adherence App` (or your preferred name)
   - Click **"Continue"**

3. **Configure Google Analytics (Optional)**
   - Choose whether to enable Google Analytics
   - For this project, you can **disable Analytics** (not required for MVP)
   - Or enable it if you want analytics (free tier includes basic analytics)
   - Click **"Create project"**

4. **Wait for Project Creation**
   - Firebase will create your project (takes 30-60 seconds)
   - Click **"Continue"** when project is ready

## Step 2: Add Android App to Firebase Project

1. **From Firebase Console Dashboard**
   - You should see your project overview
   - Click the **Android icon** (or "Add app" → Android)

2. **Register App**
   - **Android package name**: `com.medicationadherence.app`
     - ⚠️ **CRITICAL**: This must match exactly with `applicationId` in `app/build.gradle.kts`
     - Verify in your project: `app/build.gradle.kts` → `defaultConfig` → `applicationId`
   
   - **App nickname** (optional): `Medication Adherence Android`
   - **Debug signing certificate SHA-1** (optional): Leave blank for now
     - You can add this later for additional features

3. **Click "Register app"**

## Step 3: Download google-services.json

1. **Download Configuration File**
   - Firebase will show a download button for `google-services.json`
   - Click **"Download google-services.json"**

2. **Place File in Project**
   - Open your project in Android Studio
   - Navigate to: `app/` directory (at the root of `app` folder)
   - **Replace** the existing `google-services.json` file with the downloaded one
   - ⚠️ **Important**: The file should be at: `app/google-services.json` (not in `src/main/`)

3. **Verify File Location**
   ```
   MSD-Project/
   └── app/
       ├── build.gradle.kts
       ├── google-services.json  ← Should be here
       └── src/
   ```

4. **Sync Project**
   - In Android Studio, click **"Sync Now"** when prompted
   - Or go to: **File → Sync Project with Gradle Files**

## Step 4: Enable Email/Password Authentication

1. **Navigate to Authentication**
   - In Firebase Console, click **"Authentication"** in the left sidebar
   - Click **"Get started"** if you see it (first time setup)

2. **Enable Sign-in Methods**
   - Click on the **"Sign-in method"** tab
   - You'll see a list of authentication providers

3. **Enable Email/Password**
   - Find **"Email/Password"** in the list
   - Click on it
   - Toggle **"Enable"** to ON
   - **Email link (passwordless sign-in)** can stay disabled for now
   - Click **"Save"**

4. **Verify Setup**
   - You should see "Email/Password" with a green checkmark
   - Status should show "Enabled"

## Step 5: Configure Firestore Database (Optional - for Future Use)

> **Note**: This step is optional. The app currently uses local Room database, but Firestore can be added later for cloud sync.

1. **Navigate to Firestore**
   - In Firebase Console, click **"Firestore Database"** in the left sidebar
   - Click **"Create database"**

2. **Choose Security Rules**
   - Select **"Start in test mode"** (for development)
   - ⚠️ **Important**: Test mode allows read/write to all users. Update rules before production!

3. **Choose Location**
   - Select a location closest to your users (e.g., `us-central`, `europe-west`, etc.)
   - Click **"Enable"**

4. **Wait for Database Creation**
   - Takes 1-2 minutes
   - You'll see an empty database

## Step 6: Verify Firebase Configuration

### 6.1 Check google-services.json

Open `app/google-services.json` and verify it contains:

```json
{
  "project_info": {
    "project_number": "123456789012",  // Should be a real number
    "project_id": "your-project-id",   // Should match your Firebase project
    "storage_bucket": "your-project.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "android_client_info": {
          "package_name": "com.medicationadherence.app"  // Must match!
        }
      }
    }
  ]
}
```

### 6.2 Verify Dependencies in build.gradle.kts

Open `app/build.gradle.kts` and verify these are present:

```kotlin
plugins {
    // ... other plugins
    id("com.google.gms.google-services")  // Should be here
}

dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
}
```

### 6.3 Check Project-Level build.gradle.kts

Open `build.gradle.kts` (project level) and verify:

```kotlin
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")  // Should be here
    }
}
```

## Step 7: Test Firebase Connection

1. **Build and Run**
   - In Android Studio, click **"Build"** → **"Make Project"** (Ctrl+F9 / Cmd+F9)
   - Ensure there are no build errors

2. **Run the App**
   - Connect an Android device or start an emulator
   - Click **"Run"** (Shift+F10)
   - The app should launch

3. **Test Sign Up**
   - Navigate to Sign Up screen
   - Enter a test email (e.g., `test@example.com`)
   - Enter password (at least 6 characters)
   - Click "Sign Up"
   - If successful, you should see the profile setup screen

4. **Verify in Firebase Console**
   - Go to **Firebase Console → Authentication → Users**
   - You should see the newly created user
   - User email and creation time should be visible

## Step 8: Troubleshooting Common Issues

### Issue 1: "google-services.json not found"
**Solution:**
- Ensure `google-services.json` is in `app/` directory (not `app/src/`)
- File name must be exactly `google-services.json` (lowercase, with hyphen)
- Sync project: **File → Sync Project with Gradle Files**

### Issue 2: "Package name mismatch"
**Solution:**
- Verify `applicationId` in `app/build.gradle.kts` matches package name in Firebase
- Must be exactly: `com.medicationadherence.app`
- If you changed it, update Firebase project settings or re-add the app

### Issue 3: "Authentication failed"
**Solution:**
- Check Firebase Console → Authentication → Sign-in methods
- Ensure "Email/Password" is enabled
- Check internet connection on device/emulator
- Verify app is using correct Firebase project (check `google-services.json`)

### Issue 4: "Build errors with Firebase"
**Solution:**
- Ensure Google Services plugin is applied: `id("com.google.gms.google-services")`
- Verify Firebase BOM version matches dependencies
- Clean and rebuild: **Build → Clean Project**, then **Build → Rebuild Project**

### Issue 5: "SHA-1 certificate error" (if using additional Firebase features)
**Solution:**
- This is optional but needed for some features like Phone Auth
- Get SHA-1:
  - Windows: `gradlew signingReport`
  - Mac/Linux: `./gradlew signingReport`
- Add SHA-1 in Firebase Console → Project Settings → Your Android App

## Step 9: Security Rules (For Future Firestore Use)

When you're ready to use Firestore, update security rules:

1. **Firebase Console → Firestore Database → Rules**
2. **Replace with the following complete security rules:**

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper function to check if user is authenticated
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Helper function to check if the userId matches the authenticated user
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    
    // Patients Collection: /patients/{userId}
    // Users can only read/write their own profile
    // Document ID is the user's Firebase Auth UID
    match /patients/{userId} {
      allow read: if isOwner(userId);
      allow create: if isAuthenticated() && request.auth.uid == userId && 
                       request.resource.data.id == userId;
      allow update: if isOwner(userId) && 
                       resource.data.id == userId;
      allow delete: if isOwner(userId);
    }
    
    // Medications Collection: /medications/{medicationId}
    // Medications are stored in a flat collection with userId field
    // Users can only access medications where userId matches their auth.uid
    match /medications/{medicationId} {
      // Allow read if the medication belongs to the authenticated user
      allow read: if isAuthenticated() && 
                     (resource.data.userId == request.auth.uid || 
                      request.resource.data.userId == request.auth.uid);
      
      // Allow create if userId field matches authenticated user
      allow create: if isAuthenticated() && 
                       request.resource.data.userId == request.auth.uid;
      
      // Allow update if the existing medication belongs to the user
      // AND the userId field is not being changed
      allow update: if isAuthenticated() && 
                       resource.data.userId == request.auth.uid &&
                       request.resource.data.userId == request.auth.uid;
      
      // Allow delete (soft delete via isActive flag) if medication belongs to user
      allow delete: if isAuthenticated() && 
                       resource.data.userId == request.auth.uid;
    }
    
    // Adherence Records Collection: /adherence_records/{recordId}
    // Adherence records are stored with userId and medicationId fields
    // Users can only access records where userId matches their auth.uid
    match /adherence_records/{recordId} {
      // Allow read if the record belongs to the authenticated user
      allow read: if isAuthenticated() && 
                     (resource.data.userId == request.auth.uid || 
                      request.resource.data.userId == request.auth.uid);
      
      // Allow create if userId field matches authenticated user
      allow create: if isAuthenticated() && 
                       request.resource.data.userId == request.auth.uid;
      
      // Allow update if the existing record belongs to the user
      // AND the userId field is not being changed
      allow update: if isAuthenticated() && 
                       resource.data.userId == request.auth.uid &&
                       request.resource.data.userId == request.auth.uid;
      
      // Allow delete if record belongs to user
      allow delete: if isAuthenticated() && 
                       resource.data.userId == request.auth.uid;
    }
    
    // Deny all other access by default
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

### Important Notes:

1. **Collection Structure**:
   - **Patients**: `/patients/{userId}` - Document ID is the user's Firebase Auth UID
   - **Medications**: `/medications/{medicationId}` - Flat collection with `userId` field inside documents
   - **Adherence Records**: `/adherence_records/{recordId}` - Flat collection with `userId` and `medicationId` fields

2. **Security Principles**:
   - All operations require authentication (`isAuthenticated()`)
   - Users can only access their own data (userId must match auth.uid)
   - On create/update, the userId field cannot be changed to prevent unauthorized access
   - Default deny rule at the end ensures no unexpected access

3. **Testing the Rules**:
   - Use Firebase Console → Firestore Database → Rules → Rules Playground
   - Test scenarios:
     - User reading their own patient profile ✅
     - User trying to read another user's patient profile ❌
     - User creating a medication with their userId ✅
     - User creating a medication with another user's userId ❌

4. **Click "Publish"** after updating the rules

## Step 10: Firebase Free Tier Limits Recap

Your app can use these free limits:

| Service | Free Tier Limit |
|---------|----------------|
| **Authentication** | Unlimited email/password sign-ins |
| **Firestore Reads** | 50,000 per day |
| **Firestore Writes** | 20,000 per day |
| **Firestore Storage** | 1 GB |
| **Cloud Messaging** | Unlimited notifications |
| **Hosting** | 1 GB, 10 GB transfer/month |

**For this project**: These limits are more than sufficient. You won't need to pay unless you exceed these (unlikely for MVP/student project).

## Additional Resources

- **Firebase Documentation**: https://firebase.google.com/docs
- **Firebase Console**: https://console.firebase.google.com/
- **Android Setup Guide**: https://firebase.google.com/docs/android/setup
- **Authentication Guide**: https://firebase.google.com/docs/auth/android/start

## Verification Checklist

Before proceeding, verify:

- [ ] Firebase project created
- [ ] Android app added with package name `com.medicationadherence.app`
- [ ] `google-services.json` downloaded and placed in `app/` folder
- [ ] Project synced successfully (no build errors)
- [ ] Email/Password authentication enabled in Firebase Console
- [ ] Successfully tested sign up flow
- [ ] User appears in Firebase Console → Authentication → Users

## Next Steps After Setup

1. **Test Authentication Flow**
   - Sign up with a test account
   - Sign out and sign in again
   - Verify session persistence

2. **Test Profile Editing**
   - Navigate to profile screen
   - Edit profile information
   - Verify changes are saved

3. **Optional: Add Firestore Sync**
   - Implement Firestore integration for cloud storage
   - Sync profile data across devices
   - Enable real-time updates

---

**Need Help?** Check the troubleshooting section or refer to Firebase documentation.

