package com.app.timetable.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.timetable.data.local.entity.Task
import com.app.timetable.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import com.app.timetable.ui.viewmodel.HomeUiState
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onAddTaskClick: () -> Unit, // Callback for navigation later
    onTaskClick: (Int) -> Unit,
    onAddSubjectClick: () -> Unit,
    onSettingsClick: () -> Unit // <--- ADD THIS
) {
    // Collect the sorted tasks from the ViewModel
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()

    // Collecting new UI State
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Timetable") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Small FAB for Subject
                SmallFloatingActionButton(
                    onClick = onAddSubjectClick,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Create, contentDescription = "Add Subject")
                }

                // Main FAB for Task
                FloatingActionButton(onClick = onAddTaskClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 2. The Smart Dashboard Card (Always at the top)
            item {
                DashboardCard(state)
            }

            item {
                Text(
                    "Your Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // 3. The Task List
            if (state.tasks.isEmpty()) {
                item {
                    Text(
                        text = "No tasks yet. Relax!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            } else {
                items(state.tasks, key = { it.taskID }) { task ->
                    TaskItem(
                        task = task,
                        onClick = { onTaskClick(task.taskID) },
                        onCheckedChange = { isChecked ->
                            viewModel.onTaskCheckedChange(task, isChecked)
                        },
                        onDeleteClick = {
                            viewModel.deleteTask(task)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardCard(state: HomeUiState) {
    // Dynamic Color: Green if studying, Blue if free
    val cardColor = if (state.isStudyTime) Color(0xFFC8E6C9) else Color(0xFFBBDEFB)
    val textColor = if (state.isStudyTime) Color(0xFF1B5E20) else Color(0xFF0D47A1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = textColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.isStudyTime) "It's Study Time!" else "You are Free",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            // Subtext: Recommendation
            if (state.isStudyTime) {
                if (state.recommendedTask != null) {
                    Text(
                        text = "Recommended Task:",
                        style = MaterialTheme.typography.labelLarge,
                        color = textColor.copy(alpha = 0.8f)
                    )
                    Text(
                        text = state.recommendedTask.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor
                    )
                    Text(
                        text = "Due: ${formatDeadline(state.recommendedTask.deadline)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor
                    )
                } else {
                    Text("You have a study slot, but no pending tasks! Great job.", color = textColor)
                }
            } else {
                // Free Time Logic
                if (state.nextSession != null) {
                    Text(
                        "Next study session starts at ${formatTime(state.nextSession.startTime)}.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor
                    )
                } else {
                    Text(
                        "No more sessions scheduled for today.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Color(0xFFE0E0E0) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onCheckedChange
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Task Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Priority Badge
                    PriorityBadge(priority = task.priority)

                    Spacer(modifier = Modifier.width(8.dp))

                    // Deadline Text
                    Text(
                        text = "Due: ${formatDeadline(task.deadline)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Delete Button
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun PriorityBadge(priority: Int) {
    val (color, text) = when (priority) {
        1 -> Color(0xFFFFCDD2) to "High"   // Red-ish
        2 -> Color(0xFFFFE0B2) to "Med-High" // Orange-ish
        3 -> Color(0xFFFFF9C4) to "Medium" // Yellow-ish
        4 -> Color(0xFFC8E6C9) to "Low"    // Green-ish
        else -> Color(0xFFF5F5F5) to "V. Low" // Grey
    }

    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black
        )
    }
}

// Helper to format timestamp
fun formatDeadline(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}