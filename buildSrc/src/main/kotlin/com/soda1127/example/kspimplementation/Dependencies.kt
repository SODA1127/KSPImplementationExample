package com.soda1127.example.kspimplementation

object Dependencies {

    const val androidGradleVersion = "4.2.2"
    const val minSdk = 21
    const val compileSdk = 30
    const val targetSdk = 30
    const val buildToolVersion = "30.0.3"

    const val kotlinVersion = "1.5.20"
    const val kspVersion = "1.5.20-1.0.0-beta04"

    object ClassPath {
        const val androidGradle = "com.android.tools.build:gradle:$androidGradleVersion"
        const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
        const val ksp = "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:$kspVersion"
    }

    object Kotlin {
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
        const val poet = "com.squareup:kotlinpoet:1.7.2"
    }

    object AndroidX {
        const val coreKtx = "androidx.core:core-ktx:1.6.0"
        const val appCompat = "androidx.appcompat:appcompat:1.3.0"
        const val material = "com.google.android.material:material:1.3.0"
        const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.0.4"
    }

    object Ksp {
        const val symbolProcessingApi = "com.google.devtools.ksp:symbol-processing-api:$kspVersion"
    }

    object Test {
        const val junit4 = "junit:junit:4.+"
    }

}
