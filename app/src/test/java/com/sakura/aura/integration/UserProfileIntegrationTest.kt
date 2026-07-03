package com.sakura.aura.integration

import com.sakura.aura.data.model.request.UpdateProfileRequest
import com.sakura.aura.data.remote.ApiService
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserProfileIntegrationTest {

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
    fun `get profile then update then get stats`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""
                {
                    "id": 1, "username": "testuser", "email": "t@t.com",
                    "nombreCompleto": "Test User", "nombreEspiritual": "Luz",
                    "signoZodiacal": "Piscis", "fotoUrl": null,
                    "role": "Cliente", "fechaRegistro": "2026-07-01T21:00:00Z"
                }
            """.trimIndent())
        )

        val profileResponse = apiService.getProfile()
        assertTrue(profileResponse.isSuccessful)
        assertEquals("testuser", profileResponse.body()!!.username)
        assertEquals("Luz", profileResponse.body()!!.nombreEspiritual)

        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""
                {
                    "id": 1, "username": "testuser", "email": "t@t.com",
                    "nombreCompleto": "Updated User", "nombreEspiritual": "Sol",
                    "signoZodiacal": "Leo", "fotoUrl": "https://img.com/avatar.png",
                    "role": "Cliente", "fechaRegistro": "2026-07-01T21:00:00Z"
                }
            """.trimIndent())
        )

        val updateResponse = apiService.updateProfile(
            UpdateProfileRequest("Updated User", "Sol", "Leo", "https://img.com/avatar.png")
        )
        assertTrue(updateResponse.isSuccessful)
        assertEquals("Updated User", updateResponse.body()!!.nombreCompleto)
        assertEquals("Sol", updateResponse.body()!!.nombreEspiritual)

        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""
                {
                    "bpmPromedio": 70.0, "nivelEstresPromedio": 25.0,
                    "sesionesTotales": 15, "auraDominante": "Azul",
                    "rachaActualDias": 7, "ultimaSesion": "2026-07-01T20:30:00Z"
                }
            """.trimIndent())
        )

        val statsResponse = apiService.getUserStats()
        assertTrue(statsResponse.isSuccessful)
        assertEquals(70.0, statsResponse.body()!!.bpmPromedio, 0.01)
        assertEquals(15, statsResponse.body()!!.sesionesTotales)

        val request1 = server.takeRequest()
        assertEquals("/api/User/profile", request1.path)
        assertEquals("GET", request1.method)

        val request2 = server.takeRequest()
        assertEquals("/api/User/profile", request2.path)
        assertEquals("PUT", request2.method)

        val request3 = server.takeRequest()
        assertEquals("/api/User/stats", request3.path)
        assertEquals("GET", request3.method)
    }
}
