import { useState } from 'react';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Switch } from '../ui/switch';
import { Badge } from '../ui/badge';
import { ChevronLeft, User } from 'lucide-react';
import { Patient } from '../../App';

type Props = {
  onComplete: (patient: Patient) => void;
  onBack: () => void;
};

const commonConditions = [
  'Diabetes',
  'Hypertension',
  'Heart Disease',
  'Asthma',
  'Arthritis',
  'High Cholesterol',
  'Thyroid',
  'Depression',
];

export default function ProfileSetup({ onComplete, onBack }: Props) {
  const [name, setName] = useState('');
  const [age, setAge] = useState('');
  const [emergencyContact, setEmergencyContact] = useState('');
  const [selectedConditions, setSelectedConditions] = useState<string[]>([]);
  const [dataSharing, setDataSharing] = useState(false);

  const toggleCondition = (condition: string) => {
    setSelectedConditions(prev => 
      prev.includes(condition) 
        ? prev.filter(c => c !== condition)
        : [...prev, condition]
    );
  };

  const handleContinue = () => {
    if (!name || !age) return;
    
    const patient: Patient = {
      id: Date.now().toString(),
      name,
      age,
      healthConditions: selectedConditions,
      emergencyContact,
    };
    
    onComplete(patient);
  };

  return (
    <div className="min-h-screen bg-white">
      {/* Header */}
      <div className="bg-blue-600 text-white p-4 pb-8">
        <button onClick={onBack} className="mb-4">
          <ChevronLeft className="w-6 h-6" />
        </button>
        <h1 className="text-white">Your Profile</h1>
        <p className="text-blue-100">Tell us a bit about yourself</p>
      </div>

      {/* Form */}
      <div className="p-6 space-y-6 -mt-4 bg-white rounded-t-3xl relative">
        <div className="flex justify-center -mt-12 mb-4">
          <div className="w-20 h-20 bg-blue-600 rounded-full flex items-center justify-center shadow-lg border-4 border-white">
            <User className="w-10 h-10 text-white" />
          </div>
        </div>

        {/* Full Name */}
        <div className="space-y-2">
          <Label htmlFor="name">Full Name *</Label>
          <Input 
            id="name"
            placeholder="Enter your full name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="h-12"
          />
        </div>

        {/* Age */}
        <div className="space-y-2">
          <Label htmlFor="age">Age *</Label>
          <Input 
            id="age"
            type="number"
            placeholder="Enter your age"
            value={age}
            onChange={(e) => setAge(e.target.value)}
            className="h-12"
          />
        </div>

        {/* Health Conditions */}
        <div className="space-y-2">
          <Label>Health Conditions (Optional)</Label>
          <p className="text-sm text-gray-600">Select any that apply</p>
          <div className="flex flex-wrap gap-2 pt-2">
            {commonConditions.map((condition) => (
              <Badge
                key={condition}
                variant={selectedConditions.includes(condition) ? "default" : "outline"}
                className="cursor-pointer h-9 px-4"
                onClick={() => toggleCondition(condition)}
              >
                {condition}
              </Badge>
            ))}
          </div>
        </div>

        {/* Emergency Contact */}
        <div className="space-y-2">
          <Label htmlFor="emergency">Emergency Contact (Optional)</Label>
          <Input 
            id="emergency"
            type="tel"
            placeholder="Phone number"
            value={emergencyContact}
            onChange={(e) => setEmergencyContact(e.target.value)}
            className="h-12"
          />
        </div>

        {/* Data Sharing */}
        <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
          <div className="flex-1 pr-4">
            <Label htmlFor="data-sharing">Share data with caregivers</Label>
            <p className="text-sm text-gray-600">Allow family members to view your medication adherence</p>
          </div>
          <Switch 
            id="data-sharing"
            checked={dataSharing}
            onCheckedChange={setDataSharing}
          />
        </div>

        {/* Continue Button */}
        <div className="pt-4">
          <Button 
            className="w-full h-14 bg-blue-600 hover:bg-blue-700"
            onClick={handleContinue}
            disabled={!name || !age}
          >
            Continue
          </Button>
        </div>

        <div className="flex justify-center gap-2 pt-2">
          <div className="w-2 h-2 bg-blue-600 rounded-full"></div>
          <div className="w-2 h-2 bg-gray-300 rounded-full"></div>
          <div className="w-2 h-2 bg-gray-300 rounded-full"></div>
          <div className="w-2 h-2 bg-gray-300 rounded-full"></div>
        </div>
      </div>
    </div>
  );
}
