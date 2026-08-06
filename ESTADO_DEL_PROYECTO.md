# Estado del proyecto — CORSYNC-Movil

> Documento de contexto para retomar el trabajo en otra máquina o sesión.
> Última actualización: **1 ago 2026** · Rama: `feature/unity-compatibility`
>
> **El build ya pasa y `libil2cpp.so` está compilado** (§3.5). Lo único que
> bloquea el objetivo es la escena de Unity, que necesita el Editor (§4.1).
> Para probar sin el prototipo físico, ver el simulador IoT (§7).

---

## 1. Objetivo

Que el usuario vea su aura **animada en 3D, con la cámara abierta**, alimentada
en tiempo real por la telemetría de un sensor IoT.

Flujo completo:

```
Usuario pulsa "Escanear"
   → SignalR (telemetryHub)
   → bridge del backend
   → prototipo IoT (ESP32_MAX30102)
   → telemetría de vuelta a la app
   → color del aura + animación Unity sobre la cámara
```

**Reparto de responsabilidades (importante, no duplicar trabajo):**

- El **backend** hace toda la limpieza y clasificación. El campo `aura` llega ya
  calculado como string (`"Rojo"`, `"Morado"`, …).
- El **móvil** maneja la animación entera. En C# **no va lógica de mapeo ni
  derivación**: solo un receptor que toma el valor y lo enlaza a los parámetros
  visuales.

---

## 2. Cómo funciona hoy el pipeline

| Tramo | Estado |
|---|---|
| SignalR → app (conexión, telemetría, aura) | ✅ Funciona |
| Cálculo del aura dominante de la sesión | ✅ Corregido (ver §3) |
| Persistencia de la lectura al backend | ✅ Funciona |
| **Build de la app (`:app:assembleDebug`)** | ✅ **Verde** — ver §3.5 |
| **`libil2cpp.so`** | ✅ **Compilado** (arm64-v8a, 27 MB) |
| Animación Unity | ❌ No implementada — bloqueada por §4.2 |
| Cámara / AR | ❌ No implementada — bloqueada por §4.2 |

**Para probar sin el prototipo físico** hay un simulador del ESP32 en
`CORSYNC-Backend/Tools/iot-simulator` (ver §7).

---

## 3. Lo que ya se hizo — Fase 1 (commit `65d61d9`)

Verificado con **83 tests unitarios, 0 fallos** — corridos apartando
temporalmente los dos archivos de test que ya venían rotos de antes (ver §4.5);
esos se dejaron intactos en el repo.

### 3.1 El bug del color gris

**Causa:** el backend manda el aura en masculino (`"Rojo"`, `"Morado"`,
`"Amarillo"`) pero `AuraColorUi` comparaba contra etiquetas en femenino, y
`"Amarillo"` ni siquiera existía en el enum. **Tres de los seis colores posibles
caían en `NEUTRAL` y se pintaban gris.** El historial se veía bien porque usaba
otro mapeador que sí era correcto — de ahí que el mismo escaneo saliera gris en
vivo y con color en el historial.

**Arreglo:** la normalización vive ahora en un único lugar,
`domain/model/Aura.kt`, y tanto Home como Historial/Analytics pasan por ahí.
Había además un test que *blindaba el bug* (`assertEquals(NEUTRAL,
fromString("Morado"))`); fue reemplazado por tests contra el vocabulario real
del contrato.

### 3.2 Lecturas perdidas cuando llegaban muchos datos

- `StateFlow` es *conflated* por diseño: descartaba telemetría justo cuando más
  llegaba. Se añadió un `SharedFlow` con buffer en `SignalRService` para
  acumular sin pérdidas; el `StateFlow` quedó solo para lo que se muestra en vivo.
- **Carrera al iniciar:** `isScanning` se marcaba *después* de pedir la medición,
  y las lecturas de esa ventana se descartaban. Ahora se marca antes.
- **Falla silenciosa:** si la muestra quedaba vacía no se asignaba `scanResult`
  y la pantalla no mostraba ni color ni error. Ahora reporta el problema.
- El aura dominante se agrupa ya normalizada (para que `"Rojo"` y `"Roja"` no
  cuenten como auras distintas), pero **se persiste el string original** del
  backend para no cambiarle el vocabulario al servidor.

### 3.3 Cuatro bloqueadores de build preexistentes

El proyecto **no compilaba desde el commit de Unity** (`3d2675e`, 21 jul).

1. `android.enableR8` en `gradle.properties` — eliminado en AGP 7.0; el proyecto
   corre AGP **9.3.0**. Fallo duro. *(Ojo: `UNITY_INTEGRATION.md` dice AGP 8.7,
   está desactualizado.)*
2. `UnityPlayerActivity` se mergeaba como LAUNCHER exportada → **dos íconos de la
   app** en el drawer. Se le removió el intent-filter desde el manifest del host.
3. `minSdk` 26 del host vs 27 del módulo Unity. Subido a **27** (cuesta el
   soporte a Android 8.0).
4. **KSP `2.0.21-*` con Kotlin `2.2.10`**, y **Hilt 2.51.1** cuya copia de
   XProcessing no entiende las proyecciones estrella de KSP moderno — reventaba
   al validar los `@Binds` que genera `@HiltViewModel`. Alineados a KSP
   `2.2.0-2.0.2` y Hilt `2.56.2`.

### 3.4 El `.gitignore` que se comió un archivo

El repo tenía un `*.jar` global. Cuando se subió el módulo Unity, git excluyó
**en silencio** a `unity-classes.jar` (los `.aar` sí pasaron, no coinciden con el
patrón). Por eso el módulo estaba a medias sin que nadie se enterara.

Ya se añadió la excepción `!/unityLibrary/libs/*.jar` y el jar está commiteado.

---

### 3.5 El build ya pasa — `libil2cpp.so` compilado (1 ago 2026)

`:app:assembleDebug` termina en verde y produce un APK de **81.5 MB** con
`lib/arm64-v8a/libil2cpp.so` (27 MB) dentro. Se compiló en Windows con el
`il2cpp.exe` que trae el propio export; **no hizo falta el Editor de Unity**.

Hubo que resolver cuatro cosas, ninguna documentada antes porque el build nunca
había llegado tan lejos:

**1. El NDK.** No estaba instalado y tampoco había `cmdline-tools` para bajarlo
con `sdkmanager`. Se descargó el zip directo del repositorio de Google. **Ojo con
la versión:** el zip rotulado `android-ndk-r21d-windows-x86_64.zip` trae en
realidad `Pkg.Revision = 21.3.6528147`, no el `21.4.7075529` que cita esta guía.
AGP resuelve el NDK por **nombre de carpeta** y luego verifica el `Pkg.Revision`,
así que los tres tienen que coincidir:

```
%LOCALAPPDATA%\Android\Sdk\ndk\21.3.6528147\   ← nombre de carpeta
        source.properties → Pkg.Revision = 21.3.6528147
unityLibrary/build.gradle → ndkVersion '21.3.6528147'
```

**2. `Could not find method BuildIl2Cpp()`.** El export declara
`task BuildIl2CppTask` **dentro** del bloque `android {}`. Con Gradle 9 la
resolución de scope del closure ya no alcanza desde ahí las funciones del script.
Se movió la tarea (y su `afterEvaluate`) al nivel del script.

**3. `Could not find method exec()`.** **Gradle 9 eliminó `Project.exec()`**, que
es lo que usaba la función `BuildIl2Cpp` del export. Se reemplazó por
`ProcessBuilder` con `inheritIO()` — no depende de la API de Gradle y además deja
ver el avance de il2cpp en vivo. `ant.move` se cambió por operaciones de `File`
por el mismo motivo.

**4. `BuildIl2CppTask` corría en cada build.** La tarea no declara inputs ni
outputs, así que Gradle la consideraba siempre desactualizada y relanzaba
il2cpp.exe cada vez. Se le puso un `onlyIf` que la salta si el `.so` ya existe;
para forzarla: `gradlew :app:assembleDebug -PforceIl2Cpp`.

**Solo se compila `arm64-v8a`.** Cada ABI extra es una pasada completa de IL2CPP
sobre los ~269 MB de C++ (la primera tardó ~25 min). El filtro está en **dos**
sitios y deben ir sincronizados: `abiFilters` de `unityLibrary` y el de `app`.
Si solo se pone en uno, el APK se lleva `lib/x86_64/libunity.so` **sin** su
`libil2cpp.so` y la app instala en un emulador x86_64 para reventar al arrancar
Unity. Para recuperar x86_64 hay que añadir el ABI en los dos `abiFilters` **y**
la llamada `BuildIl2Cpp(..., 'x64', 'x86_64', ...)` en la tarea.

> **Commitear `unityLibrary/src/main/jniLibs/arm64-v8a/libil2cpp.so`** (27 MB)
> para que nadie más tenga que repetir la compilación, y en particular para que
> quien trabaje en Linux pueda construir — ahí `il2cpp.exe` no corre.

### 3.6 El sync de Android Studio fallaba (AGP demasiado nuevo)

Síntoma engañoso: **compilaba perfecto por CLI pero Studio no sincronizaba**, así
que no se podía instalar ni depurar desde el IDE.

```
The project is using an incompatible version (AGP 9.3.0) of the
Android Gradle plugin. Latest supported version is AGP 9.2.1
```

Android Studio comprueba la versión de AGP contra la suya y aborta; **Gradle no
hace esa comprobación**, de ahí que por línea de comandos nunca diera problema.
Studio 2025.3.4 admite hasta AGP 9.2.1, así que se bajó `agp` a **9.2.1** en
`libs.versions.toml`.

> Si alguien actualiza Android Studio, puede volver a subir AGP. La regla es que
> **AGP nunca puede ser más nuevo que el Studio que abre el proyecto**; al revés
> sí se tolera.

El error vive en `%LOCALAPPDATA%\Google\AndroidStudio<versión>\log\idea.log`, que
es donde conviene buscar cuando el sync falla y el panel del IDE no dice gran cosa.

---

## 4. Lo que falta

### 4.1 Bloqueador único — la escena de Unity está vacía

Con el `.so` ya compilado (§3.5), **lo que queda no se puede resolver sin el
Editor de Unity 2021.3.4f1**. Se volvió a verificar el 1 ago 2026 extrayendo los
símbolos de `Assembly-CSharp.cpp`: el assembly de usuario contiene exactamente
tres tipos — `FireAnimation`, `RutaVuelo` y `RutaVuelo2` — y cada uno solo tiene
`Start`, `Update` y constructor. **No hay ningún método público al que
`UnitySendMessage` pueda llamar, ni un solo componente de AR Foundation.**

O sea: el APK ya arranca Unity, pero Unity no tiene forma de enterarse del aura
ni de mostrar la cámara. El detalle está en §4.2.

### 4.1-bis Estado del export del 1 ago (`unityLibrary1`, ya integrado)

El equipo de Unity entregó un export nuevo con las **7 escenas de aura**. Se
integró sobre `unityLibrary` conservando los parches de §3.5. Verificado
descomprimiendo `data.unity3d` (UnityFS + LZ4):

| Índice | Escena |
|---|---|
| 0 | `AuraNeutral` ← arranca aquí |
| 1 | `AuraRoja` |
| 2 | `AuraAzul` |
| 3 | `AuraVerde` |
| 4 | `AuraVioleta` |
| 5 | `AuraNaranja` |
| 6 | `AuraRosa` |

`Scenes In Build` está completo y correcto. **No hay escena `AuraAmarilla`** y no
va a haberla: el bridge mapea `AMARILLA → Naranja` (ver `UnityAuraBridge.kt`).

**Lo que ese export todavía NO trae: el script receptor.** Confirmado en dos
fuentes sin comprimir (`Assembly-CSharp.cpp` y `global-metadata.dat`): los
únicos tipos de usuario siguen siendo `FireAnimation`, `RutaVuelo` y
`RutaVuelo2`. Sin `AuraReceiver`, `UnitySendMessage` no tiene destinatario y
Unity se queda en la escena 0 para siempre.

Se le entregó al equipo el script ya escrito y un checklist en
`ParaEquipoUnity/`. Cuando devuelvan el export corregido, la integración es:
espejar `src/` y `libs/` **sin tocar `build.gradle`**, quitar el atributo
`package=` del manifest (AGP 8+ lo prohíbe y el export lo reintroduce cada vez),
y recompilar.

> Para inspeccionar un export sin abrir Unity, el script que lista escenas y
> busca el receptor quedó documentado en §3.5; parsea el bundle UnityFS
> descomprimiendo los bloques LZ4.

### 4.2 Trabajo dentro de la escena de Unity

La escena exportada **no tiene nada de lo que necesitamos**. Se verificó
descompilando `Assembly-CSharp.cpp` y el bundle `data.unity3d`:

- **Cero componentes de AR Foundation.** No hay `ARSession`, `ARCameraBackground`,
  `XROrigin`, `ARCameraManager` ni `ARPlaneManager`. Los *paquetes* de ARCore sí
  están instalados (por eso el manifest los exige), pero la escena usa una cámara
  normal de Unity. **Hoy no hay passthrough de cámara.**
- **Los únicos scripts propios** son `FireAnimation` (campos: `speed`, `mat`,
  `offset`), `RutaVuelo` / `RutaVuelo2` (waypoints) y el asset pack
  `EpicToonFX`. **Todos tienen solo `Start`, `Update` y constructor.**
- **No existe ningún método receptor.** `UnitySendMessage` necesita un método
  público con nombre, y no hay ninguno al que llamar. No es un tema de lógica:
  falta el enchufe.
- La animación **no está parametrizada por nada**: `speed` y los waypoints son
  constantes puestas en el Inspector.

Hay que abrir el Editor de Unity (2021.3.4f1, proyecto "DemoAura") y:

1. Montar la cámara y la sesión AR en la escena.
2. Agregar un método público receptor en un MonoBehaviour — un *setter simple*,
   sin lógica de mapeo.
3. Enlazar el color y la velocidad a los campos que ya existen, en vez de las
   constantes del Inspector.

### 4.3 Puente Kotlin → Unity — ✅ hecho (2 ago 2026)

Ya está escrito y compila. Tres piezas:

- **`unity/UnityAuraBridge.kt`** — traduce `AuraColorUi` al nombre que espera el
  receptor y llama a `UnityPlayer.UnitySendMessage`. Aquí vive el mapeo
  `AMARILLA → Naranja`.
- **`unity/AuraUnityActivity.kt`** — hereda de `UnityPlayerActivity`, lee el aura
  del Intent y reenvía el mensaje en 0/400/1000/2000/3500 ms. `UnitySendMessage`
  no confirma entrega ni avisa si se pierde, y Unity tarda en levantar; por eso
  se reintenta y por eso el receptor es idempotente.
- **`HomeScreen.kt`** — botón *"Ver mi aura en 3D"* en la tarjeta de resultado.

Se lanza por Intent y no embebido en un `AndroidView`, al revés de lo que
recomendaba la versión anterior de este documento. El motivo de aquella
recomendación era que el host podía morir a media medición y cortar la conexión
al hub; **ya no aplica**, porque el aura se muestra cuando la sesión terminó y
el promedio está calculado. No hay telemetría en vivo que perder.

### 4.3-bis Referencia del puente (versión original, para contexto)

Recién después de 4.2 tiene sentido. `SignalRService` ya es `@Singleton`, así
que el puente puede observar `telemetryStream` sin abrir una segunda conexión al
hub, y empujar con `UnityPlayer.UnitySendMessage(...)`.

Nota de arquitectura: conviene **embeber** `UnityPlayer` en un `AndroidView` de
Compose (en el lugar donde hoy está el `AuraCanvas` vacío de `HomeScreen.kt`) en
vez de lanzar `UnityPlayerActivity` por Intent. La activity tiene
`launchMode="singleTask"`, o sea que va en su propia task; bajo presión de
memoria el host puede morir y `HomeViewModel.onCleared()` llama `disconnect()`,
dejando la animación sin datos a media medición.

### 4.4 Decisión pendiente de confirmar — Augmented Faces

Se eligió **cámara frontal con seguimiento de rostro** (Augmented Faces). El
Razr 50 se verificó como compatible con ARCore y Depth API.

**Matiz por confirmar en el dispositivo:** *motion tracking* y *Depth API* son
capacidades de **cámara trasera**. Augmented Faces usa la **frontal** y se
certifica por separado — un equipo puede tener ARCore completo y no soportarla.
Comprobar con el teléfono conectado:

```bash
adb shell pm list packages | grep com.google.ar.core
```

Si no devuelve nada, no hay ARCore en ese equipo y habría que caer a la opción
sin AR (cámara frontal con la animación libre encima, que además permitiría
quitar las restricciones de dispositivo).

También conviene revisar que el manifest de Unity exige hoy
`com.google.ar.core.depth` como **requerido**, algo que para Augmented Faces no
hace falta y que restringe innecesariamente los dispositivos donde instala.

### 4.5 Deuda conocida (no bloquea)

- `AuthViewModelTest` y `ProfileViewModelTest` **no compilan**: alguien añadió
  parámetros a esos ViewModels (`tokenManager`, `biometricCipherHelper`,
  `loginUseCase`) sin actualizar los tests. Es previo a la fase 1 y no se tocó.
  Mantiene la suite en rojo si se corre completa.
- `gradle.properties` conserva varios flags legacy que el commit de Unity añadió
  y que solo generan warnings de deprecación. Se dejaron intactos por si el
  módulo Unity los necesita; convendría depurarlos.
- Room está declarado como dependencia y en KSP, pero **no hay ni una `@Entity`,
  `@Dao` ni `@Database`** en el proyecto. Es peso muerto en el build.

---

## 5. Datos de referencia

| Dato | Valor |
|---|---|
| Unity | **2021.3.4f1** · proyecto "DemoAura" |
| Backend AGP / Gradle | AGP **9.2.1** · Gradle **9.6.1** |
| Android Studio | **2025.3.4** — soporta hasta AGP 9.2.1 (ver §3.6) |
| Kotlin / KSP / Hilt | 2.2.10 · 2.2.0-2.0.2 · 2.56.2 |
| minSdk / compileSdk | 27 / 35 |
| ABIs | `arm64-v8a` (solo — ver §3.5) |
| NDK | **21.3.6528147** (no el 21.4 que se citaba; ver §3.5) |
| Dispositivo de pruebas | Motorola Razr 50 (ARM64) |
| Hub SignalR | `/telemetryHub` · device `ESP32_MAX30102` |
| Backend | Desplegado en `http://corsync.runasp.net` — la app apunta ahí |

**Quién hizo qué:**

- **Saul Reyna** (`saul-greyna`) — export de Unity y alta del módulo
  `unityLibrary` (commits `3d2675e`, `098bf12`). Trabaja en **Windows**; es quien
  tiene el entorno capaz de compilar IL2CPP.
- **IDGS-902-23002409** — app móvil y fase 1 de fixes (`65d61d9`).

---

## 6. Compilar

```bat
gradlew.bat :app:assembleDebug
```

Sale en `app\build\outputs\apk\debug\app-debug.apk` (~81 MB). Si el `.so` ya está
en el repo, tarda segundos: `BuildIl2CppTask` se salta sola.

**Si `libil2cpp.so` NO está** (repo recién clonado y el binario no se commiteó),
la primera compilación tarda ~25 min y hace falta el NDK **21.3.6528147** en
`%LOCALAPPDATA%\Android\Sdk\ndk\21.3.6528147\`. Los detalles y las trampas de
versión están en §3.5.

> En **Linux** esto no funciona: el export solo trae `il2cpp.exe`, sin binarios de
> Linux. Ahí hay que traerse el `.so` ya compilado desde una máquina Windows.

---

## 7. Probar sin el prototipo físico — simulador IoT

En **`CORSYNC-Backend/Tools/iot-simulator`** (su propio README tiene el detalle).

```bat
cd CORSYNC-Backend\Tools\iot-simulator
node simulator.js
```

Se conecta al backend **desplegado**, se registra como `ESP32_MAX30102` y queda
esperando. Panel de control en **http://localhost:5300**.

- Habla el protocolo SignalR crudo igual que el firmware (negotiate, handshake
  terminado en `0x1E`, invocaciones JSON). Sin dependencias de npm; requiere
  Node 22+ por el `WebSocket` nativo.
- **Obedece `StartTelemetry` / `StopTelemetry`**: no envía nada hasta que pulses
  "Escanear" en la app. El panel tiene un botón para forzarlo sin teléfono.
- Los escenarios (Rojo…Morado) fijan `bpm` y `gsrVoltaje` en el centro de su
  franja, con ruido que nunca cruza un umbral: **el aura que pides es la que
  devuelve el backend**. Verificado ida y vuelta el 1 ago 2026.
- Para probar el **aura dominante** usa *Ciclo* o *Deriva*; con un escenario fijo
  siempre saldría el mismo color.

No toca el backend, así que **no hay nada que redesplegar**.
