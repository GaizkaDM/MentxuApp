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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                // Do not change the username below. It should always be "mapbox" (not your username).
                username = "mapbox"
                // Use the secret token you stored in gradle.properties or local.properties as the password
                password = providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN").getOrElse(
                    run {
                        // Fallback: Try reading from local.properties file manually
                        val localProperties = java.util.Properties()
                        val localPropertiesFile = File(rootDir, "local.properties")
                        if (localPropertiesFile.exists()) {
                            localPropertiesFile.inputStream().use { stream ->
                                localProperties.load(stream)
                            }
                        }
                        // Limpiar espacios en blanco y obtener token
                        val token = localProperties.getProperty("MAPBOX_DOWNLOADS_TOKEN")?.trim() ?: "MISSING_TOKEN"
                        
                        // DIAGNÓSTICO PARA COMPAÑEROS
                        if (token == "MISSING_TOKEN" || token.isEmpty()) {
                            println("❌ ERROR CRÍTICO: No se encuentra MAPBOX_DOWNLOADS_TOKEN en local.properties ni gradle.properties")
                        } else if (!token.startsWith("sk.")) {
                            println("⚠️ ADVERTENCIA: El token parece incorrecto (debe empezar por 'sk.'). Valor actual: ${token.take(5)}...")
                        } else {
                            println("✅ TOKEN DETECTADO CORRECTAMENTE: ${token.take(10)}...")
                        }
                        
                        token
                    }
                )
            }
        }
    }
}

rootProject.name = "MentxuApp"
include(":app")
 