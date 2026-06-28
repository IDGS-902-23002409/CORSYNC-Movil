package com.sakura.aura.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Esquema oscuro ────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary        = CorsyncPink,
    onPrimary      = CorsyncDark,
    secondary      = CorsyncPinkLight,
    background     = CorsyncDark,
    surface        = CorsyncDarkSurf,
    onBackground   = CorsyncWhite,
    onSurface      = CorsyncWhite,
    surfaceVariant = CorsyncCard,
    outline        = CorsyncGray,
)

// ── Esquema claro ─────────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary        = Color(0xFFD4477A),
    onPrimary      = Color.White,
    secondary      = CorsyncPink,
    background     = CorsyncLightBg,
    surface        = CorsyncLightSurf,
    onBackground   = CorsyncLightText,
    onSurface      = CorsyncLightText,
    surfaceVariant = CorsyncLightCard,
    outline        = CorsyncLightBorder,
)

// ── Tema principal ────────────────────────────────────────────────────────────
@Composable
fun SakuraTheme(
    isLightTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isLightTheme) LightColorScheme else DarkColorScheme,
        typography  = SakuraTypography,
        content     = content
    )
}