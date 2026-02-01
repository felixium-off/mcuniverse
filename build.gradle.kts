plugins {
    java
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.minestom:minestom:2025.12.20-1.21.11")
    implementation("org.slf4j:slf4j-simple:2.0.12")
    implementation("io.github.revxrsal:lamp.common:4.0.0-rc.14")
    implementation("io.github.revxrsal:lamp.minestom:4.0.0-rc.14")
    implementation("net.worldseed.multipart:WorldSeedEntityEngine:11.5.6")
}

application {
    mainClass.set("org.example.Main")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}