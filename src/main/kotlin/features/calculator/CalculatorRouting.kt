package com.example.features.calculator

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureCalculatorRouting() {
    routing {
        authenticate("auth-jwt") {
            get("/calculator") {
                val principal = call.principal<JWTPrincipal>()
                val login = principal?.payload?.getClaim("login")?.asString()
                val role = principal?.payload?.getClaim("role")?.asString()

                if (role == "user") {
                    // Здесь можно вернуть HTML-страницу с калькулятором
                    call.respondText("Welcome to the calculator, $login!")
                } else {
                    call.respond(HttpStatusCode.Forbidden, "Access denied")
                }
            }
        }
    }
}