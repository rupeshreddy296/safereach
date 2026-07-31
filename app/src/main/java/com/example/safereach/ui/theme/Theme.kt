package com.example.safereach.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val EnterpriseColorScheme = darkColorScheme(
    primary = OffWhite,
    secondary = DarkSteel,
    tertiary = AccentRed,
    background = MidnightBlue,
    surface = DarkSteel,
    onPrimary = MidnightBlue,
    onSecondary = OffWhite,
    onTertiary = OffWhite,
    onBackground = OffWhite,
    onSurface = OffWhite
)

@Composable
fun SAFEREACHTheme(
    darkTheme: Boolean = true, // Force Enterprise Dark Theme by default
    dynamicColor: Boolean = false, // Disable dynamic color to maintain brand consistency
    content: @Composable () -> Unit
) {
    val colorScheme = EnterpriseColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
