package com.dakbit.fortune

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

val Midnight = Color(0xFF090A2B)
val Navy = Color(0xFF171744)
val Amber = Color(0xFFF2C879)
val Coral = Color(0xFFE7A7C4)
val Cream = Color(0xFF080923)
val WarmWhite = Color(0xFF29284F)
val Ink = Color(0xFFF8F2FF)
val Muted = Color(0xFFB8B2CF)
val Line = Color(0xFF5E5986)
val MoonIvory = Color(0xFFFFF4D8)
val Lavender = Color(0xFF9C91D4)
val Glass = Color(0x9A2A294F)
val GlassStrong = Color(0xD044416A)

private val DakbitColors = darkColorScheme(
    primary = Amber,
    onPrimary = Midnight,
    secondary = Amber,
    onSecondary = Midnight,
    tertiary = Coral,
    background = Cream,
    onBackground = Ink,
    surface = GlassStrong,
    onSurface = Ink,
    outline = Line,
)

private val DakbitTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 31.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 27.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
    ),
)

@Composable
fun DakbitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DakbitColors,
        typography = DakbitTypography,
        content = content,
    )
}
