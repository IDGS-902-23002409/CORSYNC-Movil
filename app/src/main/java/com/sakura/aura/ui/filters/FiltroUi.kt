package com.sakura.aura.ui.filters

/**
 * Los filtros de cámara que trae el proyecto de Unity.
 *
 * [sufijo] es el nombre de la escena **sin** el prefijo `Filtro`, que es lo que
 * viaja por `UnitySendMessage`: el receptor de Unity lo reconstruye igual que
 * hace con las auras ("Fuego" → `FiltroFuego`). Los valores salen del
 * `BuildSettings` del export, no de una lista inventada — si Unity renombra una
 * escena, hay que tocar esta tabla o el filtro deja de cargar en silencio.
 *
 * [puntosRequeridos] es el umbral de puntos acumulados en desafíos completados
 * que desbloquea el filtro (ver [FiltroStore]). Mariposas está en 0 a propósito:
 * el módulo de la galería tiene que ofrecer algo usable desde el primer día.
 */
enum class FiltroUi(
    val sufijo: String,
    val hex: Long,
    val label: String,
    val descripcion: String,
    val puntosRequeridos: Int
) {
    MARIPOSAS(
        sufijo = "Mariposas",
        hex = 0xFFE91E8C,
        label = "Mariposas",
        descripcion = "Un revoloteo rosa alrededor tuyo",
        puntosRequeridos = 0
    ),
    LUCIERNAGAS(
        sufijo = "Luciernagas",
        hex = 0xFFF1C40F,
        label = "Luciérnagas",
        descripcion = "Luces cálidas flotando en la noche",
        puntosRequeridos = 150
    ),
    LLUVIA(
        sufijo = "Lluvia",
        hex = 0xFF5DADE2,
        label = "Lluvia",
        descripcion = "Calma de tormenta suave",
        puntosRequeridos = 300
    ),
    FUEGO(
        sufijo = "Fuego",
        hex = 0xFFE67E22,
        label = "Fuego",
        descripcion = "Llamas envolviendo tu aura",
        puntosRequeridos = 500
    ),
    DRAGON_BALL(
        sufijo = "DragonBall",
        hex = 0xFF2ECC71,
        label = "Aura Saiyan",
        descripcion = "El clásico. Solo para constantes",
        puntosRequeridos = 800
    );

    /** Nombre completo de la escena en Unity. Solo para diagnóstico y logs. */
    val escena: String get() = "Filtro$sufijo"

    companion object {
        fun fromSufijo(value: String?): FiltroUi? =
            entries.firstOrNull { it.sufijo.equals(value, ignoreCase = true) }
    }
}
