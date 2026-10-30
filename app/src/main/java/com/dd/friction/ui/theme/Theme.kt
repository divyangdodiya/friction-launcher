package com.dd.friction.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Monochrome on purpose: color is one of the hooks that makes a phone feel rewarding.
private val FrictionColors = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    secondary = Color.White,
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF121212),
    onSurfaceVariant = Color(0xFF8A8A8A),
    surfaceContainerHigh = Color(0xFF161616),
    outline = Color(0xFF3A3A3A),
    outlineVariant = Color(0xFF242424),
)

private val FrictionTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Thin, fontSize = 76.sp, letterSpacing = (-2).sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Light, fontSize = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 20.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 19.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 15.sp, letterSpacing = 0.5.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, letterSpacing = 1.sp),
)

@Composable
fun FrictionTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FrictionColors,
        typography = FrictionTypography,
        content = content,
    )
}
