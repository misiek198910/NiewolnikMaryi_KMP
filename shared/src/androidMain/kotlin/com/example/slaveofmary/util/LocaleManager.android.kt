package com.example.slaveofmary.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.lang.ref.WeakReference
import java.util.Locale

/**
 * Jednorazowy uchwyt do Context/Activity, żeby LocaleManager mógł działać
 * jako obiekt (bez wstrzykiwania Context do każdej metody z commonMain).
 *
 * WAŻNE: wywołaj AndroidAppContextHolder.init(this) w MainActivity.onCreate()
 * PRZED super.onCreate() / attachBaseContext(), najlepiej w polu inicjalizowanym
 * jak najwcześniej — patrz przykład w MainActivity.kt.
 */
object AndroidAppContextHolder {
    lateinit var appContext: Context
    private var currentActivityRef: WeakReference<Activity>? = null

    fun init(activity: Activity) {
        appContext = activity.applicationContext
        currentActivityRef = WeakReference(activity)
    }

    fun currentActivity(): Activity? = currentActivityRef?.get()
}

private const val PREFS_NAME = "locale_prefs"
private const val KEY_LANGUAGE = "language_override"

private fun prefs(context: Context): SharedPreferences =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

actual object LocaleManager {

    actual fun setLanguage(languageCode: String) {
        val context = AndroidAppContextHolder.appContext

        // Zapis własny (używany też przez attachBaseContext na starszych API).
        prefs(context).edit().putString(KEY_LANGUAGE, languageCode).apply()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: natywne, systemowe wsparcie per-app language.
            // Zero dodatkowych zależności (to android.app.LocaleManager, nie AppCompat).
            val systemLocaleManager =
                context.getSystemService(android.app.LocaleManager::class.java)
            systemLocaleManager.applicationLocales = LocaleList.forLanguageTags(languageCode)
        }
        // < API 33: sam zapis w SharedPreferences nie zmienia jeszcze zasobów —
        // trzeba przeładować Activity, żeby attachBaseContext() nałożył nowy Locale
        // (patrz applyLanguageChange()).
    }

    actual fun getCurrentLanguage(): String {
        val context = AndroidAppContextHolder.appContext
        val saved = prefs(context).getString(KEY_LANGUAGE, null)
        if (saved != null) return saved

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val systemLocaleManager =
                context.getSystemService(android.app.LocaleManager::class.java)
            val locales = systemLocaleManager.applicationLocales
            if (!locales.isEmpty) locales[0]?.language ?: Locale.getDefault().language
            else Locale.getDefault().language
        } else {
            Locale.getDefault().language
        }
    }

    actual fun applyLanguageChange() {
        // Rekreacja Activity ujednolica zachowanie na wszystkich wersjach API:
        // - < 33: dopiero teraz attachBaseContext() nałoży zapisany Locale,
        // - 33+: system zwykle i tak by to zrobił, ale wymuszamy dla spójności i szybkości.
        AndroidAppContextHolder.currentActivity()?.recreate()
    }

    /**
     * Owija bazowy Context tak, by jego zasoby (stringi, itp.) były w zapisanym
     * języku. Wywołaj w MainActivity.attachBaseContext() — patrz przykład w MainActivity.kt.
     * Dotyczy głównie API < 33 (na 33+ system i tak zarządza tym sam, ale owinięcie
     * nie szkodzi i ujednolica zachowanie).
     */
    fun wrapContext(base: Context): Context {
        val saved = prefs(base).getString(KEY_LANGUAGE, null) ?: return base

        val locale = Locale(saved)
        Locale.setDefault(locale)

        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLocales(LocaleList(locale))

        return base.createConfigurationContext(config)
    }
}