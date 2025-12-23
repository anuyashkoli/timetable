package com.app.timetable.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.app.timetable.data.local.entity.WorkSession
import kotlinx.coroutines.flow.Flow
@Dao
interface WorkSessionDao {
    @Upsert
    suspend fun upsertWorkSession(session: WorkSession)

    @Query("SELECT * FROM workSessionTable WHERE dayOfWeek = :dayOfWeek")
    fun getSessionForDay(dayOfWeek: Int): Flow<List<WorkSession>>

}