package com.kevinxu.remaidata.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme: ColorScheme = lightColorScheme(
    primary = MaimaiPrimary,
    onPrimary = MaimaiOnPrimary,
    primaryContainer = MaimaiPrimaryContainer,
    onPrimaryContainer = MaimaiOnPrimaryContainer,
    secondary = MaimaiSecondary,
    onSecondary = MaimaiOnSecondary,
    secondaryContainer = MaimaiSecondaryContainer,
    onSecondaryContainer = MaimaiOnSecondaryContainer,
    tertiary = MaimaiTertiary,
    onTertiary = MaimaiOnTertiary,
    tertiaryContainer = MaimaiTertiaryContainer,
    onTertiaryContainer = MaimaiOnTertiaryContainer,
    background = MaimaiBackground,
    onBackground = MaimaiOnBackground,
    surface = MaimaiSurface,
    onSurface = MaimaiOnSurface,
    surfaceVariant = MaimaiSurfaceVariant,
    onSurfaceVariant = MaimaiOnSurfaceVariant,
    outline = MaimaiOutline
)

@Composable
fun MaimaiDataTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
