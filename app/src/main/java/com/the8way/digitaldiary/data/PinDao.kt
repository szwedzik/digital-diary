package com.the8way.digitaldiary.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PinDao {
    @Query("SELECT * FROM pin_table LIMIT 1")
    suspend fun getPin(): Pin?

    @Insert
    suspend fun insertPin(pin: Pin)

    @Update
    suspend fun updatePin(pin: Pin)

    @Query("DELETE FROM pin_table")
    suspend fun deleteAllPins()
}