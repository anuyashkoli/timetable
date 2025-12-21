package com.app.timetable.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.timetable.data.local.entity.Subject
import com.app.timetable.data.local.entity.Task
import com.app.timetable.data.local.dao.TaskDao

@Database (
    entities = [Subject::class,Task::class],
    version = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
abstract fun taskDao(): TaskDao
}