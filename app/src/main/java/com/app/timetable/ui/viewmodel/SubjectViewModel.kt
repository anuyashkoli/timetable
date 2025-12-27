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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SubjectViewModel @Inject constructor(
    private val repository: TimetableRepository
) : ViewModel() {

    val subjects: StateFlow<List<Subject>> = repository.getAllSubjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    // 2. State for Editing (NEW)
    private val _subjectState = MutableStateFlow<Subject?>(null)
    val subjectState = _subjectState.asStateFlow()

    // 3. Load Subject by ID (NEW)
    fun loadSubject(subjectId: Int) {
        if (subjectId == -1) {
            _subjectState.value = null
            return
        }
        viewModelScope.launch {
            repository.getAllSubjects().collect { list ->
                _subjectState.value = list.find { it.subjectID == subjectId }
            }
        }
    }
    fun saveSubject(
        id: Int = 0,
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

            // Preserves existing stats if updating, starts from scratch if creating new subject
            val currentSubject = _subjectState.value

            val subjectToSave = if (id != 0 && currentSubject != null) {
                // UPDATE: Keep existing progress
                currentSubject.copy(
                    name = name,
                    goalTime = goalInMillis,
                    color = colorString,
                    // Recalculate remaining based on new goal, but keep studied time
                    remainingTime = (goalInMillis - currentSubject.studiedTime).coerceAtLeast(0L)
                )
            } else {
                // CREATE: New Subject
                Subject(
                    subjectID = 0,
                    name = name,
                    priority = 1.0f,
                    color = colorString,
                    goalTime = goalInMillis,
                    remainingTime = goalInMillis,
                    studiedTime = 0L,
                    deadline = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)
                )
            }

            repository.insertSubject(subjectToSave)
            onSuccess()
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }
}