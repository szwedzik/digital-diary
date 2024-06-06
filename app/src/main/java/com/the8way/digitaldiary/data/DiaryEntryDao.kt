package com.the8way.digitaldiary.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryEntryDao {
    @Insert
    suspend fun insert(diaryEntry: DiaryEntry)

    @Update
    suspend fun update(diaryEntry: DiaryEntry): Int

    @Query("SELECT * FROM diary_entries")
    fun getAllEntries(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE id = :id")
    suspend fun getEntryById(id: Int): DiaryEntry

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Int)

    @Insert
    suspend fun insertAll(diaryEntries: List<DiaryEntry>)

    @Query("DELETE FROM diary_entries")
    fun deleteAllEntries()
}
