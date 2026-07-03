package com.sakura.aura.data.repository

import com.sakura.aura.data.model.request.CreateReadingRequest
import com.sakura.aura.data.model.response.ReadingResponse
import com.sakura.aura.data.model.response.ReadingSummaryResponse
import com.sakura.aura.data.remote.ApiService
import io.mockk.*
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import retrofit2.Response

class ReadingsRepositoryImplTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    private val apiService: ApiService = mockk()
    private lateinit var repository: ReadingsRepositoryImpl

    private val mockReading = ReadingResponse(
        id = 1, dispositivoId = "ESP32_001", bpmPromedio = 72.0, bpmMaximo = 85.0,
        bpmMinimo = 60.0, gsrRawPromedio = 1200, gsrVoltajePromedio = 1.0,
        nivelEstres = 25.0, auraDominante = "Verde", notas = null,
        duracionSegundos = 300, fechaInicio = "2026-07-01T20:25:00Z",
        fechaFin = "2026-07-01T20:30:00Z"
    )
    private val mockSummary = ReadingSummaryResponse(
        bpmPromedioGlobal = 72.8, nivelEstresPromedio = 28.4,
        totalSesiones = 12, auraMasFrecuente = "Verde",
        distribucionAuras = mapOf("Verde" to 7)
    )

    @Before
    fun setUp() {
        repository = ReadingsRepositoryImpl(apiService)
    }

    @Test
    fun `getReadings success returns list`() = runBlocking {
        coEvery { apiService.getReadings(any(), any()) } returns Response.success(listOf(mockReading))

        val result = repository.getReadings()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
        assertEquals("ESP32_001", result.getOrNull()!!.first()?.dispositivoId)
    }

    @Test
    fun `getReadings failure returns error`() = runBlocking {
        coEvery { apiService.getReadings(any(), any()) } returns Response.error(500, mockk(relaxed = true) { every { string() } returns "Error" })

        val result = repository.getReadings()

        assertTrue(result.isFailure)
    }

    @Test
    fun `createReading success returns reading`() = runBlocking {
        val request = CreateReadingRequest(
            dispositivoId = "ESP32_001", bpmPromedio = 72.0, bpmMaximo = 85.0,
            bpmMinimo = 60.0, gsrRawPromedio = 1200, gsrVoltajePromedio = 1.0,
            nivelEstres = 25.0, auraDominante = "Verde", notas = null,
            duracionSegundos = 300, fechaInicio = "2026-07-01T20:25:00Z",
            fechaFin = "2026-07-01T20:30:00Z"
        )
        coEvery { apiService.createReading(any()) } returns Response.success(mockReading)

        val result = repository.createReading(request)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.id)
    }

    @Test
    fun `createReading failure returns error message`() = runBlocking {
        val request = CreateReadingRequest(
            dispositivoId = "ESP32_001", bpmPromedio = 72.0, bpmMaximo = 85.0,
            bpmMinimo = 60.0, gsrRawPromedio = 1200, gsrVoltajePromedio = 1.0,
            nivelEstres = 25.0, auraDominante = "Verde", notas = null,
            duracionSegundos = 300, fechaInicio = "2026-07-01T20:25:00Z",
            fechaFin = "2026-07-01T20:30:00Z"
        )
        coEvery { apiService.createReading(any()) } returns Response.error(400, mockk<okhttp3.ResponseBody>(relaxed = true) {
            every { string() } returns "Validation failed"
        })

        val result = repository.createReading(request)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Validation failed") == true)
    }

    @Test
    fun `getSummary success returns summary`() = runBlocking {
        coEvery { apiService.getReadingsSummary() } returns Response.success(mockSummary)

        val result = repository.getSummary()

        assertTrue(result.isSuccess)
        assertEquals(72.8, result.getOrNull()!!.bpmPromedioGlobal, 0.01)
        assertEquals(12, result.getOrNull()!!.totalSesiones)
    }

    @Test
    fun `getSummary failure returns error`() = runBlocking {
        coEvery { apiService.getReadingsSummary() } returns Response.error(500, mockk(relaxed = true) { every { string() } returns "Error" })

        val result = repository.getSummary()

        assertTrue(result.isFailure)
    }
}
