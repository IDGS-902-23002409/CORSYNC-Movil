package com.sakura.aura.ui.challenges

import com.sakura.aura.data.model.response.ChallengeResponse
import com.sakura.aura.data.model.response.MedalResponse
import com.sakura.aura.domain.repository.ChallengesRepository
import io.mockk.*
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ChallengesViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    private val challengesRepository: ChallengesRepository = mockk()
    private lateinit var viewModel: ChallengesViewModel

    private val mockChallenges = listOf(
        ChallengeResponse(1, "Primera Lectura", "Desc", null, "Sesiones", 1, "sesiones", 10, 1, true, 100.0, "2026-06-30T10:15:00Z")
    )
    private val mockMedals = listOf(
        MedalResponse(1, "Primer Escaneo", null, null, "2026-06-30T10:15:00Z")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadData success updates challenges and medals`() = runTest {
        coEvery { challengesRepository.getChallenges() } returns Result.success(mockChallenges)
        coEvery { challengesRepository.getMedals() } returns Result.success(mockMedals)

        viewModel = ChallengesViewModel(challengesRepository)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.challenges.size)
        assertEquals(1, state.medals.size)
        assertEquals("Primera Lectura", state.challenges.first().titulo)
    }

    @Test
    fun `loadData challenges failure sets error`() = runTest {
        coEvery { challengesRepository.getChallenges() } returns Result.failure(Exception("Error al obtener desaf\u00edos"))
        coEvery { challengesRepository.getMedals() } returns Result.success(mockMedals)

        viewModel = ChallengesViewModel(challengesRepository)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Error al obtener desaf\u00edos", state.error)
        assertTrue(state.challenges.isEmpty())
    }

    @Test
    fun `loadData medals failure doesnt crash`() = runTest {
        coEvery { challengesRepository.getChallenges() } returns Result.success(mockChallenges)
        coEvery { challengesRepository.getMedals() } returns Result.failure(Exception("Medals error"))

        viewModel = ChallengesViewModel(challengesRepository)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.challenges.size)
        assertTrue(state.medals.isEmpty())
    }
}
