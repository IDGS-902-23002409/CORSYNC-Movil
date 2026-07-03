package com.sakura.aura.ui.auth

import com.sakura.aura.domain.model.AuthToken
import com.sakura.aura.domain.model.LoginCredentials
import com.sakura.aura.domain.model.RegistrationData
import com.sakura.aura.domain.model.UserProfile
import com.sakura.aura.domain.usecase.LoginUseCase
import com.sakura.aura.domain.usecase.RegisterUseCase
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

    private val loginUseCase: LoginUseCase = mockk()
    private val registerUseCase: RegisterUseCase = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val mockUser = UserProfile(
        id = 1, username = "testuser", email = "t@t.com",
        fullName = "Test User", spiritualName = null,
        zodiacSign = null, photoUrl = null, role = "Cliente",
        registrationDate = "2026-07-01T21:00:00Z"
    )
    private val mockAuth = AuthToken(
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
        coEvery { loginUseCase(LoginCredentials("u", "p")) } returns Result.success(mockAuth)

        val viewModel = AuthViewModel(loginUseCase, registerUseCase)
        viewModel.login("u", "p")

        val state = viewModel.uiState.value
        assertTrue("Expected Success, got $state", state is AuthUiState.Success)
        assertEquals("jwt123", (state as AuthUiState.Success).token)
    }

    @Test
    fun `login emits Error on failure`() {
        coEvery { loginUseCase(LoginCredentials("u", "p")) } returns Result.failure(Exception("Credenciales inv\u00e1lidas"))

        val viewModel = AuthViewModel(loginUseCase, registerUseCase)
        viewModel.login("u", "p")

        val state = viewModel.uiState.value
        assertTrue("Expected Error, got $state", state is AuthUiState.Error)
        assertEquals("Credenciales inv\u00e1lidas", (state as AuthUiState.Error).message)
    }

    @Test
    fun `register emits Success when successful`() {
        coEvery { registerUseCase(any<RegistrationData>()) } returns Result.success(mockAuth)

        val viewModel = AuthViewModel(loginUseCase, registerUseCase)
        viewModel.register("newuser", "n@t.com", "Pass123!", "New User")

        val state = viewModel.uiState.value
        assertTrue("Expected Success, got $state", state is AuthUiState.Success)
        assertEquals("testuser", (state as AuthUiState.Success).username)
    }

    @Test
    fun `register emits Error on failure`() {
        coEvery { registerUseCase(any<RegistrationData>()) } returns Result.failure(Exception("Error al registrarse"))

        val viewModel = AuthViewModel(loginUseCase, registerUseCase)
        viewModel.register("u", "e", "p", "n")

        val state = viewModel.uiState.value
        assertTrue("Expected Error, got $state", state is AuthUiState.Error)
        assertEquals("Error al registrarse", (state as AuthUiState.Error).message)
    }

    @Test
    fun `resetState returns to Idle`() {
        val viewModel = AuthViewModel(loginUseCase, registerUseCase)

        assertEquals(AuthUiState.Idle, viewModel.uiState.value)
        viewModel.resetState()
        assertEquals(AuthUiState.Idle, viewModel.uiState.value)
    }
}
