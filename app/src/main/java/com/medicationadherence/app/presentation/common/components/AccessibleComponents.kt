package com.medicationadherence.app.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Accessible button with minimum 44dp touch target
 */
@Composable
fun AccessibleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentDescription: String? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 44.dp)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            },
        enabled = enabled,
        colors = colors,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Large text with scalable font size
 */
@Composable
fun LargeText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 18,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color.Unspecified,
    contentDescription: String? = null
) {
    Text(
        text = text,
        modifier = modifier.semantics {
            contentDescription?.let { this.contentDescription = it }
        },
        fontSize = fontSize.sp,
        fontWeight = fontWeight,
        color = color,
        textAlign = TextAlign.Start
    )
}

/**
 * Medication card component
 */
@Composable
fun MedicationCard(
    medicationName: String,
    dosage: String,
    scheduledTime: String,
    status: String,
    onTakeClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Medication name
            LargeText(
                text = medicationName,
                fontSize = 22,
                fontWeight = FontWeight.Bold,
                contentDescription = "Medication name: $medicationName"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Dosage and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LargeText(
                    text = "Dosage: $dosage",
                    fontSize = 18,
                    contentDescription = "Dosage: $dosage"
                )
                LargeText(
                    text = scheduledTime,
                    fontSize = 18,
                    fontWeight = FontWeight.Medium,
                    contentDescription = "Scheduled time: $scheduledTime"
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Status indicator
            StatusIndicator(
                status = status,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AccessibleButton(
                    text = "TAKE",
                    onClick = onTakeClick,
                    enabled = isEnabled && status == "PENDING",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentDescription = "Take $medicationName medication"
                )
                
                AccessibleButton(
                    text = "SKIP",
                    onClick = onSkipClick,
                    enabled = isEnabled && status == "PENDING",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    contentDescription = "Skip $medicationName medication"
                )
            }
        }
    }
}

/**
 * Status indicator component
 */
@Composable
fun StatusIndicator(
    status: String,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (status.uppercase()) {
        "TAKEN" -> MaterialTheme.colorScheme.primary to "✓ Taken"
        "SKIPPED" -> MaterialTheme.colorScheme.error to "✗ Skipped"
        "MISSED" -> MaterialTheme.colorScheme.error to "⚠ Missed"
        else -> MaterialTheme.colorScheme.outline to "⏰ Pending"
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        LargeText(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 16,
            fontWeight = FontWeight.Medium,
            color = color,
            contentDescription = "Medication status: $text"
        )
    }
}

/**
 * Progress indicator for adherence tracking
 */
@Composable
fun ProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String = "Adherence Rate"
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LargeText(
                text = label,
                fontSize = 18,
                fontWeight = FontWeight.Medium,
                contentDescription = "$label: ${(progress * 100).toInt()}%"
            )
            LargeText(
                text = "${(progress * 100).toInt()}%",
                fontSize = 18,
                fontWeight = FontWeight.Bold,
                contentDescription = "Percentage: ${(progress * 100).toInt()}%"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    }
}

/**
 * Medication card item for the patient dashboard
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationCardItem(
    medicationWithSchedule: com.medicationadherence.app.domain.model.MedicationWithSchedule,
    onTakeClick: (String) -> Unit,
    onSkipClick: (String) -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val medication = medicationWithSchedule.medication
    val nextSchedule = medicationWithSchedule.schedules
        .filter { it.status == com.medicationadherence.app.domain.model.AdherenceStatus.PENDING }
        .minByOrNull { it.scheduledTime }

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onDetailsClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = medication.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Dosage: ${medication.dosage}",
                style = MaterialTheme.typography.bodyMedium
            )

            nextSchedule?.let { schedule ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Next: ${schedule.scheduledTime}",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onTakeClick(schedule.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Take")
                    }
                    OutlinedButton(
                        onClick = { onSkipClick(schedule.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Skip")
                    }
                }
            } ?: run {
                // Show status if no pending schedule
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All doses completed for today",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}