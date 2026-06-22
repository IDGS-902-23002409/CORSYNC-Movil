package com.sakura.aura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.sakura.aura.ui.theme.auth.AuthScreen
import com.sakura.aura.ui.theme.SakuraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SakuraTheme {
                // Por ahora mostramos AuthScreen directamente
                // Cuando tengamos NavGraph lo reemplazamos
                AuthScreen(
                    onLoginSuccess = {
                        // TODO: navegar a HomeScreen
                    }
                )
            }
        }
    }
}