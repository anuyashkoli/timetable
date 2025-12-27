package com.app.timetable.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.timetable.ui.viewmodel.SubjectViewModel
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubjectScreen(
    viewModel: SubjectViewModel = hiltViewModel(),
    subjectId: Int = -1, // -1 means "New Subject"
    onBackClick: () -> Unit
) {
    val subjectToEdit by viewModel.subjectState.collectAsState()

    // Load data
    LaunchedEffect(subjectId) {
        viewModel.loadSubject(subjectId)
    }

    // Form State
    var name by remember { mutableStateOf("") }
    var goalHours by remember { mutableStateOf("10") }
    var selectedColor by remember { mutableStateOf(Color(0xFFEF9A9A)) } // Default Red-ish

    // Pre-fill form when data arrives
    LaunchedEffect(subjectToEdit) {
        subjectToEdit?.let { sub ->
            name = sub.name
            goalHours = (sub.goalTime / 3600000f).toString() // Convert back to hours
            try {
                selectedColor = Color(sub.color.toColorInt())
            } catch (e: Exception) { /* keep default */ }
        }
    }

    // Pre-defined colors for the user to pick
    val colors = listOf(
        Color(0xFFEF9A9A), Color(0xFFF48FB1), Color(0xFFCE93D8),
        Color(0xFF9FA8DA), Color(0xFF90CAF9), Color(0xFF80CBC4),
        Color(0xFFA5D6A7), Color(0xFFFFCC80), Color(0xFFBCAAA4)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (subjectId == -1) "New Subject" else "Edit Subject") },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Subject Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 2. Goal
            OutlinedTextField(
                value = goalHours,
                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) goalHours = it },
                label = { Text("Study Goal (Hours)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 3. Color
            Text("Subject Color", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selectedColor == color) 3.dp else 0.dp,
                                color = if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 4. Save
            Button(
                onClick = {
                    viewModel.saveSubject(
                        id = if (subjectId == -1) 0 else subjectId, // Pass ID
                        name = name,
                        goalHours = goalHours.toFloatOrNull() ?: 0f,
                        selectedColor = selectedColor,
                        onSuccess = onBackClick
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text(if (subjectId == -1) "Save Subject" else "Update Subject")
            }
        }
    }
}