package com.sakura.aura.domain.util

import androidx.compose.ui.graphics.Color
import com.sakura.aura.domain.model.Aura

object AuraMapper {

    /**
     * La normalización del string vive en [Aura.fromBackend] para que Home,
     * Historial y Analytics no puedan divergir en qué cuenta como "Rojo".
     */
    fun auraColorFromString(color: String): Color = when (Aura.fromBackend(color)) {
        Aura.ROJO        -> Color(0xFFE74C3C)
        Aura.NARANJA     -> Color(0xFFE67E22)
        Aura.AMARILLO    -> Color(0xFFF1C40F)
        Aura.VERDE       -> Color(0xFF2ECC71)
        Aura.AZUL        -> Color(0xFF5DADE2)
        Aura.MORADO      -> Color(0xFF9B59B6)
        Aura.ROSA        -> Color(0xFFE91E8C)
        Aura.DESCONOCIDA -> Color(0xFFCCCCCC)
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
