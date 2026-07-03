package com.sakura.aura.ui.history

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sakura.aura.ui.theme.SakuraTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun historyScreen_showsTitle() {
        composeTestRule.setContent {
            SakuraTheme {
                HistoryScreen(navController = androidx.navigation.compose.rememberNavController())
            }
        }

        composeTestRule.onNodeWithText("Historial").assertExists()
    }
}
