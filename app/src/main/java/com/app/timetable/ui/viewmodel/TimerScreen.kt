package com.app.timetable.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.timetable.ui.viewmodel.TimerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    taskId: Int,
    viewModel: TimerViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }

    val task by viewModel.task.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val totalTime by viewModel.totalTime.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Focus Mode") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = task?.title ?: "Loading...",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Circular Timer
            Box(contentAlignment = Alignment.Center) {
                CircularProgress(
                    progress = currentTime.toFloat() / totalTime.toFloat(),
                    modifier = Modifier.size(250.dp)
                )
                Text(
                    text = formatTimer(currentTime),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Controls
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                // Play/Pause Button
                Button(
                    onClick = { viewModel.toggleTimer() },
                    modifier = Modifier.size(80.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) Color(0xFFFFB74D) else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Check else Icons.Default.PlayArrow, // Using Check as "Pause" icon placeholder or import Pause
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Finish Button
                Button(
                    onClick = { viewModel.finishSession(onComplete = onBackClick) },
                    modifier = Modifier.size(80.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Finish")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Stop & Mark Complete", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun CircularProgress(progress: Float, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        val strokeWidth = 20.dp.toPx()

        // Track
        drawCircle(
            color = track,
            style = Stroke(width = strokeWidth)
        )

        // Progress
        drawArc(
            color = primary,
            startAngle = -90f,
            sweepAngle = 360 * progress,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = Size(size.width, size.height)
        )
    }
}

fun formatTimer(millis: Long): String {
    val seconds = millis / 1000
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}