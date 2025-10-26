import { useState } from 'react';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Input } from '../ui/input';
import { Textarea } from '../ui/textarea';
import { Badge } from '../ui/badge';
import { Home, Bell, MessageCircle, BarChart3, ChevronLeft, Send, Phone, Heart, ThumbsUp, Smile } from 'lucide-react';
import { Patient } from '../../App';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '../ui/dialog';
import { toast } from 'sonner@2.0.3';

type Props = {
  connectedPatients: Patient[];
  onNavigate: (screen: string) => void;
};

type Message = {
  id: string;
  patientId: string;
  content: string;
  sender: 'caregiver' | 'patient';
  timestamp: string;
  read: boolean;
};

const messageTemplates = [
  { icon: Heart, text: "Great job staying on track! Keep it up! 💪", category: "encouragement" },
  { icon: ThumbsUp, text: "Proud of you for taking your medications consistently!", category: "encouragement" },
  { icon: Smile, text: "Just checking in - how are you feeling today?", category: "check-in" },
  { icon: MessageCircle, text: "Don't forget to take your [TIME] medication", category: "reminder" },
  { icon: Phone, text: "Would you like to schedule a call to chat?", category: "check-in" },
];

export default function MessageCenter({ connectedPatients, onNavigate }: Props) {
  const [selectedPatient, setSelectedPatient] = useState<Patient | null>(null);
  const [messageText, setMessageText] = useState('');
  const [showTemplates, setShowTemplates] = useState(false);
  const [messages, setMessages] = useState<Message[]>([
    {
      id: '1',
      patientId: connectedPatients[0]?.id || '1',
      content: "I took my morning medication!",
      sender: 'patient',
      timestamp: '8:30 AM',
      read: true,
    },
    {
      id: '2',
      patientId: connectedPatients[0]?.id || '1',
      content: "That's wonderful! Keep up the great work! 💪",
      sender: 'caregiver',
      timestamp: '8:45 AM',
      read: true,
    },
    {
      id: '3',
      patientId: connectedPatients[0]?.id || '1',
      content: "Thank you for the support!",
      sender: 'patient',
      timestamp: '9:00 AM',
      read: true,
    },
  ]);

  const handleSendMessage = () => {
    if (!messageText.trim() || !selectedPatient) return;

    const newMessage: Message = {
      id: Date.now().toString(),
      patientId: selectedPatient.id,
      content: messageText,
      sender: 'caregiver',
      timestamp: new Date().toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' }),
      read: true,
    };

    setMessages([...messages, newMessage]);
    setMessageText('');
    toast.success('Message sent');
  };

  const handleUseTemplate = (template: string) => {
    setMessageText(template);
    setShowTemplates(false);
  };

  const patientMessages = selectedPatient 
    ? messages.filter(m => m.patientId === selectedPatient.id)
    : [];

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 p-4">
        <div className="flex items-center gap-3">
          {selectedPatient ? (
            <>
              <button onClick={() => setSelectedPatient(null)}>
                <ChevronLeft className="w-6 h-6" />
              </button>
              <div className="flex-1">
                <h1>{selectedPatient.name}</h1>
                <p className="text-sm text-gray-600">Active now</p>
              </div>
              <Button variant="outline" size="icon">
                <Phone className="w-5 h-5" />
              </Button>
            </>
          ) : (
            <>
              <button onClick={() => onNavigate('family-dashboard')}>
                <ChevronLeft className="w-6 h-6" />
              </button>
              <h1>Messages</h1>
            </>
          )}
        </div>
      </div>

      {/* Content */}
      {!selectedPatient ? (
        <div className="p-6">
          <h2 className="mb-4">Conversations</h2>
          {connectedPatients.length === 0 ? (
            <Card className="p-8 text-center">
              <div className="w-16 h-16 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <MessageCircle className="w-8 h-8 text-purple-600" />
              </div>
              <h3 className="mb-2">No conversations yet</h3>
              <p className="text-gray-600">Connect to a patient to start messaging</p>
            </Card>
          ) : (
            <div className="space-y-3">
              {connectedPatients.map((patient) => {
                const lastMessage = messages.filter(m => m.patientId === patient.id).pop();
                return (
                  <Card 
                    key={patient.id}
                    className="p-4 cursor-pointer hover:shadow-md transition-shadow"
                    onClick={() => setSelectedPatient(patient)}
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center flex-shrink-0">
                        <span className="text-purple-600">{patient.name.charAt(0)}</span>
                      </div>
                      <div className="flex-1">
                        <div className="flex items-center justify-between mb-1">
                          <h3>{patient.name}</h3>
                          <span className="text-xs text-gray-500">{lastMessage?.timestamp}</span>
                        </div>
                        <p className="text-sm text-gray-600 truncate">
                          {lastMessage?.content || 'No messages yet'}
                        </p>
                      </div>
                    </div>
                  </Card>
                );
              })}
            </div>
          )}
        </div>
      ) : (
        <>
          {/* Messages View */}
          <div className="flex flex-col h-[calc(100vh-180px)]">
            {/* Messages List */}
            <div className="flex-1 overflow-y-auto p-6 space-y-4">
              {patientMessages.map((message) => (
                <div 
                  key={message.id}
                  className={`flex ${message.sender === 'caregiver' ? 'justify-end' : 'justify-start'}`}
                >
                  <div className={`max-w-[70%] ${
                    message.sender === 'caregiver'
                      ? 'bg-purple-600 text-white'
                      : 'bg-white border border-gray-200'
                  } rounded-2xl px-4 py-3`}>
                    <p className="text-sm">{message.content}</p>
                    <p className={`text-xs mt-1 ${
                      message.sender === 'caregiver' ? 'text-purple-200' : 'text-gray-500'
                    }`}>
                      {message.timestamp}
                    </p>
                  </div>
                </div>
              ))}
            </div>

            {/* Message Input */}
            <div className="border-t border-gray-200 bg-white p-4">
              <div className="flex gap-2 mb-2">
                <Button 
                  variant="outline" 
                  size="sm"
                  onClick={() => setShowTemplates(true)}
                  className="flex-shrink-0"
                >
                  Templates
                </Button>
              </div>
              <div className="flex gap-2">
                <Textarea 
                  placeholder="Type a message..."
                  value={messageText}
                  onChange={(e) => setMessageText(e.target.value)}
                  className="resize-none"
                  rows={2}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' && !e.shiftKey) {
                      e.preventDefault();
                      handleSendMessage();
                    }
                  }}
                />
                <Button 
                  size="icon"
                  className="h-auto bg-purple-600 hover:bg-purple-700"
                  onClick={handleSendMessage}
                  disabled={!messageText.trim()}
                >
                  <Send className="w-5 h-5" />
                </Button>
              </div>
            </div>
          </div>
        </>
      )}

      {/* Message Templates Dialog */}
      <Dialog open={showTemplates} onOpenChange={setShowTemplates}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Message Templates</DialogTitle>
          </DialogHeader>
          <div className="space-y-2">
            {messageTemplates.map((template, idx) => {
              const IconComponent = template.icon;
              return (
                <button
                  key={idx}
                  onClick={() => handleUseTemplate(template.text)}
                  className="w-full text-left p-4 hover:bg-gray-50 rounded-lg transition-colors border border-gray-200"
                >
                  <div className="flex items-start gap-3">
                    <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center flex-shrink-0">
                      <IconComponent className="w-5 h-5 text-purple-600" />
                    </div>
                    <div className="flex-1">
                      <Badge variant="secondary" className="mb-2 text-xs">
                        {template.category}
                      </Badge>
                      <p className="text-sm">{template.text}</p>
                    </div>
                  </div>
                </button>
              );
            })}
          </div>
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
            className="flex flex-col items-center gap-1 text-gray-600"
          >
            <Bell className="w-6 h-6" />
            <span className="text-xs">Alerts</span>
          </button>
          
          <button 
            onClick={() => onNavigate('messages')}
            className="flex flex-col items-center gap-1 text-purple-600"
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
