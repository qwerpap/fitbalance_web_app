package com.example.database.users

//data transfer object
data class UserDTO(
    val id: String,  // Убрали "?" - теперь это non-null тип
    val login: String,
    val password: String,
    val email: String? = null,
    val role: String? = "user"
)