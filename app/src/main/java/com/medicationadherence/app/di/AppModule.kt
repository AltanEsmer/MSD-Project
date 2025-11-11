package com.medicationadherence.app.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.medicationadherence.app.data.auth.FirebaseAuthDataSource
import com.medicationadherence.app.data.firestore.FirestoreAdherenceDataSource
import com.medicationadherence.app.data.firestore.FirestoreMedicationDataSource
import com.medicationadherence.app.data.firestore.FirestorePatientDataSource
import com.medicationadherence.app.data.firestore.FirestoreTokenDataSource
import com.medicationadherence.app.data.local.LocalMedicationDataSource
import com.medicationadherence.app.data.local.database.MedicationDatabase
import com.medicationadherence.app.data.local.dao.*
import com.medicationadherence.app.data.repository.AuthRepositoryImpl
import com.medicationadherence.app.data.repository.MedicationRepositoryImpl
import com.medicationadherence.app.data.repository.PatientRepositoryImpl
import com.medicationadherence.app.data.work.MedicationReminderManager
import com.medicationadherence.app.domain.repository.AuthRepository
import com.medicationadherence.app.domain.repository.MedicationRepository
import com.medicationadherence.app.domain.repository.PatientRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for dependency injection
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMedicationDatabase(@ApplicationContext context: Context): MedicationDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            MedicationDatabase::class.java,
            "medication_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun providePatientDao(database: MedicationDatabase): PatientDao {
        return database.patientDao()
    }

    @Provides
    fun provideMedicationDao(database: MedicationDatabase): MedicationDao {
        return database.medicationDao()
    }

    @Provides
    fun provideMedicationScheduleDao(database: MedicationDatabase): MedicationScheduleDao {
        return database.medicationScheduleDao()
    }

    @Provides
    fun provideAdherenceRecordDao(database: MedicationDatabase): AdherenceRecordDao {
        return database.adherenceRecordDao()
    }

    @Provides
    fun provideMedicationReminderDao(database: MedicationDatabase): MedicationReminderDao {
        return database.medicationReminderDao()
    }

    @Provides
    @Singleton
    fun provideLocalMedicationDataSource(
        medicationDao: MedicationDao,
        medicationScheduleDao: MedicationScheduleDao,
        adherenceRecordDao: AdherenceRecordDao,
        medicationReminderDao: MedicationReminderDao
    ): LocalMedicationDataSource {
        return LocalMedicationDataSource(
            medicationDao = medicationDao,
            medicationScheduleDao = medicationScheduleDao,
            adherenceRecordDao = adherenceRecordDao,
            medicationReminderDao = medicationReminderDao
        )
    }

    @Provides
    @Singleton
    fun provideMedicationReminderManager(
        @ApplicationContext context: Context,
        localDataSource: LocalMedicationDataSource
    ): MedicationReminderManager {
        return MedicationReminderManager(context, localDataSource)
    }

    @Provides
    @Singleton
    fun provideMedicationRepository(
        localDataSource: LocalMedicationDataSource,
        firestoreMedicationDataSource: FirestoreMedicationDataSource,
        firestoreAdherenceDataSource: FirestoreAdherenceDataSource,
        firebaseAuth: FirebaseAuth,
        reminderManager: MedicationReminderManager
    ): MedicationRepository {
        return MedicationRepositoryImpl(
            localDataSource = localDataSource,
            firestoreMedicationDataSource = firestoreMedicationDataSource,
            firestoreAdherenceDataSource = firestoreAdherenceDataSource,
            firebaseAuth = firebaseAuth,
            reminderManager = reminderManager
        )
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirestorePatientDataSource(
        firestore: FirebaseFirestore
    ): FirestorePatientDataSource {
        return FirestorePatientDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideFirestoreMedicationDataSource(
        firestore: FirebaseFirestore
    ): FirestoreMedicationDataSource {
        return FirestoreMedicationDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideFirestoreAdherenceDataSource(
        firestore: FirebaseFirestore
    ): FirestoreAdherenceDataSource {
        return FirestoreAdherenceDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideFirestoreTokenDataSource(
        firestore: FirebaseFirestore
    ): FirestoreTokenDataSource {
        return FirestoreTokenDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuthDataSource(
        firebaseAuth: FirebaseAuth
    ): FirebaseAuthDataSource {
        return FirebaseAuthDataSource(firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuthDataSource: FirebaseAuthDataSource
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuthDataSource)
    }

    @Provides
    @Singleton
    fun providePatientRepository(
        firestorePatientDataSource: FirestorePatientDataSource,
        patientDao: PatientDao,
        firebaseAuth: FirebaseAuth
    ): PatientRepository {
        return PatientRepositoryImpl(
            firestorePatientDataSource = firestorePatientDataSource,
            patientDao = patientDao,
            firebaseAuth = firebaseAuth
        )
    }
}
