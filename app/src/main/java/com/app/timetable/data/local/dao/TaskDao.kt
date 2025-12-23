package com.app.timetable.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.app.timetable.data.local.entity.Task
import kotlinx.coroutines.flow.Flow

@Dao

interface TaskDao {
    @Upsert
    suspend fun upsertTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM taskTable ORDER BY priority ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM taskTable WHERE isCompleted = 0 ORDER BY priority ASC ") // Ascending; 1 is High and 5 is Low
    fun dueHighPriorityTasks(): Flow<List<Task>>

    @Query("SELECT * FROM taskTable WHERE isCompleted = 0 ORDER BY priority ASC") // Query to get all incomplete tasks for scheduling
    fun getIncompleteTasks(): Flow<List<Task>>
}