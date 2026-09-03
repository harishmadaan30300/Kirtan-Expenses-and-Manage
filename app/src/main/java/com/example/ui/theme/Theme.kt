package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    palette: DevotionalPalette = DevotionalPalette.SAFFRON,
    language: AppLanguage = AppLanguage.HINDI,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val appColors = createDynamicAppColors(isDark = isDark, palette = palette)

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = appColors.primary,
            onPrimary = appColors.cardBg,
            primaryContainer = appColors.primaryContainer,
            onPrimaryContainer = appColors.onPrimaryContainer,
            secondary = appColors.accent,
            onSecondary = Color.White,
            secondaryContainer = appColors.donationBtnBg,
            onSecondaryContainer = appColors.donationBtnText,
            tertiary = appColors.expenseRed,
            onTertiary = Color.White,
            tertiaryContainer = appColors.expenseBtnBg,
            onTertiaryContainer = appColors.expenseBtnText,
            background = appColors.canvas,
            surface = appColors.cardBg,
            onBackground = appColors.textPrimary,
            onSurface = appColors.textPrimary,
            surfaceVariant = appColors.divider,
            onSurfaceVariant = appColors.textSecondary,
            outline = appColors.cardBorder,
        )
    } else {
        lightColorScheme(
            primary = appColors.primary,
            onPrimary = Color.White,
            primaryContainer = appColors.primaryContainer,
            onPrimaryContainer = appColors.onPrimaryContainer,
            secondary = appColors.accent,
            onSecondary = Color.White,
            secondaryContainer = appColors.donationBtnBg,
            onSecondaryContainer = appColors.donationBtnText,
            tertiary = appColors.expenseRed,
            onTertiary = Color.White,
            tertiaryContainer = appColors.expenseBtnBg,
            onTertiaryContainer = appColors.expenseBtnText,
            background = appColors.canvas,
            surface = appColors.cardBg,
            onBackground = appColors.textPrimary,
            onSurface = appColors.textPrimary,
            surfaceVariant = appColors.divider,
            onSurfaceVariant = appColors.textSecondary,
            outline = appColors.cardBorder,
        )
    }

    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalAppLanguage provides language
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}


