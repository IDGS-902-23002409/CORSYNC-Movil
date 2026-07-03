package com.sakura.aura.ui.challenges

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sakura.aura.data.model.response.ChallengeResponse
import com.sakura.aura.data.model.response.MedalResponse
import com.sakura.aura.navigation.SakuraBottomNavBar
import com.sakura.aura.ui.components.SakuraBackground
import com.sakura.aura.ui.theme.LocalThemeViewModel
import com.sakura.aura.ui.theme.SakuraPink

@Composable
fun ChallengesScreen(navController: NavController) {

    val themeViewModel = LocalThemeViewModel.current
    val isLight by themeViewModel.isLightTheme.collectAsState()
    val challengesViewModel: ChallengesViewModel = hiltViewModel()
    val uiState by challengesViewModel.uiState.collectAsState()

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

                    if (uiState.challenges.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Aún no hay desafíos disponibles.",
                                    color = textSub,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    items(uiState.challenges.size) { i ->
                        ChallengeCard(
                            challenge = uiState.challenges[i],
                            textMain = textMain,
                            textSub = textSub,
                            cardBg = cardBg,
                            iconBg = iconBg,
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
                            if (uiState.medals.isEmpty()) {
                                Text(
                                    "Aún no has obtenido medallas.",
                                    color = textSub,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                uiState.medals.take(5).forEach { medal ->
                                    MedalBadge(
                                        medal = medal,
                                        textMain = textMain,
                                        medalBg = medalBg,
                                        medalBgOff = medalBgOff,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    challenge  : ChallengeResponse,
    textMain   : Color,
    textSub    : Color,
    cardBg     : Color,
    iconBg     : Color,
    trackColor : Color
) {
    val progress = (challenge.progresoActual.toFloat() / challenge.metaObjetivo.toFloat()).coerceIn(0f, 1f)
    val progressPercent = (progress * 100).toInt()
    val isComplete = challenge.completado
    val progressColor = when {
        isComplete                 -> Color(0xFF2ECC71)
        progress > 0.6f -> Color(0xFFF39C12)
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
                    Text(challenge.icono ?: "✦", fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(challenge.titulo, color = textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(3.dp))
                    Text(challenge.descripcion, color = textSub, fontSize = 12.sp, lineHeight = 17.sp)
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
                    progress = { progress },
                    modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(50.dp)),
                    color      = progressColor,
                    trackColor = trackColor
                )
                Text(
                    "$progressPercent%",
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
    medal      : MedalResponse,
    textMain   : Color,
    medalBg    : Color,
    medalBgOff : Color,
    modifier   : Modifier = Modifier
) {
    val unlocked = medal.fechaObtenida != null

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (unlocked) medalBg else medalBgOff)
        ) {
            Text(
                text = medal.icono ?: "✦",
                fontSize = 20.sp,
                color = if (unlocked) SakuraPink else textMain.copy(alpha = 0.2f)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = medal.nombre,
            color = if (unlocked) textMain.copy(alpha = 0.7f) else textMain.copy(alpha = 0.25f),
            fontSize = 11.sp
        )
    }
}
