package com.example.slaveofmary

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.slaveofmary.billing.LocalIsPremium
import com.example.slaveofmary.data.db.AppDatabaseProvider
import com.example.slaveofmary.data.repository.SubscriptionRepository
import com.example.slaveofmary.ui.screens.InformationScreen
import com.example.slaveofmary.ui.screens.MainScreen
import com.example.slaveofmary.ui.screens.ModlitwyScreen
import com.example.slaveofmary.ui.screens.NowennyScreen
import com.example.slaveofmary.ui.screens.PrivacyPolicyScreen
import com.example.slaveofmary.ui.screens.SettingsScreen
import com.example.slaveofmary.ui.screens.ShowTextScreen
import com.example.slaveofmary.ui.screens.SplashScreen
import com.example.slaveofmary.ui.screens.SubscriptionScreen
import com.example.slaveofmary.ui.screens.TraktatScreen
import com.example.slaveofmary.ui.theme.AppColors
import com.example.slaveofmary.ui.theme.AppTheme
import com.russhwolf.settings.Settings

sealed class Screen {
    object Splash : Screen()
    object Main : Screen()
    object Traktat : Screen()
    object Modlitwy : Screen()
    object Nowenny : Screen()
    object Settings : Screen()
    object Information : Screen()
    object Subscription : Screen()
    object PrivacyPolicy : Screen()
    data class ShowText(val file: String, val title: String, val previousScreen: Screen) : Screen()
}

@Composable
fun App() {

    val settings = remember { Settings() }

    LaunchedEffect(Unit) {
        migrateOldAppData(settings)
    }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

    val subscriptionRepository = remember {
        SubscriptionRepository(AppDatabaseProvider.database.subscriptionDao())
    }
    val isPremium by subscriptionRepository.isPremiumFlow.collectAsState(initial = null)

    AppTheme {
        CompositionLocalProvider(LocalIsPremium provides (isPremium == true)) {
            // Surface (przezroczysty) ustawia domyślny kolor treści na jasny —
            // dzięki temu nieostylowany Text nie wychodzi czarny na ciemnym tle.
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent,
                contentColor = AppColors.textPrimary
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.appBackgroundBrush)
                ) {
                    AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(280)) +
                                slideInHorizontally(animationSpec = tween(280)) { width -> width / 8 }) togetherWith
                                fadeOut(animationSpec = tween(180))
                    },
                    label = "screenTransition"
                ) { screen ->
                    when (screen) {
                        is Screen.Splash -> {
                            SplashScreen(
                                onNavigateToMain = { currentScreen = Screen.Main }
                            )
                        }
                        is Screen.Main -> {
                            MainScreen(
                                onNavigateToTraktat = { currentScreen = Screen.Traktat },
                                onNavigateToModlitwy = { currentScreen = Screen.Modlitwy },
                                onNavigateToNowenny = { currentScreen = Screen.Nowenny },
                                onNavigateToSettings = { currentScreen = Screen.Settings },
                                onNavigateToInformacje = { currentScreen = Screen.Information }
                            )
                        }
                        is Screen.Traktat -> {
                            TraktatScreen(
                                onNavigateToText = { fileName, title ->
                                    currentScreen = Screen.ShowText(
                                        file = fileName,
                                        title = title,
                                        previousScreen = Screen.Traktat
                                    )
                                },
                                onNavigateBack = { currentScreen = Screen.Main }
                            )
                        }
                        is Screen.Modlitwy -> {
                            ModlitwyScreen(
                                onNavigateToText = { fileName, title ->
                                    currentScreen = Screen.ShowText(
                                        file = fileName,
                                        title = title,
                                        previousScreen = Screen.Modlitwy
                                    )
                                },
                                onNavigateBack = { currentScreen = Screen.Main }
                            )
                        }
                        is Screen.ShowText -> {
                            ShowTextScreen(
                                fileName = screen.file,
                                title = screen.title,
                                onNavigateBack = { currentScreen = screen.previousScreen }
                            )
                        }
                        is Screen.Nowenny -> {
                            NowennyScreen(
                                onNavigateToText = { fileName, title ->
                                    currentScreen = Screen.ShowText(
                                        file = fileName,
                                        title = title,
                                        previousScreen = Screen.Nowenny
                                    )
                                },
                                onNavigateBack = { currentScreen = Screen.Main }
                            )
                        }
                        is Screen.Settings -> {
                            SettingsScreen(
                                onNavigateBack = { currentScreen = Screen.Main },
                                onNavigateToSubscription = { currentScreen = Screen.Subscription },
                                onNavigateToPrivacyPolicy = { currentScreen = Screen.PrivacyPolicy }
                            )
                        }
                        is Screen.Information -> {
                            InformationScreen(
                                onNavigateBack = { currentScreen = Screen.Main }
                            )
                        }
                        is Screen.Subscription -> {
                            SubscriptionScreen(
                                onNavigateBack = { currentScreen = Screen.Settings }
                            )
                        }
                        is Screen.PrivacyPolicy -> {
                            PrivacyPolicyScreen(
                                onNavigateBack = { currentScreen = Screen.Settings }
                            )
                        }
                    }
                }
                }
            }
        }
    }
}

fun migrateOldAppData(settings: Settings) {
    //Zabezpieczenie nadpisania migracjii
    if (settings.getBoolean("is_migrated_v2", false)) {
        return
    }

    // --- MIGRACJA TRAKTATU ---
    val sections = listOf(12, 7, 7, 7)

    for (idx in sections.indices) {
        for (i in 0 until sections[idx]) {
            val oldKey = "$idx$i"
            val newKey = "pref_day_${idx}_$i"
            val oldFloatValue = settings.getFloat(oldKey, 1.0f)

            if (oldFloatValue == 0.5f) {
                settings.putBoolean(newKey, true)
            }
        }
    }

    // --- MIGRACJA NOWENNY ---
    for (i in 0 until 9) {
        val oldKey = "4$i"
        val newKey = "pref_nowenna_0_$i"

        val oldFloatValue = settings.getFloat(oldKey, 1.0f)

        if (oldFloatValue == 0.5f) {
            settings.putBoolean(newKey, true)
        }
    }

    settings.putBoolean("is_migrated_v2", true)
}