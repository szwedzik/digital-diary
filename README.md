# Digital Diary

Digital Diary is an Android application that allows users to create, edit, and manage diary entries with text, images, audio recordings, and location data. This project is developed using Kotlin, Dagger Hilt for dependency injection, Room for database management, and Google Maps API for location features.

## Features

- Add, edit, and delete diary entries
- Attach images to entries from the gallery or camera
- Draw on images with customizable brush size and color
- Record and attach audio notes to entries
- Automatically capture the user's location and display it on a map
- Persist data locally using Room database
- Dependency injection using Dagger Hilt

## Screenshots

### Main Screen
![Main Screen](path/to/main_screen_screenshot.png)

### Add Entry
![Add Entry](path/to/add_entry_screenshot.png)

### Edit Entry
![Edit Entry](path/to/edit_entry_screenshot.png)

### Draw on Image
![Draw on Image](path/to/draw_on_image_screenshot.png)

## Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/yourusername/digital-diary.git
    ```

2. Open the project in Android Studio.

3. Build the project to install all dependencies.

4. Run the project on an Android emulator or a physical device.

## Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 30 or later
- Google Maps API key (add to `AndroidManifest.xml` file) and replace `REPLACEME` with your API key:
    ```xml
    <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="REPLACEME"/>
    ```

## Dependencies

- [Dagger Hilt](https://dagger.dev/hilt/) for dependency injection
- [Room](https://developer.android.com/training/data-storage/room) for database management
- [Google Maps](https://developers.google.com/maps/documentation/android-sdk/overview) for location services
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) for asynchronous programming

## Getting Started

### Setting Up Dependency Injection

We use Dagger Hilt for dependency injection to provide instances of the Room database, DAOs, and other utilities.

```kotlin
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
```
### Initializing Sample Data

We initialize the database with sample data to showcase the application's functionality.

```kotlin
object AppInitializer {

    fun initializeDatabase(context: Context) {
        val database = DiaryEntryDatabase.getDatabase(context)
        val diaryEntryDao = database.diaryEntryDao()

        CoroutineScope(Dispatchers.IO).launch {
            diaryEntryDao.clearAll()
            val initialEntries = listOf(
                DiaryEntry(
                    title = "Welcome to Digital Diary",
                    content = "This is a sample entry to welcome you to Digital Diary. You can add your own entries by clicking on the + button below.",
                    imageUri = "",
                    audioUri = "",
                    latitude = 52.237049,
                    longitude = 21.017532,
                    createdTime = Date().time,
                    updatedTime = null
                ),
                DiaryEntry(
                    title = "Sample Entry",
                    content = "This is a sample entry. You can edit this entry by clicking on it.",
                    imageUri = "",
                    audioUri = "",
                    latitude = 40.758896,
                    longitude = -73.985130,
                    createdTime = Date().time,
                    updatedTime = null
                )
            )
            diaryEntryDao.insertAll(initialEntries)
        }
    }
}
```
## Key Components
### AddEntryActivity
Handles adding new diary entries, capturing images, recording audio, and fetching the user's current location.

### EditEntryActivity
Allows users to edit existing diary entries, including updating text, images, audio recordings, and locations.

## Permissions
The application requires the following permissions:

- ACCESS_FINE_LOCATION
- ACCESS_COARSE_LOCATION
- CAMERA
- RECORD_AUDIO
Permissions are requested at runtime for devices running Android M (API 23) or later.
- 
```kotlin
private fun checkPermissions() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
        requestPermissions()
    } else {
        getLocation()
    }
}
```

# Contributing
1. Fork the repository
2. Create your feature branch (git checkout -b feature/my-new-feature)
3. Commit your changes (git commit -am 'Add some feature')
4. Push to the branch (git push origin feature/my-new-feature)
5. Create a new Pull Request

# License
This project is licensed under the MIT License - see the [LICENSE](https://github.com/szwedzik/digital-diary?tab=MIT-1-ov-file) file for details.