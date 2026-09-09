package com.example.slaveofmary.billing

/**
 * Kontroluje, czy wejście do subskrypcji ma być w ogóle pokazane w UI.
 * Na iOS ustawione na false do czasu zatwierdzenia aplikacji w App Store
 * i wdrożenia płatności — wtedy po prostu zmień na true.
 */
expect val isSubscriptionAvailable: Boolean