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
    }
}

rootProject.name = "CGLabs"
include(":app")
include(":core")
include(":lab1")
include(":lab2")
include(":lab3")
include(":lab4")
include(":libs:ui")
include(":libs:utils")

// Указываем новое расположение модулей
project(":core").projectDir = file("modules/core")
project(":lab1").projectDir = file("modules/lab1")
project(":lab2").projectDir = file("modules/lab2")
project(":lab3").projectDir = file("modules/lab3")
project(":lab4").projectDir = file("modules/lab4")
project(":libs:ui").projectDir = file("modules/libs/ui")
project(":libs:utils").projectDir = file("modules/libs/utils")
