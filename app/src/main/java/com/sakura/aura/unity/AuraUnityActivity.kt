package com.sakura.aura.unity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.sakura.aura.ui.home.AuraColorUi

/**
 * Vista 3D del aura. Hospeda el player de Unity y le dice qué escena mostrar.
 *
 * Toda la fontanería (controles, captura de foto, salida, parches de
 * compatibilidad) vive en [UnityCaptureActivity]; aquí solo queda el aura que
 * se está mostrando.
 */
class AuraUnityActivity : UnityCaptureActivity() {

    private lateinit var aura: AuraColorUi

    override val prefijoFoto = "aura"

    override fun onCreate(savedInstanceState: Bundle?) {
        aura = runCatching {
            AuraColorUi.valueOf(intent.getStringExtra(EXTRA_AURA) ?: "")
        }.getOrDefault(AuraColorUi.NEUTRAL)
        super.onCreate(savedInstanceState)
    }

    override fun enviarMensaje() = UnityAuraBridge.send(aura)

    companion object {
        private const val EXTRA_AURA = "aura"

        fun launch(context: Context, aura: AuraColorUi) {
            context.startActivity(
                Intent(context, AuraUnityActivity::class.java)
                    .putExtra(EXTRA_AURA, aura.name)
            )
            // Fundido en vez del slide por defecto: el arranque de Unity ya es
            // brusco de por sí (splash + cámara), esto lo suaviza un poco.
            (context as? Activity)?.overridePendingTransition(
                android.R.anim.fade_in, android.R.anim.fade_out
            )
        }
    }
}
