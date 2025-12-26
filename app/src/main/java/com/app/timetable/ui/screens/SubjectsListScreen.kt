package com.app.timetable.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.timetable.data.local.entity.Subject
import com.app.timetable.ui.viewmodel.SubjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsListScreen(
    viewModel: SubjectViewModel = hiltViewModel(),
    onAddSubjectClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val subjects by viewModel.subjects.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Subjects") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSubjectClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Subject")
            }
        }
    ) { innerPadding ->
        if (subjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No subjects yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(subjects) { subject ->
                    SubjectItem(
                        subject = subject,
                        onDeleteClick = { viewModel.deleteSubject(subject) }
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectItem(subject: Subject, onDeleteClick: () -> Unit) {
    val color = try {
        Color(android.graphics.Color.parseColor(subject.color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Color Dot
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(color, shape = MaterialTheme.shapes.small)
                )
                Spacer(modifier = Modifier.width(16.dp))

                // Details
                Column {
                    Text(text = subject.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Goal: ${subject.goalTime / 3600000} hours",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}