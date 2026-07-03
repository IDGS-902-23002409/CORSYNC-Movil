package com.sakura.aura.data.mapper

import com.sakura.aura.data.model.request.CreateReadingRequest
import com.sakura.aura.data.model.response.ReadingResponse
import com.sakura.aura.data.model.response.ReadingSummaryResponse
import com.sakura.aura.domain.model.NewReadingData
import com.sakura.aura.domain.model.Reading
import com.sakura.aura.domain.model.ReadingSummary

fun ReadingResponse.toDomain(): Reading = Reading(
    id = id,
    deviceId = dispositivoId,
    avgBpm = bpmPromedio,
    maxBpm = bpmMaximo,
    minBpm = bpmMinimo,
    avgGsrRaw = gsrRawPromedio,
    avgGsrVoltage = gsrVoltajePromedio,
    stressLevel = nivelEstres,
    dominantAura = auraDominante,
    notes = notas,
    durationSeconds = duracionSegundos,
    startDate = fechaInicio,
    endDate = fechaFin
)

fun ReadingSummaryResponse.toDomain(): ReadingSummary = ReadingSummary(
    globalAvgBpm = bpmPromedioGlobal,
    globalAvgStress = nivelEstresPromedio,
    totalSessions = totalSesiones,
    mostFrequentAura = auraMasFrecuente,
    auraDistribution = distribucionAuras
)

fun com.sakura.aura.data.model.response.TelemetryResponse.toDomain(): com.sakura.aura.domain.model.Telemetry = com.sakura.aura.domain.model.Telemetry(
    id = id,
    deviceId = dispositivoId,
    ir = ir,
    bpm = bpm,
    avgBpm = bpmPromedio,
    gsrRaw = gsrRaw,
    gsrVoltage = gsrVoltaje,
    aura = aura,
    timestamp = fechaHora
)

fun NewReadingData.toRequest(): CreateReadingRequest = CreateReadingRequest(
    dispositivoId = deviceId,
    bpmPromedio = avgBpm,
    bpmMaximo = maxBpm,
    bpmMinimo = minBpm,
    gsrRawPromedio = avgGsrRaw,
    gsrVoltajePromedio = avgGsrVoltage,
    nivelEstres = stressLevel,
    auraDominante = dominantAura,
    notas = notes,
    duracionSegundos = durationSeconds,
    fechaInicio = startDate,
    fechaFin = endDate
)
