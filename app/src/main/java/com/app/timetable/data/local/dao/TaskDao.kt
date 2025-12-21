package com.app.timetable.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.app.timetable.data.local.entity.Task
import kotlinx.coroutines.flow.Flow

@Dao

interface TaskDao {
    @Query("SELECT * FROM taskTable WHERE isCompleted = 0 ORDER BY priority ASC ") // Ascending; 1 is High and 5 is Low
    fun dueHighPriorityTasks(): Flow<List<Task>>
}