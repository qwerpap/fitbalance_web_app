package com.example.features.login

import kotlinx.serialization.Serializable


//Входные данные
@Serializable
data class LoginReceiveRemote(
    val login: String,
    val password: String
)


@Serializable
data class LoginResponceRemote(
    val token: String,
    val role: String
)