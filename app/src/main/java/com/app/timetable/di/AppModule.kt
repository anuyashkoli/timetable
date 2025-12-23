package com.app.timetable.di

import android.content.Context
import androidx.room.Room
import com.app.timetable.data.local.AppDatabase
import com.app.timetable.data.repository.TimetableRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "timetable_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideTimetableRepository(db: AppDatabase): TimetableRepository {
        return TimetableRepository(
            subjectDao = db.subjectDao(),
            taskDao = db.taskDao(),
            workSessionDao = db.workSessionDao()
        )
    }
}