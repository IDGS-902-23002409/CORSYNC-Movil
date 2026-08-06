// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // 2.51.1 traía una copia de XProcessing que no entendía las proyecciones
    // estrella de KSP moderno y reventaba al validar los @Binds que genera
    // @HiltViewModel. Debe ir alineada con `hilt` en libs.versions.toml.
    id("com.google.dagger.hilt.android") version "2.56.2" apply false
    // El prefijo de KSP debe seguir la versión de Kotlin (2.2.10). Estaba en
    // 2.0.21-*, y ese desfase rompía kspDebugKotlin con "KSTypeArgument.type
    // should not have been null ... STAR null".
    id("com.google.devtools.ksp") version "2.2.0-2.0.2" apply false
}