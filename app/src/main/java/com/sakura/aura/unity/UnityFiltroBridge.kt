package com.sakura.aura.unity

import com.sakura.aura.ui.filters.FiltroUi
import com.unity3d.player.UnityPlayer

/**
 * Puente Android → Unity para los filtros de cámara.
 *
 * Mismo patrón que [UnityAuraBridge]: el proyecto de Unity resuelve el filtro
 * **por escena** (`FiltroFuego`, `FiltroMariposas`, …) y un `MonoBehaviour`
 * receptor las carga. Aquí solo se traduce el enum de la app al sufijo que
 * espera ese receptor.
 *
 * ⚠️ **Requiere `MostrarFiltro` en `AuraReceiver.cs`** (ver
 * `ParaEquipoUnity/INSTRUCCIONES_FILTROS.md`). El export de filtros que se
 * recibió reutiliza tal cual el script del aura, cuyo único método —
 * `MostrarAura`— antepone la cadena `"Aura"` al valor recibido. Con ese script,
 * mandar "Fuego" pide la escena `AuraFuego`, que no existe: el receptor loguea
 * el error y la vista se queda en la escena de arranque. No es algo que se pueda
 * sortear desde Android — hace falta el método nuevo y un export único.
 */
object UnityFiltroBridge {

    /** GameObject y método del receptor. Deben coincidir con `AuraReceiver.cs`. */
    private const val RECEIVER_OBJECT = "AuraReceiver"
    private const val RECEIVER_METHOD = "MostrarFiltro"

    /**
     * Sufijo que se manda al receptor; él le antepone "Filtro" para formar la
     * escena ("Fuego" → `FiltroFuego`).
     */
    fun sceneSuffixFor(filtro: FiltroUi): String = filtro.sufijo

    /**
     * Envía el filtro al receptor de Unity.
     *
     * `UnitySendMessage` no confirma entrega ni falla de forma visible: si el
     * runtime todavía no levantó, el mensaje se pierde en silencio. Por eso
     * [UnityCaptureActivity] llama a esto varias veces mientras Unity arranca, y
     * por eso el receptor es idempotente.
     */
    fun send(filtro: FiltroUi) {
        UnityPlayer.UnitySendMessage(RECEIVER_OBJECT, RECEIVER_METHOD, sceneSuffixFor(filtro))
    }
}
