using UnityEngine;
using UnityEngine.SceneManagement;

/// <summary>
/// Receptor de mensajes desde la app Android (CORSYNC-Movil).
///
/// La app llama:
///   UnityPlayer.UnitySendMessage("AuraReceiver", "MostrarAura", "Roja");
/// y este script carga la escena "AuraRoja".
///
/// INSTALACIÓN (2 minutos, sin escribir código):
///   1. Copiar este archivo a Assets/Scripts/ del proyecto DemoAura.
///   2. Abrir la escena AuraNeutral (la que quede en el índice 0 del Build).
///   3. GameObject > Create Empty. Nombrarlo EXACTAMENTE: AuraReceiver
///   4. Arrastrar este script a ese GameObject.
///   5. Guardar la escena. Listo.
///
/// Valores que puede recibir (los manda la app, ya normalizados):
///   "Neutral", "Roja", "Naranja", "Verde", "Azul", "Violeta", "Rosa"
/// La app NUNCA manda "Amarilla": la convierte a "Naranja" antes de enviar.
/// </summary>
public class AuraReceiver : MonoBehaviour
{
    private static AuraReceiver instancia;

    void Awake()
    {
        // Sobrevive a los cambios de escena y evita duplicados si la escena
        // que lo contiene se recarga.
        if (instancia != null && instancia != this)
        {
            Destroy(gameObject);
            return;
        }
        instancia = this;
        DontDestroyOnLoad(gameObject);
    }

    /// <summary>
    /// Único punto de entrada desde Android. Recibe el nombre del aura
    /// (sin el prefijo "Aura") y carga la escena correspondiente.
    /// </summary>
    public void MostrarAura(string aura)
    {
        string escena = "Aura" + aura; // "Roja" -> "AuraRoja"

        // Idempotente: la app puede reenviar el mensaje varias veces para
        // asegurar la entrega mientras Unity termina de arrancar. Si la
        // escena pedida ya está activa, no se recarga.
        if (SceneManager.GetActiveScene().name == escena)
            return;

        if (Application.CanStreamedLevelBeLoaded(escena))
        {
            SceneManager.LoadScene(escena);
        }
        else
        {
            // Escena no agregada en File > Build Settings > Scenes In Build.
            Debug.LogError("[AuraReceiver] La escena '" + escena +
                "' no está en Build Settings. Agregarla y re-exportar.");
        }
    }
}
