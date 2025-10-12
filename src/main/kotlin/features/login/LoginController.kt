package com.example.features.login

import com.example.database.users.Users
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond

class LoginController(private val call: ApplicationCall) {

    suspend fun performLogin() {
        val request = call.receive<LoginReceiveRemote>()
        
        val user = Users.fetchUser(request.login)
        
        if (user == null) {
            call.respond(HttpStatusCode.NotFound, "User not found")
            return
        }
        
        if (user.password != request.password) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid password")
            return
        }
        
        call.respond(
            HttpStatusCode.OK,
            LoginResponseRemote(
                id = user.id,
                login = user.login,
                email = user.email,
                role = user.role
            )
        )
    }
}
