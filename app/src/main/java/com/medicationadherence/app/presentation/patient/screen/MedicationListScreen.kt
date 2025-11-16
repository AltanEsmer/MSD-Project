package com.medicationadherence.app.presentation.patient.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medicationadherence.app.domain.model.Medication
import com.medicationadherence.app.presentation.common.components.*
import com.medicationadherence.app.presentation.patient.viewmodel.MedicationViewModel
import com.medicationadherence.app.presentation.theme.*

/**
 * Medication List Screen - View and manage all medications
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(
    viewModel: MedicationViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onAddNew: () -> Unit,
    onEdit: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMessages: () -> Unit = {}
) {
    val todayMedications by viewModel.todayMedications.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var medicationToDelete by remember { mutableStateOf<String?>(null) }
    
    // Extract unique medications
    val medications = todayMedications.map { it.medication }.distinctBy { it.id }
    
    // Filter medications based on search
    val filteredMedications = if (searchQuery.isBlank()) {
        medications
    } else {
        medications.filter { 
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Gray50)
        ) {
            // Header
            TopAppBar(
                title = { Text("All Medications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
            
            // Search Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    placeholder = { Text("Search medications...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
            
            // Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Add New Button
                item {
                    Button(
                        onClick = onAddNew,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue600
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add New Medication",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                // Medications List
                if (filteredMedications.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.Medication,
                            title = if (searchQuery.isBlank()) "No medications found" else "No results",
                            message = if (searchQuery.isBlank()) 
                                "Add your first medication to get started" 
                            else 
                                "Try a different search term",
                            actionText = if (searchQuery.isBlank()) "Add Medication" else null,
                            onActionClick = if (searchQuery.isBlank()) onAddNew else null
                        )
                    }
                } else {
                    items(filteredMedications) { medication ->
                        MedicationListItem(
                            medication = medication,
                            onEdit = { onEdit(medication.id) },
                            onDelete = {
                                medicationToDelete = medication.id
                                showDeleteDialog = true
                            }
                        )
                    }
                }
                
                // Bottom padding for nav bar
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        
        // Bottom Navigation
        BottomNavBar(
            selectedItem = 1,
            onItemSelected = { index ->
                when (index) {
                    0 -> onNavigateToHome()
                    1 -> {} // Already on medications
                    2 -> onNavigateToHistory()
                    3 -> onNavigateToMessages()
                    4 -> onNavigateToProfile()
                }
            },
            items = listOf(
                BottomNavItem(Icons.Default.Home, "Home"),
                BottomNavItem(Icons.Default.Medication, "Medications"),
                BottomNavItem(Icons.Default.History, "History"),
                BottomNavItem(Icons.Default.Message, "Messages"),
                BottomNavItem(Icons.Default.Person, "Profile")
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
    
    // Delete Confirmation Dialog
    ConfirmDialog(
        title = "Delete Medication?",
        message = "This action cannot be undone. This will permanently delete this medication and all its history.",
        confirmText = "Delete",
        dismissText = "Cancel",
        onConfirm = {
            medicationToDelete?.let { id ->
                viewModel.deleteMedication(id)
            }
            showDeleteDialog = false
            medicationToDelete = null
        },
        onDismiss = {
            showDeleteDialog = false
            medicationToDelete = null
        },
        isVisible = showDeleteDialog
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicationListItem(
    medication: Medication,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Blue100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = null,
                    tint = Blue600,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${medication.dosage} • ${medication.frequency.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
                
                // Time badges
                if (medication.frequency.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        medication.frequency.take(3).forEach { time ->
                            Badge(
                                containerColor = Gray200
                            ) {
                                Text(
                                    text = time,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gray700
                                )
                            }
                        }
                        if (medication.frequency.size > 3) {
                            Badge(
                                containerColor = Gray200
                            ) {
                                Text(
                                    text = "+${medication.frequency.size - 3}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gray700
                                )
                            }
                        }
                    }
                }
                
                if (!medication.instructions.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = medication.instructions,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
            }
            
            // Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Text("Edit")
                            }
                        },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Red600
                                )
                                Text("Delete", color = Red600)
                            }
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

