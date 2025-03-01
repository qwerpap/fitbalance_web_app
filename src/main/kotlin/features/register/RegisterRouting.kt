package com.example.features.register

import com.example.cache.InMemoryCache
import com.example.cache.TokenCache
import com.example.features.login.LoginResponceRemote
import com.example.utils.isValidEmail
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.util.UUID

fun Application.configureRegisterRouting() {
    routing {
        get("/register") {
            val receive = call.receive(RegisterReceiveRemote:: class)
            if (!receive.email.isValidEmail()) {
                call.respond(HttpStatusCode.BadRequest, "Email is not valid")
            }

            if (InMemoryCache.userList.map { it.login }.contains(receive.login)){
                call.respond(HttpStatusCode.Conflict, "User already exists")
            }

            val token = UUID.randomUUID().toString()
            InMemoryCache.userList.add(receive)
            InMemoryCache.token.add(TokenCache(login = receive.login, token = token))

            call.respond(RegisterResponseRemote(token = token))
        }
    }
}
