package com.example.slaveofmary.billing

import kotlinx.coroutines.runBlocking

/**
 * Synchroniczny odczyt statusu premium do użycia ze Swift (AppOpenAdHelper) — wywoływany
 * raz, na starcie aplikacji, zanim jeszcze rysuje się UI, więc krótkie zablokowanie
 * wątku na czas lokalnego odczytu z Room jest tu bezpieczne.
 */
fun isPremiumUserBlocking(): Boolean = runBlocking { isPremiumUserNow() }
