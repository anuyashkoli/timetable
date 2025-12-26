package com.app.timetable.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.timetable.data.local.entity.Subject
import com.app.timetable.data.repository.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color

@HiltViewModel
class SubjectViewModel @Inject constructor(
    private val repository: TimetableRepository
) : ViewModel() {

    fun saveSubject(
        name: String,
        goalHours: Float,
        selectedColor: Color,
        onSuccess: () -> Unit
    ) {
        if (name.isBlank()) return

        viewModelScope.launch {
            // Convert goal hours to milliseconds
            val goalInMillis = (goalHours * 60 * 60 * 1000).toLong()

            // Convert Color object to Hex String (e.g., "#FF0000")
            val colorString = String.format("#%06X", (0xFFFFFF and selectedColor.toArgb()))

            val newSubject = Subject(
                name = name,
                priority = 1.0f, // Default priority, can be adjusted later
                color = colorString,
                goalTime = goalInMillis,
                remainingTime = goalInMillis, // Initially, remaining = goal
                studiedTime = 0L,
                deadline = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000) // Default 1 week, can be updated
            )

            repository.insertSubject(newSubject)
            onSuccess()
        }
    }
}