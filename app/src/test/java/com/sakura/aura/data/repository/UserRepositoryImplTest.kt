package com.sakura.aura.data.repository

import com.sakura.aura.data.model.request.UpdateProfileRequest
import com.sakura.aura.data.model.response.UserResponse
import com.sakura.aura.data.model.response.UserStatsResponse
import com.sakura.aura.data.remote.ApiService
import io.mockk.*
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import retrofit2.Response

class UserRepositoryImplTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    private val apiService: ApiService = mockk()
    private lateinit var repository: UserRepositoryImpl

    private val mockUser = UserResponse(
        id = 1, username = "testuser", email = "test@test.com",
        nombreCompleto = "Test User", nombreEspiritual = "Luz",
        signoZodiacal = "Piscis", fotoUrl = null, role = "Cliente",
        fechaRegistro = "2026-07-01T21:00:00Z"
    )
    private val mockStats = UserStatsResponse(
        bpmPromedio = 72.5, nivelEstresPromedio = 30.0,
        sesionesTotales = 10, auraDominante = "Verde",
        rachaActualDias = 3, ultimaSesion = "2026-07-01T20:30:00Z"
    )

    @Before
    fun setUp() {
        repository = UserRepositoryImpl(apiService)
    }

    @Test
    fun `getProfile success returns user`() = runBlocking {
        coEvery { apiService.getProfile() } returns Response.success(mockUser)

        val result = repository.getProfile()

        assertTrue(result.isSuccess)
        assertEquals("testuser", result.getOrNull()!!.username)
        assertEquals("Test User", result.getOrNull()!!.nombreCompleto)
    }

    @Test
    fun `getProfile failure returns error`() = runBlocking {
        coEvery { apiService.getProfile() } returns Response.error(500, mockk(relaxed = true) { every { string() } returns "Server error" })

        val result = repository.getProfile()

        assertTrue(result.isFailure)
    }

    @Test
    fun `updateProfile success returns updated user`() = runBlocking {
        coEvery { apiService.updateProfile(any()) } returns Response.success(mockUser)

        val result = repository.updateProfile(UpdateProfileRequest("Test User", "Luz", "Piscis", null))

        assertTrue(result.isSuccess)
        assertEquals("Luz", result.getOrNull()!!.nombreEspiritual)
    }

    @Test
    fun `updateProfile failure returns error`() = runBlocking {
        coEvery { apiService.updateProfile(any()) } returns Response.error(400, mockk(relaxed = true) { every { string() } returns "Bad request" })

        val result = repository.updateProfile(UpdateProfileRequest(null, null, null, null))

        assertTrue(result.isFailure)
    }

    @Test
    fun `getStats success returns stats`() = runBlocking {
        coEvery { apiService.getUserStats() } returns Response.success(mockStats)

        val result = repository.getStats()

        assertTrue(result.isSuccess)
        assertEquals(72.5, result.getOrNull()!!.bpmPromedio, 0.01)
        assertEquals(10, result.getOrNull()!!.sesionesTotales)
    }

    @Test
    fun `getStats failure returns error`() = runBlocking {
        coEvery { apiService.getUserStats() } returns Response.error(500, mockk(relaxed = true) { every { string() } returns "Error" })

        val result = repository.getStats()

        assertTrue(result.isFailure)
    }
}
