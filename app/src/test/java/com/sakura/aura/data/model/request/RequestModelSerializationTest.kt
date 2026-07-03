package com.sakura.aura.data.model.request

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class RequestModelSerializationTest {

    private val gson = Gson()

    @Test
    fun loginRequest_serializesCorrectly() {
        val request = LoginRequest("testuser", "pass123")
        val json = gson.toJson(request)

        assertTrue(json.contains("\"username\":\"testuser\""))
        assertTrue(json.contains("\"password\":\"pass123\""))
    }

    @Test
    fun registerRequest_serializesCorrectly() {
        val request = RegisterRequest("newuser", "new@test.com", "Pass123!", "New User")
        val json = gson.toJson(request)

        assertTrue(json.contains("\"username\":\"newuser\""))
        assertTrue(json.contains("\"email\":\"new@test.com\""))
        assertTrue(json.contains("\"password\":\"Pass123!\""))
        assertTrue(json.contains("\"nombreCompleto\":\"New User\""))
    }

    @Test
    fun logoutRequest_serializesCorrectly() {
        val request = LogoutRequest("token123", "refresh456")
        val json = gson.toJson(request)

        assertTrue(json.contains("\"token\":\"token123\""))
        assertTrue(json.contains("\"refreshToken\":\"refresh456\""))
    }

    @Test
    fun refreshTokenRequest_serializesCorrectly() {
        val request = RefreshTokenRequest("expiredJwt", "refreshTokenValue")
        val json = gson.toJson(request)

        assertTrue(json.contains("\"token\":\"expiredJwt\""))
        assertTrue(json.contains("\"refreshToken\":\"refreshTokenValue\""))
    }

    @Test
    fun createReadingRequest_serializesCorrectly() {
        val request = CreateReadingRequest(
            dispositivoId = "ESP32_001",
            bpmPromedio = 72.5,
            bpmMaximo = 85.0,
            bpmMinimo = 60.0,
            gsrRawPromedio = 1200,
            gsrVoltajePromedio = 1.05,
            nivelEstres = 25.0,
            auraDominante = "Verde",
            notas = "Buena sesi\u00f3n",
            duracionSegundos = 300,
            fechaInicio = "2026-07-01T20:25:00Z",
            fechaFin = "2026-07-01T20:30:00Z"
        )
        val json = gson.toJson(request)

        assertTrue(json.contains("\"dispositivoId\":\"ESP32_001\""))
        assertTrue(json.contains("\"bpmPromedio\":72.5"))
        assertTrue(json.contains("\"auraDominante\":\"Verde\""))
        assertTrue(json.contains("\"notas\":\"Buena sesi\u00f3n\""))
        assertTrue(json.contains("\"duracionSegundos\":300"))
    }

    @Test
    fun createReadingRequest_withoutNotas_omitsField() {
        val request = CreateReadingRequest(
            dispositivoId = "ESP32_001",
            bpmPromedio = 72.0,
            bpmMaximo = 80.0,
            bpmMinimo = 65.0,
            gsrRawPromedio = 1000,
            gsrVoltajePromedio = 0.9,
            nivelEstres = 20.0,
            auraDominante = "Azul",
            notas = null,
            duracionSegundos = 180,
            fechaInicio = "2026-07-01T20:25:00Z",
            fechaFin = "2026-07-01T20:28:00Z"
        )
        val json = gson.toJson(request)

        assertFalse(json.contains("\"notas\""))
    }

    @Test
    fun updateProfileRequest_serializesCorrectly() {
        val request = UpdateProfileRequest(
            nombreCompleto = "Carlos Zen",
            nombreEspiritual = "Luz de Luna",
            signoZodiacal = "Piscis",
            fotoUrl = "https://images.corsync.com/avatars/user3.png"
        )
        val json = gson.toJson(request)

        assertTrue(json.contains("\"nombreCompleto\":\"Carlos Zen\""))
        assertTrue(json.contains("\"nombreEspiritual\":\"Luz de Luna\""))
        assertTrue(json.contains("\"signoZodiacal\":\"Piscis\""))
        assertTrue(json.contains("\"fotoUrl\":\"https://images.corsync.com/avatars/user3.png\""))
    }

    @Test
    fun updateProfileRequest_withNullFields_omitsThem() {
        val request = UpdateProfileRequest(
            nombreCompleto = "Carlos Zen",
            nombreEspiritual = null,
            signoZodiacal = null,
            fotoUrl = null
        )
        val json = gson.toJson(request)

        assertTrue(json.contains("\"nombreCompleto\":\"Carlos Zen\""))
    }
}
