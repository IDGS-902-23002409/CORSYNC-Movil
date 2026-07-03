package com.sakura.aura.integration

import com.sakura.aura.data.model.request.*
import com.sakura.aura.data.remote.ApiService
import com.sakura.aura.data.remote.RetrofitClient
import com.sakura.aura.data.remote.TokenManager
import io.mockk.*
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthFlowIntegrationTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    private lateinit var server: MockWebServer
    private lateinit var apiService: ApiService
    private val tokenManager: TokenManager = mockk()

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
    fun `register then login then logout full flow`() = runBlocking {
        // Simular registro exitoso
        server.enqueue(
            MockResponse().setResponseCode(201).setBody("""
                {
                    "token": "reg_jwt",
                    "refreshToken": "reg_rt",
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

        every { tokenManager.saveTokens(any(), any()) } just Runs

        val registerResponse = apiService.register(
            RegisterRequest("testuser", "t@t.com", "Pass123!", "Test User")
        )
        assertTrue(registerResponse.isSuccessful)
        assertEquals("reg_jwt", registerResponse.body()!!.token)

        // Simular login exitoso
        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""
                {
                    "token": "login_jwt",
                    "refreshToken": "login_rt",
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

        val loginResponse = apiService.login(LoginRequest("testuser", "Pass123!"))
        assertTrue(loginResponse.isSuccessful)
        assertEquals("login_jwt", loginResponse.body()!!.token)

        // Verificar que se hicieron 2 requests
        val request1 = server.takeRequest()
        assertEquals("/api/Auth/register", request1.path)
        assertEquals("POST", request1.method)

        val request2 = server.takeRequest()
        assertEquals("/api/Auth/login", request2.path)
        assertEquals("POST", request2.method)
    }

    @Test
    fun `logout sends correct request`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""{"message":"Sesión cerrada exitosamente."}""")
        )

        val logoutResponse = apiService.logout(LogoutRequest("jwt", "rt"))

        assertTrue(logoutResponse.isSuccessful)

        val request = server.takeRequest()
        assertEquals("/api/Auth/logout", request.path)
        val bodyStr = request.body.readUtf8()
        assertTrue(bodyStr.contains("\"token\":\"jwt\""))
        assertTrue(bodyStr.contains("\"refreshToken\":\"rt\""))
    }

    @Test
    fun `refresh token endpoint`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""
                {
                    "token": "new_jwt",
                    "refreshToken": "new_rt",
                    "expiration": "2026-07-03T05:00:00Z",
                    "user": {
                        "id": 1, "username": "testuser", "email": "t@t.com",
                        "nombreCompleto": "Test User", "nombreEspiritual": null,
                        "signoZodiacal": null, "fotoUrl": null, "role": "Cliente",
                        "fechaRegistro": "2026-07-01T21:00:00Z"
                    }
                }
            """.trimIndent())
        )

        val response = apiService.refreshToken(RefreshTokenRequest("expired", "rt"))
        assertTrue(response.isSuccessful)
        assertEquals("new_jwt", response.body()!!.token)
    }
}
