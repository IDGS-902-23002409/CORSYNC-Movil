package com.sakura.aura.data.model.response

import com.google.gson.annotations.SerializedName

data class DistributionResponse(
    @SerializedName("distribucionAuras")
    val distribucionAuras: Map<String, Int> = emptyMap(),
    @SerializedName("distribucionEstres")
    val distribucionEstres: Map<String, Int> = emptyMap(),
    @SerializedName("distribucionBpm")
    val distribucionBpm: Map<String, Int> = emptyMap()
)
