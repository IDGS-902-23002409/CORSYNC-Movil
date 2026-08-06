# Integrar el export de filtros — guía de traspaso

> **Para quién es este documento:** para quien retome el trabajo en otra máquina
> o en otra sesión (persona o IA) **sin contexto previo**. Se explica desde cero
> qué se estaba haciendo, qué quedó hecho y qué falta.
>
> Escrito el **6 ago 2026**. Estado vivo general del proyecto:
> `ESTADO_DEL_PROYECTO.md` (§4.6 cubre filtros).

---

## 0. Resumen en 30 segundos

CORSYNC-Movil es una app Android (Kotlin + Compose) que muestra el "aura" del
usuario animada en 3D con la cámara abierta, renderizada por **Unity as a
Library**. Se le añadieron **filtros de cámara** (tipo Instagram) que se
desbloquean con el sistema de recompensas.

- **El lado Android está terminado y compilando.** No hay que escribir código.
- **Falta el export de Unity.** El que entregaron no sirve (§2).
- Cuando llegue el export bueno: se integra siguiendo §4 y se verifica con §5.

---

## 1. Cómo funciona la parte de Unity (contexto necesario)

La app **no** dibuja el aura ni los filtros. Los dibuja Unity, embebido como
módulo `:unityLibrary`. La comunicación es de una sola dirección:

```
Kotlin                                   Unity (C#)
──────                                   ──────────
UnityPlayer.UnitySendMessage(
    "AuraReceiver",        ← GameObject
    "MostrarAura",         ← método
    "Roja")                ← argumento     →  AuraReceiver.MostrarAura("Roja")
                                                 └→ carga la escena "AuraRoja"
```

Cada aura y cada filtro es **una escena de Unity distinta**. El receptor arma el
nombre concatenando un prefijo: `"Aura" + "Roja"` → `AuraRoja`.

Restricción dura que explica todo lo demás: **Unity as a Library solo admite UN
módulo por app.** Un solo `com.unity3d.player`, un solo `libunity.so`, una sola
carpeta `assets/bin/Data/`. No pueden coexistir dos Unity.

---

## 2. Por qué el export que entregaron no sirve

Está en `unityLibraryFiltros/` (sin trackear en git, ~409 MB). Trae las 5
escenas de filtros correctas, pero tiene **dos fallos independientes**:

**Fallo 1 — es un export separado.** Es un proyecto Unity completo y aparte:
mismo paquete `com.unity3d.player`, misma clase `UnityPlayerActivity`, mismos
`libunity.so`/`libmain.so` que `unityLibrary/`. Meterlo en el APK da clases y
`.so` duplicados. Por eso **NO está en `settings.gradle.kts`** — añadirlo rompe
el build. No es un descuido; es deliberado.

**Fallo 2 — el script no puede cargar un filtro.** Le copiaron tal cual el
`AuraReceiver.cs` de las auras, cuyo único método antepone `"Aura"`:

```csharp
public void MostrarAura(string aura) {
    string escena = "Aura" + aura;   // "Fuego" → "AuraFuego", que NO existe
```

Verificado en el C++ de IL2CPP
(`unityLibraryFiltros/.../il2cppOutput/Assembly-CSharp.cpp`), donde sobrevive
hasta el comentario original del C#. No hay ningún valor que se le pueda mandar
para que resuelva a `FiltroFuego`. **No es sorteable desde Android.**

Además el export **no trae `libil2cpp.so`** (haría falta compilarlo igual).

**Lo que se pidió:** un **único export con las 12 escenas** (7 auras + 5
filtros) y el receptor con un método `MostrarFiltro` nuevo. El documento que se
le mandó al equipo de Unity es `ParaEquipoUnity/INSTRUCCIONES_FILTROS.md`, y el
script corregido `ParaEquipoUnity/AuraReceiver.cs`.

---

## 3. Lo que YA está hecho del lado Android (no tocar)

Todo esto compila y está verificado (`:app:assembleDebug` verde, 83 tests sin
fallos).

| Archivo | Qué hace |
|---|---|
| `ui/filters/FiltroUi.kt` | Los 5 filtros: nombre de escena, color, umbral de puntos |
| `ui/filters/FiltroStore.kt` | Persiste desbloqueados y seleccionado (`SharedPreferences`) |
| `ui/filters/FiltrosViewModel.kt` | Puntos = suma de desafíos completados → desbloqueo |
| `ui/gallery/FiltroCarousel.kt` | El carrusel tipo Instagram |
| `ui/gallery/GalleryScreen.kt` | Integra el carrusel como cabecera del grid |
| `unity/UnityCaptureActivity.kt` | Base común (controles, captura, guardado) |
| `unity/AuraUnityActivity.kt` | Vista del aura — ahora solo lo suyo |
| `unity/FiltroUnityActivity.kt` | Cámara con filtro |
| `unity/UnityFiltroBridge.kt` | Puente → `MostrarFiltro` |

Decisiones tomadas, por si se cuestionan:

- **Desbloqueo monótono:** una vez ganado, se persiste y no se re-evalúa. Los
  puntos vienen del backend; si esa llamada falla, el conteo baja a 0 y sin
  persistencia los filtros se "re-bloquearían" solos.
- **Umbrales:** Mariposas 0 · Luciérnagas 150 · Lluvia 300 · Fuego 500 · Aura
  Saiyan 800. Mariposas en 0 para que el módulo no salga vacío el primer día.
- **Fotos con filtro** van a la misma carpeta `Pictures/CORSYNC` con prefijo
  `filtro_`, así salen en la galería sin tocar `AuraPhotoStore`.
- `FiltroUnityActivity` comparte el proceso `:unityplayer` con
  `AuraUnityActivity` a propósito: solo puede haber un `UnityPlayer` vivo.

**Comportamiento hoy, con el export viejo:** al pulsar "Usar" se abre la cámara
pero se ve la escena de arranque del aura, porque las escenas `Filtro*` no están
en ese bundle. Es degradación limpia, no un crash. **Eso es lo esperado hasta
que llegue el export bueno.**

---

## 4. Cómo integrar el export cuando llegue

### Paso 0 — Verificar el zip ANTES de nada (2 minutos)

No gastes 25 minutos de compilación en un export malo. Descomprime el zip a un
lado y corre:

```bash
python3 Tools/inspeccionar_export_unity.py RUTA_AL_ZIP_DESCOMPRIMIDO/src/main/assets/bin/Data/data.unity3d
```

Tiene que decir **`✓ Las 12 escenas están y AuraNeutral es la de arranque.`**
Si falta alguna o el índice 0 no es `AuraNeutral`, **devuélvelo** — no sigas.

> ⚠️ No intentes verificar esto con `strings data.unity3d | grep level`. Miente:
> el listado va comprimido en LZ4 y `level1..level6` se codifican como
> referencias a `level0`, así que parece haber una sola escena. El script de
> arriba descomprime de verdad.

También conviene confirmar que el receptor trae el método nuevo:

```bash
grep -c "AuraReceiver_MostrarFiltro" RUTA/src/main/Il2CppOutputProject/Source/il2cppOutput/Assembly-CSharp_CodeGen.c
```

Debe devolver un número mayor que 0. Si devuelve 0, no aplicaron el script
corregido → devuélvelo.

### Paso 1 — Copiar los datos, NO la carpeta entera

⚠️ **`unityLibrary/build.gradle` NO se toca.** El nuestro tiene parches
imprescindibles para Gradle 9 que el export pisa (tarea `BuildIl2CppTask` movida
fuera del bloque `android{}`, `ProcessBuilder` en vez de `Project.exec()` que
Gradle 9 eliminó, `ndkVersion '21.3.6528147'`, `abiFilters` solo arm64, y el
`onlyIf` que evita recompilar il2cpp en cada build). Si lo pisas, el proyecto
deja de compilar y cuesta un rato entender por qué.

Del export nuevo se copian **encima de `unityLibrary/`**:

```
src/main/assets/          ← el bundle con las 12 escenas + metadata
src/main/Il2CppOutputProject/   ← el C++ generado (ahora con MostrarFiltro)
src/main/jniLibs/         ← los .so que traiga
src/main/jniStaticLibs/
src/main/res/
libs/
src/main/AndroidManifest.xml
```

Y se **conservan** los nuestros: `build.gradle` y `proguard-unity.txt`.

### Paso 2 — Recompilar `libil2cpp.so` (~25 min, en Windows)

El C# cambió (método nuevo), así que el `libil2cpp.so` commiteado quedó viejo:
lleva dentro el `AuraReceiver` sin `MostrarFiltro`. **Hay que rehacerlo o el
filtro no cargará** aunque las escenas estén.

```bash
gradlew :app:assembleDebug -PforceIl2Cpp
```

Requisitos, que conviene tener listos **antes** de que llegue el zip:

- **Máquina Windows.** El `il2cpp.exe` viene dentro del propio export y no corre
  en Linux. Este es el cuello de botella real del cronograma.
- **NDK r21**, versión exacta `21.3.6528147`. Los tres tienen que coincidir:
  nombre de carpeta en `%LOCALAPPDATA%\Android\Sdk\ndk\21.3.6528147\`,
  `Pkg.Revision` de su `source.properties`, y el `ndkVersion` del
  `build.gradle`. (El zip rotulado `r21d` trae esa revisión, no la
  `21.4.7075529` que cita la documentación de Unity.)

Detalle completo de cómo se resolvió la primera vez: `ESTADO_DEL_PROYECTO.md`
§3.5.

**Plan B si no hay máquina Windows:** si el equipo de Unity mandó también un APK
construido del mismo estado exacto del proyecto, se puede extraer
`lib/arm64-v8a/libil2cpp.so` de dentro del APK y colocarlo en
`unityLibrary/src/main/jniLibs/arm64-v8a/`. **Riesgo real:** si ese APK no salió
del mismo estado que el export, el `libil2cpp.so` y el `global-metadata.dat` no
casan y la app revienta al abrir Unity con un error difícil de diagnosticar. Es
plan B, no plan A.

### Paso 3 — Commitear el `.so`

`unityLibrary/src/main/jniLibs/arm64-v8a/libil2cpp.so` (~27 MB) **sí se
versiona**, para que nadie repita los 25 minutos y para que se pueda construir
desde Linux. El `.gitignore` ya tiene la excepción; verifica que git lo vea:

```bash
git status --short unityLibrary/src/main/jniLibs/
```

Si no aparece, revisa `.gitignore` (hay un `*.jar` global que ya se comió a
`unity-classes.jar` una vez; la excepción `!/unityLibrary/libs/*.jar` existe por
eso).

### Paso 4 — Código de Android

**Ninguno.** Cero cambios. Solo si Unity renombró alguna escena: ajustar el
campo `sufijo` en `app/src/main/java/com/sakura/aura/ui/filters/FiltroUi.kt`.

---

## 5. Checklist de verificación

### Build

```bash
./gradlew :app:assembleDebug
```

- [ ] Termina en `BUILD SUCCESSFUL`.
- [ ] El APK contiene `lib/arm64-v8a/libil2cpp.so`:
      `unzip -l app/build/outputs/apk/debug/app-debug.apk | grep libil2cpp`
- [ ] Tests: `./gradlew :app:testDebugUnitTest` → **83 tests, 0 fallos**.
      ⚠️ Para que corran hay que apartar temporalmente `AuthViewModelTest.kt` y
      `ProfileViewModelTest.kt`: **ya venían rotos de antes** (les faltan
      parámetros de constructor, ver `ESTADO_DEL_PROYECTO.md` §4.5). No son
      regresión — si fallan esos dos y solo esos, está bien.

### En el teléfono

Requiere un dispositivo **arm64 con ARCore** (el de pruebas es un Motorola Razr
50). No sirve emulador x86_64: solo se compila arm64.

**Auras (regresión — esto ya funcionaba, no se puede romper):**
- [ ] Escanear → abrir la vista 3D → se ve el aura del color correcto.
- [ ] Botón de cámara → "Foto guardada en Galería".
- [ ] Botón atrás → vuelve a la app **sin cerrarla**.

**Filtros (lo nuevo):**
- [ ] En Galería aparece el carrusel "Mis filtros" arriba, con scroll junto a
      las fotos.
- [ ] Mariposas sale desbloqueado desde el principio.
- [ ] Los bloqueados salen en gris, con candado y los puntos que faltan.
- [ ] Tocar uno desbloqueado le pone anillo de color; el botón cambia a
      "Usar <nombre>".
- [ ] Pulsar "Usar" abre la cámara **y se ve el filtro** ← *esto es lo que hoy
      no funciona y es la prueba de que la integración salió bien.*
- [ ] Foto con filtro → aparece en la galería al volver.
- [ ] Los 5 filtros cargan su escena correcta (probar uno por uno).

**Si "Usar" abre la cámara pero no se ve el filtro:** la escena no cargó. Mira
el log — el receptor escribe el error:

```bash
adb logcat | grep -i "AuraReceiver\|Unity"
```

`[AuraReceiver] La escena 'FiltroX' no está en Build Settings` significa que esa
escena no entró en el export → volver a §4 paso 0.

---

## 6. Datos de referencia rápidos

| Dato | Valor |
|---|---|
| Escenas de aura | `AuraNeutral` (índice 0) `AuraRoja` `AuraNaranja` `AuraVerde` `AuraAzul` `AuraVioleta` `AuraRosa` |
| Escenas de filtro | `FiltroMariposas` `FiltroLuciernagas` `FiltroLluvia` `FiltroFuego` `FiltroDragonBall` |
| GameObject receptor | `AuraReceiver` |
| Métodos | `MostrarAura(string)` · `MostrarFiltro(string)` |
| Sin escena `AuraAmarilla` | La app mapea Amarilla → Naranja a propósito |
| Acentos | Ninguno en nombres de escena (`Luciernagas` sin tilde) |
| Unity | 2021.3.4f1 · IL2CPP · arm64-v8a · minSdk 27 |
| NDK | 21.3.6528147 (exacto) |
| Carpeta de fotos | `Pictures/CORSYNC` (`aura_*.jpg`, `filtro_*.jpg`) |

Documentos relacionados:
- `ESTADO_DEL_PROYECTO.md` — estado vivo del proyecto entero (§4.6 = filtros)
- `ParaEquipoUnity/INSTRUCCIONES_FILTROS.md` — lo que se le pidió a Unity
- `ParaEquipoUnity/AuraReceiver.cs` — el script corregido que deben aplicar
- `Tools/inspeccionar_export_unity.py` — validador de exports
