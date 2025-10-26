import { useState, useEffect } from 'react';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';
import { Textarea } from '../ui/textarea';
import { ChevronLeft, Plus, X, Pill, Droplet, Syringe, Tablets } from 'lucide-react';
import { Medication } from '../../App';
import { toast } from 'sonner@2.0.3';
import {
  Select as RadixSelect,
  SelectContent as RadixSelectContent,
  SelectItem as RadixSelectItem,
  SelectTrigger as RadixSelectTrigger,
  SelectValue as RadixSelectValue,
} from '../ui/select';

type Props = {
  medication: Medication | null;
  onSave: (medication: Medication) => void;
  onCancel: () => void;
};

const medicationIcons = [
  { name: 'Pill', icon: 'pill', component: Pill },
  { name: 'Tablet', icon: 'tablets', component: Tablets },
  { name: 'Liquid', icon: 'droplet', component: Droplet },
  { name: 'Injection', icon: 'syringe', component: Syringe },
];

export default function AddEditMedication({ medication, onSave, onCancel }: Props) {
  const [name, setName] = useState('');
  const [dosage, setDosage] = useState('');
  const [frequency, setFrequency] = useState('');
  const [times, setTimes] = useState<string[]>(['']);
  const [instructions, setInstructions] = useState('');
  const [selectedIcon, setSelectedIcon] = useState('pill');
  const [importance, setImportance] = useState<'high' | 'medium' | 'low'>('medium');

  useEffect(() => {
    if (medication) {
      setName(medication.name);
      setDosage(medication.dosage);
      setFrequency(medication.frequency);
      setTimes(medication.times);
      setInstructions(medication.instructions);
      setSelectedIcon(medication.icon);
      setImportance(medication.importance);
    }
  }, [medication]);

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

  const handleSave = () => {
    if (!name || !dosage || !frequency || times.some(t => !t)) {
      toast.error('Please fill in all required fields');
      return;
    }

    const savedMed: Medication = {
      id: medication?.id || Date.now().toString(),
      name,
      dosage,
      frequency,
      times: times.filter(t => t),
      instructions,
      icon: selectedIcon,
      importance,
    };

    onSave(savedMed);
    toast.success(medication ? 'Medication updated' : 'Medication added');
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 p-4">
        <div className="flex items-center gap-3">
          <button onClick={onCancel}>
            <ChevronLeft className="w-6 h-6" />
          </button>
          <h1>{medication ? 'Edit Medication' : 'Add Medication'}</h1>
        </div>
      </div>

      {/* Form */}
      <div className="p-6 space-y-6">
        {/* Medication Type / Icon */}
        <div className="space-y-2">
          <Label>Medication Type</Label>
          <div className="flex gap-2">
            {medicationIcons.map(({ icon, component: IconComponent }) => (
              <button
                key={icon}
                onClick={() => setSelectedIcon(icon)}
                className={`flex-1 h-16 rounded-xl flex flex-col items-center justify-center gap-1 ${
                  selectedIcon === icon 
                    ? 'bg-blue-600 text-white' 
                    : 'bg-white text-gray-600 border border-gray-200'
                }`}
              >
                <IconComponent className="w-6 h-6" />
              </button>
            ))}
          </div>
        </div>

        {/* Basic Information */}
        <div className="bg-white rounded-lg p-4 space-y-4">
          <h3>Basic Information</h3>
          
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
              placeholder="e.g., 100mg, 2 tablets"
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
                <SelectItem value="Every 4 hours">Every 4 hours</SelectItem>
                <SelectItem value="Every 6 hours">Every 6 hours</SelectItem>
                <SelectItem value="Every 8 hours">Every 8 hours</SelectItem>
                <SelectItem value="As needed">As needed</SelectItem>
                <SelectItem value="Weekly">Weekly</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        {/* Schedule */}
        <div className="bg-white rounded-lg p-4 space-y-4">
          <h3>Schedule</h3>
          
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
              Add Another Time
            </Button>
          </div>
        </div>

        {/* Additional Details */}
        <div className="bg-white rounded-lg p-4 space-y-4">
          <h3>Additional Details</h3>
          
          <div className="space-y-2">
            <Label htmlFor="instructions">Instructions</Label>
            <Textarea 
              id="instructions"
              placeholder="e.g., Take with food, Avoid alcohol"
              value={instructions}
              onChange={(e) => setInstructions(e.target.value)}
              className="min-h-20"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="importance">Importance Level</Label>
            <Select value={importance} onValueChange={(val) => setImportance(val as 'high' | 'medium' | 'low')}>
              <SelectTrigger className="h-12">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="high">🔴 High - Critical medication</SelectItem>
                <SelectItem value="medium">🟡 Medium - Important</SelectItem>
                <SelectItem value="low">🟢 Low - As needed</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex gap-3 pt-4">
          <Button
            variant="outline"
            className="flex-1 h-14"
            onClick={onCancel}
          >
            Cancel
          </Button>
          <Button
            className="flex-1 h-14 bg-blue-600 hover:bg-blue-700"
            onClick={handleSave}
          >
            {medication ? 'Save Changes' : 'Add Medication'}
          </Button>
        </div>
      </div>
    </div>
  );
}
