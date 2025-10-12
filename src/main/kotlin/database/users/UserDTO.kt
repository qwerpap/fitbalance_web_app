package com.example.database.users

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: String,
    val googleId: String,
    val email: String,
    val login: String? = null,
    val password: String? = null, // Оставляем для обычной авторизации (опционально)
    val role: String = "user" // "user" или "admin"
)
