package com.sakura.aura.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sakura.aura.ui.theme.auth.AuthScreen
import com.sakura.aura.ui.theme.home.HomeScreen

@Composable
fun SakuraNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = SakuraRoutes.AUTH
    ) {
        composable(SakuraRoutes.AUTH) {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate(SakuraRoutes.HOME) {
                        popUpTo(SakuraRoutes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(SakuraRoutes.HOME) {
            HomeScreen(navController = navController)
        }

        // Estas las implementaremos en los siguientes pasos
        composable(SakuraRoutes.HISTORY)    { PlaceholderScreen("Historial") }
        composable(SakuraRoutes.CHALLENGES) { PlaceholderScreen("Desafíos") }
        composable(SakuraRoutes.PROFILE)    { PlaceholderScreen("Perfil") }
    }
}

// Pantalla temporal hasta implementar cada una
@Composable
private fun PlaceholderScreen(name: String) {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = name,
            color = androidx.compose.ui.graphics.Color.White
        )
    }
}