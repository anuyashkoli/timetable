package com.app.timetable.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.timetable.data.local.entity.WorkSession
import com.app.timetable.ui.viewmodel.SettingsViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val sessions by viewModel.sessions.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Schedule") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Session")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Define when you are free to study.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(sessions) { session ->
                SessionItem(
                    session = session,
                    onDeleteClick = {
                        viewModel.deleteWorkSession(session)
                    }
                )
            }
        }

        if (showDialog) {
            val context = LocalContext.current // Get context for Toast

            AddSessionDialog(
                onDismiss = { showDialog = false },
                onSave = { day, sH, sM, eH, eM ->
                    // Compare total minutes to check validity
                    if ((eH * 60 + eM) <= (sH * 60 + sM)) {
                        android.widget.Toast.makeText(context, "Session cannot end before it starts!", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.saveSession(day, sH, sM, eH, eM)
                        android.widget.Toast.makeText(context, "Session added! from ${String.format("%02d:%02d", sH, sM)} to ${String.format("%02d:%02d", eH, eM)}",android.widget.Toast.LENGTH_SHORT).show()
                        showDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun SessionItem(
    session: WorkSession,
    onDeleteClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(getDayName(session.dayOfWeek), style = MaterialTheme.typography.titleMedium)
                Text(
                    "${formatTime(session.startTime)} - ${formatTime(session.endTime)}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Delete Button
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Session",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddSessionDialog(
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int, Int, Int) -> Unit
) {
    var selectedDay by remember { mutableIntStateOf(Calendar.MONDAY) }
    var startHour by remember { mutableIntStateOf(9) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(10) }
    var endMinute by remember { mutableIntStateOf(0) }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Study Slot") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Day Picker
                Text("Select Day:")
                ScrollableDayPicker(selectedDay) { selectedDay = it }

                // Time Pickers
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = {
                        TimePickerDialog(context, { _, h, m -> startHour = h; startMinute = m }, startHour, startMinute, false).show()
                    }) { Text("Start: ${String.format("%02d:%02d", startHour, startMinute)}") }

                    Button(onClick = {
                        TimePickerDialog(context, { _, h, m -> endHour = h; endMinute = m }, endHour, endMinute, false).show()
                    }) { Text("End: ${String.format("%02d:%02d", endHour, endMinute)}") }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(selectedDay, startHour, startMinute, endHour, endMinute) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ScrollableDayPicker(selectedDay: Int, onDaySelected: (Int) -> Unit) {
    val days = listOf(
        Calendar.SUNDAY to "Sun", Calendar.MONDAY to "Mon", Calendar.TUESDAY to "Tue",
        Calendar.WEDNESDAY to "Wed", Calendar.THURSDAY to "Thu", Calendar.FRIDAY to "Fri",
        Calendar.SATURDAY to "Sat"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEach { (dayId, label) ->
            FilterChip(
                selected = selectedDay == dayId,
                onClick = { onDaySelected(dayId) },
                label = { Text(label) }
            )
        }
    }
}

// Helpers
fun getDayName(day: Int): String {
    return when(day) {
        Calendar.SUNDAY -> "Sunday"
        Calendar.MONDAY -> "Monday"
        Calendar.TUESDAY -> "Tuesday"
        Calendar.WEDNESDAY -> "Wednesday"
        Calendar.THURSDAY -> "Thursday"
        Calendar.FRIDAY -> "Friday"
        Calendar.SATURDAY -> "Saturday"
        else -> "Unknown"
    }
}

fun formatTime(millis: Long): String {
    val hours = (millis / 3600000).toInt()
    val minutes = ((millis % 3600000) / 60000).toInt()
    return String.format("%02d:%02d", hours, minutes)
}