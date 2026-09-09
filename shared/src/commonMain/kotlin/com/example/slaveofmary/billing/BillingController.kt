package com.example.slaveofmary.billing

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

interface BillingController {
    val monthlyPrice: StateFlow<String>
    val yearlyPrice: StateFlow<String>
    fun buyMonthly()
    fun buyYearly()
    fun restore()
    fun manage()
}

/**
 * Tworzy kontroler billingu dla bieżącej platformy.
 * Zwraca null, jeśli billing nie jest (jeszcze) obsługiwany — SubscriptionScreen
 * pokazuje wtedy komunikat "w przygotowaniu" zamiast przycisków zakupu.
 */
@Composable
expect fun rememberBillingController(): BillingController?