package com.example.pokedex.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PokedexRed,
    onPrimary = Color.White,
    secondary = PokeBlue,
    tertiary = PokeYellow,
    secondaryContainer = Color(0xFFFFDAD6),
    onSecondaryContainer = Color(0xFF410006),
    background = LightBackground,
    surface = LightBackground,
    surfaceContainer = Color(0xFFF7ECEB),
)

private val DarkColorScheme = darkColorScheme(
    primary = PokedexRedDark,
    onPrimary = Color.White,
    secondary = Color(0xFFBAC3FF),
    tertiary = PokeYellow,
    secondaryContainer = Color(0xFF73000F),
    onSecondaryContainer = Color(0xFFFFDAD6),
    background = DarkBackground,
    surface = DarkBackground,
    surfaceContainer = Color(0xFF231E1F),
)

// Dynamic (wallpaper) colours are left off so the app always keeps its Pokédex look.
@Composable
fun PokedexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
