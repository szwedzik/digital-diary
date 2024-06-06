package com.the8way.digitaldiary.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [DiaryEntry::class, Pin::class], version = 2, exportSchema = false)
abstract class DiaryEntryDatabase : RoomDatabase() {
    abstract fun diaryEntryDao(): DiaryEntryDao
    abstract fun pinDao(): PinDao

    companion object {
        @Volatile
        private var INSTANCE: DiaryEntryDatabase? = null

        fun getDatabase(context: Context): DiaryEntryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiaryEntryDatabase::class.java,
                    "digital_diary_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
