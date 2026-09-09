package com.example.slaveofmary.data.repository

import com.example.slaveofmary.data.dao.SubscriptionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SubscriptionRepository(private val dao: SubscriptionDao) {

    /** true = premium aktywne, false = brak, null dopóki jeszcze nie odczytano z bazy. */
    val isPremiumFlow: Flow<Boolean?> = dao.observePremiumStatus()

    suspend fun getSavedStatus() = dao.getStatus()
}