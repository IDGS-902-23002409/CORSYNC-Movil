package com.sakura.aura.data.repository

import com.sakura.aura.data.model.response.ChallengeResponse
import com.sakura.aura.data.model.response.MedalResponse
import com.sakura.aura.data.remote.ApiService
import io.mockk.*
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import retrofit2.Response

class ChallengesRepositoryImplTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    private val apiService: ApiService = mockk()
    private lateinit var repository: ChallengesRepositoryImpl

    private val mockChallenge = ChallengeResponse(
        id = 1, titulo = "Primera Lectura", descripcion = "Realiza tu primer escaneo",
        icono = null, tipo = "Sesiones", metaObjetivo = 1, unidadMedida = "sesiones",
        puntos = 10, progresoActual = 1, completado = true,
        porcentajeProgreso = 100.0, fechaCompletado = "2026-06-30T10:15:00Z"
    )
    private val mockMedal = MedalResponse(
        id = 1, nombre = "Primer Escaneo", descripcion = null,
        icono = null, fechaObtenida = "2026-06-30T10:15:00Z"
    )

    @Before
    fun setUp() {
        repository = ChallengesRepositoryImpl(apiService)
    }

    @Test
    fun `getChallenges success returns list`() = runBlocking {
        coEvery { apiService.getChallenges() } returns Response.success(listOf(mockChallenge))

        val result = repository.getChallenges()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
        assertEquals("Primera Lectura", result.getOrNull()!!.first()?.titulo)
    }

    @Test
    fun `getChallenges failure returns error`() = runBlocking {
        coEvery { apiService.getChallenges() } returns Response.error(500, mockk(relaxed = true) { every { string() } returns "Error" })

        val result = repository.getChallenges()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getMedals success returns list`() = runBlocking {
        coEvery { apiService.getMedals() } returns Response.success(listOf(mockMedal))

        val result = repository.getMedals()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
        assertEquals("Primer Escaneo", result.getOrNull()!!.first()?.nombre)
    }

    @Test
    fun `getMedals failure returns error`() = runBlocking {
        coEvery { apiService.getMedals() } returns Response.error(500, mockk(relaxed = true) { every { string() } returns "Server error" })

        val result = repository.getMedals()

        assertTrue(result.isFailure)
    }
}
