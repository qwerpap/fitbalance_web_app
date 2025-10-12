package com.example.features.admin

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
data class UserListItem(
    val id: String,
    val email: String,
    val role: String,
    val google_id: String?
)

@Serializable
data class UpdateRoleRequest(
    val role: String
)

fun Application.configureAdminRouting() {
    routing {
        authenticate("auth-jwt") {
            // Получить список всех пользователей (только для админов)
            get("/admin/users") {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                
                if (role != "admin") {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied. Admin role required."))
                    return@get
                }
                
                try {
                    val users = UsersCached.fetchAll()
                    val userList = users.map { user ->
                        UserListItem(
                            id = user.id,
                            email = user.email,
                            role = user.role,
                            google_id = user.googleId
                        )
                    }
                    
                    call.respond(userList)
                } catch (e: Exception) {
                    println("Error fetching users: ${e.message}")
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to fetch users"))
                }
            }
            
            // Изменить роль пользователя (только для админов)
            put("/admin/users/{userId}/role") {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                
                if (role != "admin") {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied. Admin role required."))
                    return@put
                }
                
                val userId = call.parameters["userId"]
                if (userId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "User ID is required"))
                    return@put
                }
                
                // Получаем cacheService
                val cacheService = try {
                    call.cacheService
                } catch (e: Exception) {
                    null
                }
                
                try {
                    val updateRequest = call.receive<UpdateRoleRequest>()
                    val newRole = updateRequest.role
                    
                    // Валидация роли
                    if (newRole !in listOf("user", "admin")) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid role. Must be 'user' or 'admin'"))
                        return@put
                    }
                    
                    // Проверяем, что пользователь существует (с кэшированием)
                    val existingUser = UsersCached.fetchUserById(userId, cacheService)
                    if (existingUser == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                        return@put
                    }
                    
                    // Обновляем роль (с кэшированием)
                    val success = UsersCached.updateUserRole(userId, newRole, cacheService)
                    
                    if (success) {
                        call.respond(mapOf("message" to "User role updated successfully", "newRole" to newRole))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                    }
                    
                } catch (e: Exception) {
                    println("Error updating user role: ${e.message}")
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to update user role"))
                }
            }
        }
    }
}

