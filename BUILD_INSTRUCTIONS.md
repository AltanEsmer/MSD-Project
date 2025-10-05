# 🚀 Medication Adherence App - Build Instructions

## Prerequisites

Before building the project, ensure you have:

1. **Android Studio** (latest stable version)
2. **JDK 8 or higher** installed
3. **Android SDK** with API level 24+ installed
4. **Git** (for version control)

## 📱 Project Setup

### 1. Open Project in Android Studio

1. Launch Android Studio
2. Click "Open an existing Android Studio project"
3. Navigate to your project folder: `C:\Users\esmer\Desktop\Projects\MSD Project\MSD-Project`
4. Select the root folder and click "OK"

### 2. Sync Project with Gradle Files

1. Android Studio should automatically prompt to sync Gradle files
2. If not, go to **File → Sync Project with Gradle Files**
3. Wait for the sync to complete

### 3. Configure SDK Path (if needed)

If you get SDK path errors:
1. Go to **File → Project Structure**
2. Under **SDK Location**, set your Android SDK path
3. Default path: `C:\Users\esmer\AppData\Local\Android\Sdk`

## 🔧 Build Configuration

### Gradle Files Created:
- ✅ `build.gradle.kts` (project level)
- ✅ `app/build.gradle.kts` (app level)
- ✅ `settings.gradle.kts`
- ✅ `gradle/wrapper/gradle-wrapper.properties`
- ✅ `gradlew` and `gradlew.bat` (wrapper scripts)

### Dependencies Included:
- **Jetpack Compose** for modern UI
- **Room Database** for local storage
- **Hilt** for dependency injection
- **WorkManager** for background tasks
- **Firebase** (placeholder configuration)
- **Kotlin Coroutines** for async operations

## 🏗️ Building the Project

### Option 1: Build from Android Studio
1. Click **Build → Make Project** (Ctrl+F9)
2. Or click **Build → Build Bundle(s) / APK(s) → Build APK(s)**

### Option 2: Build from Command Line
```bash
# Navigate to project directory
cd "C:\Users\esmer\Desktop\Projects\MSD Project\MSD-Project"

# Build debug APK
.\gradlew assembleDebug

# Build release APK
.\gradlew assembleRelease
```

## 🚨 Common Build Issues & Solutions

### Issue 1: "SDK location not found"
**Solution:**
1. Create `local.properties` file in project root
2. Add: `sdk.dir=C:\\Users\\esmer\\AppData\\Local\\Android\\Sdk`
3. Sync project again

### Issue 2: "Gradle wrapper not found"
**Solution:**
1. Android Studio should auto-download gradle wrapper
2. If not, go to **File → Settings → Build → Gradle**
3. Select "Use Gradle wrapper"

### Issue 3: "Kotlin version mismatch"
**Solution:**
1. Update Kotlin version in `build.gradle.kts`
2. Sync project with Gradle files

### Issue 4: "Firebase configuration missing"
**Solution:**
1. The project includes a placeholder `google-services.json`
2. For production, replace with your actual Firebase config
3. For development, the placeholder should work

## 📱 Running the App

### On Emulator:
1. Create a virtual device in Android Studio
2. Select API level 24+ (Android 7.0)
3. Click **Run → Run 'app'** (Shift+F10)

### On Physical Device:
1. Enable Developer Options on your Android device
2. Enable USB Debugging
3. Connect device via USB
4. Click **Run → Run 'app'**

## 🎯 What You'll See

When the app runs successfully, you'll see:

1. **Patient Dashboard** with:
   - "Today's Medications" title
   - Empty state message (no medications added yet)
   - "Add Medication" button
   - Accessible UI with large text and touch targets

2. **Core Features Working**:
   - Clean architecture navigation
   - Room database initialization
   - Hilt dependency injection
   - Material Design 3 theme
   - Accessibility features

## 🔍 Project Structure

```
app/
├── src/main/java/com/medicationadherence/app/
│   ├── data/                    # Data layer
│   │   ├── local/              # Room database
│   │   ├── repository/         # Repository implementations
│   │   └── work/               # WorkManager
│   ├── domain/                 # Domain layer
│   │   ├── model/              # Business models
│   │   └── repository/         # Repository interfaces
│   ├── presentation/           # Presentation layer
│   │   ├── patient/            # Patient app screens
│   │   ├── common/             # Shared components
│   │   └── theme/              # Material Design theme
│   └── di/                     # Dependency injection
├── src/main/res/               # Resources
└── google-services.json        # Firebase config
```

## 🚀 Next Steps After Successful Build

1. **Add Sample Data**: Create some test medications to see the UI
2. **Test Accessibility**: Use TalkBack to verify screen reader support
3. **Add Firebase**: Replace placeholder config with real Firebase project
4. **Implement Add Medication**: Create the medication entry form
5. **Add Family App**: Implement caregiver dashboard

## 📞 Troubleshooting

If you encounter issues:

1. **Clean Project**: Build → Clean Project
2. **Rebuild**: Build → Rebuild Project
3. **Invalidate Caches**: File → Invalidate Caches and Restart
4. **Check Logs**: View → Tool Windows → Logcat

## ✅ Success Indicators

Your build is successful when:
- ✅ Gradle sync completes without errors
- ✅ App builds and installs on device/emulator
- ✅ Patient dashboard loads with empty state
- ✅ No runtime crashes or errors
- ✅ UI displays with proper Material Design theme

The foundation is now ready for implementing the full medication adherence features!
