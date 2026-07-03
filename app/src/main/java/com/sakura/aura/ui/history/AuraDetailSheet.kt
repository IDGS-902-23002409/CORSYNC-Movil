package com.sakura.aura.ui.history

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
import com.sakura.aura.data.model.response.ReadingResponse

private fun auraColorFromString(color: String): Color = when (color.lowercase()) {
    "rojo", "roja" -> Color(0xFFE74C3C)
    "naranja" -> Color(0xFFE67E22)
    "amarillo", "amarilla" -> Color(0xFFF1C40F)
    "verde" -> Color(0xFF2ECC71)
    "azul" -> Color(0xFF5DADE2)
    "morado", "violeta", "morada" -> Color(0xFF9B59B6)
    "rosa" -> Color(0xFFE91E8C)
    else -> Color(0xFFCCCCCC)
}

private fun stressLabel(level: Double): String = when {
    level < 30 -> "Bajo"
    level < 60 -> "Medio"
    else -> "Alto"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraDetailSheet(
    reading: ReadingResponse,
    onDismiss: () -> Unit
) {
    val auraColor = auraColorFromString(reading.auraDominante)

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(auraColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reading.fechaInicio.take(10),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SheetMetricBadge(
                    value = "${reading.bpmPromedio.toInt()} bpm",
                    modifier = Modifier.weight(1f)
                )
                SheetMetricBadge(
                    value = "${stressLabel(reading.nivelEstres)} · ${reading.nivelEstres.toInt()}%",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            DetailChartCard(
                title  = "Flujo Cardíaco",
                color  = Color(0xFFE91E8C),
                points = listOf(0.5f, 0.52f, 0.48f, 0.51f, 0.53f, 0.5f, 0.52f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailChartCard(
                title  = "Conductancia GSR",
                color  = Color(0xFF5DADE2),
                points = listOf(0.3f, 0.32f, 0.31f, 0.33f, 0.31f, 0.32f, 0.30f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OracleMessage(auraName = reading.auraDominante)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

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
                    drawCircle(color = color, radius = 4f, center = Offset(x1, y1))
                }
                val lastX = (points.size - 1) * w
                val lastY = h - (points.last() * h * 0.8f + h * 0.1f)
                drawCircle(color = color, radius = 4f, center = Offset(lastX, lastY))
            }

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

@Composable
private fun OracleMessage(auraName: String) {
    val colorName = auraName.lowercase()
    val message = when {
        colorName.contains("violet") || colorName.contains("morad") -> "Tu energía violeta habla de intuición profunda. La mente descansa y el espíritu viaja libre."
        colorName.contains("azul")   -> "El azul de tu aura refleja calma oceánica. Fluyes con la corriente del universo."
        colorName.contains("rosa")   -> "Rosa cuarzo: amor incondicional y paz interior. Tu corazón late en armonía."
        colorName.contains("rojo")   -> "Energía roja intensa. El fuego interior busca expresarse. Respira y canaliza tu poder."
        colorName.contains("verde")  -> "Verde sanador. Tu aura irradia equilibrio y conexión con la naturaleza."
        colorName.contains("naranja")  -> "Naranja creativo. La pasión y la alegría fluyen a través de ti."
        colorName.contains("amarill")  -> "Amarillo radiante. Tu mente brilla con claridad y optimismo."
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
