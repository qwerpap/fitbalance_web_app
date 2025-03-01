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

    Database.connect(
        url = "jdbc:postgresql://localhost:5432/fitbalance",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "salfetka5"
    )

    //place for CRUT
    Users.deleteUser("test_user")

//Users.deleteUser("testik")


    configureRouting()
    configureLoginRouting()
    configureRegisterRouting()
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


