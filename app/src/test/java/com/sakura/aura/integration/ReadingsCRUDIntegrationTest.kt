package com.sakura.aura.integration

import com.sakura.aura.data.model.request.CreateReadingRequest
import com.sakura.aura.data.remote.ApiService
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ReadingsCRUDIntegrationTest {

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
    fun `create reading then list then get summary`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(201).setBody("""
                {
                    "id": 1, "dispositivoId": "ESP32_001", "bpmPromedio": 72.0,
                    "bpmMaximo": 85.0, "bpmMinimo": 60.0, "gsrRawPromedio": 1200,
                    "gsrVoltajePromedio": 1.0, "nivelEstres": 25.0,
                    "auraDominante": "Verde", "notas": "Buena sesión",
                    "duracionSegundos": 300, "fechaInicio": "2026-07-01T20:25:00Z",
                    "fechaFin": "2026-07-01T20:30:00Z"
                }
            """.trimIndent())
        )

        val createResponse = apiService.createReading(
            CreateReadingRequest(
                dispositivoId = "ESP32_001", bpmPromedio = 72.0, bpmMaximo = 85.0,
                bpmMinimo = 60.0, gsrRawPromedio = 1200, gsrVoltajePromedio = 1.0,
                nivelEstres = 25.0, auraDominante = "Verde", notas = "Buena sesión",
                duracionSegundos = 300, fechaInicio = "2026-07-01T20:25:00Z",
                fechaFin = "2026-07-01T20:30:00Z"
            )
        )
        assertTrue(createResponse.isSuccessful)
        assertEquals(1, createResponse.body()!!.id)
        assertEquals("Verde", createResponse.body()!!.auraDominante)

        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""
                [
                    {
                        "id": 1, "dispositivoId": "ESP32_001", "bpmPromedio": 72.0,
                        "bpmMaximo": 85.0, "bpmMinimo": 60.0, "gsrRawPromedio": 1200,
                        "gsrVoltajePromedio": 1.0, "nivelEstres": 25.0,
                        "auraDominante": "Verde", "notas": null,
                        "duracionSegundos": 300, "fechaInicio": "2026-07-01T20:25:00Z",
                        "fechaFin": "2026-07-01T20:30:00Z"
                    }
                ]
            """.trimIndent())
        )

        val listResponse = apiService.getReadings()
        assertTrue(listResponse.isSuccessful)
        assertEquals(1, listResponse.body()!!.size)

        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""
                {
                    "bpmPromedioGlobal": 72.8, "nivelEstresPromedio": 28.4,
                    "totalSesiones": 1, "auraMasFrecuente": "Verde",
                    "distribucionAuras": {"Verde": 1}
                }
            """.trimIndent())
        )

        val summaryResponse = apiService.getReadingsSummary()
        assertTrue(summaryResponse.isSuccessful)
        assertEquals(1, summaryResponse.body()!!.totalSesiones)

        val request1 = server.takeRequest()
        assertEquals("/api/Readings", request1.path)
        assertEquals("POST", request1.method)

        val request2 = server.takeRequest()
        assertTrue(request2.path!!.startsWith("/api/Readings"))
        assertEquals("GET", request2.method)

        val request3 = server.takeRequest()
        assertEquals("/api/Readings/summary", request3.path)
        assertEquals("GET", request3.method)
    }
}
