# Integración de Unity (Unity as a Library — UaaL) en CORSYNC-Movil

App host: **Sakura / CORSYNC** (`com.sakura.aura`) — Jetpack Compose, AGP 8.7, Kotlin 2.0.21, Gradle 9.4.1, compileSdk 35.

> **Idea clave:** NO se degrada la app host. Unity 2021.3.4f1 exporta un
> módulo Gradle (`unityLibrary`) que trae sus `.so` **precompilados**. Ese
> módulo mantiene *internamente* compileSdk 30 / buildTools 30.0.2, mientras la
> app sigue moderna. Solo hay que hacer que AGP 8 pueda construir ese módulo.

La tabla de versiones que te dieron (AGP 7.0.4, Gradle 7.2, Kotlin 1.5.31,
compileSdk 30, NDK 21.4, buildTools 30.0.2) es el **entorno de build de Unity**,
no lo que la app host debe usar.

---

## 0. Componentes SDK requeridos (una sola vez)

El módulo exportado por Unity 2021.3 referencia estos componentes. Instálalos:

```bash
SDK=/home/charizardbellako/android-sdk-local
"$SDK/cmdline-tools/latest/bin/sdkmanager" \
  "platforms;android-30" \
  "build-tools;30.0.2" \
  "ndk;21.4.7075529"
```

- `platforms;android-30` y `build-tools;30.0.2` → los usa `unityLibrary`.
- `ndk;21.4.7075529` → solo si el `unityLibrary/build.gradle` exportado declara
  `ndkVersion`/`ndkPath`. Si no lo declara (los `.so` van precompilados en
  `jniLibs`), **no** hace falta el NDK en el host.

---

## 1. Exportar desde Unity (Unity 2021.3.4f1 LTS)

1. Instala módulo **Android Build Support** (con OpenJDK, SDK y NDK) en Unity Hub.
2. `File > Build Settings… > Platform: Android > Switch Platform`.
3. Marca **Export Project** (obligatorio para UaaL). Player Settings recomendados:
   - **Scripting Backend:** IL2CPP (o Mono).
   - **Target Architectures:** ARM64 (+ ARMv7 si necesitas 32-bit).
   - **Minimum API Level:** 27 (o el que uses; debe ser ≤ minSdk del host = 26 → sube el host a 27 o baja Unity; ver §3).
4. `Build` → elige una carpeta, p. ej. `~/unity-export/`.

Resultado: una carpeta con estos módulos Gradle:
```
unity-export/
├── launcher/            ← app demo de Unity (NO se usa; es referencia)
├── unityLibrary/        ← ESTE es el módulo que integramos
│   ├── src/main/AndroidManifest.xml
│   ├── libs/            ← unity-classes.jar, etc.
│   ├── src/main/jniLibs/ ← libunity.so, libil2cpp.so (precompilados)
│   └── build.gradle
└── ...
```

---

## 2. Copiar el módulo al proyecto host

```bash
cp -r ~/unity-export/unityLibrary  /home/charizardbellako/Documentos/uni/IDGS/CORSYNC-Movil/unityLibrary
```

El árbol del repo queda:
```
CORSYNC-Movil/
├── app/
├── unityLibrary/        ← nuevo
├── settings.gradle.kts
└── build.gradle.kts
```

---

## 3. Editar `settings.gradle.kts` (host)

**a) Incluir el módulo** (al final del archivo):
```kotlin
include(":app")
include(":unityLibrary")                                     // NUEVO
```

**b) Añadir los repos que Unity necesita** dentro de
`dependencyResolutionManagement { repositories { … } }`:
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://repo.eclipse.org/content/repositories/paho-releases/") }
        flatDir { dirs("${rootProject.projectDir}/unityLibrary/libs") }   // NUEVO: jars de Unity
    }
}
```

> Si `unityLibrary` trae su propio `build.gradle` (Groovy) con bloques
> `repositories {}`, elimínalos o el modo `FAIL_ON_PROJECT_REPOS` fallará.

---

## 4. Adaptar `unityLibrary/build.gradle` a AGP 8

El módulo viene con sintaxis de AGP 7 (Groovy). Ajustes mínimos para AGP 8.7:

```groovy
android {
    namespace 'com.unity3d.player'      // AGP 8 EXIGE namespace (antes iba en el manifest)
    compileSdkVersion 30                 // se queda en 30 — no lo subas
    buildToolsVersion '30.0.2'

    defaultConfig {
        minSdkVersion 27                 // debe ser >= minSdk del host
        targetSdkVersion 30
        // ...
    }
    // Si declara ndkVersion/ndkPath y no quieres NDK en el host, comenta esas líneas
    // ndkVersion '21.4.7075529'
}
```

Además, **borra** del `unityLibrary/src/main/AndroidManifest.xml` el atributo
`package="com.unity3d.player"` de la etiqueta `<manifest>` (AGP 8 lo prohíbe;
ya está en `namespace`).

---

## 5. Conectar el módulo a la app (`app/build.gradle.kts`)

En `dependencies { … }` añade:
```kotlin
implementation(project(":unityLibrary"))
implementation(fileTree("${rootProject.projectDir}/unityLibrary/libs") { include("*.jar") })
```

El host **no** cambia de versiones: sigue en Kotlin 2.0 / AGP 8.7 / compileSdk 35.

---

## 6. Manifest del host

`UnityPlayerActivity` (o `UnityPlayerGameActivity` en Unity 2022+) vive dentro
del módulo `unityLibrary`, así que ya está declarada por el merge de manifests.
No necesitas re-declararla. Solo asegura los permisos que use tu contenido Unity
(vibración, etc.) si aplica.

---

## 7. Lanzar Unity desde tu UI Compose

Opción simple: abrir la Activity de Unity con un `Intent`.

```kotlin
// En un @Composable, con el context:
val context = LocalContext.current
Button(onClick = {
    val intent = Intent(context, com.unity3d.player.UnityPlayerActivity::class.java)
    context.startActivity(intent)
}) { Text("Abrir vista 3D (Unity)") }
```

Para comunicación bidireccional (Android ↔ Unity):
- **Android → Unity:** `UnityPlayer.UnitySendMessage("GameObject", "Metodo", "dato")`.
- **Unity → Android:** desde C# usa `AndroidJavaClass`/`AndroidJavaObject` para
  llamar métodos estáticos de una clase Kotlin/Java de tu app.

---

## 8. Checklist de errores comunes

| Síntoma | Causa / arreglo |
|---|---|
| `Namespace not specified` | Falta `namespace` en `unityLibrary/build.gradle` (§4). |
| `package attribute is deprecated` / falla merge | Quita `package=` del manifest de `unityLibrary` (§4). |
| `FAIL_ON_PROJECT_REPOS` error | Quita bloques `repositories {}` del `build.gradle` del módulo (§3b). |
| `uses-sdk:minSdkVersion 26 cannot be smaller… ` | minSdk del módulo (27) > host (26). Sube el host a `minSdk = 27`. |
| `Build-Tools 30.0.2 not found` | Ejecuta el `sdkmanager` de §0. |
| `NDK not configured` | Instala NDK 21.4 (§0) o comenta `ndkVersion` en el módulo. |
| Duplicados `libunity.so` / `2 files found` | Añade `packaging { jniLibs { pickFirsts += "**/libunity.so" } }` en `app`. |

---

## Resumen de qué toca y qué NO

- ✅ Se añade el módulo `unityLibrary` (compileSdk 30 interno).
- ✅ Se editan `settings.gradle.kts` y `app/build.gradle.kts` (solo para incluir el módulo).
- ✅ Se ajusta `unityLibrary/build.gradle` para AGP 8 (namespace, manifest).
- ✅ Posiblemente `minSdk = 26 → 27` en el host.
- ❌ NO se degrada AGP / Gradle / Kotlin / compileSdk de la app.
- ❌ NO se toca el código Compose existente.
