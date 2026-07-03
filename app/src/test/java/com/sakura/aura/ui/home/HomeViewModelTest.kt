package com.sakura.aura.ui.home

import app.cash.turbine.test
import com.sakura.aura.data.remote.SignalRService
import com.sakura.aura.data.remote.TokenManager
import io.mockk.*
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    private val signalRService: SignalRService = mockk()
    private val tokenManager: TokenManager = mockk()
    private lateinit var viewModel: HomeViewModel

    private val _isConnected = MutableStateFlow(false)
    private val _telemetry = MutableStateFlow<com.sakura.aura.data.model.response.TelemetryResponse?>(null)
    private val _error = MutableStateFlow<String?>(null)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { signalRService.isConnected } returns _isConnected
        every { signalRService.telemetry } returns _telemetry
        every { signalRService.error } returns _error

        viewModel = HomeViewModel(signalRService, tokenManager)
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
    fun `connect with no token sets error`() {
        every { tokenManager.getJwtToken() } returns null

        viewModel.connect()

        assertEquals("No hay sesi\u00f3n activa", viewModel.uiState.value.error)
    }

    @Test
    fun `connect with token calls signalR`() = runTest {
        every { tokenManager.getJwtToken() } returns "test_jwt"
        coEvery { signalRService.connect("test_jwt") } just Runs

        viewModel.connect()

        coVerify { signalRService.connect("test_jwt") }
    }

    @Test
    fun `startScan updates isScanning`() = runTest {
        every { tokenManager.getJwtToken() } returns "test_jwt"
        coEvery { signalRService.connect("test_jwt") } just Runs
        every { signalRService.startMeasurement() } just Runs

        viewModel.startScan()

        assertTrue(viewModel.uiState.value.isScanning)
        verify { signalRService.startMeasurement() }
    }

    @Test
    fun `stopScan updates isScanning to false`() = runTest {
        every { signalRService.stopMeasurement() } just Runs

        viewModel.stopScan()

        assertFalse(viewModel.uiState.value.isScanning)
        verify { signalRService.stopMeasurement() }
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
