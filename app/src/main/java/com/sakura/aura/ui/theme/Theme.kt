// ui/theme/Theme.kt
package com.sakura.aura.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary        = SakuraPink,
    onPrimary      = SakuraDark,
    secondary      = SakuraPinkLight,
    background     = SakuraDark,
    surface        = SakuraDarkSurf,
    onBackground   = SakuraWhite,
    onSurface      = SakuraWhite,
    surfaceVariant = SakuraCard,
    outline        = SakuraGray,
)

@Composable
fun SakuraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = SakuraTypography,
        content     = content
    )
}