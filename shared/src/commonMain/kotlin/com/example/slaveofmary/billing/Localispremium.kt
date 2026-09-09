package com.example.slaveofmary.billing

import androidx.compose.runtime.compositionLocalOf

/**
 * Globalny status "czy użytkownik ma aktywne premium", dostępny w całym drzewie
 * kompozycji bez przekazywania go ręcznie przez parametry każdego ekranu.
 * Ustawiany raz w App() na podstawie SubscriptionRepository.isPremiumFlow.
 *
 * Używamy compositionLocalOf (nie staticCompositionLocalOf), bo ta wartość
 * faktycznie się zmienia w czasie działania aplikacji (np. tuż po zakupie),
 * a nie jest ustawiana raz i zapomniana.
 */
val LocalIsPremium = compositionLocalOf { false }