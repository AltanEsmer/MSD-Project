# Medication Adherence App - Project Structure & Components Guide

**Purpose:** Reference guide for all components, their purposes, and typical directory locations in a Clean Architecture Android app.

**Last Updated:** January 2025

---

## 📁 PROJECT OVERVIEW

This app follows **Clean Architecture** principles with three main layers:
- **Data Layer** (`data/`) - Data sources, repositories, database
- **Domain Layer** (`domain/`) - Business models and use cases
- **Presentation Layer** (`presentation/`) - UI, ViewModels, composables

---

## 1. DATA LAYER COMPONENTS

### 📂 Location: `app/src/main/java/com/medicationadherence/app/data/`

#### **Entities** (`data/local/entity/`)
**Purpose:** Room database table definitions

| File | Purpose |
|------|---------|
| `Entities.kt` | Room entities (PatientEntity, MedicationEntity, MedicationScheduleEntity, AdherenceRecordEntity, MedicationReminderEntity) |

**Location:** `data/local/entity/Entities.kt`

**Key Annotations:**
- `@Entity(tableName = "...")` - Table definition
- `@PrimaryKey` - Primary key
- `@TypeConverters` - Custom converters for complex types

---

#### **Database** (`data/local/database/`)
**Purpose:** Room database configuration

| File | Purpose |
|------|---------|
| `MedicationDatabase.kt` | Room database singleton with tables and DAOs |

**Location:** `data/local/database/MedicationDatabase.kt`

**Key Features:**
- Singleton instance
- Database version management
- DAO declarations

---

#### **DAOs (Data Access Objects)** (`data/local/dao/`)
**Purpose:** SQL queries and database operations

| File | Purpose |
|------|---------|
| `Daos.kt` | Interface definitions (MedicationDao, AdherenceDao, ReminderDao) |

**Location:** `data/local/dao/Daos.kt`

**Key Methods:**
- `@Query` - Custom SQL queries
- `@Insert` - Insert operations
- `@Update` - Update operations
- `@Delete` - Delete operations
- `Flow<T>` - Reactive observable returns

---

#### **Type Converters** (`data/local/converter/`)
**Purpose:** Convert complex types for Room storage

| File | Purpose |
|------|---------|
| `TypeConverters.kt` | DateTime converters, List converters |

**Location:** `data/local/converter/TypeConverters.kt`

**Common Converters:**
- `LocalDateTime` ↔ `String` (ISO format)
- `List<String>` ↔ `String` (JSON format)
- `AdherenceStatus` ↔ `String` (enum)

---

#### **Mappers** (`data/local/mapper/`)
**Purpose:** Convert between Entity and Domain models

| File | Purpose |
|------|---------|
| `DataMappers.kt` | Mapping functions (entityToModel, modelToEntity) |

**Location:** `data/local/mapper/DataMappers.kt`

**Pattern:**
```kotlin
fun MedicationEntity.toModel(): Medication
fun Medication.toEntity(): MedicationEntity
```

---

#### **Data Source** (`data/local/`)
**Purpose:** Abstraction for database operations

| File | Purpose |
|------|---------|
| `LocalMedicationDataSource.kt` | Wrapper around DAOs for dependency inversion |

**Location:** `data/local/LocalMedicationDataSource.kt`

**Implementation:**
- Implements data source interface
- Uses DAOs internally
- Handles error cases

---

#### **Repository Implementation** (`data/repository/`)
**Purpose:** Concrete implementation of domain repository interface

| File | Purpose |
|------|---------|
| `MedicationRepositoryImpl.kt` | Implements MedicationRepository interface |

**Location:** `data/repository/MedicationRepositoryImpl.kt`

**Responsibilities:**
- Combines multiple data sources
- Caching logic
- Network sync coordination
- Error handling and retry logic

---

#### **Sample Data** (`data/local/`)
**Purpose:** Initial test data seeding

| File | Purpose |
|------|---------|
| `SampleDataSeeder.kt` | Populates database with initial medications |

**Location:** `data/local/SampleDataSeeder.kt`

**Use Case:**
- First app launch
- Testing/demo purposes
- Development data

---

#### **WorkManager** (`data/work/`)
**Purpose:** Background task management

| File | Purpose |
|------|---------|
| `MedicationReminderWorker.kt` | Worker for medication reminder notifications |
| `MedicationReminderManager.kt` | Manages worker scheduling and cancellation |

**Location:** `data/work/`

**Features:**
- Periodic work requests
- One-time work requests
- Constraint-based scheduling
- Notification triggering

---

## 2. DOMAIN LAYER COMPONENTS

### 📂 Location: `app/src/main/java/com/medicationadherence/app/domain/`

#### **Domain Models** (`domain/model/`)
**Purpose:** Pure business logic models

| File | Purpose |
|------|---------|
| `Models.kt` | Patient, Medication, MedicationSchedule, AdherenceStatus, AdherenceRecord |

**Location:** `domain/model/Models.kt`

**Key Characteristics:**
- No Android dependencies
- Pure Kotlin data classes
- Business logic only
- Easily testable

**Example Models:**
```kotlin
data class Medication(
    val id: String,
    val name: String,
    val dosage: String,
    val frequency: List<String>,
    val instructions: String,
    val isActive: Boolean
)

enum class AdherenceStatus {
    TAKEN, MISSED, SKIPPED, PENDING
}
```

---

#### **Repository Interfaces** (`domain/repository/`)
**Purpose:** Define contracts for data operations

| File | Purpose |
|------|---------|
| `RepositoryInterfaces.kt` | Interface definitions (MedicationRepository) |

**Location:** `domain/repository/RepositoryInterfaces.kt`

**Key Interface:**
```kotlin
interface MedicationRepository {
    fun getTodayMedications(): Flow<List<MedicationWithSchedule>>
    suspend fun logDose(medicationId: String, status: AdherenceStatus)
    suspend fun addMedication(medication: Medication)
}
```

**Benefits:**
- Abstraction from data source
- Easy to mock in tests
- Testability

---

## 3. PRESENTATION LAYER COMPONENTS

### 📂 Location: `app/src/main/java/com/medicationadherence/app/presentation/`

#### **ViewModels** (`presentation/patient/viewmodel/`)
**Purpose:** Business logic for UI, state management

| File | Purpose |
|------|---------|
| `MedicationViewModel.kt` | Manages medication state, handles user actions |

**Location:** `presentation/patient/viewmodel/MedicationViewModel.kt`

**Key Features:**
- Extends `ViewModel`
- Uses Kotlin Coroutines and Flow
- Exposes `StateFlow` for UI updates
- No Android UI dependencies
- Lifecycle-aware

**Common ViewModel Pattern:**
```kotlin
@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val repository: MedicationRepository
) : ViewModel() {
    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()
    
    fun loadMedications() { /* ... */ }
    fun addMedication(medication: Medication) { /* ... */ }
}
```

---

#### **Screens** (`presentation/patient/screen/`, `presentation/family/`)
**Purpose:** Full-screen composables with navigation

| Screen Type | Files | Purpose |
|------------|-------|---------|
| **Patient Screens** | `ModernPatientDashboardScreen.kt`<br>`ModernAddMedicationScreen.kt`<br>`MedicationDetailsScreen.kt`<br>`AdherenceHistoryScreen.kt`<br>`PatientProfileScreen.kt`<br>`WelcomeScreen.kt`<br>`ProfileSetupScreen.kt` | Patient-facing screens |
| **Family Screens** | `FamilyDashboardScreen.kt`<br>`FamilyAlertsScreen.kt`<br>`FamilyMessagesScreen.kt`<br>`FamilyReportsScreen.kt`<br>`FamilyWelcomeScreen.kt` | Caregiver monitoring screens |

**Location:** 
- Patient: `presentation/patient/screen/`
- Family: `presentation/family/`

**Screen Structure:**
```kotlin
@Composable
fun MyScreen(
    viewModel: MyViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold { padding ->
        // Screen content
    }
}
```

---

#### **UI Components** (`presentation/common/components/`)
**Purpose:** Reusable UI components

| File | Purpose |
|------|---------|
| `AccessibleComponents.kt` | Accessibility-focused components |
| `ModernComponents.kt` | Modern Material 3 components |

**Location:** `presentation/common/components/`

**Common Components:**
- `AccessibleButton` - 44dp minimum touch target
- `LargeText` - Scalable text (18sp+)
- `MedicationCard` - Medication display card
- `ProgressIndicator` - Adherence visualization
- `EmptyState` - No data placeholder
- `StatCard` - Dashboard statistics
- `BottomNavBar` - Bottom navigation

---

#### **Theme** (`presentation/theme/`)
**Purpose:** Material Design theme configuration

| File | Purpose |
|------|---------|
| `Color.kt` | Color palette definitions |
| `Type.kt` | Typography styles |
| `Theme.kt` | Material Design 3 theme |

**Location:** `presentation/theme/`

**Color System:**
```kotlin
val Blue600 = Color(0xFF2563EB)
val Green600 = Color(0xFF16A34A)
val Orange600 = Color(0xFFEA580C)
val Gray50 = Color(0xFFF9FAFB)
```

**Typographic Scale:**
- `displayLarge` - 57sp
- `headlineMedium` - 28sp
- `titleMedium` - 16sp
- `bodyMedium` - 14sp
- `labelMedium` - 12sp

---

#### **MainActivity** (`presentation/`)
**Purpose:** Android app entry point

| File | Purpose |
|------|---------|
| `MainActivity.kt` | App entry point, sets content, handles lifecycle |

**Location:** `presentation/MainActivity.kt`

**Responsibilities:**
- Initialize app content
- Handle permissions
- Seed sample data if needed
- Navigation setup
- Lifecycle management

---

#### **Application Class** (root)
**Purpose:** App-level initialization

| File | Purpose |
|------|---------|
| `MedicationApp.kt` | Hilt application class, global setup |

**Location:** `app/src/main/java/com/medicationadherence/app/MedicationApp.kt`

**Key Setup:**
```kotlin
@HiltAndroidApp
class MedicationApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Global initialization
    }
}
```

---

## 4. DEPENDENCY INJECTION COMPONENTS

### 📂 Location: `app/src/main/java/com/medicationadherence/app/di/`

#### **Hilt Modules** (`di/`)
**Purpose:** Dependency injection configuration

| File | Purpose |
|------|---------|
| `AppModule.kt` | Provides dependencies (Database, Repository, DataSource) |

**Location:** `di/AppModule.kt`

**Common Annotations:**
- `@Module` - Marks as DI module
- `@InstallIn(SingletonComponent::class)` - Scope
- `@Provides` - Provides dependency
- `@Singleton` - Single instance
- `@Inject` - Request dependency
- `@HiltViewModel` - ViewModel injection

**Module Pattern:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MedicationDatabase {
        return Room.databaseBuilder(...).build()
    }
}
```

---

## 5. ANDROID SPECIFIC COMPONENTS

### 📂 Location: Multiple locations

#### **AndroidManifest.xml** (`app/src/main/`)
**Purpose:** App configuration, permissions, activities

**Location:** `app/src/main/AndroidManifest.xml`

**Key Declarations:**
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.INTERNET" />

<application>
    <activity android:name=".MainActivity" />
</application>
```

---

#### **Gradle Build Files**
**Purpose:** Dependencies, build configuration

| File | Purpose |
|------|---------|
| `build.gradle.kts` (project) | Project-level dependencies |
| `build.gradle.kts` (app) | App-level dependencies, build config |

**Location:** 
- Project: `build.gradle.kts`
- App: `app/build.gradle.kts`

**Key Dependencies:**
```kotlin
// Room
implementation("androidx.room:room-runtime:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")

// Compose
implementation(platform("androidx.compose:compose-bom:2023.10.01"))
implementation("androidx.compose.material3:material3")

// Hilt
implementation("com.google.dagger:hilt-android:2.48")
kapt("com.google.dagger:hilt-compiler:2.48")

// WorkManager
implementation("androidx.work:work-runtime-ktx:2.9.0")
```

---

#### **Resources** (`app/src/main/res/`)
**Purpose:** Assets, strings, themes

**Location:** `app/src/main/res/`

**Resource Files:**
- `values/strings.xml` - App strings
- `values/themes.xml` - Android XML themes
- `drawable/` - Vector graphics, icons
- `mipmap-*/` - App icons

---

#### **Navigation**
**Purpose:** Screen navigation (if using Jetpack Navigation)

**Location:** Various (navigation graph files)

**If Using Navigation Compose:**
```kotlin
NavHost(
    navController = navController,
    startDestination = "dashboard"
) {
    composable("dashboard") { DashboardScreen() }
    composable("add_medication") { AddMedicationScreen() }
}
```

---

## 6. JETPACK COMPOSE ELEMENTS

### Composables Overview

#### **Basic Composables** (Material 3)
| Component | Purpose |
|-----------|---------|
| `Column` | Vertical layout |
| `Row` | Horizontal layout |
| `Box` | Stack layout |
| `Scaffold` | App screen structure |
| `TopAppBar` | App bar with title |
| `Card` | Elevated container |
| `Button` | Primary action button |
| `OutlinedButton` | Secondary action button |
| `TextField` | Text input |
| `LazyColumn` | Efficient vertical list |

#### **State Management**
| Element | Purpose |
|---------|---------|
| `remember` | Cache value in composition |
| `mutableStateOf` | Create mutable state |
| `collectAsState` | Observe Flow in Composable |
| `LaunchedEffect` | Side effects in composables |
| `DisposableEffect` | Cleanup on composition end |

#### **Material 3 Components Used**
```kotlin
// Text
Text("Hello", style = MaterialTheme.typography.bodyLarge)

// Buttons
Button(onClick = {}) { Text("Click") }
OutlinedButton(onClick = {}) { Text("Cancel") }

// Cards
Card(modifier = Modifier.fillMaxWidth()) {
    Column(Modifier.padding(16.dp)) { /* content */ }
}

// Text Fields
OutlinedTextField(
    value = text,
    onValueChange = { text = it },
    label = { Text("Label") }
)

// Navigation
TopAppBar(
    title = { Text("Screen Title") },
    navigationIcon = { /* back button */ }
)
```

---

## 7. KEY ARCHITECTURE PATTERNS

### **Clean Architecture**
**Layers:**
```
Presentation → Domain ← Data
     ↓           ↓        ↓
ViewModels    Models  Database
Screens     Interfaces  Repos
```

### **MVVM Pattern**
**Flow:**
```
UI (Composable) ↔ ViewModel ↔ Repository ↔ DataSource
```

### **Repository Pattern**
**Abstraction:**
```
Domain (Interface) ← Data (Implementation)
```

### **State Management**
**Flow:**
```
Repository Flow → ViewModel StateFlow → UI State
```

---

## 8. FILE NAMING CONVENTIONS

### **Naming Standards**

| Type | Convention | Example |
|------|------------|---------|
| **Classes** | PascalCase | `MedicationViewModel.kt` |
| **Interfaces** | PascalCase + descriptive | `RepositoryInterfaces.kt` |
| **Entities** | PascalCase + "Entity" suffix | `PatientEntity` |
| **DAOs** | PascalCase + "Dao" suffix | `MedicationDao` |
| **Screens** | PascalCase + "Screen" suffix | `PatientDashboardScreen.kt` |
| **ViewModels** | PascalCase + "ViewModel" suffix | `MedicationViewModel.kt` |
| **Composables** | PascalCase, @Composable | `fun MedicationCard()` |
| **Files** | PascalCase.kt | `AccessibleComponents.kt` |

### **Directory Organization**

```
app/src/main/java/com/medicationadherence/app/
├── data/                    # Data layer
│   ├── local/              # Local data sources
│   │   ├── entity/         # Room entities
│   │   ├── dao/            # Data Access Objects
│   │   ├── database/       # Database configuration
│   │   ├── converter/      # Type converters
│   │   ├── mapper/         # Entity ↔ Model mappers
│   │   └── ...             # Data sources
│   ├── repository/         # Repository implementations
│   └── work/               # Background work
├── domain/                 # Domain layer
│   ├── model/              # Business models
│   └── repository/         # Repository interfaces
├── presentation/           # Presentation layer
│   ├── common/             # Shared UI
│   │   └── components/     # Reusable components
│   ├── patient/            # Patient app features
│   │   ├── screen/         # Patient screens
│   │   └── viewmodel/      # Patient ViewModels
│   ├── family/             # Family app features
│   │   └── ...             # Family screens
│   ├── theme/              # Material theme
│   └── MainActivity.kt     # Entry point
├── di/                     # Dependency injection
│   └── AppModule.kt        # Hilt modules
└── MedicationApp.kt        # Application class
```

---

## 9. TYPICAL COMPONENT RELATIONSHIPS

### **Complete Data Flow Example**

```
┌─────────────────────────────────────────────────────────┐
│                    COMPOSABLE SCREEN                    │
│  ModernPatientDashboardScreen                           │
│  - Uses: ViewModel                                      │
│  - Displays: State from ViewModel                       │
└────────────────────┬────────────────────────────────────┘
                     │ calls
                     ▼
┌─────────────────────────────────────────────────────────┐
│                       VIEWMODEL                         │
│  MedicationViewModel                                     │
│  - Uses: MedicationRepository                           │
│  - Exposes: StateFlow<List<MedicationWithSchedule>>    │
└────────────────────┬────────────────────────────────────┘
                     │ calls
                     ▼
┌─────────────────────────────────────────────────────────┐
│              DOMAIN REPOSITORY INTERFACE                 │
│  MedicationRepository                                    │
│  - Interface only, no implementation                    │
└────────────────────┬────────────────────────────────────┘
                     │ implemented by
                     ▼
┌─────────────────────────────────────────────────────────┐
│               REPOSITORY IMPLEMENTATION                  │
│  MedicationRepositoryImpl                                │
│  - Uses: LocalMedicationDataSource                      │
│  - Uses: RemoteDataSource (future)                     │
└────────────────────┬────────────────────────────────────┘
                     │ uses
                     ▼
┌─────────────────────────────────────────────────────────┐
│                      DATA SOURCE                         │
│  LocalMedicationDataSource                               │
│  - Uses: MedicationDao                                  │
└────────────────────┬────────────────────────────────────┘
                     │ uses
                     ▼
┌─────────────────────────────────────────────────────────┐
│                         DAO                             │
│  MedicationDao                                           │
│  - Annotated with @Query, @Insert, etc.                 │
└────────────────────┬────────────────────────────────────┘
                     │ uses
                     ▼
┌─────────────────────────────────────────────────────────┐
│                    ROOM DATABASE                        │
│  MedicationDatabase                                       │
│  - Contains tables                                        │
│  - Provides Dao instances                                 │
└─────────────────────────────────────────────────────────┘
```

### **Dependency Injection Flow**

```
┌─────────────────────────────────────────────────────────┐
│                    @HiltAndroidApp                       │
│  MedicationApp                                           │
│  - Generated by Hilt                                     │
│  - Provides Application context                         │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                    @HiltViewModel                        │
│  MedicationViewModel                                     │
│  @Inject constructor(repository: MedicationRepository)  │
└────────────────────┬────────────────────────────────────┘
                     │ injected dependency
                     ▼
┌─────────────────────────────────────────────────────────┐
│                    @Provides @Singleton                  │
│  AppModule                                               │
│  - Provides MedicationRepositoryImpl                     │
│  - Provides MedicationDatabase                           │
│  - Provides all dependencies                            │
└─────────────────────────────────────────────────────────┘
```

---

## 10. COMMON COMPONENT INTERACTIONS

### **Screen → ViewModel**
```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    
    // User action triggers ViewModel method
    Button(onClick = { viewModel.handleAction() }) { 
        Text("Do Something") 
    }
}
```

### **ViewModel → Repository**
```kotlin
class MyViewModel @Inject constructor(
    private val repository: MedicationRepository
) : ViewModel() {
    
    fun loadData() {
        viewModelScope.launch {
            repository.getData()
                .collect { data -> 
                    _state.value = data 
                }
        }
    }
}
```

### **Repository → DataSource**
```kotlin
class MedicationRepositoryImpl @Inject constructor(
    private val dataSource: MedicationDataSource
) : MedicationRepository {
    
    override suspend fun getMedications(): List<Medication> {
        return dataSource.getMedications()
            .map { it.toModel() }
    }
}
```

---

## 📚 ADDITIONAL RESOURCES

### **Android Documentation**
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
- [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [StateFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Material Design 3](https://m3.material.io/)

### **Clean Architecture**
- [Clean Architecture by Uncle Bob](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Android Clean Architecture](https://developer.android.com/topic/architecture)

---

## 🎯 QUICK REFERENCE

### **Where to Find...**

| What | Where |
|------|-------|
| Database tables | `data/local/entity/` |
| Queries | `data/local/dao/` |
| Business models | `domain/model/` |
| UI screens | `presentation/*/screen/` |
| Reusable UI | `presentation/common/components/` |
| State management | `presentation/*/viewmodel/` |
| Dependency injection | `di/` |
| Theme configuration | `presentation/theme/` |
| Background tasks | `data/work/` |

---

**End of Guide** 🚀

*For questions or contributions, refer to the main project documentation.*
