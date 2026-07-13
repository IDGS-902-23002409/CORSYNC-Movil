package com.sakura.aura.data.remote

import com.sakura.aura.BuildConfig

object AppConfig {
    val apiBaseUrl: String get() = BuildConfig.API_BASE_URL
    val signalrHubUrl: String get() = BuildConfig.SIGNALR_HUB_URL
    const val DEVICE_ID = "ESP32_MAX30102"
}
