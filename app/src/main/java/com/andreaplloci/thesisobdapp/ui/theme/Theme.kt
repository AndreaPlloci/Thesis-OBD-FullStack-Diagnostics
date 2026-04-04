package com.andreaplloci.thesisobdapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    background = Surface_Dark,
    surface = Surface_Dark,
    surfaceContainer = SurfaceContainer_Dark,
    surfaceContainerHigh = SurfaceContainerHigh_Dark,
    primary = Primary,
    primaryContainer = PrimaryContainer_Dark,
    onPrimary = OnPrimary,
    onBackground = OnSurface_Dark,
    onSurface = OnSurface_Dark,
    onSurfaceVariant = OnSurfaceVariant_Dark,
    error = Error_Dark,
)

private val LightColorScheme = lightColorScheme(
    background = Surface_Light,
    surface = Surface_Light,
    surfaceContainer = SurfaceContainer_Light,
    surfaceContainerHigh = SurfaceContainerHigh_Light,
    primary = Primary,
    primaryContainer = PrimaryContainer_Light,
    onPrimary = OnPrimary,
    onBackground = OnSurface_Light,
    onSurface = OnSurface_Light,
    onSurfaceVariant = OnSurfaceVariant_Light,
    error = Error_Light,
)

@Composable
fun ThesisOBDAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
