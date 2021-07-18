// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val kotlin_version by extra("1.5.20")
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(com.soda1127.example.kspimplementation.Dependencies.ClassPath.androidGradle)
        classpath(com.soda1127.example.kspimplementation.Dependencies.ClassPath.kotlin)
        classpath(com.soda1127.example.kspimplementation.Dependencies.ClassPath.ksp)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
    }
}

allprojects {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
