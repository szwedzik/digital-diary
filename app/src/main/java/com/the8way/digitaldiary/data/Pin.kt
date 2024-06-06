package com.the8way.digitaldiary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pin_table")
data class Pin(
    @PrimaryKey val id: Int = 1,
    val pinHash: String
)
