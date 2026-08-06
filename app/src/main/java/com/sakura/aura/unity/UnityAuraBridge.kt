package com.sakura.aura.unity

import com.sakura.aura.ui.home.AuraColorUi
import com.unity3d.player.UnityPlayer

/**
 * Puente Android → Unity.
 *
 * El proyecto de Unity resuelve el aura **por escena**: hay una escena por
 * color (`AuraRoja`, `AuraAzul`, …) y un `MonoBehaviour` receptor que las
 * carga. Aquí solo se traduce el enum de la app al nombre que espera ese
 * receptor; toda la lógica visual vive del lado de Unity.
 */
object UnityAuraBridge {

    /** GameObject y método del receptor. Deben coincidir con `AuraReceiver.cs`. */
    private const val RECEIVER_OBJECT = "AuraReceiver"
    private const val RECEIVER_METHOD = "MostrarAura"

    /**
     * Nombre que se manda al receptor; él le antepone "Aura" para formar la
     * escena ("Roja" → `AuraRoja`).
     *
     * [AuraColorUi.AMARILLA] se mapea a Naranja **a propósito**: el backend sí
     * puede devolver "Amarillo", pero el proyecto de Unity no tiene escena
     * `AuraAmarilla` y no va a tenerla. Naranja es la vecina inmediata en la
     * escala de activación (bpm > 85 vs bpm > 75), así que es la sustitución
     * menos mentirosa. Sin este mapeo el receptor pediría una escena
     * inexistente y la vista se quedaría congelada en la anterior.
     */
    fun sceneSuffixFor(aura: AuraColorUi): String = when (aura) {
        AuraColorUi.ROJA     -> "Roja"
        AuraColorUi.NARANJA  -> "Naranja"
        AuraColorUi.AMARILLA -> "Naranja"
        AuraColorUi.VERDE    -> "Verde"
        AuraColorUi.AZUL     -> "Azul"
        AuraColorUi.VIOLETA  -> "Violeta"
        AuraColorUi.ROSA     -> "Rosa"
        AuraColorUi.NEUTRAL  -> "Neutral"
    }

    /**
     * Envía el aura al receptor de Unity.
     *
     * `UnitySendMessage` no confirma entrega ni falla de forma visible: si el
     * runtime todavía no levantó, el mensaje se pierde en silencio. Por eso
     * [AuraUnityActivity] llama a esto varias veces mientras Unity arranca, y
     * por eso el receptor es idempotente.
     */
    fun send(aura: AuraColorUi) {
        UnityPlayer.UnitySendMessage(RECEIVER_OBJECT, RECEIVER_METHOD, sceneSuffixFor(aura))
    }
}
