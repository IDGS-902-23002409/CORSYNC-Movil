package com.sakura.aura.domain.model

data class Medal(
    val id: Int,
    val name: String,
    val description: String?,
    val icon: String?,
    val obtainedDate: String?
)
