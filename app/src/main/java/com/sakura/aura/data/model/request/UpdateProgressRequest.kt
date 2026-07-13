package com.sakura.aura.data.model.request

import com.google.gson.annotations.SerializedName

data class UpdateProgressRequest(
    @SerializedName("progresoActual")
    val progresoActual: Int
)
