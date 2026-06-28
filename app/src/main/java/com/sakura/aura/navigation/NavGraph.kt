package com.sakura.aura.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sakura.aura.ui.theme.auth.AuthScreen
import com.sakura.aura.ui.theme.challenges.ChallengesScreen
import com.sakura.aura.ui.theme.history.HistoryScreen
import com.sakura.aura.ui.theme.home.HomeScreen
import com.sakura.aura.ui.theme.profile.ProfileScreen
import com.sakura.aura.utils.ThemeViewModel

@Composable
fun SakuraNavGraph(
    navController  : NavHostController,
    themeViewModel : ThemeViewModel
) {
    NavHost(
        navController    = navController,
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

        composable(SakuraRoutes.HISTORY) {
            HistoryScreen(navController = navController)
        }

        composable(SakuraRoutes.CHALLENGES) {
            ChallengesScreen(navController = navController)
        }

        composable(SakuraRoutes.PROFILE) {
            ProfileScreen(
                navController  = navController,
                themeViewModel = themeViewModel
            )
        }
    }
}