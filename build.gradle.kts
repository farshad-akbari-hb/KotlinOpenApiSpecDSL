plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
    `maven-publish`
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

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            groupId = project.group.toString()
            artifactId = "kotlin-openapi-spec-dsl"
            version = project.version.toString()
            
            pom {
                name.set("Kotlin OpenAPI Spec DSL")
                description.set("A type-safe Kotlin DSL for generating OpenAPI 3.1.0 specifications")
                
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }
                
                developers {
                    developer {
                        id.set("farshad")
                        name.set("Farshad Akbari")
                    }
                }
            }
        }
    }
    
    repositories {
        mavenLocal()
    }
}