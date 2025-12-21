package com.app.timetable.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workSessionTable")
data class WorkSession (
    @PrimaryKey(autoGenerate = true) val workSessionID: Int = 0,
    val dayOfWeek: Int,
    val startTime: Long,
    val endTime: Long
)