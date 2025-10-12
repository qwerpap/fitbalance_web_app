package com.example.features.calculator

import com.example.cacheService
import com.example.features.calculator.data.repositories.CalculationResultRepositoryCached
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.util.UUID

class CalculatorController(private val call: ApplicationCall) {
    private val calculatorService = CalculatorService()
    
    private val cacheService = try {
        call.cacheService
    } catch (e: Exception) {
        null
    }

    suspend fun calculateAndSave() {
        try {
            val userInfo = call.receive<UserInfo>()
            val calculationId = UUID.randomUUID().toString()

            val tdee = calculatorService.calculateTDEE(userInfo)
            val result = calculatorService.calculateMacronutrients(tdee, userInfo.goal)

            val calculationDto = CalculationResultDTO(
                id = calculationId,
                userId = userInfo.userId ?: "anonymous",
                tdee = result.tdee,
                protein = result.protein,
                fat = result.fat,
                carbs = result.carbs,
                recommendedCalories = result.recommendedCalories
            )

            // Используем кэшированную версию
            val savedCalculation = CalculationResultRepositoryCached.create(calculationDto, cacheService)
            call.respond(HttpStatusCode.Created, savedCalculation)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
        }
    }

    suspend fun calculateOnly() {
        try {
            val userInfo = call.receive<UserInfo>()
            val tdee = calculatorService.calculateTDEE(userInfo)
            val result = calculatorService.calculateMacronutrients(tdee, userInfo.goal)
            call.respond(HttpStatusCode.OK, result)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
        }
    }
}
