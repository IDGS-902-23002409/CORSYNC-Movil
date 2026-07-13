package com.sakura.aura.data.model.response

import com.google.gson.annotations.SerializedName

data class WeekSummaryResponse(
    @SerializedName("bpmPromedio")
    val bpmPromedio: Double,
    @SerializedName("estresPromedio")
    val estresPromedio: Double,
    @SerializedName("sesiones")
    val sesiones: Int
)

data class ComparisonResponse(
    @SerializedName("semanaActual")
    val semanaActual: WeekSummaryResponse,
    @SerializedName("semanaAnterior")
    val semanaAnterior: WeekSummaryResponse,
    @SerializedName("bpmCambioPct")
    val bpmCambioPct: Double,
    @SerializedName("estresCambioPct")
    val estresCambioPct: Double,
    @SerializedName("sesionesCambioPct")
    val sesionesCambioPct: Double,
    @SerializedName("tendencia")
    val tendencia: String
)
