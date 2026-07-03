package com.sakura.aura.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Hardware
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sakura.aura.navigation.SakuraBottomNavBar
import com.sakura.aura.navigation.SakuraRoutes
import com.sakura.aura.ui.components.SakuraBackground
import com.sakura.aura.ui.theme.LocalThemeViewModel
import com.sakura.aura.ui.theme.SakuraPink

@Composable
fun ProfileScreen(
    navController  : NavController,
) {
    val themeViewModel = LocalThemeViewModel.current
    val isLight by themeViewModel.isLightTheme.collectAsState()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val uiState by profileViewModel.uiState.collectAsState()

    val bgCard    = if (isLight) Color(0xFFFFFFFF)             else Color(0xFF1A1A1A).copy(alpha = 0.85f)
    val textMain  = if (isLight) Color(0xFF1A1A1A)             else Color.White
    val textSub   = if (isLight) Color(0xFF666666)             else Color.White.copy(alpha = 0.4f)
    val divider   = if (isLight) Color(0xFFE0D8E0)             else Color.White.copy(alpha = 0.1f)
    val iconBg    = if (isLight) Color(0xFFF0EBF0)             else Color(0xFF2A2A2A)

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { SakuraBottomNavBar(navController) }
    ) { innerPadding ->

        SakuraBackground(
            overlayAlpha = if (isLight) 0.15f else 0.62f
        ) {
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(iconBg)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccountCircle,
                                    contentDescription = null,
                                    tint = textMain.copy(alpha = 0.6f),
                                    modifier = Modifier.size(72.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = uiState.user?.fullName ?: "Usuario",
                                color = textMain,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Light
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("✦", color = SakuraPink, fontSize = 12.sp)
                                Text(
                                    text = uiState.user?.spiritualName
                                        ?: uiState.user?.zodiacSign
                                        ?: "Buscador espiritual",
                                    color = textSub,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    uiState.user?.let { user ->
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = bgCard)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(SakuraPink.copy(alpha = 0.2f))
                                    ) {
                                        Text("✦", color = SakuraPink, fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = "AURA ESPIRITUAL DOMINANTE",
                                            color = textSub,
                                            fontSize = 10.sp,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = uiState.stats?.dominantAura ?: "Descubriendo...",
                                            color = textMain,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Light
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = bgCard)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(
                                    value = uiState.stats?.avgBpm?.toInt()?.toString() ?: "--",
                                    label = "BPM medio",
                                    textMain, textSub
                                )
                                Box(modifier = Modifier.width(1.dp).height(36.dp).background(divider))
                                StatItem(
                                    value = uiState.stats?.avgStressLevel?.let {
                                        when {
                                            it < 30 -> "Bajo"
                                            it < 60 -> "Medio"
                                            else -> "Alto"
                                        }
                                    } ?: "--",
                                    label = "Estrés",
                                    textMain, textSub
                                )
                                Box(modifier = Modifier.width(1.dp).height(36.dp).background(divider))
                                StatItem(
                                    value = uiState.stats?.totalSessions?.toString() ?: "--",
                                    label = "Sesiones",
                                    textMain, textSub
                                )
                            }
                        }
                    }

                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SettingsRow(
                                icon = if (isLight) Icons.Outlined.DarkMode
                                else Icons.Outlined.LightMode,
                                label = if (isLight) "Tema Oscuro" else "Tema Claro",
                                textColor = textMain,
                                iconBg = iconBg,
                                onClick = { themeViewModel.toggleTheme() }
                            )
                            SettingsRow(
                                icon = Icons.Outlined.Hardware,
                                label = "Ajustes de Hardware",
                                textColor = textMain,
                                iconBg = iconBg,
                                onClick = {}
                            )
                            SettingsRow(
                                icon = Icons.Outlined.AccountCircle,
                                label = "Cuenta",
                                textColor = textMain,
                                iconBg = iconBg,
                                onClick = {}
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                profileViewModel.logout()
                                navController.navigate(SakuraRoutes.AUTH) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            enabled = !uiState.isLoggingOut,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2A1A1A),
                                contentColor = Color(0xFFE74C3C)
                            )
                        ) {
                            Text(
                                text = if (uiState.isLoggingOut) "Cerrando sesión..." else "Cerrar sesión",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    textMain: Color,
    textSub: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = textMain, fontSize = 22.sp, fontWeight = FontWeight.Light)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = textSub, fontSize = 11.sp)
    }
}

@Composable
private fun SettingsRow(
    icon      : ImageVector,
    label     : String,
    textColor : Color,
    iconBg    : Color,
    onClick   : () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (textColor == Color(0xFF1A1A1A))
                Color(0xFFFFFFFF) else Color(0xFF1A1A1A).copy(alpha = 0.85f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
