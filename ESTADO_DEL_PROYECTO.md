# Estado del proyecto — CORSYNC-Movil

> Documento de contexto para retomar el trabajo en otra máquina o sesión.
> Última actualización: **28 jul 2026** · Rama: `feature/unity-compatibility`
>
> **Si vas a trabajar en Windows, salta directo a la [sección 6](#6-al-iniciar-sesión-en-windows).**
> Es la parte que cambia según el sistema operativo.

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
| Animación Unity | ❌ No implementada |
| Cámara / AR | ❌ No implementada |

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

## 4. Lo que falta

### 4.1 Bloqueador inmediato — `libil2cpp.so`

El build llega hasta `:unityLibrary:BuildIl2CppTask` y ahí muere.

**Situación:**

- `jniLibs/arm64-v8a/` y `jniLibs/x86_64/` tienen `libunity.so`, `libmain.so` y
  `libUnityOpenXR.so`, **pero NO `libil2cpp.so`**.
- `libil2cpp.so` es donde vive *todo el código C# compilado a nativo*. Sin él
  Unity arranca pero no ejecuta nada. Es obligatorio.
- El export trae los ~269 MB de C++ generado esperando que Gradle lo compile.
- **El toolchain IL2CPP del export es de Windows**: en
  `Il2CppOutputProject/IL2CPP/build/deploy/` solo hay `.exe` (`il2cpp.exe`,
  `UnityLinker.exe`, `createdump.exe`). Cero binarios de Linux.

**Por eso el sistema operativo importa:**

| Sistema | Puede generar `libil2cpp.so` |
|---|---|
| **Windows** | ✅ Sí — el `il2cpp.exe` corre. Solo falta instalar el NDK. |
| **Linux** | ❌ No — la tarea busca `il2cpp` sin extensión y no existe. Requeriría el Editor de Unity para Linux instalado. |

Alternativa si no se quiere compilar: pedirle a **Saul** (hizo el export, trabaja
en Windows) que compile de su lado y mande el archivo resultante de
`unityLibrary/src/main/jniLibs/arm64-v8a/libil2cpp.so`. Con **arm64-v8a** basta;
el dispositivo de pruebas es ARM64.

> Cuando el `.so` exista (compilado localmente o recibido), hay que **desactivar
> `BuildIl2CppTask`** en `unityLibrary/build.gradle` para que Gradle use el
> archivo ya hecho en vez de intentar recompilarlo. Si no, seguirá fallando
> aunque el `.so` esté puesto.

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

### 4.3 Puente Kotlin → Unity

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
| Backend AGP / Gradle | AGP **9.3.0** · Gradle **9.6.1** |
| Kotlin / KSP / Hilt | 2.2.10 · 2.2.0-2.0.2 · 2.56.2 |
| minSdk / compileSdk | 27 / 35 |
| ABIs | `arm64-v8a`, `x86_64` |
| Dispositivo de pruebas | Motorola Razr 50 (ARM64) |
| Hub SignalR | `/telemetryHub` · device `ESP32_MAX30102` |

**Quién hizo qué:**

- **Saul Reyna** (`saul-greyna`) — export de Unity y alta del módulo
  `unityLibrary` (commits `3d2675e`, `098bf12`). Trabaja en **Windows**; es quien
  tiene el entorno capaz de compilar IL2CPP.
- **IDGS-902-23002409** — app móvil y fase 1 de fixes (`65d61d9`).

---

## 6. Al iniciar sesión en Windows

Es el entorno donde el bloqueador de §4.1 **sí se puede resolver localmente**.

**1. Instalar el NDK que pide Unity 2021.3** (una sola vez), desde el SDK Manager
de Android Studio o por línea de comandos:

```bat
sdkmanager "ndk;21.4.7075529" "platforms;android-30" "build-tools;30.0.2"
```

**2. Verificar que Gradle encuentra el NDK.** Si hace falta, en `local.properties`:

```properties
ndk.dir=C\:\\Users\\<usuario>\\AppData\\Local\\Android\\Sdk\\ndk\\21.4.7075529
```

**3. Compilar.** La primera vez `BuildIl2CppTask` tarda bastante (decenas de
minutos): está compilando ~269 MB de C++ generado.

```bat
gradlew.bat :app:assembleDebug
```

**4. Confirmar que el `.so` se generó:**

```bat
dir unityLibrary\src\main\jniLibs\arm64-v8a\libil2cpp.so
```

Si aparece, el bloqueador quedó resuelto y se puede pasar al trabajo de escena
(§4.2). **Commitea ese `.so`** para que quien trabaje en Linux también pueda
construir sin repetir todo esto.

> Si prefieres no esperar la compilación, se puede acotar a una sola arquitectura
> quitando `x86_64` de los `abiFilters` en `unityLibrary/build.gradle`. Solo
> perderías poder correr en emulador x86; en el Razr 50 (ARM64) es indiferente.
