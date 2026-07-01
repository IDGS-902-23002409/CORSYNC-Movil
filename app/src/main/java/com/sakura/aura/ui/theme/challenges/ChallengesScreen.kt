package com.sakura.aura.ui.theme.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.sakura.aura.ui.theme.LocalThemeViewModel
import com.sakura.aura.ui.theme.SakuraPink

data class ChallengeUi(
    val icon: String, val title: String,
    val description: String,
    val progress: Float, val progressPercent: Int
)

data class MedalUi(val icon: String, val label: String, val unlocked: Boolean)

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

@Composable
fun ChallengesScreen(navController: NavController) {

    val themeViewModel = LocalThemeViewModel.current
    val isLight by themeViewModel.isLightTheme.collectAsState()

    // ── Colores adaptativos ────────────────────────────────────────────────
    val textMain  = if (isLight) Color(0xFF1A1A1A) else Color.White
    val textSub   = if (isLight) Color(0xFF666666) else Color.White.copy(alpha = 0.45f)
    val cardBg    = if (isLight) Color(0xFFFFFFFF) else Color(0xFF1A1A1A).copy(alpha = 0.85f)
    val iconBg    = if (isLight) Color(0xFFF0EBF0) else Color(0xFF2A2A2A)
    val trackColor = if (isLight) Color(0xFFE0D8E0) else Color(0xFF333333)
    val medalBg   = if (isLight) Color(0xFFEEE8EE) else Color(0xFF2A2A2A)
    val medalBgOff = if (isLight) Color(0xFFF5F2F5) else Color(0xFF1A1A1A).copy(alpha = 0.5f)

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
                            "Desafíos Interiores",
                            color = textMain,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Light
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Misiones diarias hacia la tranquilidad",
                            color = textSub,
                            fontSize = 12.sp
                        )
                    }
                }

                item { Spacer(Modifier.height(4.dp)) }

                items(challenges.size) { i ->
                    ChallengeCard(
                        challenge  = challenges[i],
                        textMain   = textMain,
                        textSub    = textSub,
                        cardBg     = cardBg,
                        iconBg     = iconBg,
                        trackColor = trackColor
                    )
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Medallas Zen",
                        color = textMain,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Light
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        medals.forEach { medal ->
                            MedalBadge(
                                medal    = medal,
                                textMain = textMain,
                                medalBg  = medalBg,
                                medalBgOff = medalBgOff,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    challenge  : ChallengeUi,
    textMain   : Color,
    textSub    : Color,
    cardBg     : Color,
    iconBg     : Color,
    trackColor : Color
) {
    val isComplete = challenge.progress >= 1f
    val progressColor = when {
        isComplete                 -> Color(0xFF2ECC71)
        challenge.progress > 0.6f -> Color(0xFFF39C12)
        else                       -> Color(0xFF5DADE2)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBg)
                ) {
                    Text(challenge.icon, fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(challenge.title, color = textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(3.dp))
                    Text(challenge.description, color = textSub, fontSize = 12.sp, lineHeight = 17.sp)
                }
                if (isComplete) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2ECC71).copy(alpha = 0.2f))
                    ) {
                        Text("✓", color = Color(0xFF2ECC71), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LinearProgressIndicator(
                    progress = { challenge.progress },
                    modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(50.dp)),
                    color      = progressColor,
                    trackColor = trackColor
                )
                Text(
                    "${challenge.progressPercent}%",
                    color = textSub,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MedalBadge(
    medal      : MedalUi,
    textMain   : Color,
    medalBg    : Color,
    medalBgOff : Color,
    modifier   : Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (medal.unlocked) medalBg else medalBgOff)
        ) {
            Text(
                text = medal.icon,
                fontSize = 20.sp,
                color = if (medal.unlocked) SakuraPink else textMain.copy(alpha = 0.2f)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = medal.label,
            color = if (medal.unlocked) textMain.copy(alpha = 0.7f) else textMain.copy(alpha = 0.25f),
            fontSize = 11.sp
        )
    }
}