package com.sakura.aura.domain.util

import androidx.compose.ui.graphics.Color

object AuraMapper {

    fun auraColorFromString(color: String): Color = when (color.lowercase()) {
        "rojo", "roja" -> Color(0xFFE74C3C)
        "naranja" -> Color(0xFFE67E22)
        "amarillo", "amarilla" -> Color(0xFFF1C40F)
        "verde" -> Color(0xFF2ECC71)
        "azul" -> Color(0xFF5DADE2)
        "morado", "violeta", "morada" -> Color(0xFF9B59B6)
        "rosa" -> Color(0xFFE91E8C)
        else -> Color(0xFFCCCCCC)
    }

    fun stressColor(level: Double): Color = when {
        level < 30 -> Color(0xFF2ECC71)
        level < 60 -> Color(0xFFF39C12)
        else -> Color(0xFFE74C3C)
    }

    fun stressLabel(level: Double): String = when {
        level < 30 -> "Bajo"
        level < 60 -> "Medio"
        else -> "Alto"
    }
}
