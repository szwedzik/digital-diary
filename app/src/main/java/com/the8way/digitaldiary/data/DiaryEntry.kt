package com.the8way.digitaldiary.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val imageUri: String,
    var audioUri: String,
    val latitude: Double,
    val longitude: Double,
    val createdTime: Long = Date().time,
    val updatedTime: Long? = null
) : Parcelable
