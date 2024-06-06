package com.the8way.digitaldiary

import android.app.Application
import com.the8way.digitaldiary.data.DiaryEntryDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DiaryApplication : Application() {
    val database: DiaryEntryDatabase by lazy { DiaryEntryDatabase.getDatabase(this) }
}