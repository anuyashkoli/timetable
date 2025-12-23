package com.app.timetable.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.timetable.data.local.dao.SubjectDao
import com.app.timetable.data.local.entity.Subject
import com.app.timetable.data.local.entity.Task
import com.app.timetable.data.local.entity.WorkSession
import com.app.timetable.data.local.dao.TaskDao
import com.app.timetable.data.local.dao.WorkSessionDao

@Database (
    entities = [Subject::class,Task::class, WorkSession::class],
    version = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
  abstract fun taskDao(): TaskDao
  abstract fun subjectDao(): SubjectDao
  abstract fun workSessionDao(): WorkSessionDao
}