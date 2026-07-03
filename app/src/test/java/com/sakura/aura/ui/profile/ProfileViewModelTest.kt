package com.sakura.aura.ui.profile

import com.sakura.aura.domain.model.UserProfile
import com.sakura.aura.domain.model.UserStats
import com.sakura.aura.domain.usecase.GetUserProfileUseCase
import com.sakura.aura.domain.usecase.GetUserStatsUseCase
import com.sakura.aura.domain.usecase.LogoutUseCase
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

    private val getUserProfileUseCase: GetUserProfileUseCase = mockk()
    private val getUserStatsUseCase: GetUserStatsUseCase = mockk()
    private val logoutUseCase: LogoutUseCase = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val mockUser = UserProfile(
        id = 1, username = "testuser", email = "t@t.com",
        fullName = "Test User", spiritualName = "Luz",
        zodiacSign = "Piscis", photoUrl = null, role = "Cliente",
        registrationDate = "2026-07-01T21:00:00Z"
    )
    private val mockStats = UserStats(
        avgBpm = 72.5, avgStressLevel = 30.0,
        totalSessions = 10, dominantAura = "Verde",
        currentStreakDays = 3, lastSession = "2026-07-01T20:30:00Z"
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
        coEvery { getUserProfileUseCase() } returns Result.success(mockUser)
        coEvery { getUserStatsUseCase() } returns Result.success(mockStats)

        val viewModel = ProfileViewModel(getUserProfileUseCase, getUserStatsUseCase, logoutUseCase)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("testuser", state.user?.username)
        assertEquals("Luz", state.user?.spiritualName)
    }

    @Test
    fun `loadProfile failure sets error`() {
        coEvery { getUserProfileUseCase() } returns Result.failure(Exception("Error de red"))
        coEvery { getUserStatsUseCase() } returns Result.success(mockStats)

        val viewModel = ProfileViewModel(getUserProfileUseCase, getUserStatsUseCase, logoutUseCase)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Error de red", state.error)
        assertNull(state.user)
    }

    @Test
    fun `logout calls logoutUseCase`() {
        coEvery { getUserProfileUseCase() } returns Result.success(mockUser)
        coEvery { getUserStatsUseCase() } returns Result.success(mockStats)
        coEvery { logoutUseCase() } returns Result.success(Unit)

        val viewModel = ProfileViewModel(getUserProfileUseCase, getUserStatsUseCase, logoutUseCase)
        viewModel.logout()

        coVerify { logoutUseCase() }
    }
}
