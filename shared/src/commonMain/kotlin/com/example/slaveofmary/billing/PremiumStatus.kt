package com.example.slaveofmary.billing

import com.example.slaveofmary.data.db.AppDatabaseProvider

/**
 * Jednorazowy odczyt statusu premium spoza modułu `shared` (np. z `MainActivity` w
 * `androidApp`, który nie ma Room na własnym classpath) — nie wystawia typu `AppDatabase`,
 * więc wywołujący nie musi widzieć Room w swoim module.
 */
suspend fun isPremiumUserNow(): Boolean =
    AppDatabaseProvider.database.subscriptionDao().getStatus()?.isPremium == true
