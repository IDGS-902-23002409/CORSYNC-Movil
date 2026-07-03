package com.sakura.aura.domain.model

data class UserProfile(
    val id: Int,
    val username: String,
    val email: String,
    val fullName: String,
    val spiritualName: String?,
    val zodiacSign: String?,
    val photoUrl: String?,
    val role: String,
    val registrationDate: String
)
