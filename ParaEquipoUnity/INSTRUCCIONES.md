## Lo único que hay que hacer

### 1. Agregar el script receptor (2 min, copy-paste)

- Copiar `AuraReceiver.cs` (viene junto a este documento) a `Assets/Scripts/`.
- Abrir la escena **AuraNeutral**.
- `GameObject > Create Empty` → nombrarlo **exactamente** `AuraReceiver`
  (sin espacios, respetando mayúsculas).
- Arrastrarle el script. Guardar la escena.

Sin este script, `UnitySendMessage` desde Android no tiene a quién llamarle y
los mensajes se pierden en silencio. Es el único código nuevo requerido.

### 2. Verificar Build Settings (1 min)

`File > Build Settings > Scenes In Build` debe tener las **7 escenas**:

```
0  AuraNeutral      ← índice 0 obligatorio (es la que arranca)
1  AuraRoja
2  AuraNaranja
3  AuraVerde
4  AuraAzul
5  AuraVioleta
6  AuraRosa
```

Si una escena no está en esa lista, `SceneManager.LoadScene` falla en el
teléfono aunque en el Editor funcione. **No hace falta escena AuraAmarilla**:
la app la convierte a Naranja antes de enviar.

### 3. Exportar (igual que la vez anterior)

- `File > Build Settings > Android` → marcar **Export Project**.
- Player Settings: IL2CPP · ARM64 · Minimum API 27 (igual que el export previo).
- Exportar a una carpeta nueva.

### 4. Entregar

Mandar la carpeta `unityLibrary` del export **completa, en zip**.

⚠️ **NO copiarla directamente encima del repo CORSYNC-Movil.** El
`build.gradle` del módulo tiene parches para Gradle 9 que el export pisa y el
proyecto deja de compilar (el detalle está en `ESTADO_DEL_PROYECTO.md` §3.5).
La integración la hacemos nosotros del lado móvil.

## Contrato (referencia)

| | |
|---|---|
| GameObject | `AuraReceiver` (en AuraNeutral, con `DontDestroyOnLoad`) |
| Método | `MostrarAura(string)` |
| Valores | `Neutral` `Roja` `Naranja` `Verde` `Azul` `Violeta` `Rosa` |
| Frecuencia | **Una vez por escaneo** (al terminar el promedio), no en tiempo real |
| Amarilla | No existe: la app la mapea a `Naranja` |
