package com.sakura.aura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.sakura.aura.navigation.SakuraNavGraph
import com.sakura.aura.ui.theme.SakuraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SakuraTheme {
                val navController = rememberNavController()
                SakuraNavGraph(navController = navController)
            }
        }
    }
}