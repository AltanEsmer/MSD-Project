import { useState } from 'react';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Card } from '../ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../ui/tabs';
import { ChevronLeft, QrCode, Mail, Hash } from 'lucide-react';
import { Patient } from '../../App';
import { toast } from 'sonner@2.0.3';

type Props = {
  onConnect: (patient: Patient) => void;
  onBack: () => void;
};

export default function PatientConnection({ onConnect, onBack }: Props) {
  const [connectionCode, setConnectionCode] = useState('');
  const [email, setEmail] = useState('');

  const handleConnectWithCode = () => {
    if (!connectionCode) {
      toast.error('Please enter a connection code');
      return;
    }

    // Mock connection
    const mockPatient: Patient = {
      id: Date.now().toString(),
      name: 'John Smith',
      age: '68',
      healthConditions: ['Hypertension', 'Diabetes'],
      emergencyContact: '+1 (555) 123-4567',
    };

    onConnect(mockPatient);
    toast.success('Connected to patient successfully');
  };

  const handleConnectWithEmail = () => {
    if (!email) {
      toast.error('Please enter an email address');
      return;
    }

    toast.success('Invitation sent! Waiting for patient approval...');
  };

  const handleScanQR = () => {
    // Mock QR code scan
    const mockPatient: Patient = {
      id: Date.now().toString(),
      name: 'Sarah Johnson',
      age: '72',
      healthConditions: ['Arthritis'],
      emergencyContact: '+1 (555) 987-6543',
    };

    onConnect(mockPatient);
    toast.success('Connected via QR code');
  };

  return (
    <div className="min-h-screen bg-white">
      {/* Header */}
      <div className="bg-purple-600 text-white p-4">
        <button onClick={onBack} className="mb-4">
          <ChevronLeft className="w-6 h-6" />
        </button>
        <h1 className="text-white">Connect to Patient</h1>
        <p className="text-purple-100">Choose a connection method</p>
      </div>

      {/* Content */}
      <div className="p-6">
        <Tabs defaultValue="code" className="w-full">
          <TabsList className="grid w-full grid-cols-3">
            <TabsTrigger value="qr">QR Code</TabsTrigger>
            <TabsTrigger value="code">Code</TabsTrigger>
            <TabsTrigger value="email">Email</TabsTrigger>
          </TabsList>

          {/* QR Code Tab */}
          <TabsContent value="qr" className="space-y-4 mt-6">
            <Card className="p-6">
              <div className="flex flex-col items-center text-center space-y-4">
                <div className="w-24 h-24 bg-purple-100 rounded-2xl flex items-center justify-center">
                  <QrCode className="w-12 h-12 text-purple-600" />
                </div>
                <div>
                  <h3 className="mb-2">Scan Patient's QR Code</h3>
                  <p className="text-gray-600">
                    Ask the patient to show their QR code from their profile settings
                  </p>
                </div>

                {/* Mock QR Scanner */}
                <div className="w-full aspect-square max-w-xs bg-gray-100 rounded-xl flex items-center justify-center border-2 border-dashed border-gray-300">
                  <div className="text-center text-gray-500">
                    <QrCode className="w-16 h-16 mx-auto mb-2 opacity-50" />
                    <p>Camera view</p>
                  </div>
                </div>

                <Button 
                  className="w-full h-14 bg-purple-600 hover:bg-purple-700"
                  onClick={handleScanQR}
                >
                  Simulate QR Scan
                </Button>
              </div>
            </Card>
          </TabsContent>

          {/* Connection Code Tab */}
          <TabsContent value="code" className="space-y-4 mt-6">
            <Card className="p-6">
              <div className="flex flex-col items-center text-center space-y-4">
                <div className="w-24 h-24 bg-blue-100 rounded-2xl flex items-center justify-center">
                  <Hash className="w-12 h-12 text-blue-600" />
                </div>
                <div>
                  <h3 className="mb-2">Enter Connection Code</h3>
                  <p className="text-gray-600">
                    Ask the patient for their 6-digit connection code
                  </p>
                </div>

                <div className="w-full space-y-2">
                  <Label htmlFor="code">Connection Code</Label>
                  <Input 
                    id="code"
                    placeholder="123-456"
                    value={connectionCode}
                    onChange={(e) => setConnectionCode(e.target.value)}
                    className="h-14 text-center text-xl tracking-widest"
                    maxLength={7}
                  />
                </div>

                <Button 
                  className="w-full h-14 bg-purple-600 hover:bg-purple-700"
                  onClick={handleConnectWithCode}
                >
                  Connect
                </Button>
              </div>
            </Card>

            <div className="p-4 bg-blue-50 rounded-lg">
              <p className="text-sm text-gray-700">
                💡 The patient can find their connection code in Settings → Family Connection
              </p>
            </div>
          </TabsContent>

          {/* Email Invitation Tab */}
          <TabsContent value="email" className="space-y-4 mt-6">
            <Card className="p-6">
              <div className="flex flex-col items-center text-center space-y-4">
                <div className="w-24 h-24 bg-green-100 rounded-2xl flex items-center justify-center">
                  <Mail className="w-12 h-12 text-green-600" />
                </div>
                <div>
                  <h3 className="mb-2">Send Email Invitation</h3>
                  <p className="text-gray-600">
                    Send a connection request to the patient's email
                  </p>
                </div>

                <div className="w-full space-y-2">
                  <Label htmlFor="email">Patient's Email</Label>
                  <Input 
                    id="email"
                    type="email"
                    placeholder="patient@example.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="h-14"
                  />
                </div>

                <Button 
                  className="w-full h-14 bg-purple-600 hover:bg-purple-700"
                  onClick={handleConnectWithEmail}
                >
                  Send Invitation
                </Button>
              </div>
            </Card>

            <div className="p-4 bg-blue-50 rounded-lg">
              <p className="text-sm text-gray-700">
                💡 The patient will need to approve your connection request
              </p>
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}
