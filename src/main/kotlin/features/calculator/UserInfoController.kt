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
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.payload?.getClaim("userId")?.asString()

        if (userId != null) {
            val userInfoDto = call.receive<UserInfoDTO>()
            val userInfo = UserInfoRepository.create(userInfoDto.copy(id = userId))
            call.respond(HttpStatusCode.Created, userInfo)
        } else {
            call.respond(HttpStatusCode.Forbidden, "Access denied")
        }
    }

    suspend fun getUserInfo() {
        val userId = call.request.queryParameters["userId"].takeIf { !it.isNullOrBlank() }
            ?: return call.respond(HttpStatusCode.BadRequest, mapOf(
                "error" to "Missing userId parameter",
                "example" to "/user-info?userId=a8a2880a-8e3c-4234-a986-49afc40df3a5"
            ))

        try {
            val userInfo = UserInfoRepository.read(userId)
                ?: return call.respond(HttpStatusCode.NotFound, mapOf(
                    "error" to "User not found",
                    "userId" to userId
                ))

            call.respond(userInfo)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "Database error",
                "details" to e.message
            ))
        }
    }

    suspend fun updateUserInfo() {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.payload?.getClaim("userId")?.asString()

        if (userId != null) {
            val userInfoDto = call.receive<UserInfoDTO>()
            val updatedUserInfo = UserInfoRepository.update(userInfoDto.copy(id = userId))
            if (updatedUserInfo != null) {
                call.respond(updatedUserInfo)
            } else {
                call.respond(HttpStatusCode.NotFound, "User info not found")
            }
        } else {
            call.respond(HttpStatusCode.Forbidden, "Access denied")
        }
    }

    suspend fun deleteUserInfo() {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.payload?.getClaim("userId")?.asString()

        if (userId != null) {
            if (UserInfoRepository.delete(userId)) {
                call.respond(HttpStatusCode.OK, "User info deleted")
            } else {
                call.respond(HttpStatusCode.NotFound, "User info not found")
            }
        } else {
            call.respond(HttpStatusCode.Forbidden, "Access denied")
        }
    }
}