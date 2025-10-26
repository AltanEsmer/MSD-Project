import { useState } from 'react';
import { Toaster } from './components/ui/sonner';

// Patient App Screens
import WelcomeScreen from './components/patient/WelcomeScreen';
import ProfileSetup from './components/patient/ProfileSetup';
import MedicationSetup from './components/patient/MedicationSetup';
import PermissionSetup from './components/patient/PermissionSetup';
import Dashboard from './components/patient/Dashboard';
import MedicationList from './components/patient/MedicationList';
import AddEditMedication from './components/patient/AddEditMedication';
import AdherenceHistory from './components/patient/AdherenceHistory';
import PatientProfile from './components/patient/PatientProfile';

// Family App Screens
import FamilyWelcome from './components/family/FamilyWelcome';
import PatientConnection from './components/family/PatientConnection';
import FamilyDashboard from './components/family/FamilyDashboard';
import AlertInbox from './components/family/AlertInbox';
import MessageCenter from './components/family/MessageCenter';
import Reports from './components/family/Reports';

export type Medication = {
  id: string;
  name: string;
  dosage: string;
  frequency: string;
  times: string[];
  instructions: string;
  icon: string;
  importance: 'high' | 'medium' | 'low';
};

export type Patient = {
  id: string;
  name: string;
  age: string;
  healthConditions: string[];
  emergencyContact: string;
};

export type AdherenceRecord = {
  medicationId: string;
  date: string;
  time: string;
  taken: boolean;
  missedReason?: string;
};

export type AppState = {
  appMode: 'patient' | 'family';
  patient: Patient | null;
  medications: Medication[];
  adherenceRecords: AdherenceRecord[];
  onboardingStep: 'welcome' | 'profile' | 'medication' | 'permission' | 'complete';
  familyStep: 'welcome' | 'connection' | 'dashboard';
  currentScreen: string;
  editingMedication: Medication | null;
  connectedPatients: Patient[];
};

export default function App() {
  const [appState, setAppState] = useState<AppState>({
    appMode: 'patient',
    patient: null,
    medications: [],
    adherenceRecords: [],
    onboardingStep: 'welcome',
    familyStep: 'welcome',
    currentScreen: 'welcome',
    editingMedication: null,
    connectedPatients: [],
  });

  const updateAppState = (updates: Partial<AppState>) => {
    setAppState(prev => ({ ...prev, ...updates }));
  };

  const switchToFamilyMode = () => {
    updateAppState({ appMode: 'family', familyStep: 'welcome', currentScreen: 'family-welcome' });
  };

  const switchToPatientMode = () => {
    updateAppState({ appMode: 'patient', onboardingStep: 'welcome', currentScreen: 'welcome' });
  };

  // Patient App Navigation
  if (appState.appMode === 'patient') {
    if (appState.onboardingStep !== 'complete') {
      // Onboarding Flow
      if (appState.onboardingStep === 'welcome') {
        return (
          <>
            <WelcomeScreen onGetStarted={() => updateAppState({ onboardingStep: 'profile', currentScreen: 'profile-setup' })} onSwitchToFamily={switchToFamilyMode} />
            <Toaster />
          </>
        );
      }
      if (appState.onboardingStep === 'profile') {
        return (
          <>
            <ProfileSetup 
              onComplete={(patient) => updateAppState({ patient, onboardingStep: 'medication', currentScreen: 'medication-setup' })} 
              onBack={() => updateAppState({ onboardingStep: 'welcome', currentScreen: 'welcome' })}
            />
            <Toaster />
          </>
        );
      }
      if (appState.onboardingStep === 'medication') {
        return (
          <>
            <MedicationSetup 
              onComplete={(medications) => updateAppState({ medications, onboardingStep: 'permission', currentScreen: 'permission-setup' })}
              onBack={() => updateAppState({ onboardingStep: 'profile', currentScreen: 'profile-setup' })}
            />
            <Toaster />
          </>
        );
      }
      if (appState.onboardingStep === 'permission') {
        return (
          <>
            <PermissionSetup 
              onComplete={() => updateAppState({ onboardingStep: 'complete', currentScreen: 'dashboard' })}
              onBack={() => updateAppState({ onboardingStep: 'medication', currentScreen: 'medication-setup' })}
            />
            <Toaster />
          </>
        );
      }
    }

    // Main App Screens
    if (appState.currentScreen === 'dashboard') {
      return (
        <>
          <Dashboard 
            patient={appState.patient!}
            medications={appState.medications}
            adherenceRecords={appState.adherenceRecords}
            onNavigate={(screen) => updateAppState({ currentScreen: screen })}
            onTakeMedication={(medicationId, time) => {
              const newRecord: AdherenceRecord = {
                medicationId,
                date: new Date().toISOString().split('T')[0],
                time,
                taken: true,
              };
              updateAppState({ adherenceRecords: [...appState.adherenceRecords, newRecord] });
            }}
          />
          <Toaster />
        </>
      );
    }

    if (appState.currentScreen === 'medications') {
      return (
        <>
          <MedicationList 
            medications={appState.medications}
            onNavigate={(screen) => updateAppState({ currentScreen: screen })}
            onAddNew={() => updateAppState({ currentScreen: 'add-medication', editingMedication: null })}
            onEdit={(medication) => updateAppState({ currentScreen: 'add-medication', editingMedication: medication })}
            onDelete={(id) => updateAppState({ medications: appState.medications.filter(m => m.id !== id) })}
          />
          <Toaster />
        </>
      );
    }

    if (appState.currentScreen === 'add-medication') {
      return (
        <>
          <AddEditMedication 
            medication={appState.editingMedication}
            onSave={(medication) => {
              if (appState.editingMedication) {
                updateAppState({ 
                  medications: appState.medications.map(m => m.id === medication.id ? medication : m),
                  currentScreen: 'medications',
                  editingMedication: null,
                });
              } else {
                updateAppState({ 
                  medications: [...appState.medications, medication],
                  currentScreen: 'medications',
                  editingMedication: null,
                });
              }
            }}
            onCancel={() => updateAppState({ currentScreen: 'medications', editingMedication: null })}
          />
          <Toaster />
        </>
      );
    }

    if (appState.currentScreen === 'history') {
      return (
        <>
          <AdherenceHistory 
            medications={appState.medications}
            adherenceRecords={appState.adherenceRecords}
            onNavigate={(screen) => updateAppState({ currentScreen: screen })}
          />
          <Toaster />
        </>
      );
    }

    if (appState.currentScreen === 'profile') {
      return (
        <>
          <PatientProfile 
            patient={appState.patient!}
            onNavigate={(screen) => updateAppState({ currentScreen: screen })}
            onUpdateProfile={(patient) => updateAppState({ patient })}
            onSwitchToFamily={switchToFamilyMode}
          />
          <Toaster />
        </>
      );
    }
  }

  // Family App Navigation
  if (appState.appMode === 'family') {
    if (appState.familyStep === 'welcome') {
      return (
        <>
          <FamilyWelcome 
            onGetStarted={() => updateAppState({ familyStep: 'connection', currentScreen: 'patient-connection' })}
            onSwitchToPatient={switchToPatientMode}
          />
          <Toaster />
        </>
      );
    }

    if (appState.currentScreen === 'patient-connection') {
      return (
        <>
          <PatientConnection 
            onConnect={(patient) => updateAppState({ 
              connectedPatients: [...appState.connectedPatients, patient],
              familyStep: 'dashboard',
              currentScreen: 'family-dashboard',
            })}
            onBack={() => updateAppState({ familyStep: 'welcome', currentScreen: 'family-welcome' })}
          />
          <Toaster />
        </>
      );
    }

    if (appState.currentScreen === 'family-dashboard') {
      return (
        <>
          <FamilyDashboard 
            connectedPatients={appState.connectedPatients}
            onNavigate={(screen) => updateAppState({ currentScreen: screen })}
          />
          <Toaster />
        </>
      );
    }

    if (appState.currentScreen === 'alerts') {
      return (
        <>
          <AlertInbox 
            onNavigate={(screen) => updateAppState({ currentScreen: screen })}
          />
          <Toaster />
        </>
      );
    }

    if (appState.currentScreen === 'messages') {
      return (
        <>
          <MessageCenter 
            connectedPatients={appState.connectedPatients}
            onNavigate={(screen) => updateAppState({ currentScreen: screen })}
          />
          <Toaster />
        </>
      );
    }

    if (appState.currentScreen === 'reports') {
      return (
        <>
          <Reports 
            connectedPatients={appState.connectedPatients}
            onNavigate={(screen) => updateAppState({ currentScreen: screen })}
          />
          <Toaster />
        </>
      );
    }
  }

  return (
    <>
      <div className="min-h-screen bg-background">
        <WelcomeScreen onGetStarted={() => updateAppState({ onboardingStep: 'profile' })} onSwitchToFamily={switchToFamilyMode} />
      </div>
      <Toaster />
    </>
  );
}
