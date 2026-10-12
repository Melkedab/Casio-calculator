package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CasioGoldText,
    onPrimary = Color.Black,
    secondary = CasioCyanText,
    onSecondary = Color.Black,
    tertiary = KeycapRedAc,
    background = CasioCasingDark,
    surface = CasioCasingMid,
    surfaceVariant = CasioCasingLight,
    onBackground = CasioLogoWhite,
    onSurface = CasioLogoWhite,
    onSurfaceVariant = CasioSubLabel
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF946A14),
    onPrimary = Color.White,
    secondary = Color(0xFF0369A1),
    onSecondary = Color.White,
    tertiary = KeycapRedAc,
    background = Color(0xFFE8ECEF),
    surface = Color(0xFFD6DBE0),
    surfaceVariant = Color(0xFFC0C7CE),
    onBackground = Color(0xFF1E2328),
    onSurface = Color(0xFF1E2328),
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to iconic Casio dark metallic chassis
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
