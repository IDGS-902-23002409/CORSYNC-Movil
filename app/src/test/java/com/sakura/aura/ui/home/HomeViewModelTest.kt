package com.sakura.aura.ui.home

import com.sakura.aura.domain.model.Telemetry
import com.sakura.aura.domain.usecase.SaveReadingUseCase
import com.sakura.aura.domain.usecase.ScanAuraUseCase
import io.mockk.*
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    private val scanAuraUseCase: ScanAuraUseCase = mockk()
    private val saveReadingUseCase: SaveReadingUseCase = mockk()
    private lateinit var viewModel: HomeViewModel

    private val _isConnected = MutableStateFlow(false)
    private val _telemetry = MutableStateFlow<Telemetry?>(null)
    private val _telemetryStream = MutableSharedFlow<Telemetry>(extraBufferCapacity = 64)
    private val _error = MutableStateFlow<String?>(null)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { scanAuraUseCase.isConnected } returns _isConnected
        every { scanAuraUseCase.telemetry } returns _telemetry
        every { scanAuraUseCase.telemetryStream } returns _telemetryStream
        every { scanAuraUseCase.error } returns _error

        viewModel = HomeViewModel(scanAuraUseCase, saveReadingUseCase)
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

    /**
     * Vocabulario exacto del contrato (integration.md §4). Este es el caso que
     * fallaba: "Rojo", "Morado" y "Amarillo" caían en NEUTRAL y se pintaban gris.
     */
    @Test
    fun `AuraColorUi mapea el vocabulario del backend`() {
        assertEquals(AuraColorUi.ROJA, AuraColorUi.fromString("Rojo"))
        assertEquals(AuraColorUi.NARANJA, AuraColorUi.fromString("Naranja"))
        assertEquals(AuraColorUi.AMARILLA, AuraColorUi.fromString("Amarillo"))
        assertEquals(AuraColorUi.VERDE, AuraColorUi.fromString("Verde"))
        assertEquals(AuraColorUi.AZUL, AuraColorUi.fromString("Azul"))
        assertEquals(AuraColorUi.VIOLETA, AuraColorUi.fromString("Morado"))
    }

    @Test
    fun `AuraColorUi acepta la forma femenina que ya usaba la UI`() {
        assertEquals(AuraColorUi.ROJA, AuraColorUi.fromString("Roja"))
        assertEquals(AuraColorUi.AMARILLA, AuraColorUi.fromString("Amarilla"))
        assertEquals(AuraColorUi.VIOLETA, AuraColorUi.fromString("Violeta"))
        assertEquals(AuraColorUi.ROSA, AuraColorUi.fromString("Rosa"))
    }

    @Test
    fun `AuraColorUi normaliza espacios y mayusculas`() {
        assertEquals(AuraColorUi.ROJA, AuraColorUi.fromString("  ROJO  "))
        assertEquals(AuraColorUi.VIOLETA, AuraColorUi.fromString("morado"))
    }

    @Test
    fun `AuraColorUi fromString unknown returns NEUTRAL`() {
        assertEquals(AuraColorUi.NEUTRAL, AuraColorUi.fromString("Desconocido"))
        assertEquals(AuraColorUi.NEUTRAL, AuraColorUi.fromString(""))
    }
}
