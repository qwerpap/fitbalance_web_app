package com.example

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable


fun Application.configureRouting() {
    routing {
        get("/login") {
            call.respond(Test(text = "Hello ktor"))
        }
    }
}
