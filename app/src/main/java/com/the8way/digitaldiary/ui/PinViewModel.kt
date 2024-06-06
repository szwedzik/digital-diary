package com.the8way.digitaldiary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.the8way.digitaldiary.data.Pin
import com.the8way.digitaldiary.data.PinDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
    private val pinDao: PinDao
) : ViewModel() {

    suspend fun getPin(): Pin? = withContext(Dispatchers.IO) {
        pinDao.getPin()
    }

    fun setPin(pinHash: String) {
        viewModelScope.launch(Dispatchers.IO) {
            pinDao.insertPin(Pin(pinHash = pinHash))
        }
    }
}
