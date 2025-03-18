package com.example

import com.example.database.users.UserDTO
import com.example.database.users.Users
import com.example.features.calculator.configureCalculatorRouting
import com.example.features.login.configureLoginRouting
import com.example.features.register.configureRegisterRouting
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.cio.CIO
import org.jetbrains.exposed.sql.Database
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.Config
import io.ktor.server.http.content.StaticContentConfig
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.uri
import org.slf4j.event.Level
import org.slf4j.LoggerFactory

fun Application.module() {

    Database.connect(
        url = "jdbc:postgresql://localhost:5432/fitbalance",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "salfetka5"
    )

    //place for CRUT

    install(CallLogging) {
        level = Level.INFO // Выберите уровень логирования: TRACE, DEBUG, INFO, WARN, ERROR
        format { call ->
            "Request: ${call.request.uri}"
        }
    }



    val log = LoggerFactory.getLogger("Application")

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "fitbalance"
            verifier(JWT.require(Algorithm.HMAC256("my-very-secure-secret-key-12345")).build())
            validate { credential ->
                if (credential.payload.getClaim("login").asString() != null) {
                    log.info("JWT validation successful: ${credential.payload}")
                    JWTPrincipal(credential.payload)
                } else {
                    log.warn("JWT validation failed: Invalid token or missing claims")
                    null
                }
            }
        }
    }


    configureRouting()
    configureLoginRouting()
    configureRegisterRouting()
    configureCalculatorRouting()
    configureSerialization()
}


fun main() {
    // Запуск сервера
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}



//    // Создание и вставка пользователя
//    val user = UserDTO(
//        login = "testik",
//        password = "testik",
//        email = "testik@example.com"
//    )
//    Users.insert(user)




//    // обновляет данные пользователя по логину
//    val updatedUser = UserDTO(
//        login = "existingLogin",
//        password = "newPassword",
//        email = "newEmail@example.com"
//    )
//    Users.updateUser("existingLogin", updatedUser)




//    // удаляет пользователя по логину
//fun deleteUser(login: String) {
//    transaction {
//        Users.deleteWhere { Users.login eq login }
//    }
//}

//Users.deleteUser("testik")


