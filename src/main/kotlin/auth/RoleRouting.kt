package com.example.auth

import com.example.cacheService
import com.example.database.users.UsersCached
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class ChangeRoleRequest(
    val userId: String,
    val newRole: String
)

fun Application.configureRoleManagement() {
    routing {
        // Защищенный маршрут - только для админов
        authenticate("auth-jwt") {
            post("/admin/change-role") {
                val principal = call.principal<JWTPrincipal>()
                val currentUserRole = principal?.payload?.getClaim("role")?.asString()

                // Проверяем, что текущий пользователь - админ
                if (currentUserRole != "admin") {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied. Admin role required."))
                    return@post
                }

                val request = call.receive<ChangeRoleRequest>()
                
                // Валидация роли
                if (request.newRole !in listOf("user", "admin")) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid role. Must be 'user' or 'admin'"))
                    return@post
                }

                // Получаем cacheService
                val cacheService = try {
                    call.cacheService
                } catch (e: Exception) {
                    null
                }

                // Обновляем роль (с кэшированием)
                val success = UsersCached.updateUserRole(request.userId, request.newRole, cacheService)
                
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Role updated successfully"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                }
            }

            // Получение информации о текущем пользователе
            get("/auth/me") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                val email = principal?.payload?.getClaim("email")?.asString()
                val role = principal?.payload?.getClaim("role")?.asString()

                if (userId != null && email != null && role != null) {
                    call.respond(
                        UserInfo(
                            id = userId,
                            email = email,
                            role = role
                        )
                    )
                } else {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                }
            }
        }
    }
}

