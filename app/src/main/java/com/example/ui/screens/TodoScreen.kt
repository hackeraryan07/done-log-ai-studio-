package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.DateUtils
import com.example.MainViewModel
import com.example.TodoItem
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(viewModel: MainViewModel, onNavigateBack: () -> Unit) {
    val allTodos by viewModel.todos.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var todoToComplete by remember { mutableStateOf<TodoItem?>(null) }
    var todoToEdit by remember { mutableStateOf<TodoItem?>(null) }
    
    // Filtering
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    val todayEnd = todayStart + 24 * 60 * 60 * 1000 - 1
    
    val pendingTodos = allTodos.filter { !it.completed && it.scheduledDate < todayStart }
    val todayTodos = allTodos.filter { it.scheduledDate in todayStart..todayEnd }
    val futureTodos = allTodos.filter { !it.completed && it.scheduledDate > todayEnd }
    val completedTodos = allTodos.filter { it.completed } // Optional to show

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Today", "Pending", "Future")

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = { Text("To-Do List") },
                windowInsets = WindowInsets(0.dp),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add Todo")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Pill-shaped Segmented Tab Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            val currentList = when (selectedTab) {
                0 -> todayTodos.sortedBy { it.completed }
                1 -> pendingTodos.sortedBy { it.completed }
                else -> futureTodos.sortedBy { it.completed }
            }
            
            if (currentList.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).alpha(0.2f),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("All caught up!", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Tap + to add a new task", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentList, key = { it.id }) { todo ->
                        TodoCard(
                            modifier = Modifier.animateItem(),
                            todo = todo,
                            onCompleteClick = if (todo.completed) null else { { todoToComplete = todo } },
                            onEditClick = if (todo.completed) null else { { todoToEdit = todo } },
                            onDelete = { viewModel.deleteTodo(todo.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        TodoAddDialog(
            initialText = "",
            initialDateMillis = null,
            onDismiss = { showAddDialog = false },
            onSave = { text, date ->
                viewModel.addTodo(text, date)
                showAddDialog = false
            }
        )
    }

    if (todoToEdit != null) {
        TodoAddDialog(
            initialText = todoToEdit!!.text,
            initialDateMillis = todoToEdit!!.scheduledDate,
            onDismiss = { todoToEdit = null },
            onSave = { text, date ->
                viewModel.updateTodo(todoToEdit!!.id, text, date)
                todoToEdit = null
            }
        )
    }

    if (todoToComplete != null) {
        AlertDialog(
            onDismissRequest = { todoToComplete = null },
            title = { Text("Complete Task") },
            text = { Text("Are you sure you want to mark this as completed? It will be saved to what you have done today.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.markTodoCompleted(todoToComplete!!)
                    todoToComplete = null
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { todoToComplete = null }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun TodoCard(
    modifier: Modifier = Modifier,
    todo: TodoItem,
    onCompleteClick: (() -> Unit)?,
    onEditClick: (() -> Unit)?,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (todo.completed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (todo.completed) 0.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Interactive Checkbox / Status Marker
            if (!todo.completed && onCompleteClick != null) {
                FilledTonalIconButton(
                    onClick = onCompleteClick,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Complete")
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Completed", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Text & Date
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.text,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (todo.completed) TextDecoration.LineThrough else null,
                    color = if (todo.completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday, 
                        contentDescription = null, 
                        modifier = Modifier.size(14.dp), 
                        tint = if (todo.completed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = DateUtils.formatDate(todo.scheduledDate),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (todo.completed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = if (todo.completed) TextDecoration.LineThrough else null
                    )
                }
            }
            
            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (onEditClick != null) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun TodoAddDialog(
    initialText: String = "",
    initialDateMillis: Long? = null,
    onDismiss: () -> Unit,
    onSave: (String, Long) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    val context = LocalContext.current
    
    val calendar = Calendar.getInstance()
    if (initialDateMillis != null) {
        calendar.timeInMillis = initialDateMillis
    } else {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }
    
    var selectedDateMillis by remember { mutableLongStateOf(calendar.timeInMillis) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            selectedDateMillis = cal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialText.isEmpty()) "New To-Do" else "Edit To-Do") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedCard(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(Modifier.width(16.dp))
                        Text("Date: ${DateUtils.formatDate(selectedDateMillis)}")
                    }
                }
                
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter task...") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (text.isNotBlank()) onSave(text, selectedDateMillis) }) {
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
