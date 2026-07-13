package com.sakura.aura.data.mapper

import com.sakura.aura.data.model.response.ComparisonResponse
import com.sakura.aura.data.model.response.DistributionResponse
import com.sakura.aura.data.model.response.TrendsResponse
import com.sakura.aura.data.model.response.WeekSummaryResponse
import com.sakura.aura.data.model.response.TrendDataPointResponse
import com.sakura.aura.domain.model.Distribution
import com.sakura.aura.domain.model.TrendDataPoint
import com.sakura.aura.domain.model.Trends
import com.sakura.aura.domain.model.WeekComparison
import com.sakura.aura.domain.model.WeekSummary

fun TrendDataPointResponse.toDomain(): TrendDataPoint = TrendDataPoint(
    date = fecha,
    avgBpm = bpmPromedio,
    maxBpm = bpmMaximo,
    minBpm = bpmMinimo,
    avgStress = estresPromedio,
    avgGsr = gsrPromedio,
    sessions = sesiones,
    avgDurationSeconds = duracionPromedioSeg
)

fun TrendsResponse.toDomain(): Trends = Trends(
    period = period,
    dataPoints = dataPoints.map { it.toDomain() }
)

fun DistributionResponse.toDomain(): Distribution = Distribution(
    auraDistribution = distribucionAuras,
    stressDistribution = distribucionEstres,
    bpmDistribution = distribucionBpm
)

fun WeekSummaryResponse.toDomain(): WeekSummary = WeekSummary(
    avgBpm = bpmPromedio,
    avgStress = estresPromedio,
    sessions = sesiones
)

fun ComparisonResponse.toDomain(): WeekComparison = WeekComparison(
    currentWeek = semanaActual.toDomain(),
    previousWeek = semanaAnterior.toDomain(),
    bpmChangePct = bpmCambioPct,
    stressChangePct = estresCambioPct,
    sessionsChangePct = sesionesCambioPct,
    trend = tendencia
)
