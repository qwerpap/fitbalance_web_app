package com.example

import com.example.database.users.UserDTO
import com.example.database.users.Users
import com.example.features.login.configureLoginRouting
import com.example.features.register.configureRegisterRouting
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.cio.CIO
import org.jetbrains.exposed.sql.Database

fun Application.module() {
    configureRouting()
    configureLoginRouting()
    configureRegisterRouting()
    configureSerialization()
}


fun main() {

    Database.connect(
        url = "jdbc:postgresql://localhost:5432/fitbalance",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "salfetka5"
    )

//    // Создание и вставка тестового пользователя
//    val user = UserDTO(
//        login = "testik",
//        password = "testik",
//        email = "testik@example.com"
//    )
//    Users.insert(user)
//    println("Пользователь добавлен!")


    // Запуск сервера
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}