package com.dietapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = GreenLight,
    onPrimaryContainer = GreenDark,
    secondary = Orange,
    onSecondary = Color.White,
    secondaryContainer = OrangeLight,
    onSecondaryContainer = OrangeDark,
    tertiary = Teal,
    onTertiary = Color.White,
    background = Background,
    onBackground = TextPrimary,
    surface = CardBg,
    onSurface = TextPrimary,
    surfaceVariant = WarmSurface,
    onSurfaceVariant = TextSecondary,
    outline = Border,
    outlineVariant = Divider,
    error = Red,
    onError = Color.White,
    inverseSurface = GreenDark,
    inverseOnSurface = Color.White,
    surfaceTint = Green,
)

@Composable
fun DietAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = DietAppTypography,
        shapes = DietAppShapes,
        content = content
    )
}
