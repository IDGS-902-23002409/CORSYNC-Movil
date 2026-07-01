package com.sakura.aura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.sakura.aura.navigation.SakuraNavGraph
import com.sakura.aura.ui.theme.LocalThemeViewModel
import com.sakura.aura.ui.theme.SakuraTheme
import com.sakura.aura.utils.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isLight by themeViewModel.isLightTheme.collectAsState()

            CompositionLocalProvider(LocalThemeViewModel provides themeViewModel) {
                SakuraTheme(isLightTheme = isLight) {
                    val navController = rememberNavController()
                    SakuraNavGraph(navController = navController)
                }
            }
        }
    }
}