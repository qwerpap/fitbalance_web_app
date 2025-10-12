package com.example

import com.example.database.users.Users
import com.example.features.login.LoginReceiveRemote
import com.example.features.login.LoginResponseRemote
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.html.respondHtml
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import io.ktor.server.request.receive
import com.example.features.calculator.CalculatorService
import com.example.features.calculator.UserInfo
import com.example.features.calculator.UserInfoDTO
import com.example.features.calculator.data.repositories.UserInfoRepository
import com.example.features.calculator.CalculationResultDTO
import com.example.features.calculator.data.repositories.CalculationResultRepositoryCached
import java.util.UUID


fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Welcome to the main page!")
        }
        
        get("/test") {
            call.respondText("Test route works!")
        }
        
        get("/google-url") {
            val clientId = "275252554843-rcm6g06uujc25t64nlmof78kvjqokbtb.apps.googleusercontent.com"
            val redirectUri = "http://localhost:8080/auth/google/callback"
            val googleAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=$clientId&redirect_uri=$redirectUri&response_type=code&scope=openid%20email%20profile&access_type=offline"
            call.respondText(googleAuthUrl)
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
                                // Сохраняем токен в localStorage и sessionStorage
                                localStorage.setItem('token', result.token);
                                sessionStorage.setItem('token', result.token);
                                // Добавляем токен в куки
                               document.cookie = 'JWT=' + result.token + '; path=/; max-age=86400';
                                // Перенаправляем с токеном в URL (на время отладки)
                                window.location.href = '/calculate?token=' + encodeURIComponent(result.token);
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
            val receive = call.receive<LoginReceiveRemote>()
            val userDTO = Users.fetchUser(receive.login) ?: run {
                call.respond(HttpStatusCode.NotFound, "User not found")
                return@post
            }

            if (userDTO.password != receive.password) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid password")
                return@post
            }

            call.respond(
                LoginResponseRemote(
                    id = userDTO.id,
                    login = userDTO.login,
                    email = userDTO.email,
                    role = userDTO.role
                )
            )
        }






        get("/calculate") {
            call.respondHtml {
                head {
                    title { +"Calculate" }
                    script {
                        unsafe {
                            +"""
                    document.addEventListener('DOMContentLoaded', function() {
                        const form = document.getElementById('calculateForm');
                        form.addEventListener('submit', async function(event) {
                            event.preventDefault();

                            const age = form.querySelector('input[name="age"]').value;
                            const gender = form.querySelector('select[name="gender"]').value;
                            const height = form.querySelector('input[name="height"]').value;
                            const weight = form.querySelector('input[name="weight"]').value;
                            const goal = form.querySelector('select[name="goal"]').value;
                            const activityLevel = form.querySelector('select[name="activityLevel"]').value;

                            const response = await fetch('/calculate', {
                                method: 'POST',
                                body: JSON.stringify({ age, gender, height, weight, goal, activityLevel }),
                                headers: { 
                                    'Content-Type': 'application/json'
                                }
                            });

                            if (response.ok) {
                                const result = await response.json();
                                alert('Calculation successful!');
                                // Здесь можно отобразить результат на странице
                                console.log(result);
                            } else {
                                const errorText = await response.text();
                                alert('Calculation failed: ' + errorText);
                            }
                        });
                    });
                    """
                        }
                    }
                }
                body {
                    h1 { +"Calculate Your Nutrition" }
                    form {
                        attributes["id"] = "calculateForm"
                        label { +"Age: " }
                        input(type = InputType.number, name = "age")
                        br
                        label { +"Gender: " }
                        select {
                            attributes["name"] = "gender"
                            option { +"Male" }
                            option { +"Female" }
                        }
                        br
                        label { +"Height (cm): " }
                        input(type = InputType.number, name = "height")
                        br
                        label { +"Weight (kg): " }
                        input(type = InputType.number, name = "weight")
                        br
                        label { +"Goal: " }
                        select {
                            attributes["name"] = "goal"
                            option { +"Lose" }
                            option { +"Maintain" }
                            option { +"Gain" }
                        }
                        br
                        label { +"Activity Level: " }
                        select {
                            attributes["name"] = "activityLevel"
                            option { +"Low" }
                            option { +"Medium" }
                            option { +"High" }
                        }
                        br
                        button(type = ButtonType.submit) { +"Calculate" }
                    }
                }
            }
        }

        post("/calculate") {
            try {
                // Получаем данные от клиента
                val userInfo = try {
                    call.receive<UserInfo>()
                } catch (e: Exception) {
                    println("Error receiving user info: ${e.message}")
                    call.respond(HttpStatusCode.BadRequest, "Invalid request body")
                    return@post
                }

                // Генерируем уникальный ID для пользователя
                val userId = UUID.randomUUID().toString()

                // Создаем UserInfoDTO
                val userInfoDTO = UserInfoDTO(
                    id = userId,
                    age = userInfo.age,
                    gender = userInfo.gender,
                    height = userInfo.height,
                    weight = userInfo.weight,
                    goal = userInfo.goal,
                    activityLevel = userInfo.activityLevel
                )

                // Сохраняем UserInfoDTO в базу данных
                try {
                    UserInfoRepository.create(userInfoDTO) // Используем create вместо insert
                } catch (e: Exception) {
                    println("Error inserting user info: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError, "Failed to save user info")
                    return@post
                }

                // Выполняем расчеты
                val calculatorService = CalculatorService()
                val tdee = calculatorService.calculateTDEE(userInfo)
                val result = calculatorService.calculateMacronutrients(tdee, userInfo.goal)

                // Создаем CalculationResultDTO
                val calculationResultDTO = CalculationResultDTO(
                    id = UUID.randomUUID().toString(),  // Уникальный ID для результата
                    userId = userId,  // Связываем результат с пользователем
                    tdee = tdee,
                    protein = result.protein,
                    fat = result.fat,
                    carbs = result.carbs,
                    recommendedCalories = result.recommendedCalories
                )

                // Получаем cacheService
                val cacheService = try {
                    call.cacheService
                } catch (e: Exception) {
                    null
                }
                
                // Сохраняем CalculationResultDTO в базу данных (с кэшированием)
                try {
                    CalculationResultRepositoryCached.create(calculationResultDTO, cacheService)
                } catch (e: Exception) {
                    println("Error inserting calculation result: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Failed to save calculation result"
                    )
                    return@post
                }

                // Возвращаем результат клиенту
                call.respond(result)


            } catch (e: Exception) {
                println("Unexpected error: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, "Unexpected error occurred")
            }
        }

        // Google OAuth роуты (временно здесь для тестирования)
        get("/auth/test") {
            val envTest = System.getenv("GOOGLE_CLIENT_ID")
            call.respond(mapOf(
                "message" to "Google Auth routing works!",
                "env_test" to (envTest ?: "NOT_SET")
            ))
        }
        
        get("/auth/google/url") {
            try {
                val clientId = System.getenv("GOOGLE_CLIENT_ID") 
                    ?: throw IllegalStateException("GOOGLE_CLIENT_ID not set")
                val redirectUri = System.getenv("GOOGLE_REDIRECT_URI") 
                    ?: "http://localhost:8080/auth/google/callback"
                
                println("DEBUG: GOOGLE_CLIENT_ID = $clientId")
                println("DEBUG: GOOGLE_REDIRECT_URI = $redirectUri")
                
                val googleAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                        "client_id=$clientId&" +
                        "redirect_uri=$redirectUri&" +
                        "response_type=code&" +
                        "scope=openid%20email%20profile&" +
                        "access_type=offline"
                
                println("DEBUG: Generated URL = $googleAuthUrl")
                call.respond(mapOf("url" to googleAuthUrl))
            } catch (e: Exception) {
                println("ERROR in /auth/google/url: ${e.message}")
                call.respond(mapOf("error" to e.message))
            }
        }

    }
}
