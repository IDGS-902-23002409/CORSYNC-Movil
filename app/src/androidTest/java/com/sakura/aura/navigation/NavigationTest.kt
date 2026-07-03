package com.sakura.aura.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sakura.aura.ui.theme.SakuraTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bottomNavBar_showsAllTabs() {
        composeTestRule.setContent {
            SakuraTheme {
                BottomNavBar(navController = rememberNavController())
            }
        }

        composeTestRule.onNodeWithText("Inicio").assertExists()
        composeTestRule.onNodeWithText("Historial").assertExists()
        composeTestRule.onNodeWithText("Desaf\u00edos").assertExists()
        composeTestRule.onNodeWithText("Perfil").assertExists()
    }
}
