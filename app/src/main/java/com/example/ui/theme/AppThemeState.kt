package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeMode(val titleHi: String, val titleEn: String, val titleHinglish: String, val icon: String) {
    SYSTEM("सिस्टम अनुसार", "System Default", "System Ke Anusaar", "📱"),
    LIGHT("लाइट मोड", "Light Mode", "Light Mode", "☀️"),
    DARK("डार्क मोड", "Dark Mode", "Dark Mode", "🌙");

    fun getTitle(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> titleHi
        AppLanguage.ENGLISH -> titleEn
        AppLanguage.HINGLISH -> titleHinglish
    }
}

enum class DevotionalPalette(
    val titleHi: String,
    val titleEn: String,
    val titleHinglish: String,
    val primaryColor: Color,
    val lightBg: Color,
    val darkBg: Color
) {
    SAFFRON(
        titleHi = "केसरिया स्वर्ण (Saffron Gold)",
        titleEn = "Saffron Gold",
        titleHinglish = "Kesariya Gold",
        primaryColor = Color(0xFF916400),
        lightBg = Color(0xFFFFDDB3),
        darkBg = Color(0xFF291800)
    ),
    VRINDAVAN(
        titleHi = "वृंदावन तुलसी (Tulsi Green)",
        titleEn = "Tulsi Green",
        titleHinglish = "Tulsi Green",
        primaryColor = Color(0xFF006E1C),
        lightBg = Color(0xFFD7F8D4),
        darkBg = Color(0xFF00390A)
    ),
    SHYAM(
        titleHi = "रॉयल ब्लू (Royal Blue)",
        titleEn = "Royal Blue",
        titleHinglish = "Royal Blue",
        primaryColor = Color(0xFF005AC1),
        lightBg = Color(0xFFD1E1FF),
        darkBg = Color(0xFF001D40)
    );

    fun getTitle(language: AppLanguage): String = when (language) {
        AppLanguage.HINDI -> titleHi
        AppLanguage.ENGLISH -> titleEn
        AppLanguage.HINGLISH -> titleHinglish
    }
}

enum class AppLanguage(val code: String, val displayName: String, val shortBadge: String) {
    HINDI("hi", "हिन्दी (Hindi)", "🇮🇳 हि"),
    ENGLISH("en", "English", "🇬🇧 EN"),
    HINGLISH("hinglish", "हिंग्लिश (Hinglish)", "🌐 MIX")
}

data class AppColors(
    val isDark: Boolean,
    val canvas: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val primary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val accent: Color,
    val navBg: Color,
    val navBorder: Color,
    val navIndicator: Color,
    val navActiveText: Color,
    val navInactiveText: Color,
    val donationBtnBg: Color,
    val donationBtnText: Color,
    val expenseBtnBg: Color,
    val expenseBtnText: Color,
    val donationGreen: Color,
    val donationGreenLight: Color,
    val expenseRed: Color,
    val expenseRedLight: Color,
    val upiBlue: Color,
    val upiBlueLight: Color,
    val cashAmber: Color,
    val cashAmberLight: Color
)

fun createDynamicAppColors(
    isDark: Boolean,
    palette: DevotionalPalette
): AppColors {
    val primaryColor = when (palette) {
        DevotionalPalette.SAFFRON -> if (isDark) Color(0xFFFFB951) else Color(0xFF916400)
        DevotionalPalette.VRINDAVAN -> if (isDark) Color(0xFF7ADC77) else Color(0xFF006E1C)
        DevotionalPalette.SHYAM -> if (isDark) Color(0xFF9ECAFF) else Color(0xFF005AC1)
    }

    val primaryContainer = when (palette) {
        DevotionalPalette.SAFFRON -> if (isDark) Color(0xFF4A3200) else Color(0xFFFFDDB3)
        DevotionalPalette.VRINDAVAN -> if (isDark) Color(0xFF00390A) else Color(0xFFD7F8D4)
        DevotionalPalette.SHYAM -> if (isDark) Color(0xFF003258) else Color(0xFFD1E1FF)
    }

    val onPrimaryContainer = when (palette) {
        DevotionalPalette.SAFFRON -> if (isDark) Color(0xFFFFE082) else Color(0xFF291800)
        DevotionalPalette.VRINDAVAN -> if (isDark) Color(0xFFA5F69F) else Color(0xFF002204)
        DevotionalPalette.SHYAM -> if (isDark) Color(0xFFD1E4FF) else Color(0xFF001D40)
    }

    return if (isDark) {
        AppColors(
            isDark = true,
            canvas = Color(0xFF141218),
            cardBg = Color(0xFF1F1D24),
            cardBorder = Color(0xFF36343B),
            divider = Color(0xFF2A2830),
            textPrimary = Color(0xFFE6E1E5),
            textSecondary = Color(0xFFCAC4D0),
            textTertiary = Color(0xFF938F99),
            primary = primaryColor,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            accent = primaryColor,
            navBg = Color(0xFF18161D),
            navBorder = Color(0xFF36343B),
            navIndicator = primaryContainer,
            navActiveText = onPrimaryContainer,
            navInactiveText = Color(0xFF938F99),
            donationBtnBg = Color(0xFF382354),
            donationBtnText = Color(0xFFEADDFF),
            expenseBtnBg = Color(0xFF492525),
            expenseBtnText = Color(0xFFFFDAD6),
            donationGreen = Color(0xFF7ADC77),
            donationGreenLight = Color(0xFF00390A),
            expenseRed = Color(0xFFFFB4AB),
            expenseRedLight = Color(0xFF690005),
            upiBlue = Color(0xFF9ECAFF),
            upiBlueLight = Color(0xFF003258),
            cashAmber = Color(0xFFFFB951),
            cashAmberLight = Color(0xFF4A3200)
        )
    } else {
        AppColors(
            isDark = false,
            canvas = Color(0xFFFAF9F6),
            cardBg = Color(0xFFFFFFFF),
            cardBorder = Color(0xFFE7E0EC),
            divider = Color(0xFFF4EFF4),
            textPrimary = Color(0xFF1C1B1F),
            textSecondary = Color(0xFF49454F),
            textTertiary = Color(0xFF79747E),
            primary = primaryColor,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            accent = primaryColor,
            navBg = Color(0xFFF3EDF7),
            navBorder = Color(0xFFCAC4D0),
            navIndicator = primaryContainer,
            navActiveText = Color(0xFF1D192B),
            navInactiveText = Color(0xFF49454F),
            donationBtnBg = Color(0xFFEADDFF),
            donationBtnText = Color(0xFF21005D),
            expenseBtnBg = Color(0xFFFFDAD6),
            expenseBtnText = Color(0xFF410002),
            donationGreen = Color(0xFF006E1C),
            donationGreenLight = Color(0xFFD7F8D4),
            expenseRed = Color(0xFFBA1A1A),
            expenseRedLight = Color(0xFFFFDAD6),
            upiBlue = Color(0xFF005AC1),
            upiBlueLight = Color(0xFFD1E1FF),
            cashAmber = Color(0xFF916400),
            cashAmberLight = Color(0xFFFFDDB3)
        )
    }
}

val LocalAppColors = staticCompositionLocalOf {
    createDynamicAppColors(isDark = false, palette = DevotionalPalette.SAFFRON)
}

val LocalAppLanguage = staticCompositionLocalOf {
    AppLanguage.HINDI
}

object AppTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current

    val language: AppLanguage
        @Composable
        get() = LocalAppLanguage.current
}
