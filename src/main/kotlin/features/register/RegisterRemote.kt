package com.example.features.register

import kotlinx.serialization.Serializable


//Входные данные
@Serializable
data class RegisterReceiveRemote(
    val login: String,
    val email: String,
    val password: String
)


@Serializable
data class RegisterResponseRemote(
    val token: String
)
