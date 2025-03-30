package com.example.features.calculator

import com.example.features.calculator.data.models.NutritionCalculation
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
            // Получаем данные из запроса
            val userInfo = call.receive<UserInfo>()

            // Выполняем расчёты
            val tdee = calculatorService.calculateTDEE(userInfo)
            val result = calculatorService.calculateMacronutrients(tdee, userInfo.goal)

            // Возвращаем результат (без сохранения в БД)
            call.respond(result)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Ошибка: ${e.message}")
        }
    }
}



//        //CREATE
//        val calculation = NutritionCalculation(
//            id = UUID.randomUUID().toString(), // Генерируем уникальный ID
//            userId = "user123",
//            tdee = 2500.0,
//            protein = 150.0,
//            fat = 80.0,
//            carbs = 300.0,
//            recommendedCalories = 2000.0,
//            date = LocalDateTime.now().toString() // Текущая дата и время
//        )
//        val createdCalculation = NutritionCalculationRepository.create(calculation)
//        println("Created calculation: $createdCalculation")



//        //READ
//        val calculationId = "12345"
//        val calculation = NutritionCalculationRepository.read(calculationId)
//        if (calculation != null) {
//            println("Calculation found: $calculation")
//        } else {
//            println("Calculation not found")
//        }


//        //UPDATE
//        val calculationId = "12345"
//        val existingCalculation = NutritionCalculationRepository.read(calculationId)
//        if (existingCalculation != null) {
//            val updatedCalculation = existingCalculation.copy(tdee = 2600.0) // Обновляем значение tdee
//            val result = NutritionCalculationRepository.update(updatedCalculation)
//            if (result != null) {
//                println("Calculation updated: $result")
//            } else {
//                println("Calculation not found")
//            }
//        } else {
//            println("Calculation not found")
//        }


//        //DELETE
//        val calculationId = "12345"
//        val isDeleted = NutritionCalculationRepository.delete(calculationId)
//        if (isDeleted) {
//            println("Calculation deleted")
//        } else {
//            println("Calculation not found")
//        }

