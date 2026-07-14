package com.sakura.aura.ui.challenges

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sakura.aura.domain.model.Challenge
import com.sakura.aura.domain.model.Medal
import com.sakura.aura.navigation.SakuraBottomNavBar
import com.sakura.aura.ui.components.SakuraBackground
import com.sakura.aura.ui.theme.LocalThemeViewModel
import com.sakura.aura.ui.theme.SakuraPink
import com.sakura.aura.ui.recommendations.RecommendationsViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check

@Composable
fun ChallengesScreen(navController: NavController, initialTab: Int = 0) {

    val themeViewModel = LocalThemeViewModel.current
    val isLight by themeViewModel.isLightTheme.collectAsState()
    val challengesViewModel: ChallengesViewModel = hiltViewModel()
    val uiState by challengesViewModel.uiState.collectAsState()

    val recommendationsViewModel: RecommendationsViewModel = hiltViewModel()
    val recState by recommendationsViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(initialTab) }

    val textMain  = if (isLight) Color(0xFF1A1A1A) else Color.White
    val textSub   = if (isLight) Color(0xFF666666) else Color.White.copy(alpha = 0.45f)
    val cardBg    = if (isLight) Color(0xFFFFFFFF) else Color(0xFF1A1A1A).copy(alpha = 0.85f)
    val itemBg    = if (isLight) Color(0xFFF9F6F9) else Color(0xFF252525)
    val iconBg    = if (isLight) Color(0xFFF0EBF0) else Color(0xFF2A2A2A)
    val trackColor = if (isLight) Color(0xFFE0D8E0) else Color(0xFF333333)
    val medalBg   = if (isLight) Color(0xFFEEE8EE) else Color(0xFF2A2A2A)
    val medalBgOff = if (isLight) Color(0xFFF5F2F5) else Color(0xFF1A1A1A).copy(alpha = 0.5f)

    val tabSelectedBg = if (isLight) SakuraPink else SakuraPink.copy(alpha = 0.85f)
    val tabUnselectedBg = if (isLight) Color(0xFFEFE8EF) else Color(0xFF2A2A2A)

    LaunchedEffect(recState.challengeMessage) {
        recState.challengeMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            recommendationsViewModel.clearMessage()
            recommendationsViewModel.loadRecommendations()
            challengesViewModel.loadData()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { SakuraBottomNavBar(navController) }
    ) { innerPadding ->
        SakuraBackground {
            if (uiState.isLoading || (selectedTab == 1 && recState.isLoading)) {
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
                                "Desafíos e Intervención",
                                color = textMain,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Light
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Misiones y recomendaciones personalizadas",
                                color = textSub,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Tab Selector Row
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(tabUnselectedBg)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val tabs = listOf("Misiones Zen", "Auto-Cuidado")
                            tabs.forEachIndexed { index, label ->
                                val selected = selectedTab == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) tabSelectedBg else Color.Transparent)
                                        .clickable { selectedTab = index }
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

                    if (selectedTab == 0) {
                        // TAB 0: Misiones Zen (General challenges & medals)
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
                        } else {
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
                    } else {
                        // TAB 1: Auto-Cuidado (Recommendations based on stress)
                        recState.recommendationsPackage?.let { pack ->
                            item {
                                StressSummaryCard(
                                    level = pack.currentStressLevel,
                                    score = pack.stressScore,
                                    message = pack.motivationalMessage,
                                    cardBg = cardBg,
                                    textMain = textMain,
                                    textSub = textSub
                                )
                            }

                            item {
                                WellnessCategoriesCard(
                                    categories = pack.wellnessCategories,
                                    cardBg = cardBg,
                                    textMain = textMain,
                                    textSub = textSub
                                )
                            }

                            if (pack.recommendations.isNotEmpty()) {
                                item {
                                    Text(
                                        "Acciones Recomendadas",
                                        color = textMain,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                items(pack.recommendations.size) { i ->
                                    RecommendationItemCard(
                                        recommendation = pack.recommendations[i],
                                        cardBg = cardBg,
                                        itemBg = itemBg,
                                        textMain = textMain,
                                        textSub = textSub
                                    )
                                }
                            }

                            if (pack.suggestedChallenges.isNotEmpty()) {
                                item {
                                    Text(
                                        "Desafíos de Estrés Sugeridos",
                                        color = textMain,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                items(pack.suggestedChallenges.size) { i ->
                                    val chal = pack.suggestedChallenges[i]
                                    SuggestedChallengeCard(
                                        challenge = chal,
                                        isAccepted = recState.acceptedChallengeId == chal.challengeId,
                                        cardBg = cardBg,
                                        textMain = textMain,
                                        textSub = textSub,
                                        onAccept = { recommendationsViewModel.acceptSuggestedChallenge(chal.challengeId) }
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
    challenge  : Challenge,
    textMain   : Color,
    textSub    : Color,
    cardBg     : Color,
    iconBg     : Color,
    trackColor : Color
) {
    val progress = (challenge.currentProgress.toFloat() / challenge.targetGoal.toFloat()).coerceIn(0f, 1f)
    val progressPercent = (progress * 100).toInt()
    val isComplete = challenge.completed
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
                    Text(challenge.icon ?: "✦", fontSize = 18.sp)
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
    medal      : Medal,
    textMain   : Color,
    medalBg    : Color,
    medalBgOff : Color,
    modifier   : Modifier = Modifier
) {
    val unlocked = medal.obtainedDate != null

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
                text = medal.icon ?: "✦",
                fontSize = 20.sp,
                color = if (unlocked) SakuraPink else textMain.copy(alpha = 0.2f)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = medal.name,
            color = if (unlocked) textMain.copy(alpha = 0.7f) else textMain.copy(alpha = 0.25f),
            fontSize = 11.sp
        )
    }
}

@Composable
fun StressSummaryCard(
    level: String,
    score: Double,
    message: String,
    cardBg: Color,
    textMain: Color,
    textSub: Color
) {
    val levelColor = when (level) {
        "Muy Alto" -> Color(0xFFC62828)
        "Alto" -> Color(0xFFE65100)
        "Moderado" -> Color(0xFFFBC02D)
        "Bajo" -> Color(0xFF1976D2)
        else -> Color(0xFF2E7D32)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(levelColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${score.toInt()}%",
                        color = levelColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "estrés",
                        color = levelColor,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Nivel de Estrés: $level",
                color = textMain,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = message,
                color = textSub,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun WellnessCategoriesCard(
    categories: Map<String, String>,
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
            Text(
                "Categorías de Bienestar",
                color = textMain,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (cat, valStr) ->
                    val color = when (valStr) {
                        "Elevado", "Muy Alto", "Alto" -> Color(0xFFEF9A9A)
                        "Moderado" -> Color(0xFFFFE082)
                        else -> Color(0xFFA5D6A7)
                    }
                    val textColor = when (valStr) {
                        "Elevado", "Muy Alto", "Alto" -> Color(0xFFC62828)
                        "Moderado" -> Color(0xFFF57F17)
                        else -> Color(0xFF2E7D32)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color.copy(alpha = 0.2f))
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when (cat) {
                                    "fisico" -> "💓 Físico"
                                    "mental" -> "🧘 Mente"
                                    else -> "🔄 Constancia"
                                },
                                color = textSub,
                                fontSize = 11.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = valStr,
                                color = textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationItemCard(
    recommendation: com.sakura.aura.domain.model.Recommendation,
    cardBg: Color,
    itemBg: Color,
    textMain: Color,
    textSub: Color
) {
    val badgeColor = when (recommendation.priority) {
        "Alta" -> Color(0xFFFFEBEE)
        "Media" -> Color(0xFFFFF8E1)
        else -> Color(0xFFE8F5E9)
    }
    val badgeTextColor = when (recommendation.priority) {
        "Alta" -> Color(0xFFC62828)
        "Media" -> Color(0xFFF57F17)
        else -> Color(0xFF2E7D32)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(itemBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(recommendation.icon, fontSize = 20.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recommendation.title,
                        color = textMain,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${recommendation.category} · ${recommendation.durationMinutes} min",
                        color = textSub,
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = recommendation.priority,
                        color = badgeTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = recommendation.description,
                color = textMain.copy(alpha = 0.8f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun SuggestedChallengeCard(
    challenge: com.sakura.aura.domain.model.SuggestedChallenge,
    isAccepted: Boolean,
    cardBg: Color,
    textMain: Color,
    textSub: Color,
    onAccept: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF3E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎯", fontSize = 20.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = challenge.title,
                        color = textMain,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Match de Relevancia: ${(challenge.priorityMatch * 100).toInt()}%",
                        color = textSub,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = challenge.reason,
                color = textMain.copy(alpha = 0.8f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onAccept,
                enabled = !isAccepted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SakuraPink,
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isAccepted) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Desafío Aceptado", color = Color.White)
                } else {
                    Text("Aceptar Desafío", color = Color.White)
                }
            }
        }
    }
}
