package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Brand Colors
val ColorInk = Color(0xFF121A33)
val ColorInkSoft = Color(0xFF28345A)
val ColorInk2 = Color(0xFF1E2A4E)
val ColorPaper = Color(0xFFF7F5F0)
val ColorPaper2 = Color(0xFFFFFFFF)
val ColorPaper3 = Color(0xFFEEEAE1)
val ColorBrass = Color(0xFFC08A2E)
val ColorBrassSoft = Color(0xFFE7C685)
val ColorBrassBright = Color(0xFFD8A548)
val ColorSage = Color(0xFF3F7D5C)
val ColorSageBg = Color(0xFFE4EFE8)
val ColorClay = Color(0xFFB33F3F)
val ColorClayBg = Color(0xFFF5E4E1)
val ColorSlate = Color(0xFF6B7280)
val ColorLine = Color(0xFFE4E0D6)
val ColorTextDark = Color(0xFF1B2333)
val ColorTextDimLight = Color(0xFF5B6478)

// Dark Theme Colors
val ColorDarkPaper = Color(0xFF0B1120)
val ColorDarkPaper2 = Color(0xFF141B2E)
val ColorDarkPaper3 = Color(0xFF1B2438)
val ColorDarkLine = Color(0xFF252F49)
val ColorDarkText = Color(0xFFEDEEF2)
val ColorDarkTextDim = Color(0xFF8D96AC)
val ColorDarkSage = Color(0xFF6FBF95)
val ColorDarkClay = Color(0xFFE17575)

// Fonts
val SpaceMonoFontFamily = FontFamily(
    Font(R.font.space_mono, FontWeight.Normal)
)

val ManropeFontFamily = FontFamily(
    Font(R.font.manrope, FontWeight.Normal)
)

val SmartCashTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = SpaceMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = SpaceMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    titleLarge = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    ),
    titleMedium = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = SpaceMonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp
    )
)

private val LightColorScheme = lightColorScheme(
    primary = ColorBrass,
    onPrimary = ColorInk,
    primaryContainer = ColorBrassSoft,
    onPrimaryContainer = ColorInk,
    secondary = ColorInk,
    onSecondary = ColorPaper,
    background = ColorPaper,
    onBackground = ColorTextDark,
    surface = ColorPaper2,
    onSurface = ColorTextDark,
    surfaceVariant = ColorPaper3,
    onSurfaceVariant = ColorTextDimLight,
    outline = ColorLine,
    error = ColorClay,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = ColorBrassBright,
    onPrimary = ColorInk,
    primaryContainer = Color(0xFF3A2E17),
    onPrimaryContainer = ColorBrassBright,
    secondary = ColorDarkText,
    onSecondary = ColorDarkPaper,
    background = ColorDarkPaper,
    onBackground = ColorDarkText,
    surface = ColorDarkPaper2,
    onSurface = ColorDarkText,
    surfaceVariant = ColorDarkPaper3,
    onSurfaceVariant = ColorDarkTextDim,
    outline = ColorDarkLine,
    error = ColorDarkClay,
    onError = ColorDarkPaper
)

@Composable
fun SmartCashTheme(
    themePreference: String = "auto",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themePreference) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SmartCashTypography,
        content = content
    )
}
