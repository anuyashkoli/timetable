package com.app.timetable.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "taskTable")
data class Task (
    @PrimaryKey(autoGenerate = true) val taskID: Int = 0,
    val title: String,
    val subjectID: Int,
    val priority: Int,
    val deadline: Long,
    val isCompleted: Boolean,
    val duration: String
)