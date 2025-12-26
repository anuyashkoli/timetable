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
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.timetable.ui.viewmodel.SubjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubjectScreen(
    viewModel: SubjectViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var goalHours by remember { mutableStateOf("10") }
    var selectedColor by remember { mutableStateOf(Color(0xFFEF9A9A)) } // Default Red-ish

    // Pre-defined colors for the user to pick
    val colors = listOf(
        Color(0xFFEF9A9A), // Red
        Color(0xFFF48FB1), // Pink
        Color(0xFFCE93D8), // Purple
        Color(0xFF9FA8DA), // Indigo
        Color(0xFF90CAF9), // Blue
        Color(0xFF80CBC4), // Teal
        Color(0xFFA5D6A7), // Green
        Color(0xFFFFCC80), // Orange
        Color(0xFFBCAAA4)  // Brown
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Subject") },
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
            // 1. Subject Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Subject Name (e.g. Math)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 2. Goal Hours
            OutlinedTextField(
                value = goalHours,
                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) goalHours = it },
                label = { Text("Study Goal (Hours)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 3. Color Picker
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

            // 4. Save Button
            Button(
                onClick = {
                    viewModel.saveSubject(
                        name = name,
                        goalHours = goalHours.toFloatOrNull() ?: 0f,
                        selectedColor = selectedColor,
                        onSuccess = onBackClick
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text("Save Subject")
            }
        }
    }
}