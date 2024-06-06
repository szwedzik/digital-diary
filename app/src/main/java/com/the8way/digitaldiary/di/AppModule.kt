package com.the8way.digitaldiary.di

import android.content.Context
import com.the8way.digitaldiary.data.DiaryEntryDao
import com.the8way.digitaldiary.data.DiaryEntryDatabase
import com.the8way.digitaldiary.data.PinDao
import com.the8way.digitaldiary.utils.LocationUtils
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
    fun provideDiaryEntryDatabase(
        @ApplicationContext appContext: Context
    ): DiaryEntryDatabase {
        return DiaryEntryDatabase.getDatabase(appContext)
    }

    @Provides
    @Singleton
    fun provideDiaryEntryDao(database: DiaryEntryDatabase): DiaryEntryDao {
        return database.diaryEntryDao()
    }

    @Provides
    @Singleton
    fun providePinDao(database: DiaryEntryDatabase): PinDao {
        return database.pinDao()
    }

    @Provides
    @Singleton
    fun provideLocationUtils(
        @ApplicationContext appContext: Context
    ): LocationUtils {
        return LocationUtils(appContext)
    }
}
