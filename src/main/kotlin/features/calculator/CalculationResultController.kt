package com.example.features.calculator

import com.example.features.calculator.data.repositories.CalculationResultRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.util.*

class CalculationResultController(private val call: ApplicationCall) {

    suspend fun createCalculation() {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.payload?.getClaim("userId")?.asString()

        if (userId != null) {
            val calculationDto = call.receive<CalculationResultDTO>()
            val calculation = CalculationResultRepository.create(calculationDto.copy(userId = userId))
            call.respond(HttpStatusCode.Created, calculation)
        } else {
            call.respond(HttpStatusCode.Forbidden, "Access denied")
        }
    }

    suspend fun getCalculation(id: String) {
        val calculation = CalculationResultRepository.read(id)
        if (calculation != null) {
            call.respond(calculation)
        } else {
            call.respond(HttpStatusCode.NotFound, "Calculation not found")
        }
    }

    suspend fun updateCalculation() {
        val calculationDto = call.receive<CalculationResultDTO>()
        val updatedCalculation = CalculationResultRepository.update(calculationDto)
        if (updatedCalculation != null) {
            call.respond(updatedCalculation)
        } else {
            call.respond(HttpStatusCode.NotFound, "Calculation not found")
        }
    }

    suspend fun deleteCalculation(id: String) {
        if (CalculationResultRepository.delete(id)) {
            call.respond(HttpStatusCode.OK, "Calculation deleted")
        } else {
            call.respond(HttpStatusCode.NotFound, "Calculation not found")
        }
    }

    suspend fun getUserCalculations(userId: String) {
        val calculations = CalculationResultRepository.findByUserId(userId)
        call.respond(calculations)
    }
}