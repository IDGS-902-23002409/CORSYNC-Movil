package com.sakura.aura.domain.model

data class Challenge(
    val id: Int,
    val title: String,
    val description: String,
    val icon: String?,
    val type: String?,
    val targetGoal: Int,
    val unit: String?,
    val points: Int,
    val currentProgress: Int,
    val completed: Boolean,
    val progressPercentage: Double,
    val completedDate: String?
)
