package com.sakura.aura.data.model.response

import com.google.gson.annotations.SerializedName

data class TrendDataPointResponse(
    @SerializedName("fecha")
    val fecha: String,
    @SerializedName("bpmPromedio")
    val bpmPromedio: Double,
    @SerializedName("bpmMaximo")
    val bpmMaximo: Double,
    @SerializedName("bpmMinimo")
    val bpmMinimo: Double,
    @SerializedName("estresPromedio")
    val estresPromedio: Double,
    @SerializedName("gsrPromedio")
    val gsrPromedio: Double,
    @SerializedName("sesiones")
    val sesiones: Int,
    @SerializedName("duracionPromedioSeg")
    val duracionPromedioSeg: Int
)

data class TrendsResponse(
    @SerializedName("period")
    val period: String,
    @SerializedName("dataPoints")
    val dataPoints: List<TrendDataPointResponse>
)
