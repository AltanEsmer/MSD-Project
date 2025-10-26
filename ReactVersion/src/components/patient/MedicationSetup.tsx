import { useState } from 'react';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';
import { Card } from '../ui/card';
import { ChevronLeft, Plus, X, Pill, Droplet, Syringe, Tablets } from 'lucide-react';
import { Medication } from '../../App';
import { toast } from 'sonner@2.0.3';

type Props = {
  onComplete: (medications: Medication[]) => void;
  onBack: () => void;
};

const medicationIcons = [
  { name: 'Pill', icon: 'pill', component: Pill },
  { name: 'Tablet', icon: 'tablets', component: Tablets },
  { name: 'Liquid', icon: 'droplet', component: Droplet },
  { name: 'Injection', icon: 'syringe', component: Syringe },
];

export default function MedicationSetup({ onComplete, onBack }: Props) {
  const [medications, setMedications] = useState<Medication[]>([]);
  const [showForm, setShowForm] = useState(false);
  
  const [name, setName] = useState('');
  const [dosage, setDosage] = useState('');
  const [frequency, setFrequency] = useState('');
  const [times, setTimes] = useState<string[]>(['']);
  const [instructions, setInstructions] = useState('');
  const [selectedIcon, setSelectedIcon] = useState('pill');

  const handleAddTime = () => {
    setTimes([...times, '']);
  };

  const handleRemoveTime = (index: number) => {
    setTimes(times.filter((_, i) => i !== index));
  };

  const handleTimeChange = (index: number, value: string) => {
    const newTimes = [...times];
    newTimes[index] = value;
    setTimes(newTimes);
  };

  const handleAddMedication = () => {
    if (!name || !dosage || !frequency || times.some(t => !t)) {
      toast.error('Please fill in all required fields');
      return;
    }

    const newMed: Medication = {
      id: Date.now().toString(),
      name,
      dosage,
      frequency,
      times: times.filter(t => t),
      instructions,
      icon: selectedIcon,
      importance: 'medium',
    };

    setMedications([...medications, newMed]);
    
    // Reset form
    setName('');
    setDosage('');
    setFrequency('');
    setTimes(['']);
    setInstructions('');
    setSelectedIcon('pill');
    setShowForm(false);
    
    toast.success('Medication added');
  };

  const handleRemoveMedication = (id: string) => {
    setMedications(medications.filter(m => m.id !== id));
  };

  const handleContinue = () => {
    if (medications.length === 0) {
      toast.error('Please add at least one medication');
      return;
    }
    onComplete(medications);
  };

  const handleSkip = () => {
    onComplete([]);
  };

  return (
    <div className="min-h-screen bg-white">
      {/* Header */}
      <div className="bg-blue-600 text-white p-4">
        <button onClick={onBack} className="mb-4">
          <ChevronLeft className="w-6 h-6" />
        </button>
        <h1 className="text-white">Your Medications</h1>
        <p className="text-blue-100">Add medications you need to take</p>
      </div>

      {/* Content */}
      <div className="p-6 space-y-4">
        {/* Medication List */}
        {medications.map((med) => {
          const IconComponent = medicationIcons.find(i => i.icon === med.icon)?.component || Pill;
          return (
            <Card key={med.id} className="p-4">
              <div className="flex items-start gap-3">
                <div className="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center flex-shrink-0">
                  <IconComponent className="w-6 h-6 text-blue-600" />
                </div>
                <div className="flex-1">
                  <h3>{med.name}</h3>
                  <p className="text-gray-600">{med.dosage} • {med.frequency}</p>
                  <p className="text-sm text-gray-500">Times: {med.times.join(', ')}</p>
                </div>
                <button 
                  onClick={() => handleRemoveMedication(med.id)}
                  className="text-gray-400 hover:text-red-600"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
            </Card>
          );
        })}

        {/* Add Medication Form */}
        {showForm ? (
          <Card className="p-4 space-y-4">
            <h3>Add Medication</h3>
            
            {/* Icon Selection */}
            <div className="space-y-2">
              <Label>Medication Type</Label>
              <div className="flex gap-2">
                {medicationIcons.map(({ icon, component: IconComponent }) => (
                  <button
                    key={icon}
                    onClick={() => setSelectedIcon(icon)}
                    className={`w-14 h-14 rounded-xl flex items-center justify-center ${
                      selectedIcon === icon 
                        ? 'bg-blue-600 text-white' 
                        : 'bg-gray-100 text-gray-600'
                    }`}
                  >
                    <IconComponent className="w-6 h-6" />
                  </button>
                ))}
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="med-name">Medication Name *</Label>
              <Input 
                id="med-name"
                placeholder="e.g., Aspirin"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="h-12"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="dosage">Dosage *</Label>
              <Input 
                id="dosage"
                placeholder="e.g., 100mg"
                value={dosage}
                onChange={(e) => setDosage(e.target.value)}
                className="h-12"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="frequency">Frequency *</Label>
              <Select value={frequency} onValueChange={setFrequency}>
                <SelectTrigger className="h-12">
                  <SelectValue placeholder="Select frequency" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Once daily">Once daily</SelectItem>
                  <SelectItem value="Twice daily">Twice daily</SelectItem>
                  <SelectItem value="Three times daily">Three times daily</SelectItem>
                  <SelectItem value="Four times daily">Four times daily</SelectItem>
                  <SelectItem value="As needed">As needed</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label>Times to Take *</Label>
              {times.map((time, index) => (
                <div key={index} className="flex gap-2">
                  <Input 
                    type="time"
                    value={time}
                    onChange={(e) => handleTimeChange(index, e.target.value)}
                    className="h-12"
                  />
                  {times.length > 1 && (
                    <Button
                      variant="outline"
                      size="icon"
                      onClick={() => handleRemoveTime(index)}
                      className="h-12 w-12"
                    >
                      <X className="w-4 h-4" />
                    </Button>
                  )}
                </div>
              ))}
              <Button
                variant="outline"
                size="sm"
                onClick={handleAddTime}
                className="w-full"
              >
                <Plus className="w-4 h-4 mr-2" />
                Add Time
              </Button>
            </div>

            <div className="space-y-2">
              <Label htmlFor="instructions">Instructions (Optional)</Label>
              <Input 
                id="instructions"
                placeholder="e.g., Take with food"
                value={instructions}
                onChange={(e) => setInstructions(e.target.value)}
                className="h-12"
              />
            </div>

            <div className="flex gap-2">
              <Button
                variant="outline"
                className="flex-1 h-12"
                onClick={() => setShowForm(false)}
              >
                Cancel
              </Button>
              <Button
                className="flex-1 h-12 bg-blue-600 hover:bg-blue-700"
                onClick={handleAddMedication}
              >
                Add
              </Button>
            </div>
          </Card>
        ) : (
          <Button
            variant="outline"
            className="w-full h-14 border-dashed border-2"
            onClick={() => setShowForm(true)}
          >
            <Plus className="w-5 h-5 mr-2" />
            Add Medication
          </Button>
        )}

        {/* Action Buttons */}
        <div className="pt-6 space-y-3">
          <Button 
            className="w-full h-14 bg-blue-600 hover:bg-blue-700"
            onClick={handleContinue}
            disabled={medications.length === 0}
          >
            Continue
          </Button>
          
          <Button 
            variant="ghost" 
            className="w-full h-14"
            onClick={handleSkip}
          >
            Skip for Now
          </Button>
        </div>

        <div className="flex justify-center gap-2 pt-2">
          <div className="w-2 h-2 bg-gray-300 rounded-full"></div>
          <div className="w-2 h-2 bg-blue-600 rounded-full"></div>
          <div className="w-2 h-2 bg-gray-300 rounded-full"></div>
          <div className="w-2 h-2 bg-gray-300 rounded-full"></div>
        </div>
      </div>
    </div>
  );
}
