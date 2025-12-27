package com.app.timetable.ui.viewmodel

import androidx.compose.remote.creation.first
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.timetable.data.local.entity.Task
import com.app.timetable.data.local.entity.WorkSession
import com.app.timetable.data.repository.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Calendar

data class HomeUiState(
    val tasks: List<Task> = emptyList(),
    val currentSession: WorkSession? = null,
    val nextSession: WorkSession? = null,
    val recommendedTask: Task? = null,
    val isStudyTime: Boolean = false
)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TimetableRepository
) : ViewModel() {

    private val _tasks = repository.getSortedTasks()
    private val _sessions = repository.getAllSessions()

    private val _ticker = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(60000) // Update every minute
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(_tasks, _sessions, _ticker) { tasks, sessions, _ ->
        calculateRecommendation(tasks, sessions)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    private fun calculateRecommendation(tasks: List<Task>, sessions: List<WorkSession>): HomeUiState {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)

        // Calculate minutes from midnight (e.g., 10:30 AM = 10*60 + 30 = 630 min)
        // Note: Your WorkSessions store millis, so we compare millis.
        // We need "Millis from start of day" for current time.
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentMillisOfDay = (currentHour * 3600000L) + (currentMinute * 60000L)

        // A. Check if we are currently in a session
        val activeSession = sessions.find { session ->
            session.dayOfWeek == currentDay &&
                    currentMillisOfDay >= session.startTime &&
                    currentMillisOfDay < session.endTime
        }

        // B. Find the next upcoming session (Today)
        // If needed, you can expand this logic to check "Tomorrow" if null
        val nextSession = sessions.filter { session ->
            session.dayOfWeek == currentDay && session.startTime > currentMillisOfDay
        }.minByOrNull { it.startTime }

        // C. Pick best task (First uncompleted task)
        val bestTask = tasks.firstOrNull { !it.isCompleted }

        return HomeUiState(
            tasks = tasks,
            currentSession = activeSession,
            nextSession = nextSession,
            recommendedTask = bestTask,
            isStudyTime = activeSession != null
        )
    }
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
            // 1. Update the Task status
            repository.insertTask(task.copy(isCompleted = isCompleted))

            // 2. Update the Subject Progress
            if (task.subjectID != 0) {
                try {
                    // Get current subjects list
                    val allSubjects = repository.getAllSubjects().first()
                    val subject = allSubjects.find { it.subjectID == task.subjectID }

                    if (subject != null) {
                        // Calculate duration in millis (Default 60 mins -> 3,600,000 ms)
                        val taskDurationMillis = if (task.duration > 0) task.duration * 60000L else 3600000L

                        // If Checking -> Add Time. If Unchecking -> Remove Time.
                        val newStudied = if (isCompleted) {
                            subject.studiedTime + taskDurationMillis
                        } else {
                            (subject.studiedTime - taskDurationMillis).coerceAtLeast(0L)
                        }

                        // Recalculate remaining
                        val newRemaining = (subject.goalTime - newStudied).coerceAtLeast(0L)

                        // Save Subject
                        repository.insertSubject(subject.copy(
                            studiedTime = newStudied,
                            remainingTime = newRemaining
                        ))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
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