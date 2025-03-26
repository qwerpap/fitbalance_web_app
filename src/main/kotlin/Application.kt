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
import com.example.JwtConfig.verifier
import com.example.features.calculator.CalculationResultDTO
import com.example.features.calculator.UserInfoDTO
import com.example.features.calculator.data.repositories.CalculationResultRepository
import com.example.features.calculator.data.repositories.UserInfoRepository
import com.typesafe.config.Config
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.http.content.StaticContentConfig
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.receive
import io.ktor.server.request.uri
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.routing.put
import io.ktor.server.routing.delete
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import org.slf4j.LoggerFactory

fun Application.module() {

    install(Authentication) {
        jwt("auth-jwt") {
            verifier = JwtConfig.verifier
            realm = JwtConfig.realm
            validate { credential ->
                // Проверяем, есть ли claim "login" (или "id")
                if (credential.payload.getClaim("login").asString().isNotEmpty()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
    Database.connect(
        url = "jdbc:postgresql://localhost:5432/fitbalance",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "salfetka5"
    )

    // CRUD для таблицы user_info
    routing {
        // CREATE: Добавление новой записи
        post("/user-info") {
            val userInfoDTO = try {
                call.receive<UserInfoDTO>() // Получаем данные из тела запроса
            } catch (e: Exception) {
                call.respondText("Invalid request body", status = HttpStatusCode.BadRequest)
                return@post
            }

            // Вставляем данные в базу данных
            UserInfoRepository.insert(userInfoDTO)
            call.respondText("User info created successfully", status = HttpStatusCode.Created)
        }

        // UPDATE: Обновление информации о пользователе по ID
        put("/user-info/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing user ID")
            val userInfoDTO = try {
                call.receive<UserInfoDTO>() // Получаем данные из тела запроса
            } catch (e: Exception) {
                call.respondText("Invalid request body", status = HttpStatusCode.BadRequest)
                return@put
            }

            // Проверяем, что ID в URL и теле запроса совпадают
            if (userInfoDTO.id != id) {
                call.respondText("ID in URL and body do not match", status = HttpStatusCode.BadRequest)
                return@put
            }

            // Обновляем данные в базе данных
            UserInfoRepository.update(userInfoDTO)
            call.respondText("User info updated successfully", status = HttpStatusCode.OK)
        }

        // DELETE: Удаление информации о пользователе по ID
        delete("/user-info/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing user ID")

            // Удаляем данные из базы данных
            UserInfoRepository.delete(id)
            call.respondText("User info deleted successfully", status = HttpStatusCode.OK)
        }
    }


    routing {
        // CREATE: Добавление новой записи
        //calculation_result Table
        post("/calculation") {
            val calculationResultDTO = try {
                call.receive<CalculationResultDTO>() // Получаем данные из тела запроса
            } catch (e: Exception) {
                call.respondText("Invalid request body", status = HttpStatusCode.BadRequest)
                return@post
            }

            // Вставляем данные в базу данных
            CalculationResultRepository.insert(calculationResultDTO)
            call.respondText("Calculation result created successfully", status = HttpStatusCode.Created)
        }


        // UPDATE: Обновление записи по ID
        put("/calculation/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing calculation ID")
            val calculationResultDTO = try {
                call.receive<CalculationResultDTO>() // Получаем данные из тела запроса
            } catch (e: Exception) {
                call.respondText("Invalid request body", status = HttpStatusCode.BadRequest)
                return@put
            }

            // Проверяем, что ID в URL и теле запроса совпадают
            if (calculationResultDTO.id != id) {
                call.respondText("ID in URL and body do not match", status = HttpStatusCode.BadRequest)
                return@put
            }

            // Обновляем данные в базе данных
            CalculationResultRepository.update(calculationResultDTO)
            call.respondText("Calculation result updated successfully", status = HttpStatusCode.OK)
        }

        //READDDDD--!!!!!

        // DELETE: Удаление записи по ID++
        delete("/calculation/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing calculation ID")

            // Удаляем данные из базы данных
            CalculationResultRepository.delete(id)
            call.respondText("Calculation result deleted successfully", status = HttpStatusCode.OK)
        }
    }

    install(CallLogging) {
        level = Level.INFO
        format { call ->
            "Request: ${call.request.uri}"
        }
    }


    val log = LoggerFactory.getLogger("Application")

    configureRouting()
    configureLoginRouting()
    configureRegisterRouting()
    configureCalculatorRouting()
    configureSerialization()
}

fun main() {
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


