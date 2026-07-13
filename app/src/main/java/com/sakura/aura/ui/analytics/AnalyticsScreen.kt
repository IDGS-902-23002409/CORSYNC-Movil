package com.sakura.aura.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sakura.aura.domain.model.*
import com.sakura.aura.domain.util.AuraMapper
import com.sakura.aura.navigation.SakuraBottomNavBar
import com.sakura.aura.ui.components.SakuraBackground
import com.sakura.aura.ui.theme.LocalThemeViewModel
import com.sakura.aura.ui.theme.SakuraPink

@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val themeViewModel = LocalThemeViewModel.current
    val isLight by themeViewModel.isLightTheme.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val textMain = if (isLight) Color(0xFF1A1A1A) else Color.White
    val textSub = if (isLight) Color(0xFF666666) else Color.White.copy(alpha = 0.45f)
    val cardBg = if (isLight) Color(0xFFFFFFFF) else Color(0xFF1A1A1A).copy(alpha = 0.85f)
    val tabSelectedBg = if (isLight) SakuraPink else SakuraPink.copy(alpha = 0.85f)
    val tabUnselectedBg = if (isLight) Color(0xFFEFE8EF) else Color(0xFF2A2A2A)

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { SakuraBottomNavBar(navController) }
    ) { innerPadding ->
        SakuraBackground {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SakuraPink)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(Modifier.height(8.dp)) }

                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Análisis de Datos",
                                color = textMain,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Light
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Monitoreo de tendencias y bio-señales",
                                color = textSub,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Period Selector
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(tabUnselectedBg)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val periods = listOf(
                                "daily" to "Diario",
                                "weekly" to "Semanal",
                                "monthly" to "Mensual"
                            )
                            periods.forEach { (key, label) ->
                                val selected = uiState.selectedPeriod == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) tabSelectedBg else Color.Transparent)
                                        .clickable { viewModel.changePeriod(key) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected) Color.White else textMain,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    if (uiState.trends == null || uiState.trends?.dataPoints?.isEmpty() == true) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = textSub,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        "Aún no hay suficientes lecturas",
                                        color = textMain,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Realiza mediciones desde la pantalla de inicio para comenzar a ver tus gráficas.",
                                        color = textSub,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        val points = uiState.trends?.dataPoints ?: emptyList()

                        // Chart 1: BPM Trend
                        item {
                            PremiumLineChartCard(
                                title = "Tendencia de Pulso Cardíaco (BPM)",
                                dataPoints = points,
                                valueSelector = { it.avgBpm.toFloat() },
                                minSelector = { it.minBpm.toFloat() },
                                maxSelector = { it.maxBpm.toFloat() },
                                color = Color(0xFFE91E63),
                                isLight = isLight,
                                cardBg = cardBg
                            )
                        }

                        // Chart 2: Stress Trend
                        item {
                            PremiumLineChartCard(
                                title = "Variación de Estrés (%)",
                                dataPoints = points,
                                valueSelector = { it.avgStress.toFloat() },
                                minSelector = { 0f },
                                maxSelector = { 100f },
                                color = Color(0xFF9C27B0),
                                isLight = isLight,
                                cardBg = cardBg
                            )
                        }

                        // Distribution Section (Aura Distribution)
                        uiState.distribution?.let { dist ->
                            if (dist.auraDistribution.isNotEmpty()) {
                                item {
                                    AuraDistributionCard(
                                        distribution = dist.auraDistribution,
                                        cardBg = cardBg,
                                        textMain = textMain,
                                        textSub = textSub
                                    )
                                }
                            }
                        }

                        // Comparison Section
                        uiState.comparison?.let { comp ->
                            item {
                                ComparisonCard(
                                    comparison = comp,
                                    cardBg = cardBg,
                                    textMain = textMain,
                                    textSub = textSub
                                )
                            }
                        }

                        // Recommendation Navigation Link Card
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SakuraPink),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate("recommendations") }
                                    .padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "💡 Recomendaciones de Bienestar",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Descubre ejercicios y desafíos sugeridos para tu estado de estrés.",
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 12.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumLineChartCard(
    title: String,
    dataPoints: List<TrendDataPoint>,
    valueSelector: (TrendDataPoint) -> Float,
    minSelector: (TrendDataPoint) -> Float,
    maxSelector: (TrendDataPoint) -> Float,
    color: Color,
    isLight: Boolean,
    cardBg: Color
) {
    val textMain = if (isLight) Color(0xFF1A1A1A) else Color.White
    val textSub = if (isLight) Color(0xFF666666) else Color.White.copy(alpha = 0.45f)
    val gridColor = if (isLight) Color(0xFFE2E8F0) else Color(0xFF2D3748)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = textMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            val values = dataPoints.map(valueSelector)
            val minVal = dataPoints.minOf(minSelector).coerceAtMost(values.minOrNull() ?: 0f)
            val maxVal = dataPoints.maxOf(maxSelector).coerceAtLeast(values.maxOrNull() ?: 100f)
            val range = (maxVal - minVal).coerceAtLeast(1f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width / (values.size - 1).coerceAtLeast(1)
                    val h = this.size.height

                    // Draw gridlines (horizontal)
                    val gridLinesCount = 3
                    for (i in 0..gridLinesCount) {
                        val y = h * (i.toFloat() / gridLinesCount)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(this.size.width, y),
                            strokeWidth = 1f
                        )
                    }

                    // Create Area Path
                    val areaPath = Path()
                    // Create Line Path
                    val linePath = Path()

                    values.forEachIndexed { index, value ->
                        val x = index * w
                        val y = h - ((value - minVal) / range * h * 0.8f + h * 0.1f)

                        if (index == 0) {
                            areaPath.moveTo(x, h)
                            areaPath.lineTo(x, y)
                            linePath.moveTo(x, y)
                        } else {
                            linePath.lineTo(x, y)
                            areaPath.lineTo(x, y)
                        }

                        if (index == values.size - 1) {
                            areaPath.lineTo(x, h)
                            areaPath.close()
                        }
                    }

                    // Draw Area Shading
                    if (values.size > 1) {
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(color.copy(alpha = 0.35f), Color.Transparent)
                            )
                        )
                        // Draw Main Line
                        drawPath(
                            path = linePath,
                            color = color,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Draw points/dots
                    values.forEachIndexed { index, value ->
                        val x = index * w
                        val y = h - ((value - minVal) / range * h * 0.8f + h * 0.1f)
                        drawCircle(
                            color = color,
                            radius = 5.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = cardBg,
                            radius = 2.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // X-Axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(dataPoints.first().date.takeLast(5), color = textSub, fontSize = 10.sp)
                if (dataPoints.size > 2) {
                    Text(dataPoints[dataPoints.size / 2].date.takeLast(5), color = textSub, fontSize = 10.sp)
                }
                Text(dataPoints.last().date.takeLast(5), color = textSub, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun AuraDistributionCard(
    distribution: Map<String, Int>,
    cardBg: Color,
    textMain: Color,
    textSub: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Distribución de Aura", color = textMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            val total = distribution.values.sum().coerceAtLeast(1)

            distribution.entries.forEach { (aura, count) ->
                val color = AuraMapper.auraColorFromString(aura)
                val pct = (count.toFloat() / total * 100).toInt()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = aura,
                        color = textMain,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$count ($pct%)",
                        color = textSub,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                // Custom ProgressBar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(count.toFloat() / total)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun ComparisonCard(
    comparison: WeekComparison,
    cardBg: Color,
    textMain: Color,
    textSub: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Comparación de Rendimiento", color = textMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Esta semana vs anterior", color = textSub, fontSize = 11.sp)
            Spacer(Modifier.height(16.dp))

            ComparisonRow("Estrés Promedio", "${comparison.currentWeek.avgStress.toInt()}%", "${comparison.previousWeek.avgStress.toInt()}%", comparison.stressChangePct, isLowerBetter = true)
            Divider(color = textSub.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
            ComparisonRow("Pulso Cardíaco", "${comparison.currentWeek.avgBpm.toInt()} bpm", "${comparison.previousWeek.avgBpm.toInt()} bpm", comparison.bpmChangePct, isLowerBetter = true)
            Divider(color = textSub.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
            ComparisonRow("Sesiones Totales", "${comparison.currentWeek.sessions}", "${comparison.previousWeek.sessions}", comparison.sessionsChangePct, isLowerBetter = false)

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (comparison.trend) {
                            "Mejorando" -> Color(0xFFE8F5E9)
                            "Necesita Cuidado" -> Color(0xFFFFEBEE)
                            else -> Color(0xFFECEFF1)
                        }
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = when (comparison.trend) {
                        "Mejorando" -> "🎉 Tendencia: ¡Mejorando! Tu nivel de estrés y pulsaciones han disminuido. ¡Excelente progreso!"
                        "Necesita Cuidado" -> "⚠️ Tendencia: Tu nivel de estrés ha aumentado esta semana. Te sugerimos visitar la sección de recomendaciones."
                        else -> "✨ Tendencia: Estable. Has mantenido niveles constantes de bio-calma."
                    },
                    color = when (comparison.trend) {
                        "Mejorando" -> Color(0xFF2E7D32)
                        "Necesita Cuidado" -> Color(0xFFC62828)
                        else -> Color(0xFF37474F)
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ComparisonRow(
    label: String,
    currentValue: String,
    previousValue: String,
    changePct: Double,
    isLowerBetter: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = Color.Gray, fontSize = 12.sp)
            Text(currentValue, color = Color.DarkGray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text("Prev: $previousValue", color = Color.Gray, fontSize = 11.sp)
            val isPositive = changePct > 0
            val isNeutral = changePct == 0.0
            val sign = if (isPositive) "+" else ""
            val color = if (isNeutral) {
                Color.Gray
            } else if (isPositive == isLowerBetter) {
                Color(0xFFC62828) // Bad (increase in stress/bpm or decrease in sessions)
            } else {
                Color(0xFF2E7D32) // Good
            }
            Text(
                text = "$sign$changePct%",
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
