package com.sakura.aura.ui.history

import app.cash.turbine.test
import com.sakura.aura.domain.model.Reading
import com.sakura.aura.domain.model.ReadingSummary
import com.sakura.aura.domain.usecase.GetReadingsUseCase
import io.mockk.*
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    private val getReadingsUseCase: GetReadingsUseCase = mockk()
    private lateinit var viewModel: HistoryViewModel

    private val mockReadings = listOf(
        Reading(1, "ESP32_001", 72.0, 85.0, 60.0, 1200, 1.0, 25.0, "Verde", null, 300, "2026-07-01T20:25:00Z", "2026-07-01T20:30:00Z")
    )
    private val mockSummary = ReadingSummary(72.8, 28.4, 12, "Verde", mapOf("Verde" to 7))

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadData success updates readings and summary`() = runTest {
        coEvery { getReadingsUseCase.getReadings() } returns Result.success(mockReadings)
        coEvery { getReadingsUseCase.getSummary() } returns Result.success(mockSummary)

        viewModel = HistoryViewModel(getReadingsUseCase)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.readings.size)
        assertEquals(72.8, state.summary?.globalAvgBpm)
    }

    @Test
    fun `loadData readings failure sets error`() = runTest {
        coEvery { getReadingsUseCase.getReadings() } returns Result.failure(Exception("Error de red"))
        coEvery { getReadingsUseCase.getSummary() } returns Result.success(mockSummary)

        viewModel = HistoryViewModel(getReadingsUseCase)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Error de red", state.error)
        assertTrue(state.readings.isEmpty())
    }

    @Test
    fun `loadData summary failure doesnt crash`() = runTest {
        coEvery { getReadingsUseCase.getReadings() } returns Result.success(mockReadings)
        coEvery { getReadingsUseCase.getSummary() } returns Result.failure(Exception("Summary error"))

        viewModel = HistoryViewModel(getReadingsUseCase)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.readings.size)
        assertNull(state.summary)
    }
}
