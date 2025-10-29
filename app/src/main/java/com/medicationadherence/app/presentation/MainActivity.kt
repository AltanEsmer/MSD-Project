package com.medicationadherence.app.presentation

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.medicationadherence.app.data.local.SampleDataSeeder
import com.medicationadherence.app.domain.repository.AuthRepository
import com.medicationadherence.app.presentation.auth.LoginScreen
import com.medicationadherence.app.presentation.auth.SignUpScreen
import kotlinx.coroutines.launch
import com.medicationadherence.app.presentation.patient.screen.*
import com.medicationadherence.app.presentation.family.*
import com.medicationadherence.app.presentation.theme.MedicationAdherenceTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Main Activity for Medication Adherence App - MVP with Modern UI
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var sampleDataSeeder: SampleDataSeeder
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Check if onboarding is complete
        val prefs = getSharedPreferences("medication_adherence_prefs", Context.MODE_PRIVATE)
        val hasCompletedOnboarding = prefs.getBoolean("onboarding_complete", false)
        val hasSeededData = prefs.getBoolean("has_seeded_data", false)
        
        // Seed sample data if not done yet
        if (!hasSeededData) {
            lifecycleScope.launch {
                try {
                    sampleDataSeeder.seedSampleData()
                    prefs.edit().putBoolean("has_seeded_data", true).apply()
                    android.util.Log.d("MainActivity", "Sample data seeded successfully")
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error seeding sample data", e)
                }
            }
        }
        
        // Check authentication state
        lifecycleScope.launch {
            val isAuthenticated = try {
                authRepository.authState.first().let {
                    it is com.medicationadherence.app.domain.model.AuthState.Authenticated
                }
            } catch (e: Exception) {
                false
            }
            
            val startDestination = when {
                !isAuthenticated -> "login"
                !hasCompletedOnboarding -> "profile_setup"
                else -> "patient_dashboard"
            }
            
            setContent {
                MedicationAdherenceTheme {
                    MedicationApp(
                        startDestination = startDestination,
                        hasCompletedOnboarding = hasCompletedOnboarding,
                        onOnboardingComplete = {
                            prefs.edit().putBoolean("onboarding_complete", true).apply()
                        },
                        authRepository = authRepository
                    )
                }
            }
        }
    }
}

/**
 * Main app composable with navigation
 */
@Composable
fun MedicationApp(
    startDestination: String = "welcome",
    hasCompletedOnboarding: Boolean = false,
    onOnboardingComplete: () -> Unit = {},
    authRepository: AuthRepository? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val navController = rememberNavController()
    
    // Handler for logout
    val scope = rememberCoroutineScope()
    val handleLogout: () -> Unit = {
        if (authRepository != null) {
            scope.launch {
                authRepository.signOut()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
    var patientName by remember { mutableStateOf("Patient") }
    var patientAge by remember { mutableStateOf("") }
    var healthConditions by remember { mutableStateOf(emptyList<String>()) }
    var emergencyContact by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf<String?>(null) }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            // Authentication Screens
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        // Check if onboarding is complete, navigate accordingly
                        val prefs = context.getSharedPreferences("medication_adherence_prefs", Context.MODE_PRIVATE)
                        val onboardingComplete = prefs.getBoolean("onboarding_complete", false)
                        if (onboardingComplete) {
                            navController.navigate("patient_dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            navController.navigate("profile_setup") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate("signup")
                    }
                )
            }
            
            composable("signup") {
                SignUpScreen(
                    onSignUpSuccess = {
                        navController.navigate("profile_setup") {
                            popUpTo("signup") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo("signup") { inclusive = true }
                        }
                    }
                )
            }
            
            // Welcome & Onboarding
            composable("welcome") {
                WelcomeScreen(
                    onGetStarted = {
                        navController.navigate("login")
                    },
                    onSwitchToFamily = {
                        navController.navigate("family_welcome")
                    }
                )
            }
            
            composable("profile_setup") {
                ProfileSetupScreen(
                    onComplete = { name, age, conditions, contact ->
                        patientName = name
                        patientAge = age
                        healthConditions = conditions
                        emergencyContact = contact
                        onOnboardingComplete()
                        navController.navigate("patient_dashboard") {
                            popUpTo("profile_setup") { inclusive = true }
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // Patient App Screens
            composable("patient_dashboard") {
                ModernPatientDashboardScreen(
                    patientName = patientName,
                    onNavigateToMedications = {
                        navController.navigate("medications_list")
                    },
                    onNavigateToHistory = {
                        navController.navigate("adherence_history")
                    },
                    onNavigateToProfile = {
                        navController.navigate("patient_profile")
                    },
                    onAddMedication = {
                        navController.navigate("add_medication")
                    },
                    onSwitchToCaregiverMode = {
                        navController.navigate("family_dashboard") {
                            popUpTo("patient_dashboard") { inclusive = true }
                        }
                    }
                )
            }
            
            composable("medications_list") {
                MedicationListScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onAddNew = {
                        navController.navigate("add_medication")
                    },
                    onEdit = { medicationId ->
                        navController.navigate("edit_medication/$medicationId")
                    },
                    onNavigateToHome = {
                        navController.navigate("patient_dashboard") {
                            popUpTo("patient_dashboard") { inclusive = true }
                        }
                    },
                    onNavigateToHistory = {
                        navController.navigate("adherence_history")
                    },
                    onNavigateToProfile = {
                        navController.navigate("patient_profile")
                    }
                )
            }
            
            composable("add_medication") {
                ModernAddMedicationScreen(
                    onCancel = {
                        navController.popBackStack()
                    },
                    onSaved = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable("edit_medication/{medicationId}") { backStackEntry ->
                val medicationId = backStackEntry.arguments?.getString("medicationId")
                ModernAddMedicationScreen(
                    medicationId = medicationId,
                    onCancel = {
                        navController.popBackStack()
                    },
                    onSaved = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable("adherence_history") {
                AdherenceHistoryScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onNavigateToHome = {
                        navController.navigate("patient_dashboard") {
                            popUpTo("patient_dashboard") { inclusive = true }
                        }
                    },
                    onNavigateToMedications = {
                        navController.navigate("medications_list")
                    },
                    onNavigateToProfile = {
                        navController.navigate("patient_profile")
                    }
                )
            }
            
            composable("patient_profile") {
                PatientProfileScreen(
                    patientName = patientName,
                    patientAge = patientAge,
                    healthConditions = healthConditions,
                    emergencyContact = emergencyContact,
                    bloodType = bloodType,
                    onBack = {
                        navController.popBackStack()
                    },
                    onSwitchToFamily = {
                        navController.navigate("family_welcome")
                    },
                    onNavigateToHome = {
                        navController.navigate("patient_dashboard") {
                            popUpTo("patient_dashboard") { inclusive = true }
                        }
                    },
                    onNavigateToMedications = {
                        navController.navigate("medications_list")
                    },
                    onNavigateToHistory = {
                        navController.navigate("adherence_history")
                    },
                    onNavigateToEditProfile = {
                        navController.navigate("edit_profile")
                    },
                    onLogout = handleLogout
                )
            }
            
            composable("edit_profile") {
                EditProfileScreen(
                    initialName = patientName,
                    initialAge = patientAge,
                    initialConditions = healthConditions,
                    initialEmergencyContact = emergencyContact,
                    initialBloodType = bloodType,
                    onSave = { name, age, conditions, contact, bt ->
                        patientName = name
                        patientAge = age
                        healthConditions = conditions
                        emergencyContact = contact
                        bloodType = bt
                        navController.popBackStack()
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }
            
            // Family App Screens
            composable("family_welcome") {
                FamilyWelcomeScreen(
                    onGetStarted = {
                        navController.navigate("family_dashboard")
                    },
                    onSwitchToPatient = {
                        navController.navigate("welcome") {
                            popUpTo("family_welcome") { inclusive = true }
                        }
                    }
                )
            }
            
            composable("family_dashboard") {
                FamilyDashboardScreen(
                    onNavigateToAlerts = {
                        navController.navigate("family_alerts")
                    },
                    onNavigateToMessages = {
                        navController.navigate("family_messages")
                    },
                    onNavigateToReports = {
                        navController.navigate("family_reports")
                    },
                    onAddPatient = {
                        // Placeholder for MVP
                    },
                    onSwitchToPatientMode = {
                        navController.navigate("patient_dashboard") {
                            popUpTo("family_dashboard") { inclusive = true }
                        }
                    }
                )
            }
            
            // Family App Screens
            composable("family_alerts") {
                FamilyAlertsScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onNavigateToHome = {
                        navController.navigate("family_dashboard") {
                            popUpTo("family_alerts") { inclusive = true }
                        }
                    },
                    onNavigateToMessages = {
                        navController.navigate("family_messages")
                    },
                    onNavigateToReports = {
                        navController.navigate("family_reports")
                    }
                )
            }
            
            composable("family_messages") {
                FamilyMessagesScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onNavigateToHome = {
                        navController.navigate("family_dashboard") {
                            popUpTo("family_messages") { inclusive = true }
                        }
                    },
                    onNavigateToAlerts = {
                        navController.navigate("family_alerts")
                    },
                    onNavigateToReports = {
                        navController.navigate("family_reports")
                    }
                )
            }
            
            composable("family_reports") {
                FamilyReportsScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onNavigateToHome = {
                        navController.navigate("family_dashboard") {
                            popUpTo("family_reports") { inclusive = true }
                        }
                    },
                    onNavigateToAlerts = {
                        navController.navigate("family_alerts")
                    },
                    onNavigateToMessages = {
                        navController.navigate("family_messages")
                    }
                )
            }
        }
    }
}
