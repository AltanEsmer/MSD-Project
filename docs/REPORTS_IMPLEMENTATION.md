# Reports Feature Implementation

## Overview
The Reports feature provides comprehensive adherence analytics for family caregivers to monitor patient medication compliance. This document describes the architecture, calculations, and implementation details.

## Architecture

### Data Layer

#### Domain Models
- **AdherenceReport** (`domain/model/Report.kt`)
  - Main report model containing all analytics
  - Fields: patientId, patientName, date range, adherence rates, doses, streaks, weekly data, medication breakdown, insights

- **WeeklyAdherenceData** (`domain/model/Report.kt`)
  - Data point for weekly trend chart
  - Fields: weekStartDate, adherenceRate, takenDoses, totalDoses

- **MedicationAdherence** (`domain/model/Report.kt`)
  - Per-medication adherence breakdown
  - Fields: medicationId, medicationName, adherenceRate, takenDoses, totalDoses, missedDoses

- **OverallSummaryStats** (`domain/model/Report.kt`)
  - Aggregate statistics across all patients
  - Fields: overallAdherence, totalMedications, totalMissedDoses, activePatients

#### Data Source
- **FirestoreReportsDataSource** (`data/firestore/FirestoreReportsDataSource.kt`)
  - Generates reports by aggregating data from adherence records
  - Performs calculations and generates insights

**Key Methods:**
- `getAdherenceReport(patientId, startDate, endDate)` - Generate full report for patient
- `getOverallSummaryStats()` - Get aggregate stats for all patients
- `getMultiplePatientReports(patientIds, startDate, endDate)` - Batch report generation

**Helper Methods:**
- `calculateStreaks(records)` - Calculate current and best streak
- `generateWeeklyData(records, startDate, endDate)` - Generate weekly adherence data points
- `generateMedicationBreakdown(medications, records)` - Calculate per-medication adherence
- `generateInsights(adherenceRate, missedDoses, streak, medicationCount)` - Generate actionable insights

### Presentation Layer

#### ViewModel
- **FamilyReportsViewModel** (`presentation/family/FamilyReportsViewModel.kt`)
  - Manages report state and data fetching
  - Handles patient selection and period changes
  - Provides export functionality

**State:**
- `patients` - List of patients with data sharing enabled
- `selectedPatient` - Currently selected patient
- `selectedPeriod` - Report time period (WEEK, MONTH, THREE_MONTHS)
- `report` - Current adherence report
- `summaryStats` - Overall statistics for all patients
- `isLoading` - Loading state
- `errorMessage` - Error state

**Functions:**
- `loadPatients()` - Load all patients with sharing enabled
- `loadSummaryStats()` - Load overall statistics
- `selectPatient(patient)` - Select patient and load their report
- `selectPeriod(period)` - Change time period and reload report
- `loadReport()` - Load report for selected patient and period
- `refresh()` - Refresh all data
- `exportReportAsText()` - Export report as shareable text

#### UI Components
- **FamilyReportsScreen** (`presentation/family/FamilyReportsScreen.kt`)
  - Main reports screen with multi-section layout

**Sections:**
1. **Header**
   - Shows selected patient name
   - Patient selector button
   - Export/share button

2. **Period Selector**
   - Filter chips for Week, Month, 3 Months
   - Updates report when changed

3. **Overall Statistics** (All Patients)
   - Overall adherence percentage
   - Active patients count
   - Displayed in stat cards

4. **Patient-Specific Summary**
   - Adherence rate
   - Current streak
   - Doses taken/total
   - Missed doses count
   - Displayed in stat cards

5. **Medication Breakdown**
   - List of medications with individual adherence rates
   - Progress bars showing completion
   - Taken/missed dose counts
   - Color-coded by performance

6. **Weekly Adherence Chart**
   - Bar chart showing weekly trends
   - Color-coded by adherence level
   - Shows last N weeks based on period

7. **Insights & Recommendations**
   - AI-generated insights based on data
   - Actionable recommendations
   - Displayed in info cards

**Dialog Components:**
- **PatientSelectorDialog** - Select which patient to view
- **ExportDialog** - Share report via messaging/email

## Report Calculations

### Adherence Rate
```kotlin
adherenceRate = (takenDoses / totalDoses) * 100
```

Where:
- `takenDoses` = count of records with status = TAKEN
- `totalDoses` = total count of adherence records in period

### Streak Calculation
**Current Streak:**
- Start from most recent record
- Count consecutive days with TAKEN status
- Break on first non-TAKEN or day gap > 1

**Best Streak:**
- Scan all records
- Find longest consecutive sequence of TAKEN
- Return max consecutive count

### Weekly Adherence Data
1. Divide date range into 7-day weeks
2. For each week:
   - Filter records in that week
   - Calculate adherence rate
   - Store as data point

### Color Coding
```kotlin
when {
    adherenceRate >= 90 -> Green (Excellent)
    adherenceRate >= 75 -> Blue (Good)
    adherenceRate >= 50 -> Orange (Needs Attention)
    else -> Red (Critical)
}
```

## Insights Generation

The system generates rule-based insights based on adherence data:

### Overall Adherence Insights
- **≥90%**: "Excellent adherence! Keep up the great work."
- **≥75%**: "Good adherence. A few improvements can make it perfect."
- **≥50%**: "Adherence needs attention. Consider setting more reminders."
- **<50%**: "Low adherence detected. Please reach out for support."

### Streak Insights
- **≥7 days**: "Amazing {streak}-day streak! Consistency is key."
- **≥3 days**: "Building momentum with a {streak}-day streak."

### Missed Dose Insights
- **>5 missed**: "{missedDoses} missed doses this period. Let's work on reducing this."

### Medication Count Insights
- **>5 medications**: "Managing {count} medications. Consider using reminders for each."

## Data Flow

### Loading Report
1. User selects patient from dropdown
2. User selects time period (Week/Month/3 Months)
3. ViewModel calculates date range from period
4. Fetches all medications for patient
5. For each medication, fetches adherence records in date range
6. Aggregates all records and performs calculations:
   - Overall adherence rate
   - Dose counts
   - Streak calculation
   - Weekly data generation
   - Per-medication breakdown
   - Insight generation
7. Returns complete AdherenceReport
8. UI renders all sections

### Exporting Report
1. User clicks export button
2. ViewModel calls `exportReportAsText()`
3. Generates plain text report with all data
4. Opens Android share sheet
5. User selects app to share via (Email, Messages, etc.)

## Firestore Queries

The Reports feature uses existing collections:
- `patients` - Patient profile data
- `medications` - Medication list
- `adherence_records` - Adherence history

**Key Queries:**
```kotlin
// Get adherence records for medication in date range
firestore.collection("adherence_records")
    .whereEqualTo("userId", userId)
    .whereEqualTo("medicationId", medicationId)
    .whereGreaterThanOrEqualTo("date", startDate)
    .whereLessThanOrEqualTo("date", endDate)
    .get()
```

No new Firestore collections are needed. Reports are generated on-demand from existing data.

## Features

### Implemented
✅ Patient selection dropdown
✅ Time period selector (Week/Month/3 Months)
✅ Overall statistics (all patients)
✅ Patient-specific summary cards
✅ Medication-by-medication breakdown
✅ Weekly adherence trend chart
✅ Current and best streak tracking
✅ AI-generated insights
✅ Export report as text
✅ Share via Android share sheet
✅ Color-coded adherence levels
✅ Loading and error states
✅ Empty states

### Not Implemented (Future Enhancements)
- PDF export with formatting
- Email report directly from app
- Custom date range picker
- Scheduled report generation
- Report history/archive
- Patient comparison view
- Time-of-day adherence patterns
- Medication interaction analysis
- Predictive adherence modeling (ML)
- Chart zoom/pan capabilities
- CSV/Excel export
- Print functionality

## UI Components Detail

### StatCard
Reusable component for displaying key metrics:
- Icon with colored background
- Label text
- Large value text
- Used for: adherence rate, streak, doses, patients, etc.

### MedicationAdherenceCard
Shows per-medication performance:
- Medication name
- Adherence percentage (color-coded)
- Progress bar
- Taken/missed counts

### WeeklyAdherenceChart
Simple bar chart visualization:
- Vertical bars for each week
- Height represents adherence rate
- Color-coded by performance level
- Week labels (W1, W2, etc.)

### InsightCard
Displays generated insights:
- Light blue background
- Lightbulb icon
- Insight text
- Multiple cards for multiple insights

## Period Options

### Week (7 days)
- Shows last 7 days of data
- Single week bar in chart
- Good for daily tracking

### Month (30 days)
- Shows last 30 days of data
- ~4 week bars in chart
- Good for pattern identification

### 3 Months (90 days)
- Shows last 90 days of data
- ~13 week bars in chart
- Good for long-term trends

## Error Handling

All operations wrapped in try-catch:
- Returns empty/zero values on errors
- Displays error message banner
- User can retry via refresh

## Performance Considerations

- Reports calculated on-demand (not pre-computed)
- Caching could be added for frequently accessed reports
- Firestore queries are indexed for fast retrieval
- Calculations run on background thread (coroutines)
- UI renders progressively as data loads

## Testing Recommendations

### Manual Testing
1. Select different patients and verify correct data
2. Change time periods and verify date ranges
3. Verify calculations are accurate
4. Test with various adherence patterns (high, low, mixed)
5. Test with patients having different medication counts
6. Test export functionality
7. Test error states (no data, network error)
8. Test empty states

### Unit Tests (To Be Implemented)
- Adherence rate calculation
- Streak calculation logic
- Weekly data generation
- Insight generation rules
- Date range calculation
- Export text formatting

### Integration Tests (To Be Implemented)
- End-to-end report generation
- Firestore data aggregation
- ViewModel state updates

## Known Issues and Limitations

1. **Performance with large datasets**: No pagination, loads all records
2. **No data caching**: Recalculates on every view
3. **Limited chart capabilities**: Simple bar chart only
4. **Basic insights**: Rule-based, not AI/ML powered
5. **No custom date ranges**: Only predefined periods
6. **Text-only export**: No PDF with formatting
7. **No offline support**: Requires internet connection

## Dependencies

- Hilt for dependency injection
- Kotlin Coroutines for async operations
- Kotlin Flows for reactive data
- Firebase Firestore for data queries
- Jetpack Compose for UI
- kotlinx-datetime for date calculations

## Future Improvements

1. **Advanced Analytics**
   - Time-of-day patterns
   - Day-of-week patterns
   - Correlation analysis
   - Predictive modeling

2. **Enhanced Visualizations**
   - Line charts for trends
   - Pie charts for breakdowns
   - Heatmaps for patterns
   - Interactive charts with zoom

3. **Export Options**
   - PDF with professional formatting
   - Excel/CSV for data analysis
   - Email with attachments
   - Scheduled report delivery

4. **Performance Optimizations**
   - Report caching
   - Incremental loading
   - Pre-computation for common reports
   - Background sync

5. **Collaboration Features**
   - Share reports with healthcare providers
   - Compare multiple patients
   - Annotate reports with notes
   - Track action items from insights
