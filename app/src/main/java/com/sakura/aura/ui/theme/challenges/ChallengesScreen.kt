package com.sakura.aura.ui.theme.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sakura.aura.navigation.SakuraBottomNavBar
import com.sakura.aura.ui.theme.components.SakuraBackground

// ── Modelos ───────────────────────────────────────────────────────────────────
data class ChallengeUi(
    val icon: String,
    val title: String,
    val description: String,
    val progress: Float,
    val progressPercent: Int
)

data class MedalUi(
    val icon: String,
    val label: String,
    val unlocked: Boolean
)

// ── Datos de muestra ───────────────────────────────────────────────────────────
private val challenges = listOf(
    ChallengeUi("💨", "Aura Verde de Sanación",
        "Controla tu respiración durante 5 minutos para alcanzar la calma.", 0.80f, 80),
    ChallengeUi("🌅", "Amanecer Consciente",
        "Realiza un escaneo de energía antes de las 8:00 a.m.", 0.45f, 45),
    ChallengeUi("🌙", "Silencio Nocturno",
        "Mantén un estrés bajo durante tu sesión de la noche.", 1.00f, 100),
)

private val medals = listOf(
    MedalUi("✦", "Pétalo", true),
    MedalUi("✦", "Brisa",  true),
    MedalUi("✦", "Loto",   true),
    MedalUi("✦", "Aurora", false),
    MedalUi("✦", "Cosmos", false),
)

// ── Pantalla Desafíos ─────────────────────────────────────────────────────────
@Composable
fun ChallengesScreen(navController: NavController) {
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
                item { Spacer(modifier = Modifier.height(12.dp)) }

                // ── Título ─────────────────────────────────────────────────
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Desafíos Interiores",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Light
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Misiones diarias hacia la tranquilidad",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 12.sp
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(4.dp)) }

                // ── Cards de desafíos ──────────────────────────────────────
                items(challenges.size) { i ->
                    ChallengeCard(challenge = challenges[i])
                }

                // ── Sección Medallas ───────────────────────────────────────
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Medallas Zen",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Light
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        medals.forEach { medal ->
                            MedalBadge(
                                medal    = medal,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

// ── Card de desafío ───────────────────────────────────────────────────────────
@Composable
private fun ChallengeCard(challenge: ChallengeUi) {
    val isComplete = challenge.progress >= 1f
    val progressColor = when {
        isComplete           -> Color(0xFF2ECC71)
        challenge.progress > 0.6f -> Color(0xFFF39C12)
        else                 -> Color(0xFF5DADE2)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A).copy(alpha = 0.85f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Ícono
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF2A2A2A))
                ) {
                    Text(text = challenge.icon, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = challenge.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = challenge.description,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }

                // Badge si está completo
                if (isComplete) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2ECC71).copy(alpha = 0.2f))
                    ) {
                        Text("✓", color = Color(0xFF2ECC71), fontSize = 14.sp,
                            fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Barra de progreso
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LinearProgressIndicator(
                    progress = { challenge.progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50.dp)),
                    color            = progressColor,
                    trackColor       = Color(0xFF333333),
                )
                Text(
                    text = "${challenge.progressPercent}%",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ── Medalla Zen ───────────────────────────────────────────────────────────────
@Composable
private fun MedalBadge(medal: MedalUi, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (medal.unlocked) Color(0xFF2A2A2A)
                    else Color(0xFF1A1A1A).copy(alpha = 0.5f)
                )
        ) {
            Text(
                text = medal.icon,
                fontSize = 20.sp,
                color = if (medal.unlocked) Color(0xFFF4A7C3)
                else Color.White.copy(alpha = 0.2f)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = medal.label,
            color = if (medal.unlocked) Color.White.copy(alpha = 0.7f)
            else Color.White.copy(alpha = 0.25f),
            fontSize = 11.sp
        )
    }
}