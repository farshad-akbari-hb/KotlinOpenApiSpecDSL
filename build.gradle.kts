plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
}

group = "me.farshad"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    // Kotlin standard library (included automatically with Kotlin 2.x)
    implementation(kotlin("stdlib"))
    // Kotlinx Serialization for JSON (latest stable version)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // KAML for YAML support with kotlinx.serialization (latest version)
    implementation("com.charleskorn.kaml:kaml:0.67.0")
    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}