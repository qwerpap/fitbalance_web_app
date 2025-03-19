package com.example.features.calculator

import com.example.features.calculator.data.models.NutritionCalculation
import com.example.features.calculator.data.repositories.NutritionCalculationRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureCalculatorRouting() {
    routing {
        authenticate("auth-jwt") {
            // Маршрут для расчета и сохранения
            post("/calculate") {
                val calculatorController = CalculatorController(call)
                calculatorController.calculateAndSave()
            }

            // CRUD для NutritionCalculation
            route("/calculations") {
                // Создание нового расчета
                post {
                    val calculation = call.receive<NutritionCalculation>()
                    val createdCalculation = NutritionCalculationRepository.create(calculation)
                    call.respond(HttpStatusCode.Created, createdCalculation)
                }

                // Получение расчета по ID
                get("/{id}") {
                    val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing id")
                    val calculation = NutritionCalculationRepository.read(id)
                    if (calculation != null) {
                        call.respond(calculation)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Calculation not found")
                    }
                }

                // Обновление расчета
                put {
                    val calculation = call.receive<NutritionCalculation>()
                    val updatedCalculation = NutritionCalculationRepository.update(calculation)
                    if (updatedCalculation != null) {
                        call.respond(updatedCalculation)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Calculation not found")
                    }
                }

                // Удаление расчета
                delete("/{id}") {
                    val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing id")
                    if (NutritionCalculationRepository.delete(id)) {
                        call.respond(HttpStatusCode.OK, "Calculation deleted")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Calculation not found")
                    }
                }

                // Получение всех расчетов для конкретного пользователя
                get("/user/{userId}") {
                    val userId = call.parameters["userId"] ?: throw IllegalArgumentException("Missing userId")
                    val userCalculations = NutritionCalculationRepository.findByUserId(userId)
                    call.respond(userCalculations)
                }
            }
        }
    }
}