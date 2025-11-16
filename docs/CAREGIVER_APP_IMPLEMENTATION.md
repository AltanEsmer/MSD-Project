# Caregiver App Implementation Documentation

## Overview
This document describes the complete implementation of the Home (Family Dashboard) and Alerts pages for the caregiver app, including data sources, ViewModels, and UI components.

## Architecture

### Data Layer

#### 1. Domain Models

**Alert.kt** (`com.medicationadherence.app.domain.model.Alert`)
- `Alert`: Main alert model with fields for type, patient info, message, timestamps, and resolution status
- `AlertType`: Enum for CRITICAL, WARNING, INFO
- `ActivityItem`: Model for timeline activity display
- `ActivityType`: Enum for TOOK_MEDICATION, MISSED_DOSE, SKIPPED_MEDICATION

#### 2. Data Sources

**FirestoreAlertDataSource.kt**
- `getActiveAlerts()`: Fetches all non-dismissed alerts from Firestore
- `saveAlert(alert)`: Persists alert to Firestore
- `dismissAlert(alertId)`: Marks alert as dismissed
- `resolveAlert(alertId)`: Marks alert as resolved
- `deleteOldAlerts(daysOld)`: Cleans up old dismissed alerts

**FirestoreAdherenceDataSource.kt** (Enhanced)
- `getRecentAdherenceRecordsForAllPatients(days)`: Gets adherence records for activity timeline
- `getAllAdherenceRecordsForUser(userId)`: Gets all adherence records for a specific user
- `AdherenceRecordWithPatient`: Data class containing adherence record with patient info

**FirestorePatientDataSource.kt** (Existing)
- `getAllPatientsWithSharingEnabled()`: Gets patients who enabled data sharing
- `getPatientAdherenceStats(patientId)`: Calculates adherence statistics

### Presentation Layer

#### 1. ViewModels

**FamilyDashboardViewModel.kt**
- **State Management**:
  - `patients`: StateFlow of patients with statistics
  - `recentActivity`: StateFlow of activity items
  - `isLoading`: Loading state
  - `errorMessage`: Error message state

- **Key Functions**:
  - `loadPatients()`: Loads patients with adherence stats
  - `loadRecentActivity()`: Generates activity timeline from adherence records
  - `getAverageAdherence()`: Calculates average adherence across all patients
  - `getActiveAlertsCount()`: Counts patients needing attention
  - `formatRelativeTime(timestamp)`: Formats timestamps to relative time strings
  - `refresh()`: Refreshes all data

**FamilyAlertsViewModel.kt**
- **State Management**:
  - `alerts`: StateFlow of all alerts
  - `isLoading`: Loading state
  - `errorMessage`: Error message state

- **Key Functions**:
  - `loadAlerts()`: Loads and generates alerts
  - `generateAlertsFromAdherenceData()`: Creates alerts based on adherence patterns
  - `dismissAlert(alertId)`: Dismisses an alert
  - `resolveAlert(alertId)`: Resolves an alert
  - `getFilteredAlerts(filter)`: Filters alerts by type
  - `getActiveAlertsCount()`: Counts active alerts

- **Alert Generation Rules**:
  - **CRITICAL**: Missed dose today OR 2+ consecutive missed doses
  - **WARNING**: Adherence rate < 80% with sufficient data
  - **INFO**: (Future: refill reminders, schedule changes)

#### 2. UI Screens

**FamilyDashboardScreen.kt**
- **Components**:
  - Header with patient count and quick stats
  - Quick stats cards (Active Alerts, Avg. Adherence)
  - Patient cards with adherence progress
  - Recent activity timeline
  - Quick action cards (Alerts, Reports)
  - Bottom navigation

- **Features**:
  - Real-time patient data from Firestore
  - Adherence rate visualization with progress bars
  - Missed dose warnings
  - Call and message buttons (placeholders for integration)
  - Activity timeline with relative timestamps
  - Pull-to-refresh capability (via refresh function)

**FamilyAlertsScreen.kt**
- **Components**:
  - Header with alert count
  - Filter tabs (All, Critical, Missed Doses, Low Adherence)
  - Alert cards with type-specific styling
  - Action buttons (View, Dismiss, Contact)
  - Bottom navigation

- **Features**:
  - Real-time alerts from ViewModel
  - Color-coded alert types (red=critical, orange=warning, blue=info)
  - Filter by alert type
  - Dismiss and resolve functionality
  - Empty state messaging

## Data Flow

### Home Page Data Flow
1. **Screen Launch**: FamilyDashboardScreen initializes with ViewModel
2. **ViewModel Init**: 
   - Calls `loadPatients()` and `loadRecentActivity()`
3. **Load Patients**:
   - Fetches patients with sharing enabled from Firestore
   - For each patient, calculates adherence stats
   - Determines status (good/attention) based on adherence and missed doses
4. **Load Activity**:
   - Fetches recent adherence records (last 7 days)
   - Maps patient IDs to patient names
   - Transforms records into ActivityItem objects
   - Sorts by timestamp descending, takes top 15
5. **UI Update**: Screen observes StateFlows and renders data

### Alerts Page Data Flow
1. **Screen Launch**: FamilyAlertsScreen initializes with ViewModel
2. **ViewModel Init**: Calls `loadAlerts()`
3. **Load Alerts**:
   - Fetches existing alerts from Firestore
   - Generates new alerts from adherence data
   - Combines and deduplicates alerts
   - Saves new alerts to Firestore
4. **Alert Generation**:
   - Gets all patients with sharing enabled
   - For each patient:
     - Check for missed doses today → CRITICAL alert
     - Check for consecutive missed doses → CRITICAL alert
     - Check adherence rate < 80% → WARNING alert
5. **Filtering**: UI applies filter based on selected tab
6. **Actions**: Dismiss/resolve updates Firestore and local state

## Key Features Implemented

### ✅ Implemented Features

1. **Real Patient Data Integration**
   - Loads patients from Firestore where `shareDataEnabled = true`
   - Calculates real adherence statistics from adherence records
   - Displays today's medication progress
   - Shows missed doses with warnings

2. **Activity Timeline**
   - Generates from actual adherence records
   - Shows last 15 activities across all patients
   - Includes patient name, action, medication name, and relative time
   - Color-coded by action type (green=took, red=missed, orange=skipped)

3. **Alert System**
   - Dynamically generates alerts from adherence patterns
   - Stores alerts in Firestore for persistence
   - Supports dismiss and resolve operations
   - Filter by alert type
   - Color-coded by severity

4. **Statistics Dashboard**
   - Average adherence across all patients
   - Active alerts count (patients needing attention)
   - Patient count
   - Per-patient adherence rates

5. **Error Handling**
   - Toast messages for errors
   - Loading indicators
   - Empty states with helpful messages
   - Graceful fallbacks

### 🔄 Partially Implemented / Placeholders

1. **Action Buttons**
   - Call patient: Button exists but needs phone intent implementation
   - Message patient: Button exists but needs navigation to messages with context
   - View patient details: Needs patient detail screen navigation

2. **Pull-to-Refresh**
   - Refresh function exists in ViewModel
   - UI needs SwipeRefresh implementation

### 📋 Future Enhancements (Out of Scope)

1. Push notifications for new alerts
2. Real-time WebSocket updates
3. Advanced alert rules and customization
4. Alert notification preferences
5. Batch alert operations
6. Medication refill alerts
7. Schedule change detection

## Database Schema

### Firestore Collections

#### `patients`
```
{
  id: string (document ID = userId)
  name: string
  email: string
  age: number
  conditions: array<string>
  emergencyContact: string
  bloodType: string?
  shareDataEnabled: boolean
  createdAt: timestamp
  updatedAt: timestamp
}
```

#### `adherence_records`
```
{
  id: string (document ID)
  userId: string (patient ID)
  medicationId: string
  medicationName: string? (optional, for display)
  date: string (ISO date format)
  status: string ("TAKEN", "MISSED", "SKIPPED", "PENDING")
  timestamp: string (ISO datetime)
  notes: string?
}
```

#### `alerts`
```
{
  id: string (document ID)
  type: string ("CRITICAL", "WARNING", "INFO")
  patientId: string
  patientName: string
  title: string
  message: string
  medicationName: string?
  timestamp: string (ISO datetime)
  isResolved: boolean
  resolvedAt: string? (ISO datetime)
  isDismissed: boolean
  dismissedAt: string? (ISO datetime)
  metadata: map<string, any>
}
```

## Configuration

### Alert Thresholds
- **Good Status**: Adherence ≥ 80% AND missed doses = 0
- **Attention Status**: Adherence < 80% OR missed doses > 0
- **Critical Alert**: Missed dose today OR 2+ consecutive missed doses
- **Warning Alert**: Adherence < 80% with at least 5 records

### Display Limits
- Recent activity: Last 15 items
- Activity lookback: 7 days
- Adherence calculation: All records (can be modified to use time range)

## Testing Notes

### Test Scenarios

1. **Empty State**
   - No patients connected → Empty state with add patient prompt
   - No alerts → "All clear" message

2. **Patient with Good Adherence**
   - Status badge: Green "✓ Good"
   - No missed dose warning
   - Progress bar shows completion

3. **Patient Needing Attention**
   - Status badge: Orange "! Attention"
   - Missed dose warning displayed
   - Generates alerts

4. **Activity Timeline**
   - Different action types display different colors
   - Relative time formatting works correctly
   - Activities sorted newest first

5. **Alert Filtering**
   - All tab shows all alerts
   - Critical tab shows only CRITICAL type
   - Missed Doses tab shows alerts with "Missed" in title
   - Low Adherence tab shows alerts with "Adherence" in title

6. **Alert Actions**
   - Dismiss removes alert from view
   - Resolve marks alert as resolved but keeps visible

## Dependencies

- **Hilt**: Dependency injection
- **Jetpack Compose**: UI framework
- **Firebase Firestore**: Data persistence
- **Kotlin Coroutines**: Async operations
- **kotlinx-datetime**: Date/time handling
- **Material3**: UI components

## Known Limitations

1. **Date Calculations**: Simplified relative time formatting
2. **Activity Medication Names**: Depends on medication name being stored in adherence records
3. **Alert Deduplication**: Simple key-based, may need refinement
4. **Real-time Updates**: Currently manual refresh, no live listeners
5. **Phone/Message Actions**: Placeholder implementations

## File Structure

```
app/src/main/java/com/medicationadherence/app/
├── domain/
│   └── model/
│       ├── Alert.kt (NEW)
│       └── Models.kt (existing)
├── data/
│   └── firestore/
│       ├── FirestoreAlertDataSource.kt (NEW)
│       ├── FirestoreAdherenceDataSource.kt (ENHANCED)
│       └── FirestorePatientDataSource.kt (existing)
└── presentation/
    └── family/
        ├── FamilyDashboardScreen.kt (ENHANCED)
        ├── FamilyDashboardViewModel.kt (ENHANCED)
        ├── FamilyAlertsScreen.kt (ENHANCED)
        └── FamilyAlertsViewModel.kt (NEW)
```

## Next Steps

To complete the implementation:

1. **Add Phone Call Intent**
   ```kotlin
   val intent = Intent(Intent.ACTION_DIAL).apply {
       data = Uri.parse("tel:${patient.emergencyContact}")
   }
   context.startActivity(intent)
   ```

2. **Add Message Navigation**
   ```kotlin
   onNavigateToMessages() // Pass patient context
   ```

3. **Add Pull-to-Refresh**
   ```kotlin
   PullRefreshIndicator(
       refreshing = isRefreshing,
       state = pullRefreshState,
       modifier = Modifier.align(Alignment.TopCenter)
   )
   ```

4. **Add Real-time Listeners** (Optional)
   ```kotlin
   firestore.collection("adherence_records")
       .addSnapshotListener { snapshot, e ->
           // Update state
       }
   ```

5. **Test with Real Data**
   - Create test patient accounts with data sharing enabled
   - Generate adherence records
   - Verify alerts generate correctly
   - Test filtering and actions

## Conclusion

This implementation provides a complete, functional caregiver app with real Firestore integration. The Home page displays live patient data with adherence statistics and activity timeline. The Alerts page dynamically generates and manages alerts based on adherence patterns. Both screens use Hilt for dependency injection, follow MVVM architecture, and provide proper error handling and loading states.
