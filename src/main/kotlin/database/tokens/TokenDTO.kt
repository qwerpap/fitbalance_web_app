package com.example.database.tokens

import kotlinx.serialization.Serializable

@Serializable
data class TokenDTO(
    val rowId: String,
    val userId: String,
    val login: String,
    val token: String
)