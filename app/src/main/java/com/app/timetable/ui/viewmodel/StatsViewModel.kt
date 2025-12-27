package com.app.timetable.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.timetable.data.repository.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SubjectStat(
    val name: String,
    val studiedMillis: Long,
    val color: Color,
    val percentage: Float
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: TimetableRepository
) : ViewModel() {

    // Transform the list of Subjects into a list of Stats
    val stats: StateFlow<List<SubjectStat>> = repository.getAllSubjects()
        .map { subjects ->
            val totalStudied = subjects.sumOf { it.studiedTime }.toFloat()
            
            // Avoid division by zero
            val total = if (totalStudied == 0f) 1f else totalStudied

            subjects.map { subject ->
                SubjectStat(
                    name = subject.name,
                    studiedMillis = subject.studiedTime,
                    color = try {
                        Color(android.graphics.Color.parseColor(subject.color))
                    } catch (e: Exception) {
                        Color.Gray
                    },
                    percentage = subject.studiedTime / total
                )
            }.sortedByDescending { it.percentage } // Show biggest slices first
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}