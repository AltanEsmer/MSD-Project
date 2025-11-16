package com.medicationadherence.app.domain.model

import kotlinx.datetime.LocalDateTime

/**
 * Alert types for caregiver notifications
 */
enum class AlertType {
    CRITICAL,
    WARNING,
    INFO
}

/**
 * Alert model for caregiver notifications
 */
data class Alert(
    val id: String = "",
    val type: AlertType,
    val patientId: String,
    val patientName: String,
    val title: String,
    val message: String,
    val medicationName: String? = null,
    val timestamp: LocalDateTime,
    val isResolved: Boolean = false,
    val resolvedAt: LocalDateTime? = null,
    val isDismissed: Boolean = false,
    val dismissedAt: LocalDateTime? = null,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Activity item for timeline display
 */
data class ActivityItem(
    val id: String = "",
    val patientId: String,
    val patientName: String,
    val action: String,
    val actionType: ActivityType,
    val medicationName: String? = null,
    val timestamp: LocalDateTime
)

/**
 * Activity types for timeline
 */
enum class ActivityType {
    TOOK_MEDICATION,
    MISSED_DOSE,
    SKIPPED_MEDICATION
}
