val ktor_version: String by project
val exposed_version: String by project

plugins {
    application
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor Core
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation("io.ktor:ktor-server-cio:$ktor_version")
    implementation(libs.ktor.server.config.yaml)
    
    // Content Negotiation & Serialization
    implementation(libs.ktor.server.content.negotiation)
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    
    // Authentication (OAuth + JWT)
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("com.auth0:java-jwt:4.4.0")
    
    // Database
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.postgresql:postgresql:42.7.3")
    
    // HTML Builder
    implementation("io.ktor:ktor-server-html-builder:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.12.0")
    
    // Logging
    implementation("io.ktor:ktor-server-call-logging:$ktor_version")
    implementation(libs.logback.classic)
    implementation("org.slf4j:slf4j-api:2.0.9")
    
    // Redis Cache
    implementation("io.lettuce:lettuce-core:6.3.0.RELEASE")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    
    // RabbitMQ Message Queue
    implementation("com.rabbitmq:amqp-client:5.20.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    
    // Testing
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}
