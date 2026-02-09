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
                        localProperties.getProperty("MAPBOX_DOWNLOADS_TOKEN") ?: "MISSING_TOKEN"
                    }
                )
            }
        }
    }
}

rootProject.name = "MentxuApp"
include(":app")
 