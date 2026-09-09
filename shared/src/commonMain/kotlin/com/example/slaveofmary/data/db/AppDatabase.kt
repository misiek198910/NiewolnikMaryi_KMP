package com.example.slaveofmary.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.slaveofmary.data.dao.SubscriptionDao
import com.example.slaveofmary.data.entity.SubscriptionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [SubscriptionEntity::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao
}

// Implementacje tego obiektu generuje procesor Room (KSP) osobno dla każdej
// platformy — NIE pisz tu `actual` ręcznie. Wymaga dodania zależności
// "androidx.room:room-compiler" jako ksp/kspAndroid/kspIosX64 itd. w build.gradle.kts.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

private fun buildDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(true)
        .build()
}

/** Pojedyncza, leniwie tworzona instancja bazy — analogicznie do RemoteClient.apiService. */
object AppDatabaseProvider {
    val database: AppDatabase by lazy {
        buildDatabase(getDatabaseBuilder())
    }
}