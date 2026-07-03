package com.sakura.aura.integration

import com.sakura.aura.data.remote.ApiService
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChallengesIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var apiService: ApiService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `get challenges and medals in parallel`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""
                [
                    {
                        "id": 1, "titulo": "Primera Lectura",
                        "descripcion": "Realiza tu primer escaneo de aura",
                        "icono": "\uD83C\uDF1F", "tipo": "Sesiones",
                        "metaObjetivo": 1, "unidadMedida": "sesiones",
                        "puntos": 10, "progresoActual": 1, "completado": true,
                        "porcentajeProgreso": 100.0, "fechaCompletado": "2026-06-30T10:15:00Z"
                    },
                    {
                        "id": 4, "titulo": "Semana Zen",
                        "descripcion": "Completa sesiones durante 7 d\u00edas seguidos",
                        "icono": "\uD83E\uDDD8", "tipo": "Racha",
                        "metaObjetivo": 7, "unidadMedida": "d\u00edas",
                        "puntos": 100, "progresoActual": 5, "completado": false,
                        "porcentajeProgreso": 71.4, "fechaCompletado": null
                    }
                ]
            """.trimIndent())
        )

        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""
                [
                    {
                        "id": 1, "nombre": "Primer Escaneo",
                        "descripcion": "Completaste tu primera lectura de aura",
                        "icono": "\uD83C\uDFC5", "fechaObtenida": "2026-06-30T10:15:00Z"
                    }
                ]
            """.trimIndent())
        )

        val challengesResponse = apiService.getChallenges()
        assertTrue(challengesResponse.isSuccessful)
        assertEquals(2, challengesResponse.body()!!.size)
        assertEquals("Primera Lectura", challengesResponse.body()!![0].titulo)
        assertTrue(challengesResponse.body()!![0].completado)
        assertFalse(challengesResponse.body()!![1].completado)

        val medalsResponse = apiService.getMedals()
        assertTrue(medalsResponse.isSuccessful)
        assertEquals(1, medalsResponse.body()!!.size)
        assertEquals("Primer Escaneo", medalsResponse.body()!![0].nombre)

        val request1 = server.takeRequest()
        assertEquals("/api/Challenges", request1.path)

        val request2 = server.takeRequest()
        assertEquals("/api/Medals", request2.path)
    }
}
