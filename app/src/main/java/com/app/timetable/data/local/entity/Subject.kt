package com.app.timetable.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjectTable")
data class Subject (
    @PrimaryKey(autoGenerate = true) val subjectID: Int = 0,
    val name: String,
    val priority: Float,
    val color: String,
    val remainingTime: Long,
    val totalStudyHours: Float,
    val deadline: Long
)