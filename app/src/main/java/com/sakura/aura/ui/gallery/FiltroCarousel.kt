package com.sakura.aura.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakura.aura.ui.filters.FiltroUi

/**
 * Bandeja de filtros al estilo del carrusel de la cámara de Instagram.
 *
 * Muestra los cinco filtros en fila: los desbloqueados se pueden tocar para
 * seleccionarlos, los bloqueados salen en gris con un candado y los puntos que
 * faltan. El seleccionado lleva un anillo del color del filtro y es el que abre
 * la cámara con el botón de abajo.
 */
@Composable
fun FiltroCarousel(
    filtros: List<FiltroUi>,
    desbloqueados: Set<FiltroUi>,
    seleccionado: FiltroUi?,
    puntos: Int,
    textMain: Color,
    textSub: Color,
    cardBg: Color,
    border: Color,
    onSeleccionar: (FiltroUi) -> Unit,
    onUsar: (FiltroUi) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Mis filtros",
                color = textMain,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                "$puntos pts",
                color = Color(0xFFE91E8C),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = "${desbloqueados.size} de ${filtros.size} desbloqueados · " +
                    "gana puntos completando Misiones Zen",
            color = textSub,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(14.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filtros, key = { it.sufijo }) { filtro ->
                FiltroBurbuja(
                    filtro = filtro,
                    desbloqueado = filtro in desbloqueados,
                    seleccionado = filtro == seleccionado,
                    textMain = textMain,
                    textSub = textSub,
                    onClick = { onSeleccionar(filtro) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        val activo = seleccionado?.takeIf { it in desbloqueados }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (activo != null) Color(activo.hex) else textSub.copy(alpha = 0.15f)
                )
                .clickable(enabled = activo != null) { activo?.let(onUsar) }
                .padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.PhotoCamera,
                contentDescription = null,
                tint = if (activo != null) Color.White else textSub,
                modifier = Modifier.size(19.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = activo?.let { "Usar ${it.label}" } ?: "Elige un filtro",
                color = if (activo != null) Color.White else textSub,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        if (activo != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = activo.descripcion,
                color = textSub,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun FiltroBurbuja(
    filtro: FiltroUi,
    desbloqueado: Boolean,
    seleccionado: Boolean,
    textMain: Color,
    textSub: Color,
    onClick: () -> Unit
) {
    val color = Color(filtro.hex)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .then(
                    if (seleccionado) Modifier.border(2.5.dp, color, CircleShape)
                    else Modifier
                )
                .padding(4.dp)
                .clip(CircleShape)
                .background(
                    if (desbloqueado) {
                        Brush.linearGradient(
                            listOf(color.copy(alpha = 0.95f), color.copy(alpha = 0.45f))
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(
                                textSub.copy(alpha = 0.22f),
                                textSub.copy(alpha = 0.12f)
                            )
                        )
                    }
                )
                .clickable(enabled = desbloqueado, onClick = onClick)
        ) {
            if (!desbloqueado) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = "Bloqueado",
                    tint = textSub,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = filtro.label,
            color = if (desbloqueado) textMain else textSub,
            fontSize = 11.sp,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        if (!desbloqueado) {
            Text(
                text = "${filtro.puntosRequeridos} pts",
                color = textSub,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(0.85f)
            )
        }
    }
}
