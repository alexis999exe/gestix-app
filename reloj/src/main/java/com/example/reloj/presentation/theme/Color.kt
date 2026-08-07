package com.example.reloj.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme

// Colores de la app móvil
val PrimaryDark = Color(0xFF1B3A5C)
val AccentYellow = Color(0xFFFFD100)
val SubtitleTeal = Color(0xFF2DC4D4)
val FormBg = Color(0xFFF0F4F8)
val TextGrey = Color(0xFF7A8B99)

// Colores de estado
val StatusOpen = Color(0xFFFF6B6B)
val StatusInProgress = Color(0xFFFFD100)
val StatusClosed = Color(0xFF2DC4D4)

val wearColorScheme: ColorScheme = ColorScheme(
    primary = AccentYellow,
    onPrimary = PrimaryDark,
    secondary = SubtitleTeal,
    onSecondary = PrimaryDark,
    tertiary = Color.White, // Usamos blanco para elementos de realce
    onTertiary = PrimaryDark,
    background = Color(0xFF0F172A), 
    onBackground = Color.White,
    surfaceContainer = Color.White, // Tarjetas blancas para que resalten sobre el fondo azul
    onSurface = PrimaryDark, // Texto oscuro sobre tarjetas blancas
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFE2E8F0),
    error = StatusOpen,
    onError = Color.White
)
