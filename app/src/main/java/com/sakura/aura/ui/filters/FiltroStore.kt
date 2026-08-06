package com.sakura.aura.ui.filters

import android.content.Context

/**
 * Qué filtros tiene desbloqueados el usuario y cuál eligió.
 *
 * El desbloqueo es **monótono**: una vez ganado, el filtro se guarda y ya no se
 * vuelve a evaluar. Se hace así porque los puntos se calculan desde los desafíos
 * que devuelve el backend, y si esa llamada falla (o el usuario abre la galería
 * sin red) el conteo baja a 0 — sin persistencia, los filtros se "re-bloquearían"
 * solos y parecería que la app le quitó al usuario algo que ya se había ganado.
 *
 * Vive en `SharedPreferences` planas y no en las cifradas de [
 * com.sakura.aura.data.remote.TokenManager]: esto no es un secreto, y el fichero
 * cifrado está inyectado por Hilt en el proceso principal, mientras que la
 * pantalla que lo consume necesita leerlo sin depender del grafo de DI.
 */
object FiltroStore {

    private const val PREFS = "corsync_filtros"
    private const val KEY_DESBLOQUEADOS = "desbloqueados"
    private const val KEY_SELECCIONADO = "seleccionado"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Puntos acumulados = suma de los desafíos ya completados. */
    fun puntosDe(desafiosCompletados: List<Int>): Int = desafiosCompletados.sum()

    fun desbloqueados(context: Context): Set<FiltroUi> {
        val guardados = prefs(context).getStringSet(KEY_DESBLOQUEADOS, emptySet()).orEmpty()
        // Siempre se incluyen los de umbral 0: si se añade un filtro gratis nuevo
        // no hace falta que el usuario vuelva a pasar por `sincronizar`.
        return (guardados.mapNotNull { FiltroUi.fromSufijo(it) } +
                FiltroUi.entries.filter { it.puntosRequeridos == 0 }).toSet()
    }

    /**
     * Contrasta los puntos actuales contra los umbrales y persiste lo ganado.
     *
     * @return los filtros que se desbloquearon **en esta llamada**, para que la
     *         galería pueda anunciarlos. Vacío si no hubo novedad.
     */
    fun sincronizar(context: Context, puntos: Int): Set<FiltroUi> {
        val previos = desbloqueados(context)
        val merecidos = FiltroUi.entries.filter { puntos >= it.puntosRequeridos }.toSet()
        val nuevos = merecidos - previos
        if (nuevos.isNotEmpty()) {
            prefs(context).edit()
                .putStringSet(KEY_DESBLOQUEADOS, (previos + merecidos).map { it.sufijo }.toSet())
                .apply()
        }
        return nuevos
    }

    fun seleccionado(context: Context): FiltroUi? {
        val filtro = FiltroUi.fromSufijo(prefs(context).getString(KEY_SELECCIONADO, null))
        // Un filtro seleccionado que ya no está desbloqueado no debería poder
        // usarse; se ignora en vez de borrarlo por si vuelve a ganarse.
        return filtro?.takeIf { it in desbloqueados(context) }
    }

    fun seleccionar(context: Context, filtro: FiltroUi) {
        prefs(context).edit().putString(KEY_SELECCIONADO, filtro.sufijo).apply()
    }
}
