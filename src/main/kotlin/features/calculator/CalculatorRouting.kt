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
        authenticate("auth-jwt") {
            // UserInfo CRUD endpoints
            route("/user-info") {
                // Create
                post {
                    UserInfoController(call).createUserInfo()
                }

                // Read
                get {
                    UserInfoController(call).getUserInfo()
                }

                // Delete
                delete("/user-info") {
                    val userId = call.request.queryParameters["userId"]

                    if (userId.isNullOrBlank()) {
                        call.respond(HttpStatusCode.BadRequest, "Missing userId parameter")
                        return@delete
                    }

                    val deleted = UserInfoRepository.delete(userId)
                    if (deleted) {
                        call.respond(HttpStatusCode.OK, "User info deleted")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "User info not found")
                    }
                }
            }




            // CalculationResult CRUD endpoints
            route("/calculations") {
                // Create
                post {
                    CalculationResultController(call).createCalculation()
                }

                // Read by ID
                get("/{id}") {
                    val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing id")
                    CalculationResultController(call).getCalculation(id)
                }

                // Update
                put {
                    CalculationResultController(call).updateCalculation()
                }

                // Delete
                delete("/{id}") {
                    val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing id")
                    CalculationResultController(call).deleteCalculation(id)
                }

                // Get all by user ID
                get("/user/{userId}") {
                    val userId = call.parameters["userId"] ?: throw IllegalArgumentException("Missing userId")
                    CalculationResultController(call).getUserCalculations(userId)
                }
            }
        }
    }
}