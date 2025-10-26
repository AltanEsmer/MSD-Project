package com.medicationadherence.app.presentation.patient.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.medicationadherence.app.presentation.common.components.FeatureCard
import com.medicationadherence.app.presentation.common.components.GradientBackground
import com.medicationadherence.app.presentation.theme.*

/**
 * Welcome Screen - First screen users see
 */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onSwitchToFamily: () -> Unit
) {
    GradientBackground(
        startColor = Blue50,
        endColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Blue600),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = "MediCare Logo",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Name
            Text(
                text = "MediCare",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Blue600
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Never miss a dose. Stay healthy, stay on track.",
                style = MaterialTheme.typography.bodyLarge,
                color = Gray600,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Features
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureCard(
                    icon = Icons.Default.Medication,
                    iconBackground = Blue100,
                    iconTint = Blue600,
                    title = "Easy Medication Tracking",
                    description = "Simple reminders for all your medications"
                )
                
                FeatureCard(
                    icon = Icons.Default.Favorite,
                    iconBackground = Green100,
                    iconTint = Green600,
                    title = "Health Progress",
                    description = "Monitor your adherence and streaks"
                )
                
                FeatureCard(
                    icon = Icons.Default.People,
                    iconBackground = Purple100,
                    iconTint = Purple600,
                    title = "Family Connection",
                    description = "Keep loved ones informed and supportive"
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // CTA Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onGetStarted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue600
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Get Started",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                OutlinedButton(
                    onClick = onSwitchToFamily,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "I'm a Family Caregiver",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Terms
            Text(
                text = "By continuing, you agree to our Terms of Service and Privacy Policy",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
                textAlign = TextAlign.Center
            )
        }
    }
}

