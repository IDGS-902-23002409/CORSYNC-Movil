package com.sakura.aura.ui.auth

import com.sakura.aura.data.model.request.LoginRequest
import com.sakura.aura.data.model.request.RegisterRequest
import com.sakura.aura.data.model.response.AuthResponse
import com.sakura.aura.data.model.response.UserResponse
import com.sakura.aura.data.remote.TokenManager
import com.sakura.aura.domain.repository.AuthRepository
import io.mockk.*
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    private val authRepository: AuthRepository = mockk()
    private val tokenManager: TokenManager = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val mockUser = UserResponse(
        id = 1, username = "testuser", email = "t@t.com",
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
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login emits Success when successful`() {
        coEvery { authRepository.login(LoginRequest("u", "p")) } returns Result.success(mockAuth)
        every { tokenManager.saveUserInfo(any(), any()) } just Runs

        val viewModel = AuthViewModel(authRepository, tokenManager)
        viewModel.login("u", "p")

        val state = viewModel.uiState.value
        assertTrue("Expected Success, got $state", state is AuthUiState.Success)
        assertEquals("jwt123", (state as AuthUiState.Success).token)
    }

    @Test
    fun `login emits Error on failure`() {
        coEvery { authRepository.login(LoginRequest("u", "p")) } returns Result.failure(Exception("Credenciales inv\u00e1lidas"))

        val viewModel = AuthViewModel(authRepository, tokenManager)
        viewModel.login("u", "p")

        val state = viewModel.uiState.value
        assertTrue("Expected Error, got $state", state is AuthUiState.Error)
        assertEquals("Credenciales inv\u00e1lidas", (state as AuthUiState.Error).message)
    }

    @Test
    fun `register emits Success when successful`() {
        coEvery { authRepository.register(any<RegisterRequest>()) } returns Result.success(mockAuth)
        every { tokenManager.saveUserInfo(any(), any()) } just Runs

        val viewModel = AuthViewModel(authRepository, tokenManager)
        viewModel.register("newuser", "n@t.com", "Pass123!", "New User")

        val state = viewModel.uiState.value
        assertTrue("Expected Success, got $state", state is AuthUiState.Success)
        assertEquals("testuser", (state as AuthUiState.Success).username)
    }

    @Test
    fun `register emits Error on failure`() {
        coEvery { authRepository.register(any<RegisterRequest>()) } returns Result.failure(Exception("Error al registrarse"))

        val viewModel = AuthViewModel(authRepository, tokenManager)
        viewModel.register("u", "e", "p", "n")

        val state = viewModel.uiState.value
        assertTrue("Expected Error, got $state", state is AuthUiState.Error)
        assertEquals("Error al registrarse", (state as AuthUiState.Error).message)
    }

    @Test
    fun `resetState returns to Idle`() {
        val viewModel = AuthViewModel(authRepository, tokenManager)

        assertEquals(AuthUiState.Idle, viewModel.uiState.value)
        viewModel.resetState()
        assertEquals(AuthUiState.Idle, viewModel.uiState.value)
    }
}
