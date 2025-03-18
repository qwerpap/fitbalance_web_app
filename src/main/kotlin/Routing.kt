package com.example

import com.auth0.jwt.JWT
import com.example.database.tokens.TokenDTO
import com.example.database.tokens.Tokens
import com.example.database.users.Users
import com.example.features.login.LoginReceiveRemote
import com.example.features.login.LoginResponceRemote
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.html.respondHtml
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import io.ktor.server.html.*
import io.ktor.server.request.receive
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.util.UUID
import com.auth0.jwt.algorithms.Algorithm


fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Welcome to the main page!")
        }

        // Страница регистрации
        get("/register") {
            call.respondHtml {
                head {
                    title { +"Register" }
                    script {
                        unsafe {
                            +"""
                            document.addEventListener('DOMContentLoaded', function() {
                                const form = document.getElementById('registerForm');
                                form.addEventListener('submit', async function(event) {
                                    event.preventDefault();

                                    const login = form.querySelector('input[name="login"]').value;
                                    const password = form.querySelector('input[name="password"]').value;
                                    const email = form.querySelector('input[name="email"]').value;

                                    const response = await fetch('/register', {
                                        method: 'POST',
                                        body: JSON.stringify({ login, password, email }),
                                        headers: { 'Content-Type': 'application/json' }
                                    });

                                    if (response.ok) {
                                        alert('Registration successful!');
                                        window.location.href = '/login';
                                    } else {
                                        const errorText = await response.text();
                                        alert('Registration failed: ' + errorText);
                                    }
                                });
                            });
                            """
                        }
                    }
                }
                body {
                    h1 { +"Register" }
                    form {
                        attributes["id"] = "registerForm"
                        label { +"Login: " }
                        input(type = InputType.text, name = "login")
                        br
                        label { +"Password: " }
                        input(type = InputType.password, name = "password")
                        br
                        label { +"Email: " }
                        input(type = InputType.email, name = "email")
                        br
                        button(type = ButtonType.submit) { +"Register" }
                    }
                    p { +"Already have an account? " }
                    a(href = "/login") { +"Login here" }
                }
            }
        }

        // Страница логина
        get("/login") {
            call.respondHtml {
                head {
                    title { +"Login" }
                    script {
                        unsafe {
                            +"""
                            document.addEventListener('DOMContentLoaded', function() {
                                const form = document.getElementById('loginForm');
                                form.addEventListener('submit', async function(event) {
                                    event.preventDefault();

                                    const login = form.querySelector('input[name="login"]').value;
                                    const password = form.querySelector('input[name="password"]').value;

                                    const response = await fetch('/login', {
                                        method: 'POST',
                                        body: JSON.stringify({ login, password }),
                                        headers: { 'Content-Type': 'application/json' }
                                    });

                                    if (response.ok) {
                                        const result = await response.json();
                                        alert('Login successful!');
                                        localStorage.setItem('token', result.token);
                                        window.location.href = '/welcome';
                                    } else {
                                        const errorText = await response.text();
                                        alert('Login failed: ' + errorText);
                                    }
                                });
                            });
                            """
                        }
                    }
                }
                body {
                    h1 { +"Login" }
                    form {
                        attributes["id"] = "loginForm"
                        label { +"Login: " }
                        input(type = InputType.text, name = "login")
                        br
                        label { +"Password: " }
                        input(type = InputType.password, name = "password")
                        br
                        button(type = ButtonType.submit) { +"Login" }
                    }
                    p { +"Don't have an account? " }
                    a(href = "/register") { +"Register here" }
                }
            }
        }

        // Обработка POST-запроса для логина
        post("/login") {
            val receive = try {
                call.receive<LoginReceiveRemote>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request body")
                return@post
            }

            // Ищем пользователя по логину
            val userDTO = Users.fetchUser(receive.login)

            if (userDTO == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
                return@post
            }

            // Проверка пароля
            if (userDTO.password != receive.password) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid password")
                return@post
            }

            // Генерация JWT токена
            val token = JWT.create()
                .withAudience("fitbalance")
                .withIssuer("fitbalance_server")
                .withClaim("login", userDTO.login)
                .withClaim("role", userDTO.role ?: "user")
                .sign(Algorithm.HMAC256("my-very-secure-secret-key-12345"))


            Tokens.insert(
                TokenDTO(
                    rowId = UUID.randomUUID().toString(),
                    login = receive.login,
                    token = token
                )
            )

            // Ответ с токеном и ролью
            call.respond(LoginResponceRemote(token = token, role = userDTO.role ?: "user"))
        }

        // Защищенный маршрут для /welcome
        authenticate("auth-jwt") {
            get("/welcome") {
                val principal = call.principal<JWTPrincipal>()
                if (principal != null) {
                    val login = principal.payload.getClaim("login").asString()
                    call.respondHtml {
                        head { title { +"Welcome" } }
                        body {
                            h1 { +"Welcome, $login!" }
                            p { +"You are successfully logged in." }
                            a(href = "/") { +"Go to main page" }
                        }
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Token is invalid or missing")
                }
            }
        }
    }
}
