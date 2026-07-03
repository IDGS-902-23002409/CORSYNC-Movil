package com.sakura.aura.data.remote

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import io.mockk.*
import io.mockk.junit4.MockKRule
import org.junit.*
import org.junit.Assert.*

class TokenManagerTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    private lateinit var prefs: EncryptedSharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var tokenManager: TokenManager

    @Before
    fun setUp() {
        prefs = mockk()
        editor = mockk()
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.apply() } just Runs
        tokenManager = TokenManager(prefs)
    }

    @Test
    fun `saveTokens stores jwt and refresh`() {
        tokenManager.saveTokens("jwt123", "refresh456")

        verify { editor.putString("jwt_token", "jwt123") }
        verify { editor.putString("refresh_token", "refresh456") }
        verify { editor.apply() }
    }

    @Test
    fun `getJwtToken returns null when empty`() {
        every { prefs.getString("jwt_token", null) } returns null

        val result = tokenManager.getJwtToken()

        assertNull(result)
    }

    @Test
    fun `getRefreshToken returns stored value`() {
        every { prefs.getString("refresh_token", null) } returns "myRefresh"

        val result = tokenManager.getRefreshToken()

        assertEquals("myRefresh", result)
    }

    @Test
    fun `getJwtToken returns stored value`() {
        every { prefs.getString("jwt_token", null) } returns "myJwt"

        val result = tokenManager.getJwtToken()

        assertEquals("myJwt", result)
    }

    @Test
    fun `getRefreshToken returns null when empty`() {
        every { prefs.getString("refresh_token", null) } returns null

        val result = tokenManager.getRefreshToken()

        assertNull(result)
    }

    @Test
    fun `saveUserInfo stores username and nombreCompleto`() {
        tokenManager.saveUserInfo("user1", "User One")

        verify { editor.putString("username", "user1") }
        verify { editor.putString("nombre_completo", "User One") }
        verify { editor.apply() }
    }

    @Test
    fun `getUsername returns stored value`() {
        every { prefs.getString("username", null) } returns "testuser"

        val result = tokenManager.getUsername()

        assertEquals("testuser", result)
    }

    @Test
    fun `getNombreCompleto returns stored value`() {
        every { prefs.getString("nombre_completo", null) } returns "Test User"

        val result = tokenManager.getNombreCompleto()

        assertEquals("Test User", result)
    }

    @Test
    fun `clearTokens removes only tokens`() {
        every { editor.remove("jwt_token") } returns editor
        every { editor.remove("refresh_token") } returns editor

        tokenManager.clearTokens()

        verify { editor.remove("jwt_token") }
        verify { editor.remove("refresh_token") }
        verify { editor.apply() }
    }

    @Test
    fun `clearAll clears everything`() {
        every { editor.clear() } returns editor

        tokenManager.clearAll()

        verify { editor.clear() }
        verify { editor.apply() }
    }
}
