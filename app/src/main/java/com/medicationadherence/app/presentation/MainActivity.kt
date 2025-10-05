package com.medicationadherence.app.presentation

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.medicationadherence.app.presentation.patient.screen.PatientDashboardScreen
import com.medicationadherence.app.presentation.patient.screen.AddMedicationScreen
import com.medicationadherence.app.presentation.theme.MedicationAdherenceTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for Medication Adherence App
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MedicationAdherenceTheme {
                MedicationApp()
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
                        // TODO: Navigate to medication details screen
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
            
            // TODO: Add more routes for:
            // - Medication details screen
            // - Settings screen
            // - Family app screens
        }
    }
}
