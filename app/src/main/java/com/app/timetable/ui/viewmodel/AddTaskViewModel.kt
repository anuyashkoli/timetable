package com.app.timetable.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.timetable.data.local.entity.Subject
import com.app.timetable.data.local.entity.Task
import com.app.timetable.data.repository.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTaskViewModel @Inject constructor(
    private val repository: TimetableRepository
) : ViewModel() {

    // fetch subjects so user can select one in the dropdown
    val subjects: StateFlow<List<Subject>> = repository.getAllSubjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveTask(
        title: String,
        subjectId: Int,
        priority: Int,
        deadline: Long,
        onSuccess: () -> Unit
    ) {
        if (title.isBlank()) return // Simple validation

        viewModelScope.launch {
            val newTask = Task(
                title = title,
                subjectID = subjectId,
                priority = priority,
                deadline = deadline,
                isCompleted = false,
                duration = 60, // Default duration 1 hour (can make this editable later)
                moduleName = ""
            )
            repository.insertTask(newTask)
            onSuccess()
        }
    }
}