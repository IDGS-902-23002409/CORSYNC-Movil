package com.sakura.aura.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

// ── Rutas de navegación ────────────────────────────────────────────────────────
object SakuraRoutes {
    const val AUTH    = "auth"
    const val HOME    = "home"
    const val HISTORY = "history"
    const val CHALLENGES = "challenges"
    const val PROFILE = "profile"
}

// ── Modelo de ítem del bottom nav ─────────────────────────────────────────────
data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Inicio",    SakuraRoutes.HOME,       Icons.Outlined.Home),
    BottomNavItem("Historial", SakuraRoutes.HISTORY,    Icons.Outlined.MenuBook),
    BottomNavItem("Desafíos",  SakuraRoutes.CHALLENGES, Icons.Outlined.EmojiEvents),
    BottomNavItem("Perfil",    SakuraRoutes.PROFILE,    Icons.Outlined.Person),
)

// ── Componente BottomNavBar ────────────────────────────────────────────────────
@Composable
fun SakuraBottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color(0xFF111111),
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(SakuraRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Color.White,
                    selectedTextColor   = Color.White,
                    unselectedIconColor = Color.White.copy(alpha = 0.4f),
                    unselectedTextColor = Color.White.copy(alpha = 0.4f),
                    indicatorColor      = Color(0xFF2A2A2A)
                )
            )
        }
    }
}