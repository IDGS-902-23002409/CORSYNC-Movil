package com.sakura.aura.data.remote

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.SocketException

@RunWith(RobolectricTestRunner::class)
class AuthRecoveryInterceptorTest {

    private val interceptor = AuthRecoveryInterceptor()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    @Test
    fun `normal request passes through`() {
        val body = """{"username":"u","password":"p"}""".toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("https://api.corsync.com/api/Auth/login")
            .post(body)
            .build()

        val mockResp = mockResponse(200, """{"token":"x"}""", request)
        val chain = FakeChain(request, mockResp)

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
    }

    @Test
    fun `socketException on register triggers login fallback`() {
        val body = """{"username":"testuser","password":"Pass123!"}""".toRequestBody(jsonMediaType)
        val registerRequest = Request.Builder()
            .url("https://api.corsync.com/api/Auth/register")
            .post(body)
            .build()

        var capturedRequest: Request? = null

        val chain = FakeChain(registerRequest, SocketException("Connection reset")) { request ->
            capturedRequest = request
            mockResponse(200, """{"token":"fallback_token","refreshToken":"rt","expiration":"2026-07-02T05:00:00Z","user":{"id":1,"username":"testuser","email":"","nombreCompleto":"Test","nombreEspiritual":"","signoZodiacal":"","fotoUrl":null,"role":"Cliente","fechaRegistro":"2026-07-01T21:00:00Z"}}""", request)
        }

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        assertNotNull(capturedRequest)
        assertEquals("/api/Auth/login", capturedRequest?.url?.encodedPath)
        assertEquals("POST", capturedRequest?.method)
    }

    @Test
    fun `socketException on non-register endpoint rethrows`() {
        val body = """{"username":"u","password":"p"}""".toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("https://api.corsync.com/api/Auth/login")
            .post(body)
            .build()

        val chain = FakeChain(request, SocketException("Connection reset"))

        assertThrows(SocketException::class.java) {
            interceptor.intercept(chain)
        }
    }

    @Test
    fun `socketException on GET non-register rethrows`() {
        val request = Request.Builder()
            .url("https://api.corsync.com/api/User/profile")
            .get()
            .build()

        val chain = FakeChain(request, SocketException("Broken pipe"))

        assertThrows(SocketException::class.java) {
            interceptor.intercept(chain)
        }
    }

    @Test
    fun `fallback request body contains only username and password`() {
        val body = """{"username":"testuser","email":"test@test.com","password":"Pass123!","nombreCompleto":"Test User"}""".toRequestBody(jsonMediaType)
        val registerRequest = Request.Builder()
            .url("https://api.corsync.com/api/Auth/register")
            .post(body)
            .build()

        var capturedBodyStr: String? = null

        val chain = FakeChain(registerRequest, SocketException("reset")) { request ->
            val buffer = okio.Buffer()
            request.body?.writeTo(buffer)
            capturedBodyStr = buffer.readUtf8()
            mockResponse(200, """{"token":"x"}""", request)
        }

        interceptor.intercept(chain)

        assertNotNull(capturedBodyStr)
        assertTrue(capturedBodyStr!!.contains("\"username\":\"testuser\""))
        assertTrue(capturedBodyStr!!.contains("\"password\":\"Pass123!\""))
        assertFalse(capturedBodyStr!!.contains("\"email\""))
        assertFalse(capturedBodyStr!!.contains("\"nombreCompleto\""))
    }

    private fun mockResponse(code: Int, body: String, request: Request? = null): Response {
        val req = request ?: Request.Builder().url("https://api.corsync.com/").build()
        return Response.Builder()
            .request(req)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("")
            .body(body.toResponseBody(jsonMediaType))
            .build()
    }

    private class FakeChain(
        private val request: Request,
        private val exceptionOrResponse: Any,
        private val onRetry: ((Request) -> Response)? = null
    ) : Interceptor.Chain {
        private var callCount = 0

        override fun request(): Request = request

        override fun proceed(request: Request): Response {
            callCount++
            if (onRetry != null && callCount > 1) {
                return onRetry(request)
            }
            return when (exceptionOrResponse) {
                is Response -> exceptionOrResponse
                is Exception -> throw exceptionOrResponse
                else -> throw RuntimeException("Unexpected")
            }
        }

        override fun connection() = null
        override fun call() = throw UnsupportedOperationException()
        override fun connectTimeoutMillis() = 10_000
        override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        override fun readTimeoutMillis() = 10_000
        override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        override fun writeTimeoutMillis() = 10_000
        override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
    }
}
