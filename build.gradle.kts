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
    // Minestom
    implementation("net.minestom:minestom:2025.12.20-1.21.11")
    implementation("org.slf4j:slf4j-simple:2.0.12")

    // MongoDB 및 Redis
    implementation(platform("org.mongodb:mongodb-driver-bom:5.6.2"))
    implementation("org.mongodb:mongodb-driver-sync")
    implementation("io.lettuce:lettuce-core:6.7.1.RELEASE")
    implementation("org.yaml:snakeyaml:2.2")

    // Lamp
    implementation("io.github.revxrsal:lamp.common:4.0.0-rc.14")
    implementation("io.github.revxrsal:lamp.minestom:4.0.0-rc.14")

    // WorldSeed
    implementation("net.worldseed.multipart:WorldSeedEntityEngine:11.5.6")

    // dotenv
    implementation("io.github.cdimascio:dotenv-java:3.2.0")

    // Micrometer 코어 및 프로메테우스 연동 모듈
    implementation("io.micrometer:micrometer-core:1.12.0")
    implementation("io.micrometer:micrometer-registry-prometheus:1.12.0")
}

configurations.all {
    resolutionStrategy {
        // 모든 Netty 관련 라이브러리를 최신 버전으로 강제 고정
        // (경고에 나온 4.1.118은 취약하므로 그 다음 버전 사용)
        eachDependency {
            if (requested.group == "io.netty") {
                useVersion("4.2.9.Final") // 혹은 최신 버전인 "4.1.120.Final" 등
            }
        }
    }
}

application {
    mainClass.set("org.example.Main")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}