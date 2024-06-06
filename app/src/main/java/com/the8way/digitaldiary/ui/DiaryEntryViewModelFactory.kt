package com.the8way.digitaldiary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.the8way.digitaldiary.data.DiaryEntryDao

class DiaryEntryViewModelFactory(private val diaryEntryDao: DiaryEntryDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiaryEntryViewModel::class.java)) {
            return DiaryEntryViewModel(diaryEntryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
