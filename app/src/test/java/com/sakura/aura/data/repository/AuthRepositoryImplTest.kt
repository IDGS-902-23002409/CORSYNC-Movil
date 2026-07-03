package com.sakura.aura.data.repository

import com.sakura.aura.data.model.response.AuthResponse
import com.sakura.aura.data.model.response.UserResponse
import com.sakura.aura.data.remote.ApiService
import com.sakura.aura.data.remote.TokenManager
import com.sakura.aura.domain.model.AuthToken
import com.sakura.aura.domain.model.LoginCredentials
import com.sakura.aura.domain.model.RegistrationData
import io.mockk.*
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Response
import java.net.SocketException

@RunWith(RobolectricTestRunner::class)
class AuthRepositoryImplTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    private val apiService: ApiService = mockk()
    private val tokenManager: TokenManager = mockk()
    private lateinit var repository: AuthRepositoryImpl

    private val mockUser = UserResponse(
        id = 1, username = "testuser", email = "test@test.com",
        nombreCompleto = "Test User", nombreEspiritual = null,
        signoZodiacal = null, fotoUrl = null, role = "Cliente",
        fechaRegistro = "2026-07-01T21:00:00Z"
    )
    private val mockAuth = AuthResponse(
        token = "jwt123", refreshToken = "rt456",
        expiration = "2026-07-02T05:00:00Z", user = mockUser
    )

    @Before
    fun setUp() {
        repository = AuthRepositoryImpl(apiService, tokenManager)
    }

    @Test
    fun `register success saves tokens and returns auth`() = runBlocking {
        coEvery { apiService.register(any()) } returns Response.success(mockAuth)
        every { tokenManager.saveTokens(any(), any()) } just Runs

        val result = repository.register(RegistrationData("u", "e", "p", "n"))

        assertTrue(result.isSuccess)
        assertEquals("jwt123", result.getOrNull()!!.token)
        coVerify { tokenManager.saveTokens("jwt123", "rt456") }
    }

    @Test
    fun `register failure returns error`() = runBlocking {
        coEvery { apiService.register(any()) } returns Response.error(400, mockk(relaxed = true) { every { string() } returns "Bad request" })

        val result = repository.register(RegistrationData("u", "e", "p", "n"))

        assertTrue(result.isFailure)
    }

    @Test
    fun `login success saves tokens`() = runBlocking {
        coEvery { apiService.login(any()) } returns Response.success(mockAuth)
        every { tokenManager.saveTokens(any(), any()) } just Runs

        val result = repository.login(LoginCredentials("u", "p"))

        assertTrue(result.isSuccess)
        assertEquals("jwt123", result.getOrNull()!!.token)
        coVerify { tokenManager.saveTokens("jwt123", "rt456") }
    }

    @Test
    fun `login failure returns error`() = runBlocking {
        coEvery { apiService.login(any()) } returns Response.error(401, mockk(relaxed = true) { every { string() } returns "Unauthorized" })

        val result = repository.login(LoginCredentials("u", "p"))

        assertTrue(result.isFailure)
    }

    @Test
    fun `logout clears tokens`() = runBlocking {
        coEvery { apiService.logout(any()) } returns Response.success(Unit)
        every { tokenManager.clearTokens() } just Runs

        val result = repository.logout("jwt", "rt")

        assertTrue(result.isSuccess)
        coVerify { tokenManager.clearTokens() }
    }

    @Test
    fun `logout network error still clears tokens`() = runBlocking {
        coEvery { apiService.logout(any()) } throws SocketException("reset")
        every { tokenManager.clearTokens() } just Runs

        val result = repository.logout("jwt", "rt")

        assertTrue(result.isSuccess)
        coVerify { tokenManager.clearTokens() }
    }

    @Test
    fun `refreshToken success saves new tokens`() = runBlocking {
        coEvery { apiService.refreshToken(any()) } returns Response.success(mockAuth)
        every { tokenManager.saveTokens(any(), any()) } just Runs

        val result = repository.refreshToken(
            AuthToken(
                token = "jwt123", refreshToken = "rt456",
                expiration = "2026-07-02T05:00:00Z",
                user = com.sakura.aura.domain.model.UserProfile(
                    id = 1, username = "testuser", email = "test@test.com",
                    fullName = "Test User", spiritualName = null,
                    zodiacSign = null, photoUrl = null, role = "Cliente",
                    registrationDate = "2026-07-01T21:00:00Z"
                )
            )
        )

        assertTrue(result.isSuccess)
        coVerify { tokenManager.saveTokens("jwt123", "rt456") }
    }

    @Test
    fun `refreshToken failure returns error`() = runBlocking {
        coEvery { apiService.refreshToken(any()) } returns Response.error(401, mockk(relaxed = true) { every { string() } returns "Invalid token" })

        val result = repository.refreshToken(
            AuthToken(
                token = "jwt123", refreshToken = "rt456",
                expiration = "2026-07-02T05:00:00Z",
                user = com.sakura.aura.domain.model.UserProfile(
                    id = 1, username = "testuser", email = "test@test.com",
                    fullName = "Test User", spiritualName = null,
                    zodiacSign = null, photoUrl = null, role = "Cliente",
                    registrationDate = "2026-07-01T21:00:00Z"
                )
            )
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `register socketException triggers login fallback`() = runBlocking {
        val registerSlot = slot<com.sakura.aura.data.model.request.RegisterRequest>()
        val loginSlot = slot<com.sakura.aura.data.model.request.LoginRequest>()

        coEvery { apiService.register(capture(registerSlot)) } throws SocketException("Connection reset")
        coEvery { apiService.login(capture(loginSlot)) } returns Response.success(mockAuth)
        every { tokenManager.saveTokens(any(), any()) } just Runs

        val registerReq = RegistrationData("testuser", "test@test.com", "Pass123!", "Test User")
        val result = repository.register(registerReq)

        assertTrue(result.isSuccess)
        assertEquals("testuser", loginSlot.captured.username)
        assertEquals("Pass123!", loginSlot.captured.password)
    }

    @Test
    fun `register login fallback failure`() = runBlocking {
        coEvery { apiService.register(any()) } throws SocketException("reset")
        coEvery { apiService.login(any()) } returns Response.error(401, mockk(relaxed = true) { every { string() } returns "Bad creds" })

        val result = repository.register(RegistrationData("u", "e", "p", "n"))

        assertTrue(result.isFailure)
    }
}
