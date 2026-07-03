package com.sakura.aura.ui.home

import com.sakura.aura.domain.model.Telemetry
import com.sakura.aura.domain.usecase.ScanAuraUseCase
import io.mockk.*
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    private val scanAuraUseCase: ScanAuraUseCase = mockk()
    private lateinit var viewModel: HomeViewModel

    private val _isConnected = MutableStateFlow(false)
    private val _telemetry = MutableStateFlow<Telemetry?>(null)
    private val _error = MutableStateFlow<String?>(null)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { scanAuraUseCase.isConnected } returns _isConnected
        every { scanAuraUseCase.telemetry } returns _telemetry
        every { scanAuraUseCase.error } returns _error

        viewModel = HomeViewModel(scanAuraUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is default`() {
        val state = viewModel.uiState.value

        assertFalse(state.isConnected)
        assertFalse(state.isScanning)
        assertNull(state.telemetry)
        assertEquals(AuraColorUi.NEUTRAL, state.auraColor)
        assertNull(state.error)
    }

    @Test
    fun `connect calls useCase connect`() = runTest {
        coEvery { scanAuraUseCase.connect() } just Runs

        viewModel.connect()

        coVerify { scanAuraUseCase.connect() }
    }

    @Test
    fun `startScan updates isScanning`() = runTest {
        coEvery { scanAuraUseCase.connect() } just Runs
        every { scanAuraUseCase.startScan() } just Runs

        viewModel.startScan()

        assertTrue(viewModel.uiState.value.isScanning)
        verify { scanAuraUseCase.startScan() }
    }

    @Test
    fun `stopScan updates isScanning to false`() = runTest {
        every { scanAuraUseCase.stopScan() } just Runs

        viewModel.stopScan()

        assertFalse(viewModel.uiState.value.isScanning)
        verify { scanAuraUseCase.stopScan() }
    }

    @Test
    fun `clearError removes error`() {
        _error.value = "some error"

        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `AuraColorUi fromString maps correctly`() {
        assertEquals(AuraColorUi.ROJA, AuraColorUi.fromString("Roja"))
        assertEquals(AuraColorUi.AZUL, AuraColorUi.fromString("Azul"))
        assertEquals(AuraColorUi.VERDE, AuraColorUi.fromString("Verde"))
        assertEquals(AuraColorUi.VIOLETA, AuraColorUi.fromString("Violeta"))
        assertEquals(AuraColorUi.NARANJA, AuraColorUi.fromString("Naranja"))
        assertEquals(AuraColorUi.ROSA, AuraColorUi.fromString("Rosa"))
    }

    @Test
    fun `AuraColorUi fromString unknown returns NEUTRAL`() {
        assertEquals(AuraColorUi.NEUTRAL, AuraColorUi.fromString("Desconocido"))
        assertEquals(AuraColorUi.NEUTRAL, AuraColorUi.fromString(""))
        assertEquals(AuraColorUi.NEUTRAL, AuraColorUi.fromString("Morado"))
    }
}
