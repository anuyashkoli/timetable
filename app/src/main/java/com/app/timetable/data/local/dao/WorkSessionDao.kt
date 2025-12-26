package com.app.timetable.data.local.dao

import android.se.omapi.Session
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.app.timetable.data.local.entity.WorkSession
import kotlinx.coroutines.flow.Flow
@Dao
interface WorkSessionDao {
    @Upsert
    suspend fun upsertWorkSession(session: WorkSession)

    @Delete
    suspend fun deleteWorkSession(session: WorkSession)

    @Query("SELECT * FROM workSessionTable ORDER BY dayOfWeek ASC")
    fun getAllSessions() : Flow<List<WorkSession>>

    @Query("SELECT * FROM workSessionTable WHERE dayOfWeek = :dayOfWeek")
    fun getSessionForDay(dayOfWeek: Int): Flow<List<WorkSession>>

}