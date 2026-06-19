package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFF59E0B),        // Amber Gold 500 (elegant light-meter gold)
    onPrimary = Color(0xFF0B0F19),      // Contrast obsidian
    primaryContainer = Color(0xFF2D2514), // Dark warm gold container
    onPrimaryContainer = Color(0xFFFBBF24), // Glowing soft gold text
    secondary = Color(0xFF3B82F6),      // Tech Blue accent
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFF94A3B8),
    background = Color(0xFF0B0F19),     // Midnight Obsidian
    onBackground = Color(0xFFF8FAFC),   // Crispy white
    surface = Color(0xFF161F30),        // Dark Slate surface
    onSurface = Color(0xFFE2E8F0),      // Readable slate white
    surfaceVariant = Color(0xFF1E293B), // Elevated border / helper surfaces
    onSurfaceVariant = Color(0xFF94A3B8) // Accessible caption gray
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFD97706),        // Rich Amber 600
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7), // Warm cream accent gold container
    onPrimaryContainer = Color(0xFF78350F), // Dark brown/amber text
    secondary = Color(0xFF2563EB),      // Strong royal blue accent
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF475569),
    background = Color(0xFFF8FAFC),     // Warm Slate 50
    onBackground = Color(0xFF0F172A),   // Slate 900
    surface = Color.White,
    onSurface = Color(0xFF0F172A),      // Slate 900
    surfaceVariant = Color(0xFFF1F5F9), // Slate 100
    onSurfaceVariant = Color(0xFF64748B) // Slate 500
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Set to false to enforce our curated premium photography aesthetic
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
