package com.sakura.aura.integration

import com.sakura.aura.data.model.request.RegisterRequest
import com.sakura.aura.data.remote.ApiService
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthRecoverySocketIntegrationTest {

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
    fun `register sends correct JSON body`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(201).setBody("""
                {
                    "token": "jwt_register",
                    "refreshToken": "rt_register",
                    "expiration": "2026-07-02T05:00:00Z",
                    "user": {
                        "id": 1, "username": "newuser", "email": "n@t.com",
                        "nombreCompleto": "New User", "nombreEspiritual": null,
                        "signoZodiacal": null, "fotoUrl": null, "role": "Cliente",
                        "fechaRegistro": "2026-07-01T21:00:00Z"
                    }
                }
            """.trimIndent())
        )

        val response = apiService.register(
            RegisterRequest("newuser", "n@t.com", "Pass123!", "New User")
        )

        assertTrue(response.isSuccessful)
        assertEquals("jwt_register", response.body()!!.token)

        val request = server.takeRequest()
        assertEquals("/api/Auth/register", request.path)
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"username\":\"newuser\""))
        assertTrue(body.contains("\"email\":\"n@t.com\""))
        assertTrue(body.contains("\"password\":\"Pass123!\""))
        assertTrue(body.contains("\"nombreCompleto\":\"New User\""))
    }
}
