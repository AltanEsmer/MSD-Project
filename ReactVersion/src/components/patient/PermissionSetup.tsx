import { useState } from 'react';
import { Button } from '../ui/button';
import { Switch } from '../ui/switch';
import { Label } from '../ui/label';
import { Card } from '../ui/card';
import { ChevronLeft, Bell, Volume2, Shield, AlertTriangle } from 'lucide-react';

type Props = {
  onComplete: () => void;
  onBack: () => void;
};

export default function PermissionSetup({ onComplete, onBack }: Props) {
  const [notifications, setNotifications] = useState(false);
  const [sound, setSound] = useState(true);
  const [dataSharing, setDataSharing] = useState(false);
  const [emergencyAlerts, setEmergencyAlerts] = useState(true);

  return (
    <div className="min-h-screen bg-white">
      {/* Header */}
      <div className="bg-blue-600 text-white p-4">
        <button onClick={onBack} className="mb-4">
          <ChevronLeft className="w-6 h-6" />
        </button>
        <h1 className="text-white">Permissions</h1>
        <p className="text-blue-100">Help us help you stay on track</p>
      </div>

      {/* Content */}
      <div className="p-6 space-y-4">
        {/* Notifications */}
        <Card className="p-4">
          <div className="flex items-start gap-4">
            <div className="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center flex-shrink-0">
              <Bell className="w-6 h-6 text-blue-600" />
            </div>
            <div className="flex-1">
              <div className="flex items-center justify-between mb-2">
                <Label htmlFor="notifications">Notification Reminders</Label>
                <Switch 
                  id="notifications"
                  checked={notifications}
                  onCheckedChange={setNotifications}
                />
              </div>
              <p className="text-sm text-gray-600">
                Get reminders when it's time to take your medications
              </p>
              {notifications && (
                <div className="mt-3 p-3 bg-green-50 rounded-lg">
                  <p className="text-sm text-green-700">
                    ✓ You'll receive timely reminders for your medications
                  </p>
                </div>
              )}
            </div>
          </div>
        </Card>

        {/* Sound Settings */}
        <Card className="p-4">
          <div className="flex items-start gap-4">
            <div className="w-12 h-12 bg-purple-100 rounded-xl flex items-center justify-center flex-shrink-0">
              <Volume2 className="w-6 h-6 text-purple-600" />
            </div>
            <div className="flex-1">
              <div className="flex items-center justify-between mb-2">
                <Label htmlFor="sound">Sound & Vibration</Label>
                <Switch 
                  id="sound"
                  checked={sound}
                  onCheckedChange={setSound}
                />
              </div>
              <p className="text-sm text-gray-600">
                Enable sounds and vibration for reminders
              </p>
            </div>
          </div>
        </Card>

        {/* Data Sharing */}
        <Card className="p-4">
          <div className="flex items-start gap-4">
            <div className="w-12 h-12 bg-green-100 rounded-xl flex items-center justify-center flex-shrink-0">
              <Shield className="w-6 h-6 text-green-600" />
            </div>
            <div className="flex-1">
              <div className="flex items-center justify-between mb-2">
                <Label htmlFor="data-sharing">Data Sharing</Label>
                <Switch 
                  id="data-sharing"
                  checked={dataSharing}
                  onCheckedChange={setDataSharing}
                />
              </div>
              <p className="text-sm text-gray-600">
                Share your adherence data with connected family members
              </p>
            </div>
          </div>
        </Card>

        {/* Emergency Alerts */}
        <Card className="p-4">
          <div className="flex items-start gap-4">
            <div className="w-12 h-12 bg-red-100 rounded-xl flex items-center justify-center flex-shrink-0">
              <AlertTriangle className="w-6 h-6 text-red-600" />
            </div>
            <div className="flex-1">
              <div className="flex items-center justify-between mb-2">
                <Label htmlFor="emergency">Emergency Alerts</Label>
                <Switch 
                  id="emergency"
                  checked={emergencyAlerts}
                  onCheckedChange={setEmergencyAlerts}
                />
              </div>
              <p className="text-sm text-gray-600">
                Alert your emergency contact if medications are missed
              </p>
            </div>
          </div>
        </Card>

        {/* Info Box */}
        <div className="p-4 bg-blue-50 rounded-lg">
          <p className="text-sm text-gray-700">
            💡 You can change these settings anytime in your profile
          </p>
        </div>

        {/* Action Buttons */}
        <div className="pt-6 space-y-3">
          <Button 
            className="w-full h-14 bg-blue-600 hover:bg-blue-700"
            onClick={onComplete}
          >
            Complete Setup
          </Button>
        </div>

        <div className="flex justify-center gap-2 pt-2">
          <div className="w-2 h-2 bg-gray-300 rounded-full"></div>
          <div className="w-2 h-2 bg-gray-300 rounded-full"></div>
          <div className="w-2 h-2 bg-gray-300 rounded-full"></div>
          <div className="w-2 h-2 bg-blue-600 rounded-full"></div>
        </div>
      </div>
    </div>
  );
}
