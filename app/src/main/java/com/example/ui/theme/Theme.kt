package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CsColorScheme = darkColorScheme(
    primary = CsOrange,
    secondary = CsYellow,
    tertiary = CsCtBlue,
    background = CsDarkBackground,
    surface = CsSurface,
    surfaceVariant = CsSurfaceVariant,
    onPrimary = CsDarkBackground,
    onSecondary = CsDarkBackground,
    onTertiary = CsDarkBackground,
    onBackground = CsTextPrimary,
    onSurface = CsTextPrimary,
    onSurfaceVariant = CsTextSecondary,
    error = CsErrorRed,
    onError = CsTextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CsColorScheme,
        typography = Typography,
        content = content
    )
}
