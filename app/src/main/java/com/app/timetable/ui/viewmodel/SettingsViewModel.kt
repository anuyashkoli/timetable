package com.app.timetable.ui.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.timetable.data.local.entity.WorkSession
import com.app.timetable.data.repository.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: TimetableRepository
) : ViewModel() {

    // Load existing sessions
    val sessions: StateFlow<List<WorkSession>> = repository.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveSession(dayOfWeek: Int, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        viewModelScope.launch {
            // We store time as "Minutes from midnight" or simple timestamps relative to a dummy date
            // For simplicity, let's just store simple millis for a generic day (e.g., 1970-01-01)
            // or just raw values if your logic expects that.
            // Here, I will store them as "Millis from start of day" to keep it simple.

            val startMillis = (startHour * 3600000L) + (startMinute * 60000L)
            val endMillis = (endHour * 3600000L) + (endMinute * 60000L)

            if (endMillis <= startMillis) {
                // Option A: Reject it (return)
                return@launch
                }

            val session = WorkSession(
                dayOfWeek = dayOfWeek,
                startTime = startMillis,
                endTime = endMillis
            )
            repository.insertSession(session)
        }
    }

    fun deleteWorkSession(session: WorkSession) {
        viewModelScope.launch {
            repository.deleteWorkSession(session)
        }
    }
}