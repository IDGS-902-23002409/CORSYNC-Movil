package com.sakura.aura.unity

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.Gravity
import android.view.KeyEvent
import android.view.PixelCopy
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import com.unity3d.player.UnityPlayerActivity

/**
 * Base de las vistas que hospedan el player de Unity con la cámara abierta.
 *
 * Concentra lo que comparten la vista del aura y la de filtros: los controles
 * superpuestos, la captura del `SurfaceView`, el guardado en `Pictures/CORSYNC`,
 * la salida con "atrás" y los parches de compatibilidad. Lo único que cambia
 * entre una y otra es **qué mensaje se le manda a Unity** ([enviarMensaje]) y
 * cómo se nombra la foto ([prefijoFoto]).
 *
 * Corre en su propio proceso (`android:process=":unityplayer"`, ver manifest)
 * porque Unity as a Library **mata el proceso** al destruirse la activity
 * (`UnityPlayer.destroy()` → `Process.killProcess`). En el proceso principal
 * eso cerraba la app completa al salir de la vista 3D. El costo es que cada
 * apertura es un arranque en frío de Unity (con su splash); la alternativa
 * — mantener Unity vivo en el proceso de la app — revive el bug del cierre.
 */
abstract class UnityCaptureActivity : UnityPlayerActivity() {

    protected val handler = Handler(Looper.getMainLooper())
    private lateinit var overlay: FrameLayout

    /** Prefijo del archivo guardado en la galería ("aura_1234.jpg"). */
    protected open val prefijoFoto: String = "captura"

    /**
     * Manda a Unity la escena que toca mostrar. Se invoca varias veces durante
     * el arranque (ver [reintentosMs]), así que **debe ser idempotente**.
     */
    protected abstract fun enviarMensaje()

    // Unity tarda en levantar y UnitySendMessage no avisa si el mensaje se
    // pierde. Se reintenta en ventanas crecientes hasta cubrir el arranque;
    // el receptor ignora los repetidos si ya está en la escena pedida.
    private val reintentosMs = longArrayOf(0, 400, 1000, 2000, 3500, 6000)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        montarControles()
    }

    override fun onResume() {
        super.onResume()
        reintentosMs.forEach { delay ->
            handler.postDelayed({ enviarMensaje() }, delay)
        }
    }

    override fun onPause() {
        handler.removeCallbacksAndMessages(null)
        super.onPause()
    }

    // ── Volver a la app ───────────────────────────────────────────────────
    // La vista de Unity consume los eventos de teclado (incluido BACK) antes
    // de que lleguen a la activity, así que se intercepta en dispatchKeyEvent.
    // El gesto de atrás entra por onBackPressed. Ambos cierran esta activity;
    // como corre en su propio proceso, la app queda intacta debajo.

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.action == KeyEvent.ACTION_UP) finish()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
    }

    // ── Compat targetSdk 34+ ──────────────────────────────────────────────
    // Unity 2021.3 registra sus BroadcastReceivers sin la bandera
    // RECEIVER_EXPORTED/NOT_EXPORTED que Android exige desde targetSdk 34, y
    // eso mataba la activity al arrancar. Unity registra a través del contexto
    // de esta activity, así que se intercepta aquí; los broadcasts del sistema
    // llegan igual con NOT_EXPORTED.

    override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?): Intent? {
        return if (Build.VERSION.SDK_INT >= 34) {
            super.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            super.registerReceiver(receiver, filter)
        }
    }

    override fun registerReceiver(
        receiver: BroadcastReceiver?,
        filter: IntentFilter?,
        broadcastPermission: String?,
        scheduler: Handler?
    ): Intent? {
        return if (Build.VERSION.SDK_INT >= 34) {
            super.registerReceiver(
                receiver, filter, broadcastPermission, scheduler, Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            super.registerReceiver(receiver, filter, broadcastPermission, scheduler)
        }
    }

    // ── Controles superpuestos (cerrar + foto) ────────────────────────────

    private fun montarControles() {
        val dp = resources.displayMetrics.density

        fun botonRedondo(sizeDp: Int, icono: Int, onClick: () -> Unit) =
            ImageButton(this).apply {
                setImageResource(icono)
                setColorFilter(Color.WHITE)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(0x66000000)
                }
                layoutParams = FrameLayout.LayoutParams(
                    (sizeDp * dp).toInt(), (sizeDp * dp).toInt()
                )
                setOnClickListener { onClick() }
            }

        overlay = FrameLayout(this)

        val cerrar = botonRedondo(44, android.R.drawable.ic_menu_close_clear_cancel) { finish() }
        (cerrar.layoutParams as FrameLayout.LayoutParams).apply {
            gravity = Gravity.TOP or Gravity.END
            topMargin = (48 * dp).toInt()
            rightMargin = (16 * dp).toInt()
        }

        val foto = botonRedondo(68, android.R.drawable.ic_menu_camera) { capturarFoto() }
        (foto.layoutParams as FrameLayout.LayoutParams).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            bottomMargin = (40 * dp).toInt()
        }

        overlay.addView(cerrar)
        overlay.addView(foto)
        addContentView(
            overlay,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    /**
     * Captura el SurfaceView donde Unity dibuja, no la ventana completa: así
     * la foto sale limpia (sin los botones superpuestos) y sin el problema de
     * que el contenido GL no aparece en un capture normal de la vista.
     */
    private fun capturarFoto() {
        val surface = buscarSurfaceView(mUnityPlayer)
        if (surface == null || surface.width == 0 || surface.height == 0) {
            aviso("La vista 3D aún no está lista")
            return
        }
        val bmp = Bitmap.createBitmap(surface.width, surface.height, Bitmap.Config.ARGB_8888)
        PixelCopy.request(surface, bmp, { resultado ->
            if (resultado == PixelCopy.SUCCESS) {
                guardarEnGaleria(bmp)
            } else {
                bmp.recycle()
                aviso("No se pudo capturar la imagen ($resultado)")
            }
        }, handler)
    }

    private fun buscarSurfaceView(grupo: ViewGroup): SurfaceView? {
        for (i in 0 until grupo.childCount) {
            when (val hijo = grupo.getChildAt(i)) {
                is SurfaceView -> return hijo
                is ViewGroup -> buscarSurfaceView(hijo)?.let { return it }
            }
        }
        return null
    }

    private fun guardarEnGaleria(bmp: Bitmap) {
        Thread {
            try {
                val valores = ContentValues().apply {
                    put(
                        MediaStore.Images.Media.DISPLAY_NAME,
                        "${prefijoFoto}_${System.currentTimeMillis()}.jpg"
                    )
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= 29) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CORSYNC")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }
                val uri = contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, valores
                ) ?: error("MediaStore rechazó la inserción")

                contentResolver.openOutputStream(uri)?.use { out ->
                    bmp.compress(Bitmap.CompressFormat.JPEG, 95, out)
                } ?: error("no se pudo abrir el stream")

                if (Build.VERSION.SDK_INT >= 29) {
                    valores.clear()
                    valores.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(uri, valores, null, null)
                }
                runOnUiThread { aviso("Foto guardada en Galería (Pictures/CORSYNC)") }
            } catch (e: Exception) {
                runOnUiThread { aviso("Error al guardar: ${e.message}") }
            } finally {
                bmp.recycle()
            }
        }.start()
    }

    protected fun aviso(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
