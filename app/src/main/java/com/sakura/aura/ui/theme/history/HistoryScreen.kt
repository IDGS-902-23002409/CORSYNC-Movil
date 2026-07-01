package com.sakura.aura.ui.theme.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sakura.aura.navigation.SakuraBottomNavBar
import com.sakura.aura.ui.theme.components.SakuraBackground
import com.sakura.aura.ui.theme.LocalThemeViewModel

data class AuraReadingUi(
    val id: Int, val name: String,
    val date: String, val time: String,
    val bpm: Int,
    val stressLevel: String, val stressPercent: Int,
    val color: Color, val stressColor: Color
)

private val sampleReadings = listOf(
    AuraReadingUi(1, "Aura Violeta", "17 Jun", "09:12", 68, "Bajo",  22, Color(0xFF9B59B6), Color(0xFF2ECC71)),
    AuraReadingUi(2, "Aura Azul",    "16 Jun", "21:40", 72, "Medio", 45, Color(0xFF5DADE2), Color(0xFFF39C12)),
    AuraReadingUi(3, "Aura Rosa",    "15 Jun", "07:55", 75, "Bajo",  18, Color(0xFFE91E8C), Color(0xFF2ECC71)),
    AuraReadingUi(4, "Aura Rojo",    "14 Jun", "18:30", 84, "Alto",  72, Color(0xFFE74C3C), Color(0xFFE74C3C)),
    AuraReadingUi(5, "Aura Verde",   "13 Jun", "08:05", 70, "Bajo",  15, Color(0xFF2ECC71), Color(0xFF2ECC71)),
)

@Composable
fun HistoryScreen(navController: NavController) {

    val themeViewModel = LocalThemeViewModel.current
    val isLight by themeViewModel.isLightTheme.collectAsState()

    // ── Colores adaptativos ────────────────────────────────────────────────
    val textMain = if (isLight) Color(0xFF1A1A1A) else Color.White
    val textSub  = if (isLight) Color(0xFF666666) else Color.White.copy(alpha = 0.45f)
    val cardBg   = if (isLight) Color(0xFFFFFFFF) else Color(0xFF1A1A1A).copy(alpha = 0.85f)

    var selectedReading by remember { mutableStateOf<AuraReadingUi?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { SakuraBottomNavBar(navController) }
    ) { innerPadding ->
        SakuraBackground {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(Modifier.height(12.dp)) }

                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Diario Energético",
                            color = textMain,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Light
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Toca un aura para ver su lectura completa",
                            color = textSub,
                            fontSize = 12.sp
                        )
                    }
                }

                item { Spacer(Modifier.height(4.dp)) }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            label = "BPM promedio", value = "74", unit = "bpm",
                            valueColor = Color(0xFFE91E8C),
                            textSub = textSub, cardBg = cardBg,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Estrés promedio", value = "37", unit = "%",
                            valueColor = Color(0xFF5DADE2),
                            textSub = textSub, cardBg = cardBg,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item { Spacer(Modifier.height(4.dp)) }

                items(sampleReadings) { reading ->
                    AuraReadingCard(
                        reading  = reading,
                        textMain = textMain,
                        textSub  = textSub,
                        cardBg   = cardBg,
                        onClick  = { selectedReading = reading }
                    )
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }

    selectedReading?.let { reading ->
        AuraDetailSheet(reading = reading, onDismiss = { selectedReading = null })
    }
}

@Composable
private fun MetricCard(
    label: String, value: String, unit: String,
    valueColor: Color, textSub: Color, cardBg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (label.contains("BPM")) "♥" else "≈",
                    color = valueColor, fontSize = 12.sp
                )
                Text(text = label, color = textSub, fontSize = 11.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = valueColor, fontSize = 32.sp, fontWeight = FontWeight.Light)
                Spacer(Modifier.width(4.dp))
                Text(
                    unit, color = valueColor.copy(alpha = 0.7f), fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            MiniChart(color = valueColor)
        }
    }
}

@Composable
private fun MiniChart(color: Color) {
    val points = listOf(0.6f, 0.4f, 0.7f, 0.5f, 0.8f, 0.45f, 0.55f)
    androidx.compose.foundation.Canvas(
        modifier = Modifier.fillMaxWidth().height(36.dp)
    ) {
        val w = size.width / (points.size - 1)
        val h = size.height
        for (i in 0 until points.size - 1) {
            drawLine(
                color = color.copy(alpha = 0.8f),
                start = androidx.compose.ui.geometry.Offset(i * w, h - points[i] * h),
                end   = androidx.compose.ui.geometry.Offset((i+1)*w, h - points[i+1]*h),
                strokeWidth = 2.5f, cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun AuraReadingCard(
    reading  : AuraReadingUi,
    textMain : Color,
    textSub  : Color,
    cardBg   : Color,
    onClick  : () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(reading.color)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(reading.name, color = textMain, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(3.dp))
                Text("${reading.date} · ${reading.time}", color = textSub, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${reading.bpm} bpm", color = textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(3.dp))
                Text(reading.stressLevel, color = reading.stressColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}