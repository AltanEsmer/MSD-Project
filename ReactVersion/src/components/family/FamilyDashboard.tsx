import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Badge } from '../ui/badge';
import { Progress } from '../ui/progress';
import { Home, Bell, MessageCircle, BarChart3, User, Plus, Phone, CheckCircle, AlertCircle, Clock } from 'lucide-react';
import { Patient } from '../../App';

type Props = {
  connectedPatients: Patient[];
  onNavigate: (screen: string) => void;
};

export default function FamilyDashboard({ connectedPatients, onNavigate }: Props) {
  // Mock data for demonstration
  const patientsWithStats = connectedPatients.map(patient => ({
    ...patient,
    adherenceRate: Math.floor(Math.random() * 30) + 70, // 70-100%
    todayTaken: Math.floor(Math.random() * 4) + 2,
    todayTotal: 6,
    lastUpdate: '2 hours ago',
    status: Math.random() > 0.3 ? 'good' : 'attention',
    missedDoses: Math.floor(Math.random() * 2),
  }));

  // Mock activity timeline
  const recentActivity = [
    {
      patient: connectedPatients[0]?.name || 'John Smith',
      action: 'Took morning medication',
      time: '8:30 AM',
      type: 'success',
    },
    {
      patient: connectedPatients[0]?.name || 'John Smith',
      action: 'Missed afternoon dose',
      time: '2:00 PM',
      type: 'warning',
    },
    {
      patient: connectedPatients[0]?.name || 'John Smith',
      action: 'Took evening medication',
      time: '6:45 PM',
      type: 'success',
    },
  ];

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* Header */}
      <div className="bg-purple-600 text-white p-6">
        <div className="flex justify-between items-start mb-4">
          <div>
            <h1 className="text-white">Family Dashboard</h1>
            <p className="text-purple-100">Monitoring {connectedPatients.length} {connectedPatients.length === 1 ? 'patient' : 'patients'}</p>
          </div>
          <button 
            onClick={() => onNavigate('patient-connection')}
            className="w-12 h-12 bg-white/20 rounded-full flex items-center justify-center"
          >
            <Plus className="w-6 h-6" />
          </button>
        </div>

        {/* Quick Stats */}
        <div className="grid grid-cols-2 gap-3">
          <div className="bg-white/10 rounded-lg p-3">
            <p className="text-purple-100 text-sm">Active Alerts</p>
            <p className="text-white">2</p>
          </div>
          <div className="bg-white/10 rounded-lg p-3">
            <p className="text-purple-100 text-sm">Avg. Adherence</p>
            <p className="text-white">92%</p>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="p-6 space-y-6">
        {/* Patient Cards */}
        <div>
          <div className="flex justify-between items-center mb-4">
            <h2>Your Patients</h2>
            <Button 
              variant="ghost" 
              size="sm"
              onClick={() => onNavigate('patient-connection')}
            >
              + Add Patient
            </Button>
          </div>

          {patientsWithStats.length === 0 ? (
            <Card className="p-8 text-center">
              <div className="w-16 h-16 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <User className="w-8 h-8 text-purple-600" />
              </div>
              <h3 className="mb-2">No patients connected</h3>
              <p className="text-gray-600 mb-4">
                Connect to a patient to start monitoring their medication adherence
              </p>
              <Button 
                onClick={() => onNavigate('patient-connection')}
                className="bg-purple-600 hover:bg-purple-700"
              >
                <Plus className="w-4 h-4 mr-2" />
                Connect to Patient
              </Button>
            </Card>
          ) : (
            <div className="space-y-3">
              {patientsWithStats.map((patient) => (
                <Card key={patient.id} className="p-4">
                  <div className="flex items-start gap-3">
                    <div className="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center flex-shrink-0">
                      <User className="w-6 h-6 text-purple-600" />
                    </div>
                    
                    <div className="flex-1">
                      <div className="flex items-start justify-between mb-2">
                        <div>
                          <h3>{patient.name}</h3>
                          <p className="text-sm text-gray-600">{patient.age} years • {patient.healthConditions[0]}</p>
                        </div>
                        <Badge variant={patient.status === 'good' ? "default" : "destructive"}>
                          {patient.status === 'good' ? '✓ Good' : '! Attention'}
                        </Badge>
                      </div>

                      <div className="space-y-2">
                        <div>
                          <div className="flex justify-between text-sm mb-1">
                            <span className="text-gray-600">Today's Progress</span>
                            <span>{patient.todayTaken}/{patient.todayTotal}</span>
                          </div>
                          <Progress value={(patient.todayTaken / patient.todayTotal) * 100} className="h-2" />
                        </div>

                        {patient.missedDoses > 0 && (
                          <div className="flex items-center gap-2 text-sm text-orange-600 bg-orange-50 px-3 py-2 rounded-lg">
                            <AlertCircle className="w-4 h-4" />
                            <span>{patient.missedDoses} dose(s) missed today</span>
                          </div>
                        )}

                        <div className="flex items-center justify-between pt-2">
                          <span className="text-sm text-gray-500">
                            <Clock className="w-4 h-4 inline mr-1" />
                            Updated {patient.lastUpdate}
                          </span>
                          <div className="flex gap-2">
                            <Button variant="outline" size="sm">
                              <MessageCircle className="w-4 h-4 mr-1" />
                              Message
                            </Button>
                            <Button variant="outline" size="sm">
                              <Phone className="w-4 h-4" />
                            </Button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </Card>
              ))}
            </div>
          )}
        </div>

        {/* Recent Activity */}
        {connectedPatients.length > 0 && (
          <div>
            <h2 className="mb-4">Recent Activity</h2>
            <Card className="p-4">
              <div className="space-y-4">
                {recentActivity.map((activity, idx) => (
                  <div key={idx} className="flex items-start gap-3">
                    <div className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 ${
                      activity.type === 'success' ? 'bg-green-100' : 'bg-orange-100'
                    }`}>
                      {activity.type === 'success' ? (
                        <CheckCircle className="w-4 h-4 text-green-600" />
                      ) : (
                        <AlertCircle className="w-4 h-4 text-orange-600" />
                      )}
                    </div>
                    <div className="flex-1">
                      <p className="text-sm">
                        <span>{activity.patient}</span>
                        <span className="text-gray-600"> {activity.action}</span>
                      </p>
                      <p className="text-xs text-gray-500">{activity.time}</p>
                    </div>
                  </div>
                ))}
              </div>
            </Card>
          </div>
        )}

        {/* Quick Actions */}
        {connectedPatients.length > 0 && (
          <div className="grid grid-cols-2 gap-3">
            <Card className="p-4">
              <button 
                onClick={() => onNavigate('alerts')}
                className="w-full text-left"
              >
                <div className="flex items-center gap-3 mb-2">
                  <div className="w-10 h-10 bg-red-100 rounded-lg flex items-center justify-center">
                    <Bell className="w-5 h-5 text-red-600" />
                  </div>
                </div>
                <h3>Alerts</h3>
                <p className="text-sm text-gray-600">2 new alerts</p>
              </button>
            </Card>

            <Card className="p-4">
              <button 
                onClick={() => onNavigate('reports')}
                className="w-full text-left"
              >
                <div className="flex items-center gap-3 mb-2">
                  <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                    <BarChart3 className="w-5 h-5 text-blue-600" />
                  </div>
                </div>
                <h3>Reports</h3>
                <p className="text-sm text-gray-600">View insights</p>
              </button>
            </Card>
          </div>
        )}
      </div>

      {/* Bottom Navigation */}
      <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 px-6 py-3">
        <div className="flex justify-around items-center max-w-md mx-auto">
          <button 
            onClick={() => onNavigate('family-dashboard')}
            className="flex flex-col items-center gap-1 text-purple-600"
          >
            <Home className="w-6 h-6" />
            <span className="text-xs">Home</span>
          </button>
          
          <button 
            onClick={() => onNavigate('alerts')}
            className="flex flex-col items-center gap-1 text-gray-600 relative"
          >
            <Bell className="w-6 h-6" />
            <span className="text-xs">Alerts</span>
            <div className="absolute top-0 right-2 w-2 h-2 bg-red-500 rounded-full"></div>
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
