package com.sakura.aura.data.model.response

import com.google.gson.annotations.SerializedName

data class RecommendationItemResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("tipo")
    val tipo: String,
    @SerializedName("titulo")
    val titulo: String,
    @SerializedName("descripcion")
    val descripcion: String,
    @SerializedName("prioridad")
    val prioridad: String,
    @SerializedName("icono")
    val icono: String,
    @SerializedName("duracionMinutos")
    val duracionMinutos: Int,
    @SerializedName("categoria")
    val categoria: String
)

data class SuggestedChallengeResponse(
    @SerializedName("challengeId")
    val challengeId: Int,
    @SerializedName("titulo")
    val titulo: String,
    @SerializedName("razon")
    val razon: String,
    @SerializedName("prioridadMatch")
    val prioridadMatch: Double
)

data class RecommendationsPackageResponse(
    @SerializedName("nivelEstresActual")
    val nivelEstresActual: String,
    @SerializedName("scoreEstres")
    val scoreEstres: Double,
    @SerializedName("categoriasBienestar")
    val categoriasBienestar: Map<String, String>,
    @SerializedName("recomendaciones")
    val recomendaciones: List<RecommendationItemResponse>,
    @SerializedName("desafiosSugeridos")
    val desafiosSugeridos: List<SuggestedChallengeResponse>,
    @SerializedName("mensajeMotivacional")
    val mensajeMotivacional: String
)
