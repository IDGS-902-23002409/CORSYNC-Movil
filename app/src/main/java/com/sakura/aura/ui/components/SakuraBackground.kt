package com.sakura.aura.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.sakura.aura.R
import com.sakura.aura.ui.theme.LocalThemeViewModel

@Composable
fun SakuraBackground(
    modifier: Modifier = Modifier,
    overlayAlpha: Float? = null,
    content: @Composable () -> Unit
) {
    val themeViewModel = LocalThemeViewModel.current
    val isLight by themeViewModel.isLightTheme.collectAsState()

    // Overlay: en claro casi transparente, en oscuro denso
    val resolvedAlpha = overlayAlpha ?: if (isLight) 0.05f else 0.62f

    Box(modifier = modifier.fillMaxSize()) {

        if (isLight) {
            // ── Fondo blanco sólido en tema claro ─────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F4F6))
            )
        } else {
            // ── Imagen de fondo en tema oscuro ─────────────────────────────
            Image(
                painter = painterResource(id = R.drawable.bg_sakura),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0D0D0D).copy(alpha = resolvedAlpha))
            )
        }

        content()
    }
}