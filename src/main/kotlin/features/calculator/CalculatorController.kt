package com.example.features.calculator

import com.example.features.calculator.data.models.NutritionCalculation
import com.example.features.calculator.data.repositories.CalculationResultRepository
import com.example.features.calculator.data.repositories.NutritionCalculationRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.time.LocalDateTime
import java.util.UUID

class CalculatorController(private val call: ApplicationCall) {
    private val calculatorService = CalculatorService()

    suspend fun calculateAndSave() {
        try {
            // Получаем данные пользователя из тела запроса
            val userInfo = call.receive<UserInfo>()

            // Генерируем случайный ID для сохранения расчета
            val calculationId = UUID.randomUUID().toString()

            // Выполняем расчёты
            val tdee = calculatorService.calculateTDEE(userInfo)
            val result = calculatorService.calculateMacronutrients(tdee, userInfo.goal)

            // Сохраняем результат в БД
            val calculationDto = CalculationResultDTO(
                id = calculationId,
                userId = "anonymous", // или null, если поле nullable
                tdee = result.tdee,
                protein = result.protein,
                fat = result.fat,
                carbs = result.carbs,
                recommendedCalories = result.recommendedCalories
            )

            val savedCalculation = CalculationResultRepository.create(calculationDto)

            // Возвращаем результат
            call.respond(savedCalculation)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
        }
    }

    suspend fun calculateOnly() {
        try {
            val userInfo = call.receive<UserInfo>()
            val tdee = calculatorService.calculateTDEE(userInfo)
            val result = calculatorService.calculateMacronutrients(tdee, userInfo.goal)
            call.respond(result)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
        }
    }
}