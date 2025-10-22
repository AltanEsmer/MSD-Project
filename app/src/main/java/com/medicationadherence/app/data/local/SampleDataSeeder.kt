package com.medicationadherence.app.data.local

import com.medicationadherence.app.domain.model.Medication
import com.medicationadherence.app.domain.repository.MedicationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeder class to populate the database with sample medications for testing
 */
@Singleton
class SampleDataSeeder @Inject constructor(
    private val medicationRepository: MedicationRepository
) {

    /**
     * Seeds the database with sample medications
     * This should only be called once on first app launch
     */
    suspend fun seedSampleData() = withContext(Dispatchers.IO) {
        try {
            // Sample Medication 1: Morning medication
            val aspirin = Medication(
                id = "",
                name = "Aspirin",
                dosage = "100mg",
                frequency = listOf("08:00"),
                instructions = "Take with food in the morning to prevent stomach upset",
                isActive = true
            )
            medicationRepository.insertMedication(aspirin)

            // Sample Medication 2: Twice daily medication
            val vitaminD = Medication(
                id = "",
                name = "Vitamin D",
                dosage = "1000 IU",
                frequency = listOf("08:00", "20:00"),
                instructions = "Take with a meal for better absorption",
                isActive = true
            )
            medicationRepository.insertMedication(vitaminD)

            // Sample Medication 3: Multiple times daily
            val lisinopril = Medication(
                id = "",
                name = "Lisinopril",
                dosage = "10mg",
                frequency = listOf("08:00", "18:00"),
                instructions = "Blood pressure medication. Take at the same time each day",
                isActive = true
            )
            medicationRepository.insertMedication(lisinopril)

            android.util.Log.d("SampleDataSeeder", "Successfully seeded 3 sample medications")
        } catch (e: Exception) {
            android.util.Log.e("SampleDataSeeder", "Error seeding sample data", e)
        }
    }
}

