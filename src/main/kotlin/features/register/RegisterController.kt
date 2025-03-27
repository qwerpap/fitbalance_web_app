package com.example.features.register

import com.example.database.tokens.TokenDTO
import com.example.database.tokens.Tokens
import com.example.database.users.UserDTO
import com.example.database.users.Users
import com.example.utils.isValidEmail
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.util.UUID

class RegisterController(private val call: ApplicationCall) {

    suspend fun registerNewUser() {
        val registerReceiveRemote = call.receive<RegisterReceiveRemote>()

        if (!registerReceiveRemote.email.isValidEmail()) {
            call.respond(HttpStatusCode.BadRequest, "Email is not valid")
            return
        }

        val userDTO = Users.fetchUser(registerReceiveRemote.login)

        if (userDTO != null) {
            call.respond(HttpStatusCode.Conflict, "User already exists")
            return
        }

        try {
            // Генерируем ID для нового пользователя
            val userId = UUID.randomUUID().toString()
            val token = UUID.randomUUID().toString()

            Users.insert(
                UserDTO(
                    id = userId, // Добавляем сгенерированный ID
                    login = registerReceiveRemote.login,
                    password = registerReceiveRemote.password,
                    email = registerReceiveRemote.email
                )
            )

            Tokens.insert(
                TokenDTO(
                    rowId = UUID.randomUUID().toString(),
                    userId = userId, // Используем тот же userId
                    login = registerReceiveRemote.login,
                    token = token
                )
            )

            call.respond(RegisterResponseRemote(token = token))
        } catch (e: ExposedSQLException) {
            call.respond(HttpStatusCode.Conflict, "User already exists")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Registration failed")
        }
    }
}