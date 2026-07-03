package com.sakura.aura.ui.profile

import com.sakura.aura.data.model.request.LogoutRequest
import com.sakura.aura.data.model.response.UserResponse
import com.sakura.aura.data.model.response.UserStatsResponse
import com.sakura.aura.data.remote.TokenManager
import com.sakura.aura.domain.repository.AuthRepository
import com.sakura.aura.domain.repository.UserRepository
import io.mockk.*
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    private val userRepository: UserRepository = mockk()
    private val authRepository: AuthRepository = mockk()
    private val tokenManager: TokenManager = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val mockUser = UserResponse(
        id = 1, username = "testuser", email = "t@t.com",
        nombreCompleto = "Test User", nombreEspiritual = "Luz",
        signoZodiacal = "Piscis", fotoUrl = null, role = "Cliente",
        fechaRegistro = "2026-07-01T21:00:00Z"
    )
    private val mockStats = UserStatsResponse(
        bpmPromedio = 72.5, nivelEstresPromedio = 30.0,
        sesionesTotales = 10, auraDominante = "Verde",
        rachaActualDias = 3, ultimaSesion = "2026-07-01T20:30:00Z"
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
    fun `loadProfile success updates user`() {
        coEvery { userRepository.getProfile() } returns Result.success(mockUser)
        coEvery { userRepository.getStats() } returns Result.success(mockStats)

        val viewModel = ProfileViewModel(userRepository, authRepository, tokenManager)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("testuser", state.user?.username)
        assertEquals("Luz", state.user?.nombreEspiritual)
    }

    @Test
    fun `loadProfile failure sets error`() {
        coEvery { userRepository.getProfile() } returns Result.failure(Exception("Error de red"))
        coEvery { userRepository.getStats() } returns Result.success(mockStats)

        val viewModel = ProfileViewModel(userRepository, authRepository, tokenManager)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Error de red", state.error)
        assertNull(state.user)
    }

    @Test
    fun `logout clears tokens`() {
        coEvery { userRepository.getProfile() } returns Result.success(mockUser)
        coEvery { userRepository.getStats() } returns Result.success(mockStats)
        every { tokenManager.getJwtToken() } returns "jwt"
        every { tokenManager.getRefreshToken() } returns "rt"
        coEvery { authRepository.logout(LogoutRequest("jwt", "rt")) } returns Result.success(Unit)

        val viewModel = ProfileViewModel(userRepository, authRepository, tokenManager)
        viewModel.logout()

        coVerify { authRepository.logout(LogoutRequest("jwt", "rt")) }
    }

    @Test
    fun `logout without tokens clears all`() {
        coEvery { userRepository.getProfile() } returns Result.success(mockUser)
        coEvery { userRepository.getStats() } returns Result.success(mockStats)
        every { tokenManager.getJwtToken() } returns null
        every { tokenManager.getRefreshToken() } returns null
        every { tokenManager.clearAll() } just Runs

        val viewModel = ProfileViewModel(userRepository, authRepository, tokenManager)
        viewModel.logout()

        verify { tokenManager.clearAll() }
    }
}
