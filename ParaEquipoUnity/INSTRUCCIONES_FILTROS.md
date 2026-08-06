# Filtros — qué hace falta para que funcionen

> **Resumen en una línea:** el export de filtros que mandaron no se puede usar
> como módulo aparte, y su script no puede cargar las escenas `Filtro*`.
> Hace falta **un solo export** con auras + filtros y el `AuraReceiver.cs`
> actualizado que viene junto a este documento.
>
> El lado Android **ya está listo y compilando**: modelo de filtros, desbloqueo
> por recompensas y el módulo de la galería. Solo falta el export.

---

## Lo que se recibió

La carpeta `unityLibraryFiltros/` es un **export completo e independiente** del
proyecto de Unity. Contiene 5 escenas, verificadas leyendo su `BuildSettings`:

| # | Escena |
|---|--------|
| 0 | `FiltroMariposas` |
| 1 | `FiltroDragonBall` |
| 2 | `FiltroLuciernagas` |
| 3 | `FiltroFuego` |
| 4 | `FiltroLluvia` |

Los efectos y las escenas están bien. El problema es cómo se entregaron.

---

## Problema 1 — no pueden convivir dos módulos de Unity

`unityLibraryFiltros/unityLibrary` es un segundo export íntegro: mismo paquete
`com.unity3d.player`, misma clase `UnityPlayerActivity`, y las mismas librerías
nativas (`libunity.so`, `libmain.so`). Android no puede empaquetar las dos:
son clases y `.so` duplicados en el mismo APK.

Y aunque se pudiera, **Unity as a Library solo admite un runtime por app**: hay
una única carpeta `assets/bin/Data/`. No existe forma de tener "el Unity de las
auras" y "el Unity de los filtros" a la vez.

**Lo que se necesita:** meter las 5 escenas de filtros en el **mismo proyecto**
que las auras y hacer **un único export** con las 12 escenas.

---

## Problema 2 — el script no puede cargar un filtro

Al proyecto de filtros se le copió tal cual el `AuraReceiver.cs` de las auras.
Su único método es:

```csharp
public void MostrarAura(string aura)
{
    string escena = "Aura" + aura;   // <-- antepone SIEMPRE "Aura"
    ...
}
```

Antepone `"Aura"` al valor recibido. Mandarle `"Fuego"` pide la escena
`AuraFuego`, que no existe → entra por el `else`, escribe un error en el log y
**la vista se queda en la escena de arranque**. No hay ningún valor que se le
pueda mandar para que salga `FiltroFuego`; no es algo que se pueda sortear desde
Android.

**Lo que se necesita:** reemplazar el script por el `AuraReceiver.cs` que
acompaña a este documento. Añade `MostrarFiltro(string)` y deja `MostrarAura`
**exactamente igual**, así que las auras siguen funcionando sin tocar nada.

---

## Los 3 pasos

### 1. Unificar el proyecto

Copiar las 5 escenas `Filtro*` a `Assets/Scenes/Filtros/` del proyecto
**DemoAura** (el de las auras), con sus prefabs y materiales.

### 2. Reemplazar el script

Copiar `AuraReceiver.cs` (viene junto a este documento) sobre el que ya está en
`Assets/Scripts/`. El GameObject `AuraReceiver` sigue igual: en la escena
`AuraNeutral`, con `DontDestroyOnLoad`.

### 3. Build Settings — las 12 escenas

`File > Build Settings > Scenes In Build` debe quedar así:

```
0   AuraNeutral        ← índice 0 obligatorio (es la que arranca)
1   AuraRoja
2   AuraNaranja
3   AuraVerde
4   AuraAzul
5   AuraVioleta
6   AuraRosa
7   FiltroMariposas
8   FiltroLuciernagas
9   FiltroLluvia
10  FiltroFuego
11  FiltroDragonBall
```

El orden del 7 al 11 da igual; el **0 sí importa**. Si una escena no está en esa
lista, `SceneManager.LoadScene` falla en el teléfono aunque en el Editor
funcione.

### 4. Exportar y entregar

- `File > Build Settings > Android` → marcar **Export Project**.
- Player Settings: IL2CPP · ARM64 · Minimum API 27 (igual que los anteriores).
- Mandar la carpeta `unityLibrary` **completa, en zip**.

⚠️ **NO copiarla directamente encima del repo CORSYNC-Movil.** El
`build.gradle` del módulo tiene parches para Gradle 9 que el export pisa y el
proyecto deja de compilar. La integración la hacemos nosotros del lado móvil.

### 5. (Opcional, pero ayuda mucho) Mandar además un APK

Si además del export pueden hacer un **Build normal a APK** del mismo proyecto,
en el mismo momento y sin tocar nada entre medias, mándenlo también.

Motivo: al exportar, Unity **no** compila `libil2cpp.so` — eso nos toca a
nosotros y son ~25 minutos en una máquina Windows concreta. Si Unity ya generó
ese archivo al construir el APK, podríamos sacarlo de ahí y ahorrarnos el paso.
Solo sirve si el APK sale **del mismo estado exacto del proyecto** que el
export; si hubo cualquier cambio entre uno y otro, no nos sirve y lo
descartamos.

---

## ⚠️ Lo más importante: no renombrar las escenas

Los nombres de las 5 escenas de filtros están **fijados en el código de la app**
(`app/src/main/java/com/sakura/aura/ui/filters/FiltroUi.kt`). Si alguna se
renombra —aunque sea solo ponerle una tilde a `Luciernagas`— ese filtro deja de
cargar **sin ningún error visible**: se abre la cámara y simplemente no pasa
nada.

Si de verdad hay que renombrar alguna, **avisar** y decir el nombre nuevo. Es un
cambio de una línea de nuestro lado, pero hay que hacerlo a propósito.

Los nombres que la app espera, exactos:

```
FiltroMariposas    FiltroLuciernagas    FiltroLluvia    FiltroFuego    FiltroDragonBall
```

---

## Contrato (referencia)

| | |
|---|---|
| GameObject | `AuraReceiver` (en AuraNeutral, con `DontDestroyOnLoad`) |
| Método auras | `MostrarAura(string)` → `"Roja"` carga `AuraRoja` |
| Método filtros | `MostrarFiltro(string)` → `"Fuego"` carga `FiltroFuego` |
| Valores aura | `Neutral` `Roja` `Naranja` `Verde` `Azul` `Violeta` `Rosa` |
| Valores filtro | `Mariposas` `Luciernagas` `Lluvia` `Fuego` `DragonBall` |
| Frecuencia | Una vez por apertura de la vista (se reintenta durante el arranque) |
| Amarilla | No existe: la app la mapea a `Naranja` |
| Acentos | Ninguno. `Luciernagas` sin tilde, igual que la escena |

Si renombran una escena, avisar: los nombres están fijados en
`app/src/main/java/com/sakura/aura/ui/filters/FiltroUi.kt` y un cambio silencioso
hace que el filtro deje de cargar sin error visible.

---

## Mientras tanto

La app ya trae el módulo de filtros completo y funcionando en la galería
(desbloqueo, selección, botón de cámara). Con el export actual —el de las
auras— al pulsar "Usar" se abre la cámara pero se ve la escena de arranque en
lugar del filtro, porque las escenas `Filtro*` no están en ese bundle. En cuanto
llegue el export unificado, **no hay que tocar código de Android**: se
reemplaza la carpeta `unityLibrary/` y los filtros salen.
