import { useState } from 'react';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Switch } from '../ui/switch';
import { Slider } from '../ui/slider';
import { Badge } from '../ui/badge';
import { Home, Pill, History, User, ChevronLeft, ChevronRight, Edit2, Users, Bell, Palette, Accessibility } from 'lucide-react';
import { Patient } from '../../App';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '../ui/dialog';

type Props = {
  patient: Patient;
  onNavigate: (screen: string) => void;
  onUpdateProfile: (patient: Patient) => void;
  onSwitchToFamily: () => void;
};

export default function PatientProfile({ patient, onNavigate, onUpdateProfile, onSwitchToFamily }: Props) {
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [editedName, setEditedName] = useState(patient.name);
  const [editedAge, setEditedAge] = useState(patient.age);
  const [editedContact, setEditedContact] = useState(patient.emergencyContact);

  // Settings states
  const [textSize, setTextSize] = useState(16);
  const [highContrast, setHighContrast] = useState(false);
  const [voiceGuidance, setVoiceGuidance] = useState(false);
  const [simplifiedMode, setSimplifiedMode] = useState(false);
  const [notifications, setNotifications] = useState(true);
  const [sound, setSound] = useState(true);

  const handleSaveProfile = () => {
    onUpdateProfile({
      ...patient,
      name: editedName,
      age: editedAge,
      emergencyContact: editedContact,
    });
    setEditDialogOpen(false);
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* Header */}
      <div className="bg-blue-600 text-white p-6 pb-12">
        <button onClick={() => onNavigate('dashboard')} className="mb-4">
          <ChevronLeft className="w-6 h-6" />
        </button>
        
        <div className="flex items-center gap-4">
          <div className="w-20 h-20 bg-white/20 rounded-full flex items-center justify-center text-white">
            <User className="w-10 h-10" />
          </div>
          <div className="flex-1">
            <h1 className="text-white">{patient.name}</h1>
            <p className="text-blue-100">{patient.age} years old</p>
          </div>
          <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
            <DialogTrigger asChild>
              <Button variant="ghost" size="icon" className="text-white">
                <Edit2 className="w-5 h-5" />
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Edit Profile</DialogTitle>
              </DialogHeader>
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="edit-name">Full Name</Label>
                  <Input 
                    id="edit-name"
                    value={editedName}
                    onChange={(e) => setEditedName(e.target.value)}
                    className="h-12"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="edit-age">Age</Label>
                  <Input 
                    id="edit-age"
                    type="number"
                    value={editedAge}
                    onChange={(e) => setEditedAge(e.target.value)}
                    className="h-12"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="edit-contact">Emergency Contact</Label>
                  <Input 
                    id="edit-contact"
                    value={editedContact}
                    onChange={(e) => setEditedContact(e.target.value)}
                    className="h-12"
                  />
                </div>
                <Button 
                  className="w-full h-12 bg-blue-600 hover:bg-blue-700"
                  onClick={handleSaveProfile}
                >
                  Save Changes
                </Button>
              </div>
            </DialogContent>
          </Dialog>
        </div>
      </div>

      {/* Content */}
      <div className="p-6 -mt-6 space-y-4">
        {/* Personal Information */}
        <Card className="p-4">
          <h3 className="mb-3">Personal Information</h3>
          <div className="space-y-3">
            {patient.healthConditions.length > 0 && (
              <div>
                <p className="text-sm text-gray-600 mb-2">Health Conditions</p>
                <div className="flex flex-wrap gap-2">
                  {patient.healthConditions.map((condition, idx) => (
                    <Badge key={idx} variant="secondary">
                      {condition}
                    </Badge>
                  ))}
                </div>
              </div>
            )}
            
            {patient.emergencyContact && (
              <div>
                <p className="text-sm text-gray-600">Emergency Contact</p>
                <p>{patient.emergencyContact}</p>
              </div>
            )}
          </div>
        </Card>

        {/* Notifications Settings */}
        <Card className="p-4">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
              <Bell className="w-5 h-5 text-blue-600" />
            </div>
            <h3>Notifications</h3>
          </div>
          
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <Label htmlFor="notif-enabled">Medication Reminders</Label>
                <p className="text-sm text-gray-600">Push notifications for doses</p>
              </div>
              <Switch 
                id="notif-enabled"
                checked={notifications}
                onCheckedChange={setNotifications}
              />
            </div>
            
            <div className="flex items-center justify-between">
              <div>
                <Label htmlFor="sound-enabled">Sound & Vibration</Label>
                <p className="text-sm text-gray-600">Alert sounds</p>
              </div>
              <Switch 
                id="sound-enabled"
                checked={sound}
                onCheckedChange={setSound}
              />
            </div>
          </div>
        </Card>

        {/* Accessibility Settings */}
        <Card className="p-4">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
              <Accessibility className="w-5 h-5 text-purple-600" />
            </div>
            <h3>Accessibility</h3>
          </div>
          
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Text Size</Label>
              <div className="flex items-center gap-3">
                <span className="text-sm">A</span>
                <Slider 
                  value={[textSize]}
                  onValueChange={(val) => setTextSize(val[0])}
                  min={12}
                  max={24}
                  step={1}
                  className="flex-1"
                />
                <span className="text-xl">A</span>
              </div>
            </div>
            
            <div className="flex items-center justify-between">
              <Label htmlFor="high-contrast">High Contrast Mode</Label>
              <Switch 
                id="high-contrast"
                checked={highContrast}
                onCheckedChange={setHighContrast}
              />
            </div>
            
            <div className="flex items-center justify-between">
              <Label htmlFor="voice-guidance">Voice Guidance</Label>
              <Switch 
                id="voice-guidance"
                checked={voiceGuidance}
                onCheckedChange={setVoiceGuidance}
              />
            </div>
            
            <div className="flex items-center justify-between">
              <Label htmlFor="simplified">Simplified Interface</Label>
              <Switch 
                id="simplified"
                checked={simplifiedMode}
                onCheckedChange={setSimplifiedMode}
              />
            </div>
          </div>
        </Card>

        {/* Family Connection */}
        <Card className="p-4">
          <button 
            onClick={onSwitchToFamily}
            className="w-full flex items-center justify-between"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                <Users className="w-5 h-5 text-green-600" />
              </div>
              <div className="text-left">
                <h3>Family Caregivers</h3>
                <p className="text-sm text-gray-600">Manage connected family members</p>
              </div>
            </div>
            <ChevronRight className="w-5 h-5 text-gray-400" />
          </button>
        </Card>

        {/* App Info */}
        <Card className="p-4 space-y-3">
          <button className="w-full flex items-center justify-between text-left">
            <span>About MediCare</span>
            <ChevronRight className="w-5 h-5 text-gray-400" />
          </button>
          
          <button className="w-full flex items-center justify-between text-left">
            <span>Privacy Policy</span>
            <ChevronRight className="w-5 h-5 text-gray-400" />
          </button>
          
          <button className="w-full flex items-center justify-between text-left">
            <span>Terms of Service</span>
            <ChevronRight className="w-5 h-5 text-gray-400" />
          </button>
          
          <div className="pt-3 border-t border-gray-200">
            <p className="text-sm text-gray-600">Version 1.0.0</p>
          </div>
        </Card>
      </div>

      {/* Bottom Navigation */}
      <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 px-6 py-3">
        <div className="flex justify-around items-center max-w-md mx-auto">
          <button 
            onClick={() => onNavigate('dashboard')}
            className="flex flex-col items-center gap-1 text-gray-600"
          >
            <Home className="w-6 h-6" />
            <span className="text-xs">Home</span>
          </button>
          
          <button 
            onClick={() => onNavigate('medications')}
            className="flex flex-col items-center gap-1 text-gray-600"
          >
            <Pill className="w-6 h-6" />
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
            className="flex flex-col items-center gap-1 text-blue-600"
          >
            <User className="w-6 h-6" />
            <span className="text-xs">Profile</span>
          </button>
        </div>
      </div>
    </div>
  );
}
