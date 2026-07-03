package com.sakura.aura.ui.challenges

import com.sakura.aura.domain.model.Challenge
import com.sakura.aura.domain.model.Medal
import com.sakura.aura.domain.usecase.GetChallengesUseCase
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

    private val getChallengesUseCase: GetChallengesUseCase = mockk()
    private lateinit var viewModel: ChallengesViewModel

    private val mockChallenges = listOf(
        Challenge(1, "Primera Lectura", "Desc", null, "Sesiones", 1, "sesiones", 10, 1, true, 100.0, "2026-06-30T10:15:00Z")
    )
    private val mockMedals = listOf(
        Medal(1, "Primer Escaneo", null, null, "2026-06-30T10:15:00Z")
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
        coEvery { getChallengesUseCase.getChallenges() } returns Result.success(mockChallenges)
        coEvery { getChallengesUseCase.getMedals() } returns Result.success(mockMedals)

        viewModel = ChallengesViewModel(getChallengesUseCase)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.challenges.size)
        assertEquals(1, state.medals.size)
        assertEquals("Primera Lectura", state.challenges.first().title)
    }

    @Test
    fun `loadData challenges failure sets error`() = runTest {
        coEvery { getChallengesUseCase.getChallenges() } returns Result.failure(Exception("Error al obtener desaf\u00edos"))
        coEvery { getChallengesUseCase.getMedals() } returns Result.success(mockMedals)

        viewModel = ChallengesViewModel(getChallengesUseCase)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Error al obtener desaf\u00edos", state.error)
        assertTrue(state.challenges.isEmpty())
    }

    @Test
    fun `loadData medals failure doesnt crash`() = runTest {
        coEvery { getChallengesUseCase.getChallenges() } returns Result.success(mockChallenges)
        coEvery { getChallengesUseCase.getMedals() } returns Result.failure(Exception("Medals error"))

        viewModel = ChallengesViewModel(getChallengesUseCase)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.challenges.size)
        assertTrue(state.medals.isEmpty())
    }
}
