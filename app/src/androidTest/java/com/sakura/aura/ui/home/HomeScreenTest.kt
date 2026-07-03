package com.sakura.aura.ui.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sakura.aura.ui.theme.SakuraTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_showsScanButton() {
        composeTestRule.setContent {
            SakuraTheme {
                HomeScreen(navController = androidx.navigation.compose.rememberNavController())
            }
        }

        composeTestRule.onNodeWithText("Escanear").assertExists()
    }
}
