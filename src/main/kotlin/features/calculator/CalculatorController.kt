package com.example.features.calculator

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.*
import io.ktor.server.response.*

class CalculatorController(private val call: ApplicationCall) {

    private val calculatorService = CalculatorService()

    suspend fun calculate() {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.payload?.getClaim("role")?.asString()

        if (role == "user") {
            val userInfo = call.receive<UserInfo>()
            val tdee = calculatorService.calculateTDEE(userInfo)
            val result = calculatorService.calculateMacronutrients(tdee, userInfo.goal)
            call.respond(result)
        } else {
            call.respond(HttpStatusCode.Forbidden, "Access denied")
        }
    }
}