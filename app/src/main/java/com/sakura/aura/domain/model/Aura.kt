package com.sakura.aura.domain.model

/**
 * Clasificación de aura que calcula el backend.
 *
 * El contrato (integration.md §4) entrega el valor en forma masculina
 * —"Rojo", "Morado", "Amarillo"— mientras que la UI se escribió en femenino.
 * Ese desajuste hacía que "Rojo", "Morado" y "Amarillo" no encontraran
 * correspondencia y cayeran en gris. [fromBackend] acepta ambas formas y es la
 * única puerta de entrada: cualquier mapeo de aura debe pasar por aquí.
 */
enum class Aura {
    ROJO,
    NARANJA,
    AMARILLO,
    VERDE,
    AZUL,
    MORADO,
    ROSA,
    DESCONOCIDA;

    companion object {
        fun fromBackend(value: String?): Aura = when (value?.trim()?.lowercase()) {
            "rojo", "roja"                -> ROJO
            "naranja"                     -> NARANJA
            "amarillo", "amarilla"        -> AMARILLO
            "verde"                       -> VERDE
            "azul"                        -> AZUL
            "morado", "morada", "violeta" -> MORADO
            "rosa"                        -> ROSA
            else                          -> DESCONOCIDA
        }
    }
}
