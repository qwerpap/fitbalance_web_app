package com.example

import com.example.features.calculator.configureCalculatorRouting
import com.example.features.login.configureLoginRouting
import com.example.features.register.configureRegisterRouting
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.cio.CIO
import org.jetbrains.exposed.sql.Database
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.uri
import org.slf4j.event.Level

fun Application.module() {
    // Content Negotiation
    configureSerialization()
    
    // Database Connection
    val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/fitbalance"
    val dbUser = System.getenv("DB_USER") ?: "postgres"
    val dbPassword = System.getenv("DB_PASSWORD") ?: "salfetka5"
    
    Database.connect(
        url = dbUrl,
        driver = "org.postgresql.Driver",
        user = dbUser,
        password = dbPassword
    )
    
    // Call Logging
    install(CallLogging) {
        level = Level.INFO
        format { call ->
            "Request: ${call.request.uri}"
        }
    }
    
    // Configure Routing
    configureRouting()
    configureLoginRouting()
    configureRegisterRouting()
    configureCalculatorRouting()
}

fun main() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

