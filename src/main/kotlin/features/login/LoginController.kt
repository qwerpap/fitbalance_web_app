package com.example.features.login

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.database.tokens.TokenDTO
import com.example.database.tokens.Tokens
import com.example.database.users.Users
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class LoginController(private val call: ApplicationCall) {

    suspend fun performLogin() {
        val receive = call.receive<LoginReceiveRemote>()
        println("🔹 Login attempt: ${receive.login}") // Логируем попытку входа

        val userDTO = Users.fetchUser(receive.login)

        if (userDTO == null) {
            println(" User not found: ${receive.login}")
            call.respond(HttpStatusCode.BadRequest, "User not found")
            return
        }

        if (userDTO.password != receive.password) {
            println(" Invalid password for user: ${receive.login}")
            call.respond(HttpStatusCode.BadRequest, "Invalid password")
            return
        }

        try {
            val secret = "my-very-secure-secret-key-12345"  // Убеждаемся, что ключ задан
            val token = JWT.create()
                .withAudience("fitbalance")
                .withIssuer("fitbalance_server")
                .withClaim("userId", userDTO.id)  // Добавлено
                .withClaim("login", userDTO.login)
                .withClaim("role", userDTO.role)
                .sign(Algorithm.HMAC256(secret))

            println("Token generated successfully: $token")

            // В методе performLogin(), где создаётся TokenDTO:
            Tokens.insert(
                TokenDTO(
                    rowId = UUID.randomUUID().toString(),
                    userId = userDTO.id, // Добавляем userId из найденного пользователя
                    login = receive.login,
                    token = token
                )
            )

            call.respond(LoginResponceRemote(token = token, role = userDTO.role ?: "user"))
        } catch (e: Exception) {
            println("🔥 ERROR: Failed to generate JWT token: ${e.message}")
            call.respond(HttpStatusCode.InternalServerError, "Internal server error")
        }
    }
}

