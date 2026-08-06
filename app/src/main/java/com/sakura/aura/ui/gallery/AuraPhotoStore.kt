package com.sakura.aura.ui.gallery

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AuraPhoto(
    val uri: Uri,
    val name: String,
    val takenAtMillis: Long
)

/**
 * Lee y comparte las capturas que la vista 3D guarda en `Pictures/CORSYNC`.
 *
 * No pide permisos de almacenamiento **a propósito**: bajo scoped storage una
 * app siempre ve el contenido que ella misma insertó en MediaStore, sin
 * `READ_MEDIA_IMAGES`. Las fotos las escribe `AuraUnityActivity`, que corre en
 * otro proceso pero con el mismo UID, así que la propiedad es la misma.
 */
object AuraPhotoStore {

    const val CARPETA = "CORSYNC"

    suspend fun cargar(context: Context): List<AuraPhoto> = withContext(Dispatchers.IO) {
        val columnas = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )

        // En API 29+ se filtra por RELATIVE_PATH; antes esa columna no existe y
        // hay que mirar DATA, que sigue disponible en esas versiones.
        val (seleccion, argumentos) = if (Build.VERSION.SDK_INT >= 29) {
            "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?" to arrayOf("%$CARPETA%")
        } else {
            @Suppress("DEPRECATION")
            "${MediaStore.Images.Media.DATA} LIKE ?" to arrayOf("%/$CARPETA/%")
        }

        val fotos = mutableListOf<AuraPhoto>()
        runCatching {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columnas,
                seleccion,
                argumentos,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idxId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val idxNombre = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val idxFecha = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idxId)
                    fotos += AuraPhoto(
                        uri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                        ),
                        name = cursor.getString(idxNombre) ?: "aura",
                        // DATE_ADDED viene en segundos, no en milisegundos.
                        takenAtMillis = cursor.getLong(idxFecha) * 1000L
                    )
                }
            }
        }
        fotos
    }

    /**
     * Abre el selector de apps del sistema (WhatsApp, Instagram, etc.).
     *
     * El `content://` de MediaStore ya es compartible; lo que hace falta es
     * `FLAG_GRANT_READ_URI_PERMISSION` para que la app receptora pueda leerlo.
     */
    fun compartir(context: Context, fotos: List<Uri>) {
        if (fotos.isEmpty()) return

        val intent = if (fotos.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, fotos.first())
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/jpeg"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(fotos))
            }
        }.apply {
            putExtra(Intent.EXTRA_TEXT, "Mi aura de hoy ✨ #CORSYNC")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Compartir mi aura"))
    }

    suspend fun borrar(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching { context.contentResolver.delete(uri, null, null) > 0 }.getOrDefault(false)
    }
}
