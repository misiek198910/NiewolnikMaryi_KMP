package com.example.slaveofmary.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

/**
 * Paleta wyprowadzona z logo aplikacji ("niewolnik_maryi_logo.png"):
 * granatowe tło z niebieską poświatą pośrodku oraz srebrno-stalowe litery
 * i łańcuchy. Cała aplikacja korzysta z ciemnego motywu opartego na tych barwach.
 */
object AppColors {
    // Tła — od granatu krawędzi logo do rozjaśnienia w jego środku.
    val background = Color(0xFF0B1826)
    val backgroundElevated = Color(0xFF122A3F)
    val surface = Color(0xFF16304A)
    val surfaceGlass = Color(0xFF0E2133)

    // Niebieski akcent = poświata z logo. Ikony, wskaźniki, aktywne elementy.
    val accent = Color(0xFF5B9BD5)
    val accentDim = Color(0xFF3E6E97)

    // Gradient kart / przycisków (niebieski jak w logo).
    val cardGradientStart = Color(0xFF3E7CB1)
    val cardGradientEnd = Color(0xFF0B3D91)

    // Tekst — srebro/stal liter i łańcuchów z logo.
    val textPrimary = Color(0xFFE4E8EC)
    val textSecondary = Color(0xFF9BA7B3)
    val onCard = Color(0xFFEFF2F5)
    val onCardSecondary = Color(0xFFC5CFD9)

    // Semantyczne.
    val danger = Color(0xFFE05B5B)
    val success = Color(0xFF57B85F)
    val gold = Color(0xFFE7C15A)

    // Elementy szczegółowe.
    val newsCard = Color(0xFF14293C)
    val dayCircle = Color(0xFFDDE3E9)
    val dayCircleText = Color(0xFF0B2E52)

    /** Tło całej aplikacji — pionowy gradient odwzorowujący poświatę z logo. */
    val appBackgroundBrush: Brush
        get() = Brush.verticalGradient(
            listOf(
                Color(0xFF0B1826),
                Color(0xFF15324C),
                Color(0xFF0A1523)
            )
        )
}

private val DarkScheme = darkColorScheme(
    primary = AppColors.accent,
    onPrimary = Color(0xFF04121F),
    secondary = AppColors.accentDim,
    onSecondary = AppColors.onCard,
    tertiary = AppColors.accent,
    background = AppColors.background,
    onBackground = AppColors.textPrimary,
    surface = AppColors.surface,
    onSurface = AppColors.textPrimary,
    surfaceVariant = AppColors.backgroundElevated,
    onSurfaceVariant = AppColors.textSecondary,
    error = AppColors.danger,
    onError = AppColors.onCard,
    outline = AppColors.textSecondary,
    outlineVariant = AppColors.accentDim
)

private val SerifTypography: Typography
    @Composable
    get() {
        val base = MaterialTheme.typography
        return base.copy(
            displayLarge = base.displayLarge.copy(fontFamily = FontFamily.Serif),
            displayMedium = base.displayMedium.copy(fontFamily = FontFamily.Serif),
            displaySmall = base.displaySmall.copy(fontFamily = FontFamily.Serif),
            headlineLarge = base.headlineLarge.copy(fontFamily = FontFamily.Serif),
            headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.Serif),
            headlineSmall = base.headlineSmall.copy(fontFamily = FontFamily.Serif),
            titleLarge = base.titleLarge.copy(fontFamily = FontFamily.Serif),
            titleMedium = base.titleMedium.copy(fontFamily = FontFamily.Serif),
            titleSmall = base.titleSmall.copy(fontFamily = FontFamily.Serif),
            bodyLarge = base.bodyLarge.copy(fontFamily = FontFamily.Serif),
            bodyMedium = base.bodyMedium.copy(fontFamily = FontFamily.Serif),
            bodySmall = base.bodySmall.copy(fontFamily = FontFamily.Serif),
            labelLarge = base.labelLarge.copy(fontFamily = FontFamily.Serif),
            labelMedium = base.labelMedium.copy(fontFamily = FontFamily.Serif),
            labelSmall = base.labelSmall.copy(fontFamily = FontFamily.Serif)
        )
    }

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = SerifTypography,
        content = content
    )
}
