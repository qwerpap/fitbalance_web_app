package com.example.features.calculator

import com.example.features.calculator.data.repositories.CalculationResultRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class CalculationResultController(private val call: ApplicationCall) {

    suspend fun createCalculation() {
        try {
            val calculationDto = call.receive<CalculationResultDTO>()
            val calculation = CalculationResultRepository.create(calculationDto)
            call.respond(HttpStatusCode.Created, calculation)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
        }
    }

    suspend fun getCalculation(id: String) {
        val calculation = CalculationResultRepository.read(id)
        if (calculation != null) {
            call.respond(HttpStatusCode.OK, calculation)
        } else {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Calculation not found"))
        }
    }

    suspend fun updateCalculation() {
        try {
            val calculationDto = call.receive<CalculationResultDTO>()
            val updatedCalculation = CalculationResultRepository.update(calculationDto)
            if (updatedCalculation != null) {
                call.respond(HttpStatusCode.OK, updatedCalculation)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Calculation not found"))
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
        }
    }

    suspend fun deleteCalculation(id: String) {
        if (CalculationResultRepository.delete(id)) {
            call.respond(HttpStatusCode.OK, mapOf("message" to "Calculation deleted"))
        } else {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Calculation not found"))
        }
    }

    suspend fun getUserCalculations(userId: String) {
        val calculations = CalculationResultRepository.findByUserId(userId)
        call.respond(HttpStatusCode.OK, calculations)
    }
}
