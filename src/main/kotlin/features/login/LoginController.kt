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

class LoginController (private val call: ApplicationCall) {


    suspend fun performLogin() {
        val receive = call.receive<LoginReceiveRemote>()
        val userDTO = Users.fetchUser(receive.login)

        if (userDTO == null) {
            call.respond(HttpStatusCode.BadRequest, "fun performLogin: User not found")
        } else if (userDTO.password == receive.password) {
            val token = UUID.randomUUID().toString()
            Tokens.insert(
                TokenDTO(
                    rowId = UUID.randomUUID().toString(),
                    login = receive.login,
                    token = token
                )
            )
            call.respond(LoginResponceRemote(token = token, role = userDTO.role ?: "user"))
        } else {
            call.respond(HttpStatusCode.BadRequest, "Invalid password")
        }
    }
}


//suspend fun performLogin() {
//    val receive = call.receive<LoginReceiveRemote>()
//    val userDTO = Users.fetchUser(receive.login)
//
//    if (userDTO == null) {
//        call.respond(HttpStatusCode.BadRequest, "User not found")
//    } else if (userDTO.password == receive.password) {
//        val token = JWT.create()
//            .withAudience("fitbalance") // Аудитория
//            .withIssuer("fitbalance_server") // Издатель
//            .withClaim("login", userDTO.login) // Добавляем claim с логином
//            .withClaim("role", userDTO.role) // Добавляем claim с ролью
//            .sign(Algorithm.HMAC256("my-very-secure-secret-key-12345")) // Подписываем токен
//
//        Tokens.insert(
//            TokenDTO(
//                rowId = UUID.randomUUID().toString(),
//                login = receive.login,
//                token = token
//            )
//        )
//
//        call.respond(LoginResponceRemote(token = token, role = userDTO.role ?: "user"))
//    } else {
//        call.respond(HttpStatusCode.BadRequest, "Invalid password")
//    }