# Medication Adherence App

A dual-app Android medication adherence system built with Kotlin, Jetpack Compose, and Clean Architecture. Designed specifically for elderly patients with comprehensive accessibility features, plus a family caregiver monitoring app.

![Status](https://img.shields.io/badge/status-phase%201%20complete-success)
![Platform](https://img.shields.io/badge/platform-Android-blue)
![Kotlin](https://img.shields.io/badge/kotlin-1.9.20-purple)
![Architecture](https://img.shields.io/badge/architecture-Clean--Architecture-green)

---

## 📱 Project Overview

This Medication Adherence app solves one of healthcare's most pressing issues: medication non-adherence. It consists of two interconnected applications:

- **Patient App**: Simple, accessible medication management for elderly users
- **Family App**: Remote monitoring and alert system for caregivers

**Target Users:** Elderly patients (65+) managing multiple medications and their family caregivers  
**Core Value:** Reliable medication tracking with family peace of mind

---

## 🏗️ Architecture

The app follows **Clean Architecture** with three distinct layers:

```
app/
├── data/                      # Data Layer
│   ├── local/                 # Room database & local storage
│   │   ├── entity/           # Room entities (DB tables)
│   │   ├── dao/              # Data Access Objects (queries)
│   │   ├── database/         # Room database configuration
│   │   ├── converter/        # Type converters (DateTime, List)
│   │   ├── mapper/           # Entity ↔ Domain model mappers
│   │   └── LocalMedicationDataSource.kt
│   ├── repository/           # Repository implementations
│   └── work/                 # WorkManager (background tasks)
│
├── domain/                    # Domain Layer
│   ├── model/                # Pure business models
│   └── repository/           # Repository interfaces
│
└── presentation/              # Presentation Layer
    ├── patient/              # Patient app
    │   ├── screen/           # Patient screens
    │   └── viewmodel/        # ViewModels
    ├── family/               # Family app
    │   └── *.kt              # Family screens
    ├── common/               # Shared UI
    │   └── components/       # Reusable components
    └── theme/                # Material Design 3 theme
```

**Architecture Patterns:**
- **Clean Architecture** - Separation of concerns (Data/Domain/Presentation)
- **MVVM** - Model-View-ViewModel for UI
- **Repository Pattern** - Data abstraction
- **Dependency Injection** - Hilt for DI

---

## ✨ Current Features

### Patient App (Phase 1 Complete ✅)

#### Core Features
- ✅ **Modern Patient Dashboard** - Personalized greeting, daily stats, medication list
- ✅ **Medication Management** - Add, view, and track medications
- ✅ **Today's Progress Tracking** - Visual progress indicators (streak, doses taken)
- ✅ **One-Tap Medication Logging** - Simple TAKE button for dose confirmation
- ✅ **Medication Details** - Full medication information with adherence history
- ✅ **Sample Data Seeding** - 3 sample medications auto-loaded on first launch

#### Technical Implementation
- ✅ **Room Database** - Local persistence with proper entities
- ✅ **MVVM Architecture** - Clean separation with ViewModels
- ✅ **Kotlin Coroutines & Flow** - Reactive data streams
- ✅ **WorkManager** - Reliable background reminder system
- ✅ **Hilt Dependency Injection** - Modular, testable code

#### Accessibility Features
- ✅ **Large Touch Targets** - Minimum 44dp for easy tapping
- ✅ **Scalable Text** - 18sp+ body text, 22sp+ headings
- ✅ **High Contrast Colors** - Better visibility for low vision
- ✅ **TalkBack Support** - Full screen reader compatibility
- ✅ **Content Descriptions** - Semantic labels for assistive tech
- ✅ **Visual Status Indicators** - Color-coded medication states

---

### Family App (Phase 1 Complete ✅)

#### Caregiver Features
- ✅ **Family Dashboard** - Real-time patient status overview
- ✅ **Patient Monitoring** - Track multiple patients simultaneously
- ✅ **Alert System** - Missed dose notifications (Critical/Warning/Info)
- ✅ **Activity Timeline** - Chronological medication event log
- ✅ **Progress Tracking** - Today's adherence with visual indicators
- ✅ **Quick Actions** - Message, call, or check patient status
- ✅ **Reports Screen** - Adherence analytics and trends

#### Monitoring Features
- ✅ **Real-Time Updates** - See medication status changes instantly
- ✅ **Alert Prioritization** - Critical alerts for missed important doses
- ✅ **Multi-Patient Support** - Monitor multiple family members
- ✅ **Adherence Analytics** - Week/month/quarter views with trends

---

## 🎯 User Experience

### Patient Journey
1. **Welcome Screen** - Onboarding for new users
2. **Dashboard** - Today's medications with large, accessible cards
3. **Add Medication** - Simple form with validation
4. **Take Medication** - One-tap confirmation
5. **Progress Tracking** - Visual feedback on adherence
6. **History View** - Detailed adherence records over time

### Caregiver Journey
1. **Family Dashboard** - Overview of all monitored patients
2. **Alerts Screen** - Filter and respond to medication issues
3. **Patient Status** - Detailed view of patient's medication schedule
4. **Messaging** - Quick communication with patient
5. **Reports** - Long-term adherence analytics and insights

---

## 🛠️ Technology Stack

### Core Android
```kotlin
// Android Core
androidx.core:core-ktx:1.12.0
androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0
androidx.activity:activity-compose:1.8.2
```

### Jetpack Compose
```kotlin
// UI Framework
androidx.compose:compose-bom:2023.10.01
androidx.compose.material3:material3
androidx.compose.material:material-icons-extended
```

### Room Database
```kotlin
// Local Persistence
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1
kapt("androidx.room:room-compiler:2.6.1")
```

### Dependency Injection & Background Work
```kotlin
// Hilt DI
com.google.dagger:hilt-android:2.48
kapt("com.google.dagger:hilt-compiler:2.48")

// WorkManager
androidx.work:work-runtime-ktx:2.9.0
androidx.hilt:hilt-work:1.1.0
```

### Firebase (Ready for Integration)
```kotlin
// Cloud Services (Future)
com.google.firebase:firebase-firestore-ktx
com.google.firebase:firebase-auth-ktx
com.google.firebase:firebase-messaging-ktx
```

---

## 📁 Key Components

### Data Layer
| Component | Purpose | Location |
|-----------|---------|----------|
| **Entities** | Room database tables | `data/local/entity/Entities.kt` |
| **DAOs** | Database queries | `data/local/dao/Daos.kt` |
| **Database** | Room configuration | `data/local/database/MedicationDatabase.kt` |
| **Mappers** | Entity ↔ Model conversion | `data/local/mapper/DataMappers.kt` |
| **Repository** | Data source abstraction | `data/repository/MedicationRepositoryImpl.kt` |
| **WorkManager** | Background tasks | `data/work/MedicationReminderWorker.kt` |

### Domain Layer
| Component | Purpose | Location |
|-----------|---------|----------|
| **Models** | Business logic models | `domain/model/Models.kt` |
| **Repository Interfaces** | Data contracts | `domain/repository/RepositoryInterfaces.kt` |

### Presentation Layer
| Component | Purpose | Location |
|-----------|---------|----------|
| **Screens** | Full-screen composables | `presentation/patient/screen/`, `presentation/family/` |
| **ViewModels** | State management | `presentation/patient/viewmodel/` |
| **UI Components** | Reusable composables | `presentation/common/components/` |
| **Theme** | Material Design 3 | `presentation/theme/` |

---

## 📖 Documentation

This repository includes comprehensive documentation:

- **[CURRENT_APP_STATUS.md](CURRENT_APP_STATUS.md)** - Detailed feature status and implementation
- **[DEMO_PRESENTATION_GUIDE.md](DEMO_PRESENTATION_GUIDE.md)** - Complete demo script for presentations
- **[PROJECT_STRUCTURE_GUIDE.md](PROJECT_STRUCTURE_GUIDE.md)** - Component reference and architecture patterns
- **[BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md)** - Setup and build guide

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or higher
- Android SDK 24+ (target SDK 34)

### Building the Project

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/medication-adherence-app.git
cd medication-adherence-app
```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the project directory

3. **Sync Gradle**
   - Android Studio will automatically sync Gradle
   - Wait for dependencies to download

4. **Run the app**
   - Connect an Android device or start an emulator
   - Click "Run" or press `Shift+F10`

### First Run
- The app will seed 3 sample medications on first launch
- Patient Dashboard shows today's medications
- Family App provides mock data for demonstration

---

## 🧪 Testing

### Unit Tests
- `MedicationViewModelTest` - ViewModel logic
- `RepositoryTest` - Data layer
- `AdherenceTrackingTest` - Adherence calculations

### UI Tests
- Patient flow (add → reminder → take)
- Family monitoring flow
- Alert system verification

Run tests:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

---

## 🎨 Design System

### Material Design 3
- **Color Palette**: Custom blue, green, orange, gray scales
- **Typography**: Scalable text system (18sp+ body, 22sp+ headings)
- **Components**: Material 3 Cards, Buttons, TextFields
- **Accessibility**: WCAG 2.1 AA compliant

### Color Scheme
```kotlin
// Primary
Blue600 (0xFF2563EB)   // App brand
Green600 (0xFF16A34A)   // Success states
Orange600 (0xFFEA580C)  // Warnings
Red600 (0xFFDC2626)     // Errors

// Neutral
Gray50 (0xFFF9FAFB)     // Background
Gray600 (0xFF6B7280)    // Secondary text
Gray900 (0xFF111827)    // Primary text
```

---

## 📊 Project Status

### ✅ Phase 1: Complete
- [x] Patient App MVP
- [x] Modern UI with Material Design 3
- [x] Room Database implementation
- [x] WorkManager reminders
- [x] Accessibility features
- [x] Sample data seeding
- [x] Family App MVP
- [x] Alert system
- [x] Reports and analytics

### 🚧 Phase 2: In Progress
- [ ] Firebase Authentication
- [ ] Firestore data sync
- [ ] Real-time push notifications
- [ ] Edit medication functionality
- [ ] Patient settings screen

### 📋 Phase 3: Planned
- [ ] Multi-language support
- [ ] Medication interaction warnings
- [ ] Doctor/pharmacy integration
- [ ] Health data export
- [ ] Advanced analytics
- [ ] Integration with health apps

---

## 🎯 MVP Success Metrics

### Patient App
- ✅ Simple, accessible interface
- ✅ Reliable reminder system
- ✅ Progress tracking
- ✅ One-tap medication logging
- ✅ Large touch targets and readable text

### Family App
- ✅ Real-time status monitoring
- ✅ Alert system for missed doses
- ✅ Multi-patient support
- ✅ Comprehensive adherence analytics
- ✅ Quick communication features

### Technical
- ✅ Clean Architecture implementation
- ✅ MVVM pattern with ViewModels
- ✅ Room database for offline support
- ✅ Hilt dependency injection
- ✅ WorkManager for background tasks
- ✅ Accessibility compliance (WCAG 2.1 AA)

---

## 👥 Target Audience

### Primary Users
- **Elderly Patients (65+)** - Managing 3+ daily medications
- **Family Caregivers** - Monitoring loved ones remotely
- **Healthcare Workers** - Providing medication adherence support

### User Needs Addressed
- Simple medication management for elderly users
- Family peace of mind through remote monitoring
- Reduced medication errors and missed doses
- Better health outcomes through improved adherence

---

## 🤝 Contributing

This is a course project (MSD Project). Contributions, issues, and pull requests are welcome!

### Development Setup
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Standards
- Kotlin style guide compliance
- Clean Architecture principles
- Accessibility-first design
- Material Design 3 guidelines
- Comprehensive test coverage

---

## 📝 License

This project is created for educational purposes as part of the MSD (Mobile Software Development) course.

---

## 📞 Contact

For questions or support, please refer to the project documentation or contact the development team.

---

## 🙏 Acknowledgments

- **Material Design 3** - Google
- **Jetpack Compose** - Google
- **Room Database** - Android
- **Hilt** - Google Dagger
- **Clean Architecture** - Robert C. Martin (Uncle Bob)

---

**Built with ❤️ for elderly patients and their families**