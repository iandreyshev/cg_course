plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.2.0"
}

android {
    namespace = "ru.iandreyshev.cglabs"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.iandreyshev.cglabs"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":lab1"))
    implementation(project(":lab2"))
    implementation(project(":lab3"))
    implementation(project(":lab4"))

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.navigation.compose)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}