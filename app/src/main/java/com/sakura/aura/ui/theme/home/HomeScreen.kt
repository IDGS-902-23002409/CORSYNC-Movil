package com.sakura.aura.ui.theme.home

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
import androidx.navigation.NavController
import com.sakura.aura.navigation.SakuraBottomNavBar
import com.sakura.aura.ui.theme.components.SakuraBackground
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

    // ── Colores adaptativos ────────────────────────────────────────────────
    val textMain   = if (isLight) Color(0xFF1A1A1A) else Color.White
    val textSub    = if (isLight) Color(0xFF555555) else Color.White.copy(alpha = 0.5f)
    val chipBg     = if (isLight) Color(0xFFFFFFFF) else Color(0xFF1E1E1E).copy(alpha = 0.85f)
    val chipBorder = if (isLight) Color(0xFFE0D8E0) else Color.White.copy(alpha = 0.15f)
    val canvasBg   = if (isLight) Color(0xFFFFFFFF) else Color(0xFF181818).copy(alpha = 0.75f)
    val canvasBorder = if (isLight) Color(0xFFE0D8E0) else Color.White.copy(alpha = 0.08f)
    val labelColor = if (isLight) Color(0xFF999999) else Color.White.copy(alpha = 0.4f)

    var isScanning by remember { mutableStateOf(false) }
    var auraColor  by remember { mutableStateOf(Color(0xFFCCCCCC)) }

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
                    isScanning  = isScanning,
                    isLight     = isLight,
                    chipBg      = chipBg,
                    chipBorder  = chipBorder,
                    textMain    = textMain
                )

                Spacer(Modifier.height(28.dp))

                Text(
                    text = "Tu Aura de Hoy",
                    color = textMain,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Centra tu respiración y deja fluir la energía",
                    color = textSub,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                // ── Canvas del Aura ────────────────────────────────────────
                AuraCanvas(
                    particles    = particles,
                    animTime     = animTime,
                    auraPulse    = auraPulse,
                    isScanning   = isScanning,
                    auraColor    = auraColor,
                    canvasBg     = canvasBg,
                    canvasBorder = canvasBorder,
                    labelColor   = labelColor,
                    modifier     = Modifier.fillMaxWidth().weight(1f)
                )

                Spacer(Modifier.height(24.dp))

                // ── Botón de escaneo ───────────────────────────────────────
                ScanButton(
                    isScanning = isScanning,
                    isLight    = isLight,
                    onClick    = {
                        isScanning = !isScanning
                        if (!isScanning) {
                            auraColor = listOf(
                                Color(0xFF9B59B6),
                                Color(0xFF5DADE2),
                                Color(0xFF2ECC71),
                                Color(0xFFE74C3C),
                            ).random()
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CosmicStatusChip(
    isScanning : Boolean,
    isLight    : Boolean,
    chipBg     : Color,
    chipBorder : Color,
    textMain   : Color
) {
    val dotColor by animateColorAsState(
        targetValue   = if (isScanning) Color(0xFF2ECC71) else SakuraPink,
        animationSpec = tween(500),
        label         = "dot"
    )

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
            Text(
                text = if (isScanning) "Conexión Cósmica: Activa"
                else "Conexión Cósmica: Estable",
                color = textMain.copy(alpha = 0.85f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AuraCanvas(
    particles    : List<AuraParticle>,
    animTime     : Float,
    auraPulse    : Float,
    isScanning   : Boolean,
    auraColor    : Color,
    canvasBg     : Color,
    canvasBorder : Color,
    labelColor   : Color,
    modifier     : Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(canvasBg)
            .border(0.5.dp, canvasBorder, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.align(Alignment.TopStart).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("〜", color = labelColor, fontSize = 11.sp)
            Text(
                text = "LIENZO 3D · UNITY",
                color = labelColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            if (isScanning) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            auraColor.copy(alpha = 0.25f * auraPulse),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = size.minDimension * 0.45f * auraPulse
                    ),
                    radius = size.minDimension * 0.45f,
                    center = Offset(cx, cy)
                )
            }

            particles.forEach { p ->
                val angle  = Math.toRadians((animTime + p.phase).toDouble())
                val spread = if (isScanning) 0.38f else 0.28f
                val x = cx + cos(angle).toFloat() * size.width  * spread * p.baseX +
                        sin(angle * 0.7).toFloat() * 20f * p.speedX
                val y = cy + sin(angle).toFloat() * size.height * spread * p.baseY +
                        cos(angle * 0.5).toFloat() * 20f * p.speedY
                val pColor = if (isScanning) auraColor else Color(0xFF9B59B6)
                val pAlpha = if (isScanning) 0.7f * auraPulse else 0.55f
                drawParticle(x, y, p.radius, pColor.copy(alpha = pAlpha))
            }
        }

        if (!isScanning) {
            Text(
                text = "Aura",
                color = labelColor.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 3.sp
            )
        }
    }
}

private fun DrawScope.drawParticle(x: Float, y: Float, radius: Float, color: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.3f), Color.Transparent),
            center = Offset(x, y), radius = radius * 3.5f
        ),
        radius = radius * 3.5f, center = Offset(x, y)
    )
    drawCircle(color = color, radius = radius, center = Offset(x, y))
}

@Composable
private fun ScanButton(isScanning: Boolean, isLight: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        targetValue   = when {
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
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor   = textColor
        )
    ) {
        Icon(Icons.Outlined.AutoAwesome, null, Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            text = if (isScanning) "Detener Escaneo" else "Iniciar Escaneo de Energía",
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
    }
}