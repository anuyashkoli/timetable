package com.app.timetable.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.timetable.ui.viewmodel.StatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val stats by viewModel.stats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Statistics") },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (stats.isEmpty() || stats.all { it.studiedMillis == 0L }) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No study data yet!", color = Color.Gray)
                }
            } else {
                // 1. The Pie Chart
                PieChart(
                    data = stats,
                    modifier = Modifier
                        .size(250.dp)
                        .padding(24.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text("Breakdown", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // 2. The List Legend
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(stats) { stat ->
                        if (stat.studiedMillis > 0) {
                            StatItem(stat)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(
    data: List<com.app.timetable.ui.viewmodel.SubjectStat>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val radius = minOf(width, height) / 2
        val strokeWidth = 40.dp.toPx()

        var startAngle = -90f

        data.forEach { stat ->
            val sweepAngle = stat.percentage * 360f
            
            // Draw Arc
            drawArc(
                color = stat.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false, // Makes it a donut chart
                style = Stroke(width = strokeWidth),
                size = Size(width - strokeWidth, height - strokeWidth),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
fun StatItem(stat: com.app.timetable.ui.viewmodel.SubjectStat) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(stat.color)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(stat.name, style = MaterialTheme.typography.bodyLarge)
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${(stat.percentage * 100).toInt()}%", 
                fontWeight = FontWeight.Bold
            )
            Text(
                formatDuration(stat.studiedMillis),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

fun formatDuration(millis: Long): String {
    val hours = millis / 3600000
    val mins = (millis % 3600000) / 60000
    return "${hours}h ${mins}m"
}