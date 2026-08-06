pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Repositorio para Paho MQTT
        maven { url = uri("https://repo.eclipse.org/content/repositories/paho-releases/") }
        flatDir { dirs("${rootProject.projectDir}/unityLibrary/libs") }
    }
}

rootProject.name = "Sakura"
include(":app")
include(":unityLibrary")
 