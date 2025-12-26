package com.app.timetable.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.timetable.data.local.entity.Subject
import com.app.timetable.ui.viewmodel.AddTaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    viewModel: AddTaskViewModel = hiltViewModel(),
    taskId: Int = -1, // -1 means "New Task"
    onBackClick: () -> Unit
) {
    val subjects by viewModel.subjects.collectAsState()
    val taskToEdit by viewModel.taskState.collectAsState()

    // Loads task data when screen opens
    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }

    // Form State
    var title by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var priority by remember { mutableIntStateOf(3) } // Default Medium
    var deadline by remember { mutableLongStateOf(System.currentTimeMillis() + 86400000) } // Default +1 day

    // When taskToEdit loads, fill the form
    LaunchedEffect(taskToEdit) {
        taskToEdit?.let { task ->
            title = task.title
            priority = task.priority
            deadline = task.deadline
            // Find the subject object that matches the ID
            selectedSubject = subjects.find { it.subjectID == task.subjectID }
        }
    }

    // Dropdown State
    var isSubjectExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == -1) "New Task" else "Edit Task") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Task Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth()
            )

            // 2. Subject Dropdown
            ExposedDropdownMenuBox(
                expanded = isSubjectExpanded,
                onExpandedChange = { isSubjectExpanded = !isSubjectExpanded }
            ) {
                OutlinedTextField(
                    value = selectedSubject?.name ?: "Select Subject (Optional)",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSubjectExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isSubjectExpanded,
                    onDismissRequest = { isSubjectExpanded = false }
                ) {
                    if (subjects.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No subjects found. Create one first!") },
                            onClick = { isSubjectExpanded = false }
                        )
                    } else {
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject.name) },
                                onClick = {
                                    selectedSubject = subject
                                    isSubjectExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // 3. Priority Selection
            Text("Priority", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { p ->
                    FilterChip(
                        selected = priority == p,
                        onClick = { priority = p },
                        label = { Text(getPriorityLabel(p)) }
                    )
                }
            }

            // 4. Deadline Picker
            DateTimePickerField(
                timestamp = deadline,
                onDateSelected = { deadline = it }
            )

            Spacer(modifier = Modifier.weight(1f))

            // 5. Save/Update Button
            Button(
                onClick = {
                    viewModel.saveTask(
                        id = if (taskId == -1) 0 else taskId, // Pass ID to update
                        title = title,
                        subjectId = selectedSubject?.subjectID ?: 0,
                        priority = priority,
                        deadline = deadline,
                        onSuccess = onBackClick
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text(if (taskId == -1) "Save Task" else "Update Task")
            }
        }
    }
}

@Composable
fun DateTimePickerField(timestamp: Long, onDateSelected: (Long) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = sdf.format(Date(timestamp)),
            onValueChange = {},
            readOnly = true,
            label = { Text("Deadline") },
            trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable {
                    showDateTimePicker(context, calendar) { newTime ->
                        onDateSelected(newTime)
                    }
                }
        )
    }
}

// Helper to show DatePicker then TimePicker
fun showDateTimePicker(context: Context, startCalendar: Calendar, onDateTimePicked: (Long) -> Unit) {
    DatePickerDialog(
        context,
        { _, year, month, day ->
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    val result = Calendar.getInstance()
                    result.set(year, month, day, hour, minute)
                    onDateTimePicked(result.timeInMillis)
                },
                startCalendar.get(Calendar.HOUR_OF_DAY),
                startCalendar.get(Calendar.MINUTE),
                false
            ).show()
        },
        startCalendar.get(Calendar.YEAR),
        startCalendar.get(Calendar.MONTH),
        startCalendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

fun getPriorityLabel(p: Int): String = when(p) {
    1 -> "High"
    2 -> "M-High"
    3 -> "Med"
    4 -> "Low"
    else -> "Min"
}