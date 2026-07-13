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
                        bpm        = uiState.telemetry!!.avgBpm,
                        auraLabel  = uiState.auraColor.label,
                        auraColor  = auraColor
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Resultado o Canvas ─────────────────────────────────────
                val result = uiState.scanResult
                if (result != null) {
                    AuraResultDisplay(
                        auraColor = Color(result.auraColor.hex),
                        auraLabel = result.auraColor.label,
                        avgBpm    = result.avgBpm,
                        stress    = result.stressLevel,
                        textMain  = textMain,
                        textSub   = textSub,
                        modifier  = Modifier.fillMaxWidth().weight(1f)
                    )
                } else {
                    AuraCanvas(
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
                }

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
                    isScanning  = uiState.isScanning,
                    isConnecting = uiState.isConnecting,
                    hasResult   = result != null,
                    isLight     = isLight,
                    onClick     = {
                        if (uiState.isScanning) homeViewModel.stopScan()
                        else if (!uiState.isConnecting) {
                            if (result != null) homeViewModel.resetScan()
                            homeViewModel.startScan()
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ScanButton(isScanning: Boolean, isConnecting: Boolean, hasResult: Boolean, isLight: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isScanning && isLight  -> Color(0xFF1A1A1A)
            isScanning && !isLight -> Color(0xFF1A1A1A)
            !isScanning && isLight -> Color(0xFF1A1A1A)
            else                   -> Color(0xFFF0F0EC)
        },
        animationSpec = tween(400), label = "scanBg"
    )
    val textColor by animateColorAsState(
        targetValue   = if (!isScanning && !isLight) Color(0xFF1A1A1A) else Color.White,
        animationSpec = tween(400), label = "scanText"
    )

    Button(
        onClick = onClick,
        enabled = !isConnecting,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor   = textColor,
            disabledContainerColor = bgColor.copy(alpha = 0.5f),
            disabledContentColor = textColor.copy(alpha = 0.5f)
        )
    ) {
        if (isConnecting) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Conectando...",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        } else {
            val label = when {
                isScanning -> "Detener Escaneo"
                hasResult  -> "Escanear de Nuevo"
                else       -> "Iniciar Escaneo de Energía"
            }
            Icon(Icons.Outlined.AutoAwesome, null, Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun AuraResultDisplay(
    auraColor: Color,
    auraLabel: String,
    avgBpm: Double,
    stress: Double,
    textMain: Color,
    textSub: Color,
    modifier: Modifier
) {
    val stressLabel = when {
        stress < 30 -> "Bajo"
        stress < 60 -> "Medio"
        else        -> "Alto"
    }
    val stressColor = when {
        stress < 30 -> Color(0xFF2ECC71)
        stress < 60 -> Color(0xFFF1C40F)
        else        -> Color(0xFFE74C3C)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("✦", fontSize = 64.sp, color = auraColor)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Tu Aura es $auraLabel",
                color = auraColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Light
            )
            Spacer(Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("♥", color = Color(0xFFE91E8C), fontSize = 18.sp)
                    Text(
                        text = "${avgBpm.toInt()}",
                        color = textMain,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Light
                    )
                    Text("bpm", color = textSub, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⏱", color = stressColor, fontSize = 18.sp)
                    Text(
                        text = stressLabel,
                        color = stressColor,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Light
                    )
                    Text("estrés", color = textSub, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Respira profundo. Tu energía está en armonía.",
                color = textSub,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun AuraCanvas(
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
    val particles = remember {
        List(18) {
            AuraParticle(
                baseX  = Random.nextFloat(),
                baseY  = Random.nextFloat(),
                radius = Random.nextFloat() * 5f + 3f,
                speedX = (Random.nextFloat() - 0.5f) * 0.3f,
                speedY = (Random.nextFloat() - 0.5f) * 0.3f,
                phase  = Random.nextFloat() * 360f
            )
        }
    }
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