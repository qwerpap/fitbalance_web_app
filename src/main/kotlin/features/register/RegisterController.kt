package com.example.features.register

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
        val request = call.receive<RegisterReceiveRemote>()
        
        if (!request.email.isValidEmail()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid email format")
            return
        }
        
        val existingUser = Users.fetchUser(request.login)
        if (existingUser != null) {
            call.respond(HttpStatusCode.Conflict, "User already exists")
            return
        }
        
        try {
            val userId = UUID.randomUUID().toString()
            
            Users.insert(
                UserDTO(
                    id = userId,
                    googleId = "", // Для обычной регистрации googleId пустой
                    email = request.email,
                    login = request.login,
                    password = request.password,
                    role = "user"
                )
            )
            
            call.respond(
                HttpStatusCode.Created,
                RegisterResponseRemote(
                    id = userId,
                    login = request.login,
                    email = request.email
                )
            )
        } catch (e: ExposedSQLException) {
            call.respond(HttpStatusCode.Conflict, "User already exists")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Registration failed: ${e.message}")
        }
    }
}
