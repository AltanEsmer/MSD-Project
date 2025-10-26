import { Button } from '../ui/button';
import { Users, Heart, Bell, TrendingUp } from 'lucide-react';

type Props = {
  onGetStarted: () => void;
  onSwitchToPatient: () => void;
};

export default function FamilyWelcome({ onGetStarted, onSwitchToPatient }: Props) {
  return (
    <div className="min-h-screen bg-gradient-to-b from-purple-50 to-white flex flex-col items-center justify-center p-6">
      <div className="max-w-md w-full text-center space-y-8">
        {/* Logo */}
        <div className="flex justify-center">
          <div className="w-24 h-24 bg-purple-600 rounded-3xl flex items-center justify-center shadow-lg">
            <Users className="w-12 h-12 text-white" />
          </div>
        </div>

        {/* App Name */}
        <div className="space-y-2">
          <h1 className="text-purple-600">MediCare Family</h1>
          <p className="text-gray-600">Support your loved ones' health journey</p>
        </div>

        {/* Features */}
        <div className="space-y-4 pt-4">
          <div className="flex items-start gap-3 text-left">
            <div className="w-10 h-10 bg-purple-100 rounded-full flex items-center justify-center flex-shrink-0">
              <Bell className="w-5 h-5 text-purple-600" />
            </div>
            <div>
              <h3>Real-time Alerts</h3>
              <p className="text-gray-600">Get notified when doses are missed</p>
            </div>
          </div>
          
          <div className="flex items-start gap-3 text-left">
            <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
              <TrendingUp className="w-5 h-5 text-blue-600" />
            </div>
            <div>
              <h3>Track Progress</h3>
              <p className="text-gray-600">Monitor adherence trends and patterns</p>
            </div>
          </div>
          
          <div className="flex items-start gap-3 text-left">
            <div className="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center flex-shrink-0">
              <Heart className="w-5 h-5 text-green-600" />
            </div>
            <div>
              <h3>Stay Connected</h3>
              <p className="text-gray-600">Send encouragement and reminders</p>
            </div>
          </div>
        </div>

        {/* CTA Buttons */}
        <div className="space-y-3 pt-6">
          <Button 
            className="w-full h-14 bg-purple-600 hover:bg-purple-700" 
            onClick={onGetStarted}
          >
            Connect to Patient
          </Button>
          
          <Button 
            variant="outline" 
            className="w-full h-14"
            onClick={onSwitchToPatient}
          >
            I'm a Patient
          </Button>
        </div>

        <p className="text-gray-500 text-sm pt-4">
          By continuing, you agree to our Terms of Service and Privacy Policy
        </p>
      </div>
    </div>
  );
}
