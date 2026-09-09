package com.example.slaveofmary.data.db

import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.slaveofmary.util.AndroidAppContextHolder

// Reużywamy AndroidAppContextHolder (ustawiony już w MainActivity.onCreate
// na potrzeby LocaleManager) zamiast trzymać osobny, drugi globalny Context.
actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val appContext = AndroidAppContextHolder.appContext
    val dbFile = appContext.getDatabasePath("slaveofmary_db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}