package com.sakura.aura.ui.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sakura.aura.ui.theme.SakuraTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun authScreen_showsLoginFields() {
        composeTestRule.setContent {
            SakuraTheme {
                AuthScreen(onLoginSuccess = {})
            }
        }

        composeTestRule.onNodeWithText("Iniciar Sesi\u00f3n").assertExists()
        composeTestRule.onNodeWithText("Registrarse").assertExists()
    }
}
