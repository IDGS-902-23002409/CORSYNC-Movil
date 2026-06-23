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
import com.sakura.aura.ui.theme.SakuraPink
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ── Modelo de partícula del aura ───────────────────────────────────────────────
private data class AuraParticle(
    val baseX: Float,
    val baseY: Float,
    val radius: Float,
    val speedX: Float,
    val speedY: Float,
    val phase: Float
)

// ── Pantalla Home ──────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(navController: NavController) {

    var isScanning by remember { mutableStateOf(false) }
    var auraColor  by remember { mutableStateOf(Color(0xFFCCCCCC)) }

    // Genera partículas aleatorias una sola vez
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

    // Animación continua para las partículas
    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing)
        ),
        label = "particles"
    )

    // Pulso del aura al escanear
    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
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

                Spacer(modifier = Modifier.height(24.dp))

                // ── Chip de estado ─────────────────────────────────────────
                CosmicStatusChip(isScanning = isScanning)

                Spacer(modifier = Modifier.height(28.dp))

                // ── Título ─────────────────────────────────────────────────
                Text(
                    text = "Tu Aura de Hoy",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Centra tu respiración y deja fluir la energía",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ── Canvas del Aura ────────────────────────────────────────
                AuraCanvas(
                    particles   = particles,
                    animTime    = animTime,
                    auraPulse   = auraPulse,
                    isScanning  = isScanning,
                    auraColor   = auraColor,
                    modifier    = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Botón de escaneo ───────────────────────────────────────
                ScanButton(
                    isScanning = isScanning,
                    onClick    = {
                        isScanning = !isScanning
                        // Simula color al "escanear"
                        if (!isScanning) {
                            auraColor = listOf(
                                Color(0xFF9B59B6), // Violeta
                                Color(0xFF5DADE2), // Azul
                                Color(0xFF2ECC71), // Verde
                                Color(0xFFE74C3C), // Rojo
                            ).random()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ── Chip "Conexión Cósmica" ────────────────────────────────────────────────────
@Composable
private fun CosmicStatusChip(isScanning: Boolean) {
    val dotColor by animateColorAsState(
        targetValue  = if (isScanning) Color(0xFF2ECC71) else Color.White,
        animationSpec = tween(500),
        label        = "dot"
    )
    val label = if (isScanning) "Conexión Cósmica: Activa" else "Conexión Cósmica: Estable"

    Surface(
        shape = RoundedCornerShape(50.dp),
        color = Color(0xFF1E1E1E).copy(alpha = 0.85f),
        modifier = Modifier.border(
            width = 0.5.dp,
            color = Color.White.copy(alpha = 0.15f),
            shape = RoundedCornerShape(50.dp)
        )
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
                text  = label,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// ── Canvas del Aura con partículas ────────────────────────────────────────────
@Composable
private fun AuraCanvas(
    particles:  List<AuraParticle>,
    animTime:   Float,
    auraPulse:  Float,
    isScanning: Boolean,
    auraColor:  Color,
    modifier:   Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF181818).copy(alpha = 0.75f))
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(20.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Etiqueta superior
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("〜", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
            Text(
                text = "LIENZO 3D · UNITY",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp
            )
        }

        // Canvas de partículas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            // Glow central cuando escanea
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

            // Partículas orbitando
            particles.forEach { p ->
                val angle  = Math.toRadians((animTime + p.phase).toDouble())
                val spread = if (isScanning) 0.38f else 0.28f
                val x = cx + cos(angle).toFloat() * size.width  * spread * p.baseX +
                        sin(angle * 0.7).toFloat() * 20f * p.speedX
                val y = cy + sin(angle).toFloat() * size.height * spread * p.baseY +
                        cos(angle * 0.5).toFloat() * 20f * p.speedY

                val pColor = if (isScanning) auraColor else Color.White
                val pAlpha = if (isScanning) 0.7f * auraPulse else 0.55f

                drawParticle(x, y, p.radius, pColor.copy(alpha = pAlpha))
            }
        }

        // Etiqueta central "Aura"
        if (!isScanning) {
            Text(
                text  = "Aura",
                color = Color.White.copy(alpha = 0.25f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 3.sp
            )
        }
    }
}

// ── Helper: dibuja partícula con glow ─────────────────────────────────────────
private fun DrawScope.drawParticle(x: Float, y: Float, radius: Float, color: Color) {
    // Halo exterior
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.3f), Color.Transparent),
            center = Offset(x, y),
            radius = radius * 3.5f
        ),
        radius = radius * 3.5f,
        center = Offset(x, y)
    )
    // Punto central
    drawCircle(
        color  = color,
        radius = radius,
        center = Offset(x, y)
    )
}

// ── Botón de escaneo ──────────────────────────────────────────────────────────
@Composable
private fun ScanButton(isScanning: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        targetValue  = if (isScanning) Color(0xFF1A1A1A) else Color(0xFFF0F0EC),
        animationSpec = tween(400),
        label        = "scanBg"
    )
    val textColor by animateColorAsState(
        targetValue  = if (isScanning) Color.White else Color(0xFF1A1A1A),
        animationSpec = tween(400),
        label        = "scanText"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor   = textColor
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = if (isScanning) "Detener Escaneo" else "Iniciar Escaneo de Energía",
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
    }
}