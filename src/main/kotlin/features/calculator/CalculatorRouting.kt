package com.example.features.calculator

import com.example.features.calculator.data.repositories.UserInfoRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureCalculatorRouting() {
    routing {
        // Первая бизнес-сущность: user_info
        route("/user-info") {
            // Чтение данных пользователя (READ)
            get {
                UserInfoController(call).getUserInfo()
            }

            // Создание нового пользователя (CREATE)
            post {
                UserInfoController(call).createUserInfo()
            }

            // Удаление данных пользователя (DELETE)
            delete {
                UserInfoController(call).deleteUserInfo()
            }
        }

        // Вторая бизнес-сущность: calculation_result
        route("/calculations") {
            // Создание расчета (CREATE)
            post {
                CalculationResultController(call).createCalculation()
            }

            // Получение расчета по ID (READ)
            get("/{id}") {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing calculation ID")
                CalculationResultController(call).getCalculation(id)
            }

            // Обновление расчета (UPDATE)
            put {
                CalculationResultController(call).updateCalculation()
            }

            // Удаление расчета (DELETE)
            delete("/{id}") {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing calculation ID")
                CalculationResultController(call).deleteCalculation(id)
            }

            // Получение всех расчетов пользователя (LIST)
            get("/user/{userId}") {
                val userId = call.parameters["userId"] ?: throw IllegalArgumentException("Missing user ID")
                CalculationResultController(call).getUserCalculations(userId)
            }
        }

        // Публичный расчет (без аутентификации)
        route("/calculator") {
            // Быстрый расчет без сохранения
            post("/quick") {
                CalculatorController(call).calculateOnly()
            }

            // Расчет с сохранением результата
            post {
                CalculatorController(call).calculateAndSave()
            }
        }
    }
}