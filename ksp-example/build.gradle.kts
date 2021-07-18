import com.soda1127.example.kspimplementation.Dependencies

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(Dependencies.Kotlin.stdlib)
    implementation(Dependencies.Ksp.symbolProcessingApi)

    implementation(Dependencies.Kotlin.poet)
}
