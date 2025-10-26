import { useState } from 'react';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Badge } from '../ui/badge';
import { Home, Bell, MessageCircle, BarChart3, ChevronLeft, AlertTriangle, AlertCircle, Info, CheckCircle2, Phone, MessageSquare } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '../ui/dialog';
import { toast } from 'sonner@2.0.3';

type Props = {
  onNavigate: (screen: string) => void;
};

type Alert = {
  id: string;
  type: 'critical' | 'warning' | 'info';
  patient: string;
  title: string;
  message: string;
  medication?: string;
  time: string;
  resolved: boolean;
};

export default function AlertInbox({ onNavigate }: Props) {
  const [selectedAlert, setSelectedAlert] = useState<Alert | null>(null);
  const [alerts, setAlerts] = useState<Alert[]>([
    {
      id: '1',
      type: 'critical',
      patient: 'John Smith',
      title: 'Missed Critical Medication',
      message: 'Blood pressure medication missed for 2 consecutive doses',
      medication: 'Lisinopril 10mg',
      time: '2 hours ago',
      resolved: false,
    },
    {
      id: '2',
      type: 'warning',
      patient: 'John Smith',
      title: 'Low Adherence Alert',
      message: 'Adherence rate dropped below 80% this week',
      time: '5 hours ago',
      resolved: false,
    },
    {
      id: '3',
      type: 'info',
      patient: 'John Smith',
      title: 'Medication Schedule Change',
      message: 'Patient updated medication schedule',
      time: '1 day ago',
      resolved: false,
    },
    {
      id: '4',
      type: 'critical',
      patient: 'John Smith',
      title: 'Missed Morning Dose',
      message: 'Diabetes medication not taken',
      medication: 'Metformin 500mg',
      time: '2 days ago',
      resolved: true,
    },
  ]);

  const activeAlerts = alerts.filter(a => !a.resolved);
  const resolvedAlerts = alerts.filter(a => a.resolved);

  const handleResolve = (alertId: string) => {
    setAlerts(alerts.map(alert => 
      alert.id === alertId ? { ...alert, resolved: true } : alert
    ));
    setSelectedAlert(null);
    toast.success('Alert resolved');
  };

  const handleCallPatient = () => {
    toast.success('Calling patient...');
    setSelectedAlert(null);
  };

  const handleSendMessage = () => {
    onNavigate('messages');
  };

  const getAlertIcon = (type: string) => {
    switch (type) {
      case 'critical':
        return <AlertTriangle className="w-5 h-5 text-red-600" />;
      case 'warning':
        return <AlertCircle className="w-5 h-5 text-orange-600" />;
      default:
        return <Info className="w-5 h-5 text-blue-600" />;
    }
  };

  const getAlertBg = (type: string) => {
    switch (type) {
      case 'critical':
        return 'bg-red-100';
      case 'warning':
        return 'bg-orange-100';
      default:
        return 'bg-blue-100';
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 p-4">
        <div className="flex items-center gap-3 mb-2">
          <button onClick={() => onNavigate('family-dashboard')}>
            <ChevronLeft className="w-6 h-6" />
          </button>
          <h1>Alerts</h1>
        </div>
        <p className="text-gray-600 ml-9">{activeAlerts.length} active alert{activeAlerts.length !== 1 ? 's' : ''}</p>
      </div>

      {/* Content */}
      <div className="p-6 space-y-6">
        {/* Active Alerts */}
        <div>
          <h2 className="mb-4">Active Alerts</h2>
          {activeAlerts.length === 0 ? (
            <Card className="p-8 text-center">
              <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <CheckCircle2 className="w-8 h-8 text-green-600" />
              </div>
              <h3 className="mb-2">All Clear!</h3>
              <p className="text-gray-600">No active alerts at this time</p>
            </Card>
          ) : (
            <div className="space-y-3">
              {activeAlerts.map((alert) => (
                <Card 
                  key={alert.id} 
                  className="p-4 cursor-pointer hover:shadow-md transition-shadow"
                  onClick={() => setSelectedAlert(alert)}
                >
                  <div className="flex items-start gap-3">
                    <div className={`w-12 h-12 ${getAlertBg(alert.type)} rounded-xl flex items-center justify-center flex-shrink-0`}>
                      {getAlertIcon(alert.type)}
                    </div>
                    
                    <div className="flex-1">
                      <div className="flex items-start justify-between mb-1">
                        <div>
                          <div className="flex items-center gap-2 mb-1">
                            <h3>{alert.title}</h3>
                            {alert.type === 'critical' && (
                              <Badge variant="destructive" className="text-xs">Urgent</Badge>
                            )}
                          </div>
                          <p className="text-sm text-gray-600">{alert.patient}</p>
                        </div>
                      </div>
                      
                      <p className="text-sm text-gray-700 mb-2">{alert.message}</p>
                      
                      {alert.medication && (
                        <div className="text-sm bg-gray-50 px-3 py-2 rounded-lg mb-2">
                          <span className="text-gray-600">Medication: </span>
                          <span>{alert.medication}</span>
                        </div>
                      )}
                      
                      <p className="text-xs text-gray-500">{alert.time}</p>
                    </div>
                  </div>
                </Card>
              ))}
            </div>
          )}
        </div>

        {/* Resolved Alerts */}
        {resolvedAlerts.length > 0 && (
          <div>
            <h2 className="mb-4">Resolved</h2>
            <div className="space-y-3">
              {resolvedAlerts.map((alert) => (
                <Card 
                  key={alert.id} 
                  className="p-4 opacity-60"
                >
                  <div className="flex items-start gap-3">
                    <div className="w-12 h-12 bg-gray-100 rounded-xl flex items-center justify-center flex-shrink-0">
                      <CheckCircle2 className="w-5 h-5 text-gray-600" />
                    </div>
                    
                    <div className="flex-1">
                      <h3 className="line-through">{alert.title}</h3>
                      <p className="text-sm text-gray-600">{alert.patient}</p>
                      <p className="text-xs text-gray-500 mt-1">{alert.time}</p>
                    </div>
                  </div>
                </Card>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Alert Detail Dialog */}
      <Dialog open={!!selectedAlert} onOpenChange={() => setSelectedAlert(null)}>
        <DialogContent>
          <DialogHeader>
            <div className="flex items-start gap-3 mb-2">
              <div className={`w-12 h-12 ${getAlertBg(selectedAlert?.type || 'info')} rounded-xl flex items-center justify-center flex-shrink-0`}>
                {getAlertIcon(selectedAlert?.type || 'info')}
              </div>
              <div className="flex-1">
                <DialogTitle>{selectedAlert?.title}</DialogTitle>
                <p className="text-sm text-gray-600">{selectedAlert?.patient}</p>
              </div>
            </div>
          </DialogHeader>
          
          <DialogDescription className="space-y-4">
            <div>
              <p className="text-gray-700">{selectedAlert?.message}</p>
            </div>

            {selectedAlert?.medication && (
              <div className="bg-gray-50 p-3 rounded-lg">
                <p className="text-sm text-gray-600">Medication</p>
                <p>{selectedAlert.medication}</p>
              </div>
            )}

            {selectedAlert?.type === 'critical' && (
              <div className="bg-red-50 border border-red-200 p-3 rounded-lg">
                <p className="text-sm text-red-800">
                  ⚠️ This is a critical alert. Consider contacting the patient immediately.
                </p>
              </div>
            )}

            <div className="text-sm text-gray-500">
              {selectedAlert?.time}
            </div>
          </DialogDescription>

          <DialogFooter className="flex-col sm:flex-col gap-2">
            <div className="flex gap-2 w-full">
              <Button 
                variant="outline" 
                className="flex-1"
                onClick={handleCallPatient}
              >
                <Phone className="w-4 h-4 mr-2" />
                Call
              </Button>
              <Button 
                variant="outline" 
                className="flex-1"
                onClick={handleSendMessage}
              >
                <MessageSquare className="w-4 h-4 mr-2" />
                Message
              </Button>
            </div>
            <Button 
              className="w-full bg-purple-600 hover:bg-purple-700"
              onClick={() => selectedAlert && handleResolve(selectedAlert.id)}
            >
              Mark as Resolved
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Bottom Navigation */}
      <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 px-6 py-3">
        <div className="flex justify-around items-center max-w-md mx-auto">
          <button 
            onClick={() => onNavigate('family-dashboard')}
            className="flex flex-col items-center gap-1 text-gray-600"
          >
            <Home className="w-6 h-6" />
            <span className="text-xs">Home</span>
          </button>
          
          <button 
            onClick={() => onNavigate('alerts')}
            className="flex flex-col items-center gap-1 text-purple-600"
          >
            <Bell className="w-6 h-6" />
            <span className="text-xs">Alerts</span>
          </button>
          
          <button 
            onClick={() => onNavigate('messages')}
            className="flex flex-col items-center gap-1 text-gray-600"
          >
            <MessageCircle className="w-6 h-6" />
            <span className="text-xs">Messages</span>
          </button>
          
          <button 
            onClick={() => onNavigate('reports')}
            className="flex flex-col items-center gap-1 text-gray-600"
          >
            <BarChart3 className="w-6 h-6" />
            <span className="text-xs">Reports</span>
          </button>
        </div>
      </div>
    </div>
  );
}
