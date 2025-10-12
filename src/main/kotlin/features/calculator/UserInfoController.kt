package com.example.features.calculator

import com.example.features.calculator.data.repositories.UserInfoRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class UserInfoController(private val call: ApplicationCall) {

    suspend fun createUserInfo() {
        try {
            val userInfoDto = call.receive<UserInfoDTO>()

            if (userInfoDto.id.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing user ID"))
                return
            }

            val createdUserInfo = UserInfoRepository.create(userInfoDto)
            call.respond(HttpStatusCode.Created, createdUserInfo)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request: ${e.message}"))
        }
    }

    suspend fun getUserInfo() {
        val userId = call.request.queryParameters["userId"]

        if (userId.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(
                    "error" to "Missing userId parameter",
                    "example" to "/user-info?userId=some-unique-id"
                )
            )
            return
        }

        try {
            val userInfo = UserInfoRepository.read(userId)
            if (userInfo != null) {
                call.respond(HttpStatusCode.OK, userInfo)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
            }
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Database error", "details" to e.message)
            )
        }
    }

    suspend fun deleteUserInfo() {
        val userId = call.request.queryParameters["userId"]

        if (userId.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing userId parameter"))
            return
        }

        try {
            val deleted = UserInfoRepository.delete(userId)
            if (deleted) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "User info deleted"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "User info not found"))
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
        }
    }
}
