package com.app.timetable.data.repository

import com.app.timetable.data.local.dao.SubjectDao
import com.app.timetable.data.local.dao.TaskDao
import com.app.timetable.data.local.dao.WorkSessionDao
import com.app.timetable.data.local.entity.Subject
import com.app.timetable.data.local.entity.Task
import com.app.timetable.data.local.entity.WorkSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class TimetableRepository(
    private val subjectDao: SubjectDao,
    private val taskDao: TaskDao,
    private val workSessionDao: WorkSessionDao
) {

    // ----------------------
    // SUBJECTS
    // ----------------------
    fun getAllSubjects(): Flow<List<Subject>> = subjectDao.getAllSubjects()

    suspend fun insertSubject(subject: Subject) {
        subjectDao.upsertSubject(subject)
    }

    suspend fun deleteSubject(subject: Subject) {
        subjectDao.deleteSubject(subject)
    }

    // ----------------------
    // TASKS (With Priority Logic)
    // ----------------------
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    /**
     * Returns a Flow of tasks automatically sorted by your Dynamic Priority Logic.
     * This ensures the UI always observes the most important tasks at the top.
     */
    fun getSortedTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { tasks ->
            tasks.sortedByDescending { task ->
                calculateTaskScore(task)
            }
        }
    }

    suspend fun insertTask(task: Task) {
        taskDao.upsertTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    // ----------------------
    // WORK SESSIONS
    // ----------------------
    fun getAllSessions(): Flow<List<WorkSession>> = workSessionDao.getAllSessions()

    suspend fun insertSession(session: WorkSession) {
        workSessionDao.upsertWorkSession(session)
    }

    // ----------------------
    // ALGORITHM
    // ----------------------
    /**
     * Calculates a score based on Deadline Urgency and User Priority.
     * Higher Score = Higher Importance.
     */
    private fun calculateTaskScore(task: Task): Long {
        val currentTime = System.currentTimeMillis()

        // (6 - priority) assumes priority 1 is highest, 5 is lowest.
        // Logic: Priority 1 -> (6-1)*1000 = 5000 pts. Priority 5 -> 1000 pts.
        val importancePoints = (6 - task.priority) * 1000L

        val timeRemaining = task.deadline - currentTime

        return if (timeRemaining < 0) {
            // Overdue tasks get massive priority boost
            1_000_000L + importancePoints
        } else {
            // Urgency increases exponentially as deadline approaches
            // Adding +1 to denominator avoids division by zero
            val urgencyPoints = 100_000_000L / ((timeRemaining / 60000) + 1)
            importancePoints + urgencyPoints
        }
    }
}