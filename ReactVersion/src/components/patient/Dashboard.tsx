import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Badge } from '../ui/badge';
import { Progress } from '../ui/progress';
import { Home, Pill as PillIcon, History, User, Pill, Droplet, Syringe, Tablets, Flame, Calendar } from 'lucide-react';
import { Medication, Patient, AdherenceRecord } from '../../App';
import { toast } from 'sonner@2.0.3';

type Props = {
  patient: Patient;
  medications: Medication[];
  adherenceRecords: AdherenceRecord[];
  onNavigate: (screen: string) => void;
  onTakeMedication: (medicationId: string, time: string) => void;
};

const medicationIcons = {
  pill: Pill,
  tablets: Tablets,
  droplet: Droplet,
  syringe: Syringe,
};

export default function Dashboard({ patient, medications, adherenceRecords, onNavigate, onTakeMedication }: Props) {
  const today = new Date().toISOString().split('T')[0];
  const currentTime = new Date().toTimeString().slice(0, 5);

  // Get today's medication schedule
  const todaySchedule = medications.flatMap(med => 
    med.times.map(time => ({
      medication: med,
      time,
      id: `${med.id}-${time}`,
    }))
  ).sort((a, b) => a.time.localeCompare(b.time));

  // Check if medication is taken
  const isTaken = (medicationId: string, time: string) => {
    return adherenceRecords.some(
      record => 
        record.medicationId === medicationId && 
        record.date === today && 
        record.time === time && 
        record.taken
    );
  };

  // Calculate today's adherence
  const todayTaken = todaySchedule.filter(item => isTaken(item.medication.id, item.time)).length;
  const todayTotal = todaySchedule.length;
  const todayPercentage = todayTotal > 0 ? Math.round((todayTaken / todayTotal) * 100) : 0;

  // Calculate streak (consecutive days with all medications taken)
  const calculateStreak = () => {
    if (medications.length === 0) return 0;
    if (adherenceRecords.length === 0) return 0;

    let streak = 0;
    const todayDate = new Date();
    todayDate.setHours(0, 0, 0, 0);
    
    let currentDate = new Date(todayDate);
    const maxDaysToCheck = 365;
    let daysChecked = 0;
    
    // Count backwards from today until we find a day with missed doses
    while (daysChecked < maxDaysToCheck) {
      const dateStr = currentDate.toISOString().split('T')[0];
      
      // Check if all medications had all their expected doses taken on this day
      let allMedicationsComplete = true;
      
      for (const medication of medications) {
        const expectedDoses = medication.times.length;
        if (expectedDoses === 0) continue; // Skip medications with no scheduled times
        
        // Get all records for this medication on this date
        const dayRecords = adherenceRecords.filter(
          r => r.medicationId === medication.id && r.date === dateStr
        );
        
        // Count how many doses were actually taken
        const takenDoses = dayRecords.filter(r => r.taken === true).length;
        
        // If we don't have enough taken doses, this day breaks the streak
        if (takenDoses < expectedDoses) {
          allMedicationsComplete = false;
          break;
        }
      }
      
      // If any medication was incomplete, stop counting
      if (!allMedicationsComplete) {
        break;
      }
      
      // This day counts towards the streak
      streak++;
      daysChecked++;
      
      // Move to previous day
      currentDate.setDate(currentDate.getDate() - 1);
    }
    
    return streak;
  };

  const streak = calculateStreak();

  const handleTakeMedication = (medicationId: string, time: string, medName: string) => {
    onTakeMedication(medicationId, time);
    toast.success(`${medName} marked as taken`);
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* Header */}
      <div className="bg-blue-600 text-white p-6 pb-8">
        <div className="flex justify-between items-start mb-4">
          <div>
            <p className="text-blue-100">Welcome back,</p>
            <h1 className="text-white">{patient.name}</h1>
          </div>
          <button 
            onClick={() => onNavigate('profile')}
            className="w-12 h-12 bg-white/20 rounded-full flex items-center justify-center"
          >
            <User className="w-6 h-6" />
          </button>
        </div>
        
        <div className="flex items-center gap-2 text-sm">
          <Calendar className="w-4 h-4" />
          {new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' })}
        </div>
      </div>

      {/* Stats Cards */}
      <div className="px-6 -mt-4 grid grid-cols-2 gap-3 mb-6">
        <Card className="p-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
              <Flame className="w-5 h-5 text-green-600" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Streak</p>
              <p className="text-green-600">{streak} days</p>
            </div>
          </div>
        </Card>
        
        <Card className="p-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
              <PillIcon className="w-5 h-5 text-blue-600" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Today</p>
              <p className="text-blue-600">{todayTaken}/{todayTotal}</p>
            </div>
          </div>
        </Card>
      </div>

      {/* Today's Progress */}
      <div className="px-6 mb-6">
        <Card className="p-4">
          <div className="flex justify-between items-center mb-2">
            <h3>Today's Progress</h3>
            <span className="text-blue-600">{todayPercentage}%</span>
          </div>
          <Progress value={todayPercentage} className="h-2" />
          <p className="text-sm text-gray-600 mt-2">
            {todayTotal - todayTaken === 0 
              ? '🎉 All medications taken today!' 
              : `${todayTotal - todayTaken} ${todayTotal - todayTaken === 1 ? 'dose' : 'doses'} remaining`
            }
          </p>
        </Card>
      </div>

      {/* Today's Medications */}
      <div className="px-6">
        <div className="flex justify-between items-center mb-4">
          <h2>Today's Medications</h2>
          <Button 
            variant="ghost" 
            size="sm"
            onClick={() => onNavigate('medications')}
          >
            View All
          </Button>
        </div>

        {todaySchedule.length === 0 ? (
          <Card className="p-8 text-center">
            <p className="text-gray-600 mb-4">No medications scheduled</p>
            <Button 
              variant="outline"
              onClick={() => onNavigate('medications')}
            >
              Add Medication
            </Button>
          </Card>
        ) : (
          <div className="space-y-3">
            {todaySchedule.map((item) => {
              const IconComponent = medicationIcons[item.medication.icon as keyof typeof medicationIcons] || Pill;
              const taken = isTaken(item.medication.id, item.time);
              const isPast = item.time < currentTime;

              return (
                <Card key={item.id} className={`p-4 ${taken ? 'bg-green-50 border-green-200' : ''}`}>
                  <div className="flex items-center gap-3">
                    <div className={`w-12 h-12 ${taken ? 'bg-green-100' : 'bg-blue-100'} rounded-xl flex items-center justify-center flex-shrink-0`}>
                      <IconComponent className={`w-6 h-6 ${taken ? 'text-green-600' : 'text-blue-600'}`} />
                    </div>
                    
                    <div className="flex-1">
                      <div className="flex items-start justify-between mb-1">
                        <h3>{item.medication.name}</h3>
                        <Badge variant={taken ? "default" : isPast ? "destructive" : "secondary"} className="ml-2">
                          {item.time}
                        </Badge>
                      </div>
                      <p className="text-gray-600">{item.medication.dosage}</p>
                      {item.medication.instructions && (
                        <p className="text-sm text-gray-500">{item.medication.instructions}</p>
                      )}
                    </div>
                    
                    {!taken && (
                      <Button 
                        size="sm"
                        className="bg-blue-600 hover:bg-blue-700"
                        onClick={() => handleTakeMedication(item.medication.id, item.time, item.medication.name)}
                      >
                        Take
                      </Button>
                    )}
                    
                    {taken && (
                      <div className="text-green-600">
                        ✓
                      </div>
                    )}
                  </div>
                </Card>
              );
            })}
          </div>
        )}
      </div>

      {/* Bottom Navigation */}
      <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 px-6 py-3">
        <div className="flex justify-around items-center max-w-md mx-auto">
          <button 
            onClick={() => onNavigate('dashboard')}
            className="flex flex-col items-center gap-1 text-blue-600"
          >
            <Home className="w-6 h-6" />
            <span className="text-xs">Home</span>
          </button>
          
          <button 
            onClick={() => onNavigate('medications')}
            className="flex flex-col items-center gap-1 text-gray-600"
          >
            <PillIcon className="w-6 h-6" />
            <span className="text-xs">Medications</span>
          </button>
          
          <button 
            onClick={() => onNavigate('history')}
            className="flex flex-col items-center gap-1 text-gray-600"
          >
            <History className="w-6 h-6" />
            <span className="text-xs">History</span>
          </button>
          
          <button 
            onClick={() => onNavigate('profile')}
            className="flex flex-col items-center gap-1 text-gray-600"
          >
            <User className="w-6 h-6" />
            <span className="text-xs">Profile</span>
          </button>
        </div>
      </div>
    </div>
  );
}
