package com.example

import com.example.features.login.configureLoginRouting
import com.example.features.register.configureRegisterRouting
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureRouting()
    configureLoginRouting()
    configureRegisterRouting()
    configureSerialization()
}
