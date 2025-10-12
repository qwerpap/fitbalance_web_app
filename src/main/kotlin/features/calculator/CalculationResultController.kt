package com.example.features.calculator

import com.example.cacheService
import com.example.features.calculator.data.repositories.CalculationResultRepositoryCached
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class CalculationResultController(private val call: ApplicationCall) {

    private val cacheService = try {
        call.cacheService
    } catch (e: Exception) {
        null
    }

    suspend fun createCalculation() {
        try {
            val calculationDto = call.receive<CalculationResultDTO>()
            val calculation = CalculationResultRepositoryCached.create(calculationDto, cacheService)
            call.respond(HttpStatusCode.Created, calculation)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
        }
    }

    suspend fun getCalculation(id: String) {
        val calculation = CalculationResultRepositoryCached.read(id, cacheService)
        if (calculation != null) {
            call.respond(HttpStatusCode.OK, calculation)
        } else {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Calculation not found"))
        }
    }

    suspend fun updateCalculation() {
        try {
            val calculationDto = call.receive<CalculationResultDTO>()
            val updatedCalculation = CalculationResultRepositoryCached.update(calculationDto, cacheService)
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
        if (CalculationResultRepositoryCached.delete(id, cacheService)) {
            call.respond(HttpStatusCode.OK, mapOf("message" to "Calculation deleted"))
        } else {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Calculation not found"))
        }
    }

    suspend fun getUserCalculations(userId: String) {
        val calculations = CalculationResultRepositoryCached.findByUserId(userId, cacheService)
        call.respond(HttpStatusCode.OK, calculations)
    }
}
