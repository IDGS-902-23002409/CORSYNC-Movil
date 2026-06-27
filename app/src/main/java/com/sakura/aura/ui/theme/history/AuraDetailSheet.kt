package com.sakura.aura.ui.theme.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraDetailSheet(
    reading: AuraReadingUi,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // ── Header ─────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(reading.color)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${reading.date} · ${reading.time}",
                        color = Color(0xFF888888),
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color(0xFF333333)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Métricas rápidas ───────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SheetMetricBadge(
                    value = "${reading.bpm}",
                    modifier = Modifier.weight(1f)
                )
                SheetMetricBadge(
                    value = "${reading.stressLevel} · ${reading.stressPercent}%",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Gráfica flujo cardíaco ─────────────────────────────────────
            DetailChartCard(
                title  = "Flujo Cardíaco",
                color  = Color(0xFFE91E8C),
                points = listOf(0.5f, 0.52f, 0.48f, 0.51f, 0.53f, 0.5f, 0.52f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Gráfica GSR ────────────────────────────────────────────────
            DetailChartCard(
                title  = "Conductancia GSR",
                color  = Color(0xFF5DADE2),
                points = listOf(0.3f, 0.32f, 0.31f, 0.33f, 0.31f, 0.32f, 0.30f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── El oráculo susurra ─────────────────────────────────────────
            OracleMessage(auraName = reading.name)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Badge de métrica ──────────────────────────────────────────────────────────
@Composable
private fun SheetMetricBadge(value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFDDDDDD)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            color = Color(0xFF444444),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}

// ── Card con gráfica de detalle ───────────────────────────────────────────────
@Composable
private fun DetailChartCard(
    title: String,
    color: Color,
    points: List<Float>
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (color == Color(0xFFE91E8C)) "♥" else "≈",
                    color = color,
                    fontSize = 13.sp
                )
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Gráfica
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                val w = size.width / (points.size - 1)
                val h = size.height
                for (i in 0 until points.size - 1) {
                    val x1 = i * w
                    val y1 = h - (points[i] * h * 0.8f + h * 0.1f)
                    val x2 = (i + 1) * w
                    val y2 = h - (points[i + 1] * h * 0.8f + h * 0.1f)
                    drawLine(
                        color = color,
                        start = Offset(x1, y1),
                        end   = Offset(x2, y2),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )
                    // Punto en cada vértice
                    drawCircle(color = color, radius = 4f, center = Offset(x1, y1))
                }
                // Último punto
                val lastX = (points.size - 1) * w
                val lastY = h - (points.last() * h * 0.8f + h * 0.1f)
                drawCircle(color = color, radius = 4f, center = Offset(lastX, lastY))
            }

            // Etiquetas de tiempo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("0s", "10s", "20s", "30s", "40s", "50s").forEach { label ->
                    Text(text = label, color = Color.White.copy(alpha = 0.3f), fontSize = 9.sp)
                }
            }
        }
    }
}

// ── Mensaje del oráculo ────────────────────────────────────────────────────────
@Composable
private fun OracleMessage(auraName: String) {
    val colorName = auraName.replace("Aura ", "").lowercase()
    val message = when {
        colorName.contains("violet") -> "Tu energía violeta habla de intuición profunda. La mente descansa y el espíritu viaja libre."
        colorName.contains("azul")   -> "El azul de tu aura refleja calma oceánica. Fluyes con la corriente del universo."
        colorName.contains("rosa")   -> "Rosa cuarzo: amor incondicional y paz interior. Tu corazón late en armonía."
        colorName.contains("rojo")   -> "Energía roja intensa. El fuego interior busca expresarse. Respira y canaliza tu poder."
        colorName.contains("verde")  -> "Verde sanador. Tu aura irradia equilibrio y conexión con la naturaleza."
        else -> "Tu energía es única. El universo reconoce tu luz interior."
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE8E0F0))
            .padding(16.dp)
    ) {
        Text(
            text = "EL ORÁCULO SUSURRA",
            color = Color(0xFF9B59B6).copy(alpha = 0.7f),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = Color(0xFF444444),
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
    }
}