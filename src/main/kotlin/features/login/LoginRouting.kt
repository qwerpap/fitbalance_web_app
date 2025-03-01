package com.example.features.login
import com.example.cache.InMemoryCache
import com.example.cache.TokenCache
import com.example.features.register.RegisterReceiveRemote
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.util.UUID
import kotlin.uuid.Uuid

fun Application.configureLoginRouting() {
    routing {
        post("/login") {
            val loginController = LoginController(call)
            loginController.performLogin()
        }
    }
}
