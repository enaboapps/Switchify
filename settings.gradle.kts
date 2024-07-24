pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.fleksy.com/maven")
            content {
                includeGroupByRegex("co.thingthing.*")
            }
        }
        google()
    }
}

rootProject.name = "Switchify"
include(":app")