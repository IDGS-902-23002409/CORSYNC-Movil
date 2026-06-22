package com.sakura.aura.ui.theme.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.sakura.aura.R

@Composable
fun SakuraBackground(
    modifier: Modifier = Modifier,
    overlayAlpha: Float = 0.62f,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {

        // Imagen de fondo bg_sakura.jpg en res/drawable/
        Image(
            painter = painterResource(id = R.drawable.bg_sakura),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay oscuro para legibilidad
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0D0D).copy(alpha = overlayAlpha))
        )

        // Contenido de la pantalla encima
        content()
    }
}