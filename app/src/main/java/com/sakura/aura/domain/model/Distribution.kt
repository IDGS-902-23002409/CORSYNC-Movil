package com.sakura.aura.domain.model

data class Distribution(
    val auraDistribution: Map<String, Int>,
    val stressDistribution: Map<String, Int>,
    val bpmDistribution: Map<String, Int>
)
