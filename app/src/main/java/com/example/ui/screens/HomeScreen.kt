package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.example.MainViewModel
import com.example.TaskItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel, onNavigateHistory: () -> Unit, onNavigateTodo: () -> Unit, onNavigateSettings: () -> Unit) {
    val allTasks by viewModel.tasks.collectAsState()
    val tasks = remember(allTasks) { allTasks.filter { com.example.DateUtils.isToday(it.timestamp) } }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskItem?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = { Text("What I Did Today") },
                windowInsets = WindowInsets(0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    Row(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                            .padding(horizontal = 2.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateTodo) {
                            Icon(Icons.Default.Checklist, contentDescription = "Todo List")
                        }
                        IconButton(onClick = onNavigateHistory) {
                            Icon(Icons.Default.History, contentDescription = "History")
                        }
                        IconButton(onClick = onNavigateSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add Activity")
            }
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Rounded.StarBorder,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).alpha(0.2f),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Nothing saved today.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Tap + to log your first activity!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskCard(task, onEdit = {
                        taskToEdit = task
                    }, onDelete = {
                        viewModel.deleteTask(task.id)
                    })
                }
            }
        }
    }

    if (showAddDialog) {
        TaskDialog(
            initialText = "",
            onDismiss = { showAddDialog = false },
            onSave = { text ->
                viewModel.addTask(text)
                showAddDialog = false
            }
        )
    }

    if (taskToEdit != null) {
        TaskDialog(
            initialText = taskToEdit!!.text,
            onDismiss = { taskToEdit = null },
            onSave = { text ->
                viewModel.updateTask(taskToEdit!!.id, text)
                taskToEdit = null
            }
        )
    }
}

@Composable
fun TaskCard(task: TaskItem, onEdit: () -> Unit, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val timeString = dateFormat.format(Date(task.timestamp))

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            // Success Icon Marker
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.text, 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule, 
                        contentDescription = null, 
                        modifier = Modifier.size(14.dp), 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        timeString, 
                        style = MaterialTheme.typography.labelMedium, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Actions Overflow Menu
            Box {
                var menuExpanded by remember { mutableStateOf(false) }
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskDialog(initialText: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialText.isEmpty()) "What did you do?" else "Edit activity") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Completed the project design") }
            )
        },
        confirmButton = {
            Button(onClick = { if(text.isNotBlank()) onSave(text) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
