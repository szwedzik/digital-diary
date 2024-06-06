package com.the8way.digitaldiary.utils

import android.content.Context
import com.the8way.digitaldiary.data.DiaryEntry
import com.the8way.digitaldiary.data.DiaryEntryDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

object AppInitializer {

    fun initializeDatabase(context: Context) {
        val database = DiaryEntryDatabase.getDatabase(context)
        val diaryEntryDao = database.diaryEntryDao()
        val pinDao = database.pinDao()


        CoroutineScope(Dispatchers.IO).launch {

            diaryEntryDao.deleteAllEntries()
            pinDao.deleteAllPins()

            val initialEntries = listOf(
                DiaryEntry(
                    title = "Warsaw #1",
                    content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc in ex mollis, placerat erat et, lobortis enim. Sed vitae ante venenatis, consequat eros vel, laoreet odio. Donec tincidunt felis in semper vehicula. Sed id finibus erat, vel dignissim erat. Mauris ullamcorper lectus a dui elementum blandit. Nulla eget ornare enim. Maecenas a dui augue. Nunc laoreet mattis arcu, a scelerisque leo. Sed in urna in risus imperdiet accumsan sed ut nulla. Vivamus quis nibh enim. Aenean dignissim ornare lectus, sit amet suscipit turpis accumsan vitae. Pellentesque euismod, nisl sit amet porta tempus, magna odio semper ex, sit amet gravida velit nibh vitae eros. Aliquam nisi est, pellentesque in semper venenatis, lacinia vitae orci.",
                    imageUri = "",
                    audioUri = "",
                    latitude = 52.237049,
                    longitude = 21.017532,
                    createdTime = Date().time,
                    updatedTime = null
                ),
                DiaryEntry(
                    title = "New York #1",
                    content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc in ex mollis, placerat erat et, lobortis enim. Sed vitae ante venenatis, consequat eros vel, laoreet odio. Donec tincidunt felis in semper vehicula. Sed id finibus erat, vel dignissim erat. Mauris ullamcorper lectus a dui elementum blandit. Nulla eget ornare enim. Maecenas a dui augue. Nunc laoreet mattis arcu, a scelerisque leo. Sed in urna in risus imperdiet accumsan sed ut nulla. Vivamus quis nibh enim. Aenean dignissim ornare lectus, sit amet suscipit turpis accumsan vitae. Pellentesque euismod, nisl sit amet porta tempus, magna odio semper ex, sit amet gravida velit nibh vitae eros. Aliquam nisi est, pellentesque in semper venenatis, lacinia vitae orci.",
                    imageUri = "",
                    audioUri = "",
                    latitude = 40.758896,
                    longitude = -73.985130,
                    createdTime = Date().time,
                    updatedTime = null
                ),
                DiaryEntry(
                    title = "Moscow #1",
                    content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc in ex mollis, placerat erat et, lobortis enim. Sed vitae ante venenatis, consequat eros vel, laoreet odio. Donec tincidunt felis in semper vehicula. Sed id finibus erat, vel dignissim erat. Mauris ullamcorper lectus a dui elementum blandit. Nulla eget ornare enim. Maecenas a dui augue. Nunc laoreet mattis arcu, a scelerisque leo. Sed in urna in risus imperdiet accumsan sed ut nulla. Vivamus quis nibh enim. Aenean dignissim ornare lectus, sit amet suscipit turpis accumsan vitae. Pellentesque euismod, nisl sit amet porta tempus, magna odio semper ex, sit amet gravida velit nibh vitae eros. Aliquam nisi est, pellentesque in semper venenatis, lacinia vitae orci.",
                    imageUri = "",
                    audioUri = "",
                    latitude = 55.751244,
                    longitude = 37.618423,
                    createdTime = Date().time,
                    updatedTime = null
                ),
                DiaryEntry(
                    title = "Washington DC #1",
                    content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc in ex mollis, placerat erat et, lobortis enim. Sed vitae ante venenatis, consequat eros vel, laoreet odio. Donec tincidunt felis in semper vehicula. Sed id finibus erat, vel dignissim erat. Mauris ullamcorper lectus a dui elementum blandit. Nulla eget ornare enim. Maecenas a dui augue. Nunc laoreet mattis arcu, a scelerisque leo. Sed in urna in risus imperdiet accumsan sed ut nulla. Vivamus quis nibh enim. Aenean dignissim ornare lectus, sit amet suscipit turpis accumsan vitae. Pellentesque euismod, nisl sit amet porta tempus, magna odio semper ex, sit amet gravida velit nibh vitae eros. Aliquam nisi est, pellentesque in semper venenatis, lacinia vitae orci.",
                    imageUri = "",
                    audioUri = "",
                    latitude = 38.894207,
                    longitude = -77.035507,
                    createdTime = Date().time,
                    updatedTime = null
                )
            )
            diaryEntryDao.insertAll(initialEntries)
        }
    }
}