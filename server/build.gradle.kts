plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("application")
}

group = "com.example"
version = "1.0-SNAPSHOT"

dependencies {
    // Ktor Server Core and Engines
    implementation("io.ktor:ktor-server-core:3.0.3")
    implementation("io.ktor:ktor-server-netty:3.0.3")
    implementation("io.ktor:ktor-server-content-negotiation:3.0.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.3")
    implementation("io.ktor:ktor-server-double-receive:3.0.3")

    // Database: PostgreSQL, HikariCP Connection Pooling, and Exposed ORM
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.jetbrains.exposed:exposed-core:0.48.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.48.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.48.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.48.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
}

application {
    mainClass.set("com.example.server.ServerMainKt")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}
