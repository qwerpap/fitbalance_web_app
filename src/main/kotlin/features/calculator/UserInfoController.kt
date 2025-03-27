package com.example.features.calculator


import com.example.features.calculator.data.repositories.UserInfoRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.util.*

class UserInfoController(private val call: ApplicationCall) {

    suspend fun createUserInfo() {
        try {
            val userInfoDto = call.receive<UserInfoDTO>()

            // Проверяем, передан ли ID пользователя
            if (userInfoDto.id.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Missing user ID")
                return
            }

            val createdUserInfo = UserInfoRepository.create(userInfoDto)
            call.respond(HttpStatusCode.Created, createdUserInfo)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
        }
    }

    suspend fun getUserInfo() {
        val userId = call.request.queryParameters["userId"]

        if (userId.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf(
                "error" to "Missing userId parameter",
                "example" to "/user-info?userId=some-unique-id"
            ))
            return
        }

        try {
            val userInfo = UserInfoRepository.read(userId)
            if (userInfo != null) {
                call.respond(userInfo)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Database error", "details" to e.message))
        }
    }

    suspend fun deleteUserInfo() {
        val userId = call.request.queryParameters["userId"]

        if (userId.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Missing userId parameter")
            return
        }

        try {
            val deleted = UserInfoRepository.delete(userId)
            if (deleted) {
                call.respond(HttpStatusCode.OK, "User info deleted")
            } else {
                call.respond(HttpStatusCode.NotFound, "User info not found")
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error occurred: ${e.message}")
            e.printStackTrace()  // Вывод ошибки в лог
        }
    }


}