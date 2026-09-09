package com.example.slaveofmary.util

import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

actual object LocaleManager {
    actual fun setLanguage(languageCode: String) {
        val defaults = NSUserDefaults.standardUserDefaults
        defaults.setObject(listOf(languageCode), forKey = "AppleLanguages")
        defaults.synchronize()
    }

    actual fun getCurrentLanguage(): String {
        return (NSLocale.preferredLanguages.firstOrNull() as? String)
            ?.substringBefore("-")
            ?: "pl"
    }

    actual fun applyLanguageChange() {
    }
}