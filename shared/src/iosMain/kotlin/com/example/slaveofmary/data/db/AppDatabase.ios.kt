package com.example.slaveofmary.data.db

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val documentsDirectory = NSFileManager.defaultManager.URLsForDirectory(
        directory = NSDocumentDirectory,
        inDomains = NSUserDomainMask
    ).first() as NSURL

    val dbFilePath = requireNotNull(documentsDirectory.path) {
        "Nie udało się wyznaczyć katalogu Documents"
    } + "/slaveofmary_db"

    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath
    )
}