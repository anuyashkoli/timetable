package com.app.timetable.data.local.dao

import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.app.timetable.data.local.entity.Subject
import kotlinx.coroutines.flow.Flow

interface SubjectDao {
    @Upsert
    suspend fun upsertSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    @Query("SELECT * FROM subjectTable ORDER BY priority ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjectTable WHERE subjectID = :subjectID")
    fun getSubjectByID(subjectID: Int): Flow<Subject>
}