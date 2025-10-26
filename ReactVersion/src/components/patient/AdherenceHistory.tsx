import { useState } from 'react';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Badge } from '../ui/badge';
import { Calendar } from '../ui/calendar';
import { Home, Pill as PillIcon, History, User, ChevronLeft, Download, TrendingUp, Calendar as CalendarIcon } from 'lucide-react';
import { Medication, AdherenceRecord } from '../../App';
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { Popover, PopoverContent, PopoverTrigger } from '../ui/popover';

type Props = {
  medications: Medication[];
  adherenceRecords: AdherenceRecord[];
  onNavigate: (screen: string) => void;
};

export default function AdherenceHistory({ medications, adherenceRecords, onNavigate }: Props) {
  const [selectedDate, setSelectedDate] = useState<Date>(new Date());
  const [viewMode, setViewMode] = useState<'week' | 'month'>('week');

  // Calculate overall adherence percentage
  const totalDoses = medications.reduce((acc, med) => acc + med.times.length, 0) * 30; // Last 30 days
  const takenDoses = adherenceRecords.filter(r => r.taken).length;
  const adherencePercentage = totalDoses > 0 ? Math.round((takenDoses / totalDoses) * 100) : 0;

  // Calculate streak
  const currentStreak = 7; // Simplified - would calculate based on actual data

  // Generate weekly data for chart
  const weeklyData = [
    { day: 'Mon', adherence: 100 },
    { day: 'Tue', adherence: 85 },
    { day: 'Wed', adherence: 100 },
    { day: 'Thu', adherence: 90 },
    { day: 'Fri', adherence: 100 },
    { day: 'Sat', adherence: 75 },
    { day: 'Sun', adherence: 95 },
  ];

  // Generate monthly trend data
  const monthlyData = [
    { week: 'Week 1', adherence: 92 },
    { week: 'Week 2', adherence: 88 },
    { week: 'Week 3', adherence: 95 },
    { week: 'Week 4', adherence: 90 },
  ];

  // Get medication-specific adherence
  const medicationAdherence = medications.map(med => {
    const medRecords = adherenceRecords.filter(r => r.medicationId === med.id);
    const taken = medRecords.filter(r => r.taken).length;
    const total = medRecords.length || 1;
    return {
      name: med.name,
      percentage: Math.round((taken / total) * 100),
    };
  });

  const handleExport = () => {
    // Simplified export functionality
    alert('Export functionality would download a PDF or CSV report');
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 p-4">
        <div className="flex items-center gap-3">
          <button onClick={() => onNavigate('dashboard')}>
            <ChevronLeft className="w-6 h-6" />
          </button>
          <div className="flex-1">
            <h1>Adherence History</h1>
          </div>
          <Button variant="outline" size="sm" onClick={handleExport}>
            <Download className="w-4 h-4 mr-2" />
            Export
          </Button>
        </div>
      </div>

      {/* Content */}
      <div className="p-6 space-y-6">
        {/* Summary Cards */}
        <div className="grid grid-cols-2 gap-3">
          <Card className="p-4">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                <TrendingUp className="w-6 h-6 text-green-600" />
              </div>
              <div>
                <p className="text-sm text-gray-600">30-Day Rate</p>
                <p className="text-green-600">{adherencePercentage}%</p>
              </div>
            </div>
          </Card>

          <Card className="p-4">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center">
                <span className="text-orange-600">🔥</span>
              </div>
              <div>
                <p className="text-sm text-gray-600">Current Streak</p>
                <p className="text-orange-600">{currentStreak} days</p>
              </div>
            </div>
          </Card>
        </div>

        {/* View Toggle */}
        <div className="flex gap-2 bg-gray-100 p-1 rounded-lg">
          <button
            onClick={() => setViewMode('week')}
            className={`flex-1 py-2 rounded-md ${
              viewMode === 'week' ? 'bg-white shadow-sm' : ''
            }`}
          >
            Week
          </button>
          <button
            onClick={() => setViewMode('month')}
            className={`flex-1 py-2 rounded-md ${
              viewMode === 'month' ? 'bg-white shadow-sm' : ''
            }`}
          >
            Month
          </button>
        </div>

        {/* Adherence Chart */}
        <Card className="p-4">
          <h3 className="mb-4">{viewMode === 'week' ? 'Weekly' : 'Monthly'} Adherence</h3>
          <ResponsiveContainer width="100%" height={200}>
            {viewMode === 'week' ? (
              <BarChart data={weeklyData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="day" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="adherence" fill="#3b82f6" radius={[8, 8, 0, 0]} />
              </BarChart>
            ) : (
              <LineChart data={monthlyData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="week" />
                <YAxis />
                <Tooltip />
                <Line type="monotone" dataKey="adherence" stroke="#3b82f6" strokeWidth={2} />
              </LineChart>
            )}
          </ResponsiveContainer>
        </Card>

        {/* Calendar View */}
        <Card className="p-4">
          <div className="flex items-center justify-between mb-4">
            <h3>Calendar View</h3>
            <Popover>
              <PopoverTrigger asChild>
                <Button variant="outline" size="sm">
                  <CalendarIcon className="w-4 h-4 mr-2" />
                  {selectedDate.toLocaleDateString('en-US', { month: 'short', year: 'numeric' })}
                </Button>
              </PopoverTrigger>
              <PopoverContent className="w-auto p-0">
                <Calendar
                  mode="single"
                  selected={selectedDate}
                  onSelect={(date) => date && setSelectedDate(date)}
                />
              </PopoverContent>
            </Popover>
          </div>

          {/* Simple calendar grid visualization */}
          <div className="grid grid-cols-7 gap-2">
            {['S', 'M', 'T', 'W', 'T', 'F', 'S'].map((day, idx) => (
              <div key={idx} className="text-center text-sm text-gray-600 py-2">
                {day}
              </div>
            ))}
            {Array.from({ length: 28 }).map((_, idx) => {
              const adherence = Math.random() > 0.2; // Mock data
              return (
                <div
                  key={idx}
                  className={`aspect-square rounded-lg flex items-center justify-center text-sm ${
                    adherence 
                      ? 'bg-green-100 text-green-700' 
                      : 'bg-red-100 text-red-700'
                  }`}
                >
                  {idx + 1}
                </div>
              );
            })}
          </div>

          <div className="flex gap-4 mt-4 text-sm">
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 bg-green-100 rounded"></div>
              <span className="text-gray-600">All doses taken</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 bg-red-100 rounded"></div>
              <span className="text-gray-600">Missed doses</span>
            </div>
          </div>
        </Card>

        {/* Medication-Specific Rates */}
        <Card className="p-4">
          <h3 className="mb-4">By Medication</h3>
          <div className="space-y-3">
            {medicationAdherence.map((med, idx) => (
              <div key={idx}>
                <div className="flex justify-between items-center mb-2">
                  <span>{med.name}</span>
                  <Badge variant={med.percentage >= 80 ? "default" : "destructive"}>
                    {med.percentage}%
                  </Badge>
                </div>
                <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                  <div 
                    className={`h-full ${med.percentage >= 80 ? 'bg-green-500' : 'bg-red-500'}`}
                    style={{ width: `${med.percentage}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        </Card>

        {/* Insights */}
        <Card className="p-4 bg-blue-50 border-blue-200">
          <h3 className="mb-2 text-blue-900">💡 Insights</h3>
          <ul className="space-y-2 text-sm text-blue-800">
            <li>• You're most consistent with morning medications</li>
            <li>• Evening doses are occasionally missed</li>
            <li>• Your adherence improved 5% this month!</li>
          </ul>
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
            <PillIcon className="w-6 h-6" />
            <span className="text-xs">Medications</span>
          </button>
          
          <button 
            onClick={() => onNavigate('history')}
            className="flex flex-col items-center gap-1 text-blue-600"
          >
            <History className="w-6 h-6" />
            <span className="text-xs">History</span>
          </button>
          
          <button 
            onClick={() => onNavigate('profile')}
            className="flex flex-col items-center gap-1 text-gray-600"
          >
            <User className="w-6 h-6" />
            <span className="text-xs">Profile</span>
          </button>
        </div>
      </div>
    </div>
  );
}
