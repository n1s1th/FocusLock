package com.focuslock.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = CharcoalPrimary,
    secondary = AccentOrange,
    tertiary = SecondaryGray,
    background = ScreenBackground,
    surface = SurfaceBright,
    surfaceVariant = SurfaceVariant,
    onPrimary = SurfaceBright,
    onSecondary = SurfaceBright,
    onTertiary = SurfaceBright,
    onBackground = CharcoalPrimary,
    onSurface = CharcoalPrimary,
    onSurfaceVariant = SecondaryGray
)

@Composable
fun FocusLockTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
