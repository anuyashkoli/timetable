package com.app.timetable.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.timetable.data.local.entity.Task
import com.app.timetable.data.repository.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TimetableRepository
) : ViewModel() {

    /**
     * A "Hot" Flow that holds the list of tasks.
     * It is automatically sorted by the logic in the Repository.
     * 'stateIn' converts the Flow to a StateFlow, which is efficient for Compose.
     */
    val tasks: StateFlow<List<Task>> = repository.getSortedTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Updates the completion status of a task.
     * Since our Repository uses 'upsert', inserting a task with the same ID updates it.
     */
    fun onTaskCheckedChange(task: Task, isCompleted: Boolean) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = isCompleted)
            repository.insertTask(updatedTask)
        }
    }

    /**
     * Deletes a task permanently.
     */
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }
}