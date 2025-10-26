import { useState } from 'react';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Input } from '../ui/input';
import { Badge } from '../ui/badge';
import { Home, Pill as PillIcon, History, User, Plus, Search, Pill, Droplet, Syringe, Tablets, MoreVertical, ChevronLeft, Edit2, Trash2 } from 'lucide-react';
import { Medication } from '../../App';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '../ui/dropdown-menu';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '../ui/alert-dialog';

type Props = {
  medications: Medication[];
  onNavigate: (screen: string) => void;
  onAddNew: () => void;
  onEdit: (medication: Medication) => void;
  onDelete: (id: string) => void;
};

const medicationIcons = {
  pill: Pill,
  tablets: Tablets,
  droplet: Droplet,
  syringe: Syringe,
};

export default function MedicationList({ medications, onNavigate, onAddNew, onEdit, onDelete }: Props) {
  const [searchQuery, setSearchQuery] = useState('');
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [medicationToDelete, setMedicationToDelete] = useState<string | null>(null);

  const filteredMedications = medications.filter(med =>
    med.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleDeleteClick = (id: string) => {
    setMedicationToDelete(id);
    setDeleteDialogOpen(true);
  };

  const confirmDelete = () => {
    if (medicationToDelete) {
      onDelete(medicationToDelete);
      setDeleteDialogOpen(false);
      setMedicationToDelete(null);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 p-4">
        <div className="flex items-center gap-3 mb-4">
          <button onClick={() => onNavigate('dashboard')}>
            <ChevronLeft className="w-6 h-6" />
          </button>
          <h1>All Medications</h1>
        </div>

        {/* Search */}
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <Input 
            placeholder="Search medications..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10 h-12"
          />
        </div>
      </div>

      {/* Content */}
      <div className="p-6">
        {/* Add New Button */}
        <Button 
          className="w-full h-14 mb-4 bg-blue-600 hover:bg-blue-700"
          onClick={onAddNew}
        >
          <Plus className="w-5 h-5 mr-2" />
          Add New Medication
        </Button>

        {/* Medications List */}
        {filteredMedications.length === 0 ? (
          <Card className="p-8 text-center">
            <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <PillIcon className="w-8 h-8 text-gray-400" />
            </div>
            <h3 className="mb-2">No medications found</h3>
            <p className="text-gray-600 mb-4">
              {searchQuery ? 'Try a different search term' : 'Add your first medication to get started'}
            </p>
            {!searchQuery && (
              <Button 
                variant="outline"
                onClick={onAddNew}
              >
                Add Medication
              </Button>
            )}
          </Card>
        ) : (
          <div className="space-y-3">
            {filteredMedications.map((med) => {
              const IconComponent = medicationIcons[med.icon as keyof typeof medicationIcons] || Pill;
              const nextDose = med.times[0]; // Simplified - would calculate actual next dose

              return (
                <Card key={med.id} className="p-4">
                  <div className="flex items-start gap-3">
                    <div className="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center flex-shrink-0">
                      <IconComponent className="w-6 h-6 text-blue-600" />
                    </div>
                    
                    <div className="flex-1">
                      <div className="flex items-start justify-between mb-1">
                        <div>
                          <h3>{med.name}</h3>
                          <p className="text-gray-600">{med.dosage} • {med.frequency}</p>
                        </div>
                        
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="icon">
                              <MoreVertical className="w-5 h-5" />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            <DropdownMenuItem onClick={() => onEdit(med)}>
                              <Edit2 className="w-4 h-4 mr-2" />
                              Edit
                            </DropdownMenuItem>
                            <DropdownMenuItem 
                              onClick={() => handleDeleteClick(med.id)}
                              className="text-red-600"
                            >
                              <Trash2 className="w-4 h-4 mr-2" />
                              Delete
                            </DropdownMenuItem>
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </div>
                      
                      <div className="flex flex-wrap gap-2 mt-2">
                        {med.times.map((time, idx) => (
                          <Badge key={idx} variant="secondary">
                            {time}
                          </Badge>
                        ))}
                      </div>
                      
                      {med.instructions && (
                        <p className="text-sm text-gray-500 mt-2">
                          {med.instructions}
                        </p>
                      )}
                      
                      <div className="mt-2 flex items-center gap-2 text-sm text-gray-600">
                        <span>Next dose:</span>
                        <span className="text-blue-600">{nextDose}</span>
                      </div>
                    </div>
                  </div>
                </Card>
              );
            })}
          </div>
        )}
      </div>

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Medication?</AlertDialogTitle>
            <AlertDialogDescription>
              This action cannot be undone. This will permanently delete this medication and all its history.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={confirmDelete} className="bg-red-600 hover:bg-red-700">
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

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
            className="flex flex-col items-center gap-1 text-blue-600"
          >
            <PillIcon className="w-6 h-6" />
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
