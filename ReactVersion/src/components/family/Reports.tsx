import { useState } from 'react';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Badge } from '../ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';
import { Home, Bell, MessageCircle, BarChart3, ChevronLeft, Download, Calendar, TrendingUp, TrendingDown } from 'lucide-react';
import { Patient } from '../../App';
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';

type Props = {
  connectedPatients: Patient[];
  onNavigate: (screen: string) => void;
};

export default function Reports({ connectedPatients, onNavigate }: Props) {
  const [selectedPatient, setSelectedPatient] = useState<string>(connectedPatients[0]?.id || '');
  const [timeRange, setTimeRange] = useState<string>('7days');

  // Mock data for charts
  const weeklyData = [
    { day: 'Mon', adherence: 100, target: 100 },
    { day: 'Tue', adherence: 85, target: 100 },
    { day: 'Wed', adherence: 100, target: 100 },
    { day: 'Thu', adherence: 90, target: 100 },
    { day: 'Fri', adherence: 100, target: 100 },
    { day: 'Sat', adherence: 75, target: 100 },
    { day: 'Sun', adherence: 95, target: 100 },
  ];

  const monthlyTrend = [
    { month: 'Jan', adherence: 88 },
    { month: 'Feb', adherence: 92 },
    { month: 'Mar', adherence: 85 },
    { month: 'Apr', adherence: 90 },
    { month: 'May', adherence: 95 },
    { month: 'Jun', adherence: 93 },
  ];

  const timePatterns = [
    { time: 'Morning', percentage: 95, color: '#22c55e' },
    { time: 'Afternoon', percentage: 78, color: '#eab308' },
    { time: 'Evening', percentage: 92, color: '#3b82f6' },
    { time: 'Night', percentage: 88, color: '#8b5cf6' },
  ];

  const medicationBreakdown = [
    { name: 'Blood Pressure', value: 95, color: '#3b82f6' },
    { name: 'Diabetes', value: 90, color: '#10b981' },
    { name: 'Cholesterol', value: 85, color: '#f59e0b' },
    { name: 'Vitamins', value: 80, color: '#8b5cf6' },
  ];

  const insights = [
    {
      type: 'positive',
      title: 'Strong Morning Adherence',
      description: 'Patient consistently takes morning medications on time (95% adherence)',
    },
    {
      type: 'warning',
      title: 'Afternoon Inconsistency',
      description: 'Afternoon doses are occasionally missed. Consider setting additional reminders.',
    },
    {
      type: 'positive',
      title: 'Overall Improvement',
      description: 'Adherence has improved by 7% compared to last month',
    },
    {
      type: 'info',
      title: 'Weekend Pattern',
      description: 'Slight decrease in adherence on weekends',
    },
  ];

  const handleExport = () => {
    alert('Exporting report as PDF...');
  };

  const currentPatient = connectedPatients.find(p => p.id === selectedPatient);

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 p-4">
        <div className="flex items-center gap-3 mb-4">
          <button onClick={() => onNavigate('family-dashboard')}>
            <ChevronLeft className="w-6 h-6" />
          </button>
          <div className="flex-1">
            <h1>Adherence Reports</h1>
          </div>
          <Button variant="outline" size="sm" onClick={handleExport}>
            <Download className="w-4 h-4 mr-2" />
            Export
          </Button>
        </div>

        {/* Filters */}
        <div className="grid grid-cols-2 gap-3">
          <Select value={selectedPatient} onValueChange={setSelectedPatient}>
            <SelectTrigger>
              <SelectValue placeholder="Select patient" />
            </SelectTrigger>
            <SelectContent>
              {connectedPatients.map(patient => (
                <SelectItem key={patient.id} value={patient.id}>
                  {patient.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Select value={timeRange} onValueChange={setTimeRange}>
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="7days">Last 7 days</SelectItem>
              <SelectItem value="30days">Last 30 days</SelectItem>
              <SelectItem value="90days">Last 90 days</SelectItem>
              <SelectItem value="1year">Last year</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* Content */}
      <div className="p-6 space-y-6">
        {/* Summary Cards */}
        <div className="grid grid-cols-2 gap-3">
          <Card className="p-4">
            <div className="flex items-center gap-2 mb-2">
              <TrendingUp className="w-5 h-5 text-green-600" />
              <p className="text-sm text-gray-600">Overall Rate</p>
            </div>
            <p className="text-green-600">92%</p>
            <p className="text-xs text-gray-500 mt-1">+5% from last period</p>
          </Card>

          <Card className="p-4">
            <div className="flex items-center gap-2 mb-2">
              <Calendar className="w-5 h-5 text-blue-600" />
              <p className="text-sm text-gray-600">Current Streak</p>
            </div>
            <p className="text-blue-600">7 days</p>
            <p className="text-xs text-gray-500 mt-1">Best: 14 days</p>
          </Card>
        </div>

        {/* Weekly Adherence Chart */}
        <Card className="p-4">
          <h3 className="mb-4">Weekly Adherence</h3>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={weeklyData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="day" />
              <YAxis />
              <Tooltip />
              <Bar dataKey="adherence" fill="#8b5cf6" radius={[8, 8, 0, 0]} />
              <Bar dataKey="target" fill="#e5e7eb" radius={[8, 8, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
          <div className="flex gap-4 mt-3 text-sm">
            <div className="flex items-center gap-2">
              <div className="w-3 h-3 bg-purple-600 rounded"></div>
              <span className="text-gray-600">Actual</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-3 h-3 bg-gray-300 rounded"></div>
              <span className="text-gray-600">Target</span>
            </div>
          </div>
        </Card>

        {/* Monthly Trend */}
        <Card className="p-4">
          <h3 className="mb-4">6-Month Trend</h3>
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={monthlyTrend}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis />
              <Tooltip />
              <Line 
                type="monotone" 
                dataKey="adherence" 
                stroke="#8b5cf6" 
                strokeWidth={3}
                dot={{ fill: '#8b5cf6', r: 4 }}
              />
            </LineChart>
          </ResponsiveContainer>
        </Card>

        {/* Time Patterns */}
        <Card className="p-4">
          <h3 className="mb-4">Adherence by Time of Day</h3>
          <div className="space-y-3">
            {timePatterns.map((pattern) => (
              <div key={pattern.time}>
                <div className="flex justify-between items-center mb-2">
                  <span>{pattern.time}</span>
                  <Badge 
                    variant={pattern.percentage >= 90 ? "default" : pattern.percentage >= 80 ? "secondary" : "destructive"}
                  >
                    {pattern.percentage}%
                  </Badge>
                </div>
                <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                  <div 
                    className="h-full"
                    style={{ 
                      width: `${pattern.percentage}%`,
                      backgroundColor: pattern.color,
                    }}
                  />
                </div>
              </div>
            ))}
          </div>
        </Card>

        {/* Medication Breakdown */}
        <Card className="p-4">
          <h3 className="mb-4">By Medication</h3>
          <div className="space-y-3">
            {medicationBreakdown.map((med) => (
              <div key={med.name} className="flex items-center justify-between">
                <div className="flex items-center gap-3 flex-1">
                  <div 
                    className="w-3 h-3 rounded-full"
                    style={{ backgroundColor: med.color }}
                  />
                  <span>{med.name}</span>
                </div>
                <div className="flex items-center gap-3">
                  <div className="w-32 h-2 bg-gray-200 rounded-full overflow-hidden">
                    <div 
                      className="h-full"
                      style={{ 
                        width: `${med.value}%`,
                        backgroundColor: med.color,
                      }}
                    />
                  </div>
                  <span className="text-sm w-12 text-right">{med.value}%</span>
                </div>
              </div>
            ))}
          </div>
        </Card>

        {/* Insights & Recommendations */}
        <Card className="p-4">
          <h3 className="mb-4">AI Insights & Recommendations</h3>
          <div className="space-y-3">
            {insights.map((insight, idx) => (
              <div 
                key={idx}
                className={`p-3 rounded-lg border ${
                  insight.type === 'positive' 
                    ? 'bg-green-50 border-green-200' 
                    : insight.type === 'warning'
                    ? 'bg-orange-50 border-orange-200'
                    : 'bg-blue-50 border-blue-200'
                }`}
              >
                <div className="flex items-start gap-2">
                  {insight.type === 'positive' && <TrendingUp className="w-4 h-4 text-green-600 mt-0.5" />}
                  {insight.type === 'warning' && <TrendingDown className="w-4 h-4 text-orange-600 mt-0.5" />}
                  {insight.type === 'info' && <BarChart3 className="w-4 h-4 text-blue-600 mt-0.5" />}
                  <div className="flex-1">
                    <p className={`text-sm mb-1 ${
                      insight.type === 'positive' 
                        ? 'text-green-900' 
                        : insight.type === 'warning'
                        ? 'text-orange-900'
                        : 'text-blue-900'
                    }`}>
                      {insight.title}
                    </p>
                    <p className="text-xs text-gray-700">{insight.description}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </Card>

        {/* Export Options */}
        <Card className="p-4">
          <h3 className="mb-4">Share Report</h3>
          <div className="grid grid-cols-2 gap-3">
            <Button variant="outline" onClick={handleExport}>
              <Download className="w-4 h-4 mr-2" />
              Download PDF
            </Button>
            <Button variant="outline" onClick={handleExport}>
              <MessageCircle className="w-4 h-4 mr-2" />
              Email Report
            </Button>
          </div>
        </Card>
      </div>

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
            className="flex flex-col items-center gap-1 text-gray-600"
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
            className="flex flex-col items-center gap-1 text-purple-600"
          >
            <BarChart3 className="w-6 h-6" />
            <span className="text-xs">Reports</span>
          </button>
        </div>
      </div>
    </div>
  );
}
