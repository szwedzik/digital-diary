package com.the8way.digitaldiary.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.the8way.digitaldiary.data.DiaryEntry
import com.the8way.digitaldiary.data.DiaryEntryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryEntryViewModel @Inject constructor(
    private val diaryEntryDao: DiaryEntryDao
) : ViewModel() {

    private val _diaryEntries = MutableLiveData<List<DiaryEntry>>()
    val diaryEntries: LiveData<List<DiaryEntry>> get() = _diaryEntries

    init {
        viewModelScope.launch {
            diaryEntryDao.getAllEntries().collect { entries ->
                _diaryEntries.value = entries.sortedBy { it.createdTime }
            }
        }
    }

    fun addDiaryEntry(diaryEntry: DiaryEntry) {
        viewModelScope.launch {
            diaryEntryDao.insert(diaryEntry)
        }
    }

    fun updateDiaryEntry(diaryEntry: DiaryEntry) {
        viewModelScope.launch {
            diaryEntryDao.update(diaryEntry)
        }
    }

    fun deleteEntryById(id: Int) {
        viewModelScope.launch {
            diaryEntryDao.deleteEntryById(id)
        }
    }

    suspend fun getEntryById(id: Int): DiaryEntry {
        return diaryEntryDao.getEntryById(id)
    }
}
