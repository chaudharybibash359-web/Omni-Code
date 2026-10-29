package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkClaudeColorScheme = darkColorScheme(
    primary = CyberPrimary,
    onPrimary = CyberOnPrimary,
    primaryContainer = CyberSurfaceVariant,
    onPrimaryContainer = CyberPrimaryBright,
    secondary = CyberSecondary,
    onSecondary = CyberOnSecondary,
    secondaryContainer = CyberSurfaceContainer,
    onSecondaryContainer = CyberSecondary,
    tertiary = CyberTertiary,
    background = CyberBackground,
    onBackground = CyberOnBackground,
    surface = CyberSurface,
    onSurface = CyberOnSurface,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = CyberOnSurfaceMuted,
    outline = CyberOutline,
    outlineVariant = CyberOutlineVariant,
    error = CyberError
)

@Composable
fun DarkClaudeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkClaudeColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = DarkClaudeTheme(content = content)
