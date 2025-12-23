package com.app.timetable.data.repository

import com.app.timetable.data.local.dao.TaskDao
import com.app.timetable.data.local.dao.SubjectDao
import com.app.timetable.data.local.dao.WorkSessionDao
import com.app.timetable.data.local.entity.Subject
import com.app.timetable.data.local.entity.Task

class TimetableRepository (
    private val subjectDao: SubjectDao,
    private val taskDao: TaskDao,
    private val workSessionDao: WorkSessionDao
)

private fun calculateTaskScore(task: Task): Long {
    val currentTime = System.currentTimeMillis()
    val importancePoints = (6 - task.priority) * 1000L

    val timeRemaining = task.deadline - currentTime

    return if (timeRemaining < 0) {
        1_000_000L + importancePoints
    } else {
        val urgencyPoints = 100_000_000L / ((timeRemaining / 60000) + 1)
        importancePoints + urgencyPoints
    }
}

// Helper Functions
data class TaskWithSubjects (
    val task: Task,
    val subject: Subject
)