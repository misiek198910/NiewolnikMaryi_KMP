package com.example.slaveofmary.util

/**
 * Prosty most do ustawiania języka aplikacji na poziomie platformy.
 * Implementacje per-platform w plikach LocaleManager.android.kt / LocaleManager.ios.kt.
 */
expect object LocaleManager {
    /** Ustawia i trwale zapisuje język aplikacji (kod ISO, np. "pl", "en"). */
    fun setLanguage(languageCode: String)

    /** Zwraca aktualnie obowiązujący (zapisany lub systemowy) język. */
    fun getCurrentLanguage(): String

    /**
     * Wymusza natychmiastowe zastosowanie zmiany języka w UI, jeśli platforma
     * na to pozwala (na Androidzie: rekreacja Activity). Wywołaj zaraz po setLanguage().
     */
    fun applyLanguageChange()
}