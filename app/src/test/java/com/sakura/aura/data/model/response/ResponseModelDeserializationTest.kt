package com.sakura.aura.data.model.response

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class ResponseModelDeserializationTest {

    private val gson = Gson()

    @Test
    fun authResponse_deserializesCorrectly() {
        val json = """
        {
            "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9",
            "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4=",
            "expiration": "2026-07-02T05:00:00Z",
            "user": {
                "id": 3,
                "username": "espiritu_libre",
                "email": "zen@corsync.com",
                "nombreCompleto": "Carlos Zen",
                "nombreEspiritual": "",
                "signoZodiacal": "",
                "fotoUrl": null,
                "role": "Cliente",
                "fechaRegistro": "2026-07-01T21:00:00Z"
            }
        }
        """.trimIndent()

        val response = gson.fromJson(json, AuthResponse::class.java)

        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9", response.token)
        assertEquals("dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4=", response.refreshToken)
        assertEquals("2026-07-02T05:00:00Z", response.expiration)
        assertEquals(3, response.user.id)
        assertEquals("espiritu_libre", response.user.username)
        assertEquals("Carlos Zen", response.user.nombreCompleto)
        assertEquals("Cliente", response.user.role)
    }

    @Test
    fun userResponse_deserializesCorrectly() {
        val json = """
        {
            "id": 1,
            "username": "espiritu_libre",
            "email": "zen@corsync.com",
            "nombreCompleto": "Carlos Zen",
            "nombreEspiritual": "Luz de Luna",
            "signoZodiacal": "Piscis",
            "fotoUrl": "https://images.corsync.com/avatars/user3.png",
            "role": "Cliente",
            "fechaRegistro": "2026-07-01T21:00:00Z"
        }
        """.trimIndent()

        val response = gson.fromJson(json, UserResponse::class.java)

        assertEquals(1, response.id)
        assertEquals("espiritu_libre", response.username)
        assertEquals("Luz de Luna", response.nombreEspiritual)
        assertEquals("Piscis", response.signoZodiacal)
        assertNotNull(response.fotoUrl)
    }

    @Test
    fun userStatsResponse_deserializesCorrectly() {
        val json = """
        {
            "bpmPromedio": 74.2,
            "nivelEstresPromedio": 32.5,
            "sesionesTotales": 12,
            "auraDominante": "Verde",
            "rachaActualDias": 5,
            "ultimaSesion": "2026-07-01T20:30:00Z"
        }
        """.trimIndent()

        val response = gson.fromJson(json, UserStatsResponse::class.java)

        assertEquals(74.2, response.bpmPromedio, 0.01)
        assertEquals(32.5, response.nivelEstresPromedio, 0.01)
        assertEquals(12, response.sesionesTotales)
        assertEquals("Verde", response.auraDominante)
        assertEquals(5, response.rachaActualDias)
        assertEquals("2026-07-01T20:30:00Z", response.ultimaSesion)
    }

    @Test
    fun readingResponse_deserializesCorrectly() {
        val json = """
        {
            "id": 1,
            "dispositivoId": "ESP32_MAX30102_01",
            "bpmPromedio": 68.4,
            "bpmMaximo": 85.0,
            "bpmMinimo": 60.0,
            "gsrRawPromedio": 1240,
            "gsrVoltajePromedio": 0.998,
            "nivelEstres": 22.5,
            "auraDominante": "Verde",
            "notas": "Excelente sesi\u00f3n",
            "duracionSegundos": 300,
            "fechaInicio": "2026-07-01T20:25:00Z",
            "fechaFin": "2026-07-01T20:30:00Z"
        }
        """.trimIndent()

        val response = gson.fromJson(json, ReadingResponse::class.java)

        assertEquals(1, response.id)
        assertEquals("ESP32_MAX30102_01", response.dispositivoId)
        assertEquals(68.4, response.bpmPromedio, 0.01)
        assertEquals(85.0, response.bpmMaximo, 0.01)
        assertEquals(60.0, response.bpmMinimo, 0.01)
        assertEquals(1240, response.gsrRawPromedio)
        assertEquals(0.998, response.gsrVoltajePromedio, 0.001)
        assertEquals(22.5, response.nivelEstres, 0.01)
        assertEquals("Verde", response.auraDominante)
        assertEquals("Excelente sesi\u00f3n", response.notas)
    }

    @Test
    fun readingSummaryResponse_deserializesCorrectly() {
        val json = """
        {
            "bpmPromedioGlobal": 72.8,
            "nivelEstresPromedio": 28.4,
            "totalSesiones": 12,
            "auraMasFrecuente": "Verde",
            "distribucionAuras": {
                "Verde": 7,
                "Azul": 3,
                "Amarilla": 2
            }
        }
        """.trimIndent()

        val response = gson.fromJson(json, ReadingSummaryResponse::class.java)

        assertEquals(72.8, response.bpmPromedioGlobal, 0.01)
        assertEquals(28.4, response.nivelEstresPromedio, 0.01)
        assertEquals(12, response.totalSesiones)
        assertEquals("Verde", response.auraMasFrecuente)
        assertEquals(3, response.distribucionAuras?.size)
        assertEquals(7, response.distribucionAuras?.get("Verde"))
    }

    @Test
    fun challengeResponse_deserializesCorrectly() {
        val json = """
        {
            "id": 1,
            "titulo": "Primera Lectura",
            "descripcion": "Realiza tu primer escaneo de aura",
            "icono": "\uD83C\uDF1F",
            "tipo": "Sesiones",
            "metaObjetivo": 1,
            "unidadMedida": "sesiones",
            "puntos": 10,
            "progresoActual": 1,
            "completado": true,
            "porcentajeProgreso": 100.0,
            "fechaCompletado": "2026-06-30T10:15:00Z"
        }
        """.trimIndent()

        val response = gson.fromJson(json, ChallengeResponse::class.java)

        assertEquals(1, response.id)
        assertEquals("Primera Lectura", response.titulo)
        assertEquals(1, response.metaObjetivo)
        assertEquals(10, response.puntos)
        assertTrue(response.completado)
        assertEquals(100.0, response.porcentajeProgreso, 0.01)
        assertNotNull(response.fechaCompletado)
    }

    @Test
    fun medalResponse_deserializesCorrectly() {
        val json = """
        {
            "id": 1,
            "nombre": "Primer Escaneo",
            "descripcion": "Completaste tu primera lectura de aura",
            "icono": "\uD83C\uDFC5",
            "fechaObtenida": "2026-06-30T10:15:00Z"
        }
        """.trimIndent()

        val response = gson.fromJson(json, MedalResponse::class.java)

        assertEquals(1, response.id)
        assertEquals("Primer Escaneo", response.nombre)
        assertNotNull(response.fechaObtenida)
    }

    @Test
    fun telemetryResponse_deserializesCorrectly() {
        val json = """
        {
            "id": 0,
            "dispositivoId": "ESP32_MAX30102",
            "ir": 87432,
            "bpm": 72.5,
            "bpmPromedio": 71,
            "gsrRaw": 1340,
            "gsrVoltaje": 1.079,
            "aura": "Rojo",
            "fechaHora": "2026-06-29T22:51:35Z"
        }
        """.trimIndent()

        val response = gson.fromJson(json, TelemetryResponse::class.java)

        assertEquals(0, response.id)
        assertEquals("ESP32_MAX30102", response.dispositivoId)
        assertEquals(87432, response.ir)
        assertEquals(72.5, response.bpm, 0.01)
        assertEquals(71, response.bpmPromedio)
        assertEquals(1340, response.gsrRaw)
        assertEquals(1.079, response.gsrVoltaje, 0.001)
        assertEquals("Rojo", response.aura)
        assertEquals("2026-06-29T22:51:35Z", response.fechaHora)
    }

    @Test
    fun readingResponse_withNullNotas_deserializesCorrectly() {
        val json = """
        {
            "id": 2,
            "dispositivoId": "ESP32_002",
            "bpmPromedio": 70.0,
            "bpmMaximo": 80.0,
            "bpmMinimo": 65.0,
            "gsrRawPromedio": 1100,
            "gsrVoltajePromedio": 0.95,
            "nivelEstres": 18.0,
            "auraDominante": "Azul",
            "notas": null,
            "duracionSegundos": 180,
            "fechaInicio": "2026-07-01T21:00:00Z",
            "fechaFin": "2026-07-01T21:03:00Z"
        }
        """.trimIndent()

        val response = gson.fromJson(json, ReadingResponse::class.java)

        assertEquals(2, response.id)
        assertNull(response.notas)
    }

    @Test
    fun userResponse_withNullOptionalFields() {
        val json = """
        {
            "id": 2,
            "username": "user",
            "email": "u@test.com",
            "nombreCompleto": "User",
            "nombreEspiritual": null,
            "signoZodiacal": null,
            "fotoUrl": null,
            "role": "Cliente",
            "fechaRegistro": "2026-07-01T21:00:00Z"
        }
        """.trimIndent()

        val response = gson.fromJson(json, UserResponse::class.java)

        assertNull(response.nombreEspiritual)
        assertNull(response.signoZodiacal)
        assertNull(response.fotoUrl)
    }
}
