package com.app.timetable.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.app.timetable.data.local.entity.Task
import com.app.timetable.data.local.entity.Subject
import com.app.timetable.data.repository.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val repository: TimetableRepository
) : ViewModel() {

    private val _task = MutableStateFlow<Task?>(null)
    val task = _task.asStateFlow()

    // Timer State
    private val _currentTime = MutableStateFlow(0L) // Current milliseconds left
    val currentTime = _currentTime.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _totalTime = MutableStateFlow(1L) // Total duration for progress bar
    val totalTime = _totalTime.asStateFlow()

    private var timerJob: Job? = null
    private var sessionStudyTime = 0L // Track how long user actually studied in this session

    fun loadTask(taskId: Int) {
        viewModelScope.launch {
            repository.getAllTasks().collect { tasks ->
                val foundTask = tasks.find { it.taskID == taskId }
                if (foundTask != null && _task.value == null) {
                    _task.value = foundTask
                    // Initialize timer with task duration (default 60 mins if 0)
                    val durationMillis = if (foundTask.duration > 0) foundTask.duration * 60000L else 60 * 60000L
                    _totalTime.value = durationMillis
                    _currentTime.value = durationMillis
                }
            }
        }
    }

    fun toggleTimer() {
        if (_isPlaying.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _isPlaying.value = true
        timerJob = viewModelScope.launch {
            while (_currentTime.value > 0) {
                delay(1000)
                _currentTime.value -= 1000
                sessionStudyTime += 1000
            }
            _isPlaying.value = false
            // Optional: Auto-finish when timer hits 0
        }
    }

    private fun pauseTimer() {
        _isPlaying.value = false
        timerJob?.cancel()
    }

    fun finishSession(onComplete: () -> Unit) {
        pauseTimer()
        viewModelScope.launch {
            val currentTask = _task.value ?: return@launch

            // 1. Mark Task as Completed
            repository.insertTask(currentTask.copy(isCompleted = true))

            // 2. Update Subject Statistics
            if (currentTask.subjectID != 0 && sessionStudyTime > 0) {
                try {
                    // Use 'first()' to get the current list just once
                    val allSubjects = repository.getAllSubjects().first()
                    val subject = allSubjects.find { it.subjectID == currentTask.subjectID }

                    if (subject != null) {
                        val newStudied = subject.studiedTime + sessionStudyTime
                        val newRemaining = (subject.goalTime - newStudied).coerceAtLeast(0L)

                        val updatedSubject = subject.copy(
                            studiedTime = newStudied,
                            remainingTime = newRemaining
                        )
                        repository.insertSubject(updatedSubject)
                    }
                } catch (e: Exception) {
                    // Handle empty flow or errors
                    e.printStackTrace()
                }
            }

            // 3. Reset session tracker
            sessionStudyTime = 0L

            onComplete()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}