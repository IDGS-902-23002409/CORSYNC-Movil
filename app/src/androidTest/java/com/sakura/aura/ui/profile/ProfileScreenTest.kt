package com.sakura.aura.ui.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sakura.aura.ui.theme.SakuraTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun profileScreen_showsLogoutButton() {
        composeTestRule.setContent {
            SakuraTheme {
                ProfileScreen(navController = androidx.navigation.compose.rememberNavController())
            }
        }

        composeTestRule.onNodeWithText("Cerrar Sesi\u00f3n").assertExists()
    }
}
