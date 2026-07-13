package com.sakura.aura.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sakura.aura.ui.auth.AuthScreen
import com.sakura.aura.ui.challenges.ChallengesScreen
import com.sakura.aura.ui.history.HistoryScreen
import com.sakura.aura.ui.home.HomeScreen
import com.sakura.aura.ui.profile.ProfileScreen
import com.sakura.aura.ui.analytics.AnalyticsScreen
import com.sakura.aura.ui.recommendations.RecommendationsScreen

@Composable
fun SakuraNavGraph(navController: NavHostController) {
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
        composable(SakuraRoutes.HOME)       { HomeScreen(navController) }
        composable(SakuraRoutes.ANALYTICS)  { AnalyticsScreen(navController) }
        composable(SakuraRoutes.RECOMMENDATIONS) { RecommendationsScreen(navController) }
        composable(SakuraRoutes.HISTORY)    { HistoryScreen(navController) }
        composable(SakuraRoutes.CHALLENGES) { ChallengesScreen(navController) }
        composable(SakuraRoutes.PROFILE)    { ProfileScreen(navController) }
    }
}