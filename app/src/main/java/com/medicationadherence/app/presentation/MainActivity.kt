package com.medicationadherence.app.presentation

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.medicationadherence.app.data.local.SampleDataSeeder
import com.medicationadherence.app.presentation.patient.screen.PatientDashboardScreen
import com.medicationadherence.app.presentation.patient.screen.AddMedicationScreen
import com.medicationadherence.app.presentation.patient.screen.MedicationDetailsScreen
import com.medicationadherence.app.presentation.theme.MedicationAdherenceTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main Activity for Medication Adherence App
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var sampleDataSeeder: SampleDataSeeder
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Seed sample data on first launch
        seedSampleDataIfFirstLaunch()
        
        setContent {
            MedicationAdherenceTheme {
                MedicationApp()
            }
        }
    }
    
    /**
     * Seeds sample data on first launch
     * Uses SharedPreferences to track if data has been seeded
     */
    private fun seedSampleDataIfFirstLaunch() {
        val prefs = getSharedPreferences("medication_adherence_prefs", Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("is_first_launch", true)
        
        if (isFirstLaunch) {
            lifecycleScope.launch {
                try {
                    sampleDataSeeder.seedSampleData()
                    // Mark that we've completed first launch
                    prefs.edit().putBoolean("is_first_launch", false).apply()
                    android.util.Log.d("MainActivity", "Sample data seeded successfully")
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error seeding sample data", e)
                }
            }
        }
    }
}

/**
 * Main app composable with navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationApp() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "patient_dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("patient_dashboard") {
                PatientDashboardScreen(
                    onAddMedication = {
                        navController.navigate("add_medication")
                    },
                    onMedicationDetails = { medicationId ->
                        navController.navigate("medication_details/$medicationId")
                    }
                )
            }

            composable("add_medication") {
                AddMedicationScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("medication_details/{medicationId}") { backStackEntry ->
                val medicationId = backStackEntry.arguments?.getString("medicationId") ?: ""
                MedicationDetailsScreen(
                    medicationId = medicationId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onEditMedication = { medId ->
                        // TODO: Navigate to edit medication screen
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
