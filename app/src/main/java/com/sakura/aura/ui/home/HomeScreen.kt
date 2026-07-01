package com.sakura.aura.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sakura.aura.navigation.SakuraBottomNavBar
import com.sakura.aura.ui.components.SakuraBackground
import com.sakura.aura.ui.theme.LocalThemeViewModel
import com.sakura.aura.ui.theme.SakuraPink
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class AuraParticle(
    val baseX: Float, val baseY: Float,
    val radius: Float,
    val speedX: Float, val speedY: Float,
    val phase: Float
)

@Composable
fun HomeScreen(navController: NavController) {

    val themeViewModel = LocalThemeViewModel.current
    val isLight by themeViewModel.isLightTheme.collectAsState()

    // ── ViewModel real con SignalR ─────────────────────────────────────────
    val homeViewModel: HomeViewModel = hiltViewModel()
    val uiState by homeViewModel.uiState.collectAsState()

    // Conectar al Hub cuando entra a la pantalla
    LaunchedEffect(Unit) {
        homeViewModel.connect()
    }

    // Colores adaptativos (igual que antes)
    val textMain    = if (isLight) Color(0xFF1A1A1A) else Color.White
    val textSub     = if (isLight) Color(0xFF555555) else Color.White.copy(alpha = 0.5f)
    val chipBg      = if (isLight) Color(0xFFFFFFFF) else Color(0xFF1E1E1E).copy(alpha = 0.85f)
    val chipBorder  = if (isLight) Color(0xFFE0D8E0) else Color.White.copy(alpha = 0.15f)
    val canvasBg    = if (isLight) Color(0xFFFFFFFF) else Color(0xFF181818).copy(alpha = 0.75f)
    val canvasBorder= if (isLight) Color(0xFFE0D8E0) else Color.White.copy(alpha = 0.08f)
    val labelColor  = if (isLight) Color(0xFF999999) else Color.White.copy(alpha = 0.4f)

    // Color del aura desde el backend
    val auraColor = Color(uiState.auraColor.hex)

    // Partículas (igual que antes)
    val particles = remember { List(18) { /* ... igual que antes ... */ } }
    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "particles"
    )
    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { SakuraBottomNavBar(navController) }
    ) { innerPadding ->
        SakuraBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                // ── Chip de estado ─────────────────────────────────────────
                CosmicStatusChip(
                    isScanning  = uiState.isScanning,
                    isConnected = uiState.isConnected,
                    isLight     = isLight,
                    chipBg      = chipBg,
                    chipBorder  = chipBorder,
                    textMain    = textMain
                )

                Spacer(Modifier.height(28.dp))

                Text(
                    text = "Tu Aura de Hoy",
                    color = textMain, fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Centra tu respiración y deja fluir la energía",
                    color = textSub, fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                // ── BPM en tiempo real (solo cuando escanea) ───────────────
                if (uiState.isScanning && uiState.telemetry != null) {
                    Spacer(Modifier.height(12.dp))
                    BpmDisplay(
                        bpm        = uiState.telemetry!!.bpmPromedio,
                        auraLabel  = uiState.auraColor.label,
                        auraColor  = auraColor
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Canvas ─────────────────────────────────────────────────
                AuraCanvas(
                    particles    = particles,
                    animTime     = animTime,
                    auraPulse    = auraPulse,
                    isScanning   = uiState.isScanning,
                    auraColor    = auraColor,
                    irValue      = uiState.telemetry?.ir ?: 0,
                    canvasBg     = canvasBg,
                    canvasBorder = canvasBorder,
                    labelColor   = labelColor,
                    modifier     = Modifier.fillMaxWidth().weight(1f)
                )

                // ── Error ──────────────────────────────────────────────────
                uiState.error?.let { error ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = Color(0xFFE74C3C),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Botón escaneo ──────────────────────────────────────────
                ScanButton(
                    isScanning = uiState.isScanning,
                    isLight    = isLight,
                    onClick    = {
                        if (uiState.isScanning) homeViewModel.stopScan()
                        else homeViewModel.startScan()
                    }
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ScanButton(isScanning: Boolean, isLight: Boolean, onClick: () -> Unit) {
    TODO("Not yet implemented")
}

@Composable
fun AuraCanvas(
    particles: List<Unit>,
    animTime: Float,
    auraPulse: Float,
    isScanning: Boolean,
    auraColor: Color,
    irValue: Int,
    canvasBg: Color,
    canvasBorder: Color,
    labelColor: Color,
    modifier: Modifier
) {
    TODO("Not yet implemented")
}

// ── BPM en tiempo real ────────────────────────────────────────────────────────
@Composable
private fun BpmDisplay(bpm: Int, auraLabel: String, auraColor: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("♥", color = Color(0xFFE91E8C), fontSize = 16.sp)
            Text(
                text = "$bpm",
                color = Color(0xFFE91E8C),
                fontSize = 28.sp,
                fontWeight = FontWeight.Light
            )
            Text("bpm", color = Color(0xFFE91E8C).copy(alpha = 0.7f), fontSize = 11.sp)
        }
        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.15f)))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("✦", color = auraColor, fontSize = 16.sp)
            Text(
                text = auraLabel,
                color = auraColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Light
            )
            Text("aura", color = auraColor.copy(alpha = 0.7f), fontSize = 11.sp)
        }
    }
}

// ── CosmicStatusChip actualizado (agrega isConnected) ────────────────────────
@Composable
private fun CosmicStatusChip(
    isScanning  : Boolean,
    isConnected : Boolean,
    isLight     : Boolean,
    chipBg      : Color,
    chipBorder  : Color,
    textMain    : Color
) {
    val dotColor by animateColorAsState(
        targetValue = when {
            isScanning  -> Color(0xFF2ECC71)
            isConnected -> SakuraPink
            else        -> Color(0xFF888888)
        },
        animationSpec = tween(500), label = "dot"
    )
    val label = when {
        isScanning  -> "Conexión Cósmica: Activa"
        isConnected -> "Conexión Cósmica: Estable"
        else        -> "Conexión Cósmica: Conectando..."
    }

    Surface(
        shape = RoundedCornerShape(50.dp),
        color = chipBg,
        shadowElevation = if (isLight) 2.dp else 0.dp,
        modifier = Modifier.border(0.5.dp, chipBorder, RoundedCornerShape(50.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(dotColor)
            )
            Text(text = label, color = textMain.copy(alpha = 0.85f), fontSize = 12.sp)
        }
    }
}