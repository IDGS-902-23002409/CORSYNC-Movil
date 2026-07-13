package com.sakura.aura.ui.recommendations

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import com.sakura.aura.domain.model.Recommendation
import com.sakura.aura.domain.model.SuggestedChallenge
import com.sakura.aura.ui.components.SakuraBackground
import com.sakura.aura.ui.theme.LocalThemeViewModel
import com.sakura.aura.ui.theme.SakuraPink

@Composable
fun RecommendationsScreen(
    navController: NavController,
    viewModel: RecommendationsViewModel = hiltViewModel()
) {
    val themeViewModel = LocalThemeViewModel.current
    val isLight by themeViewModel.isLightTheme.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val textMain = if (isLight) Color(0xFF1A1A1A) else Color.White
    val textSub = if (isLight) Color(0xFF666666) else Color.White.copy(alpha = 0.45f)
    val cardBg = if (isLight) Color(0xFFFFFFFF) else Color(0xFF1A1A1A).copy(alpha = 0.85f)
    val itemBg = if (isLight) Color(0xFFF9F6F9) else Color(0xFF252525)

    // Handle messages/toasts
    LaunchedEffect(uiState.challengeMessage) {
        uiState.challengeMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
            // Reload recommendations after accepting challenge to update the match
            viewModel.loadRecommendations()
        }
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        SakuraBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Header with back button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = textMain
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Recomendaciones Zen",
                        color = textMain,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SakuraPink)
                    }
                } else {
                    val pack = uiState.recommendationsPackage

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Current Stress Level Summary Card
                        pack?.let { p ->
                            item {
                                StressSummaryCard(
                                    level = p.currentStressLevel,
                                    score = p.stressScore,
                                    message = p.motivationalMessage,
                                    cardBg = cardBg,
                                    textMain = textMain,
                                    textSub = textSub
                                )
                            }

                            // Wellness Categories List
                            item {
                                WellnessCategoriesCard(
                                    categories = p.wellnessCategories,
                                    cardBg = cardBg,
                                    textMain = textMain,
                                    textSub = textSub
                                )
                            }

                            // Recommendations List Header
                            if (p.recommendations.isNotEmpty()) {
                                item {
                                    Text(
                                        "Acciones Recomendadas",
                                        color = textMain,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                items(p.recommendations.size) { i ->
                                    RecommendationItemCard(
                                        recommendation = p.recommendations[i],
                                        cardBg = cardBg,
                                        itemBg = itemBg,
                                        textMain = textMain,
                                        textSub = textSub
                                    )
                                }
                            }

                            // Suggested Challenges Header
                            if (p.suggestedChallenges.isNotEmpty()) {
                                item {
                                    Text(
                                        "Desafíos Sugeridos",
                                        color = textMain,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                items(p.suggestedChallenges.size) { i ->
                                    val chal = p.suggestedChallenges[i]
                                    SuggestedChallengeCard(
                                        challenge = chal,
                                        isAccepted = uiState.acceptedChallengeId == chal.challengeId,
                                        cardBg = cardBg,
                                        textMain = textMain,
                                        textSub = textSub,
                                        onAccept = { viewModel.acceptSuggestedChallenge(chal.challengeId) }
                                    )
                                }
                            }
                        }

                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
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
    recommendation: Recommendation,
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
    challenge: SuggestedChallenge,
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
