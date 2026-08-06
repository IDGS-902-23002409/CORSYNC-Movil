package com.sakura.aura.unity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.sakura.aura.ui.filters.FiltroUi

/**
 * Cámara con filtro. Gemela de [AuraUnityActivity]: mismo player de Unity,
 * misma captura, lo único que cambia es la escena que se pide.
 *
 * Las fotos se guardan en la misma carpeta `Pictures/CORSYNC` que las del aura
 * — con prefijo `filtro_` — para que aparezcan en la galería sin tocar
 * [com.sakura.aura.ui.gallery.AuraPhotoStore].
 */
class FiltroUnityActivity : UnityCaptureActivity() {

    private lateinit var filtro: FiltroUi

    override val prefijoFoto = "filtro"

    override fun onCreate(savedInstanceState: Bundle?) {
        filtro = FiltroUi.fromSufijo(intent.getStringExtra(EXTRA_FILTRO))
            ?: FiltroUi.MARIPOSAS
        super.onCreate(savedInstanceState)
    }

    override fun enviarMensaje() = UnityFiltroBridge.send(filtro)

    companion object {
        private const val EXTRA_FILTRO = "filtro"

        fun launch(context: Context, filtro: FiltroUi) {
            context.startActivity(
                Intent(context, FiltroUnityActivity::class.java)
                    .putExtra(EXTRA_FILTRO, filtro.sufijo)
            )
            (context as? Activity)?.overridePendingTransition(
                android.R.anim.fade_in, android.R.anim.fade_out
            )
        }
    }
}
