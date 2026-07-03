package com.sakura.aura.integration

import com.sakura.aura.data.model.request.LoginRequest
import com.sakura.aura.data.remote.ApiService
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenRefreshIntegrationTest {

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
    fun `login then token refresh returns new tokens`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""
                {
                    "token": "first_jwt",
                    "refreshToken": "first_rt",
                    "expiration": "2026-07-01T22:00:00Z",
                    "user": {
                        "id": 1, "username": "testuser", "email": "t@t.com",
                        "nombreCompleto": "Test User", "nombreEspiritual": null,
                        "signoZodiacal": null, "fotoUrl": null, "role": "Cliente",
                        "fechaRegistro": "2026-07-01T21:00:00Z"
                    }
                }
            """.trimIndent())
        )

        val loginResponse = apiService.login(LoginRequest("testuser", "Pass123!"))
        val firstJwt = loginResponse.body()!!.token
        val firstRt = loginResponse.body()!!.refreshToken

        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""
                {
                    "token": "refreshed_jwt",
                    "refreshToken": "new_rt",
                    "expiration": "2026-07-02T05:00:00Z",
                    "user": {
                        "id": 1, "username": "testuser", "email": "t@t.com",
                        "nombreCompleto": "Test User", "nombreEspiritual": null,
                        "signoZodiacal": null, "fotoUrl": null, "role": "Cliente",
                        "fechaRegistro": "2026-07-01T21:00:00Z"
                    }
                }
            """.trimIndent())
        )

        val refreshResponse = apiService.refreshToken(
            com.sakura.aura.data.model.request.RefreshTokenRequest(firstJwt, firstRt)
        )
        assertTrue(refreshResponse.isSuccessful)
        assertEquals("refreshed_jwt", refreshResponse.body()!!.token)
        assertEquals("new_rt", refreshResponse.body()!!.refreshToken)
        assertNotEquals(firstJwt, refreshResponse.body()!!.token)

        val loginRequest = server.takeRequest()
        assertEquals("/api/Auth/login", loginRequest.path)

        val refreshRequest = server.takeRequest()
        assertEquals("/api/Auth/refresh-token", refreshRequest.path)
    }
}
