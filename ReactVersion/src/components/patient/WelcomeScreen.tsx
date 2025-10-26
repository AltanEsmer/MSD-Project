import { Button } from '../ui/button';
import { Pill, Heart, Users } from 'lucide-react';

type Props = {
  onGetStarted: () => void;
  onSwitchToFamily: () => void;
};

export default function WelcomeScreen({ onGetStarted, onSwitchToFamily }: Props) {
  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white flex flex-col items-center justify-center p-6">
      <div className="max-w-md w-full text-center space-y-8">
        {/* Logo */}
        <div className="flex justify-center">
          <div className="w-24 h-24 bg-blue-600 rounded-3xl flex items-center justify-center shadow-lg">
            <Pill className="w-12 h-12 text-white" />
          </div>
        </div>

        {/* App Name */}
        <div className="space-y-2">
          <h1 className="text-blue-600">MediCare</h1>
          <p className="text-gray-600">Never miss a dose. Stay healthy, stay on track.</p>
        </div>

        {/* Features */}
        <div className="space-y-4 pt-4">
          <div className="flex items-start gap-3 text-left">
            <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
              <Pill className="w-5 h-5 text-blue-600" />
            </div>
            <div>
              <h3>Easy Medication Tracking</h3>
              <p className="text-gray-600">Simple reminders for all your medications</p>
            </div>
          </div>
          
          <div className="flex items-start gap-3 text-left">
            <div className="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center flex-shrink-0">
              <Heart className="w-5 h-5 text-green-600" />
            </div>
            <div>
              <h3>Health Progress</h3>
              <p className="text-gray-600">Monitor your adherence and streaks</p>
            </div>
          </div>
          
          <div className="flex items-start gap-3 text-left">
            <div className="w-10 h-10 bg-purple-100 rounded-full flex items-center justify-center flex-shrink-0">
              <Users className="w-5 h-5 text-purple-600" />
            </div>
            <div>
              <h3>Family Connection</h3>
              <p className="text-gray-600">Keep loved ones informed and supportive</p>
            </div>
          </div>
        </div>

        {/* CTA Buttons */}
        <div className="space-y-3 pt-6">
          <Button 
            className="w-full h-14 bg-blue-600 hover:bg-blue-700" 
            onClick={onGetStarted}
          >
            Get Started
          </Button>
          
          <Button 
            variant="outline" 
            className="w-full h-14"
            onClick={onSwitchToFamily}
          >
            I'm a Family Caregiver
          </Button>
        </div>

        <p className="text-gray-500 text-sm pt-4">
          By continuing, you agree to our Terms of Service and Privacy Policy
        </p>
      </div>
    </div>
  );
}
