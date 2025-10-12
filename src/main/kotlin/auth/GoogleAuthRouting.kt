package com.example.auth

import com.example.database.users.UserDTO
import com.example.database.users.Users
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class GoogleUserInfo(
    val id: String,
    val email: String,
    val name: String? = null,
    val picture: String? = null
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserInfo
)

@Serializable
data class UserInfo(
    val id: String,
    val email: String,
    val role: String
)

// Хранилище использованных кодов (в реальном приложении лучше использовать Redis или БД)
private val usedCodes = mutableSetOf<String>()

fun Application.configureGoogleAuth(httpClient: HttpClient) {
    routing {
        // Тестовый роут
        get("/auth/test") {
            call.respond(mapOf("message" to "Google Auth routing works!"))
        }
        
        // Получение Google OAuth URL для авторизации
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

        // Callback endpoint после авторизации в Google
        get("/auth/google/callback") {
            val code = call.parameters["code"]
            if (code == null) {
                call.respondText("Authorization code not found", status = HttpStatusCode.BadRequest)
                return@get
            }
            
            // Проверяем, не используется ли этот код повторно
            val requestId = "${System.currentTimeMillis()}-${code.hashCode()}"
            println("DEBUG: Processing callback with request ID: $requestId")
            
            // Проверяем, не был ли код уже использован
            if (usedCodes.contains(code)) {
                println("WARNING: Code already used, returning error")
                call.respondText("Authorization code already used", status = HttpStatusCode.BadRequest)
                return@get
            }

            try {
                // Обмен кода на токен
                val clientId = System.getenv("GOOGLE_CLIENT_ID")!!
                val clientSecret = System.getenv("GOOGLE_CLIENT_SECRET")!!
                val redirectUri = System.getenv("GOOGLE_REDIRECT_URI") 
                    ?: "http://localhost:8080/auth/google/callback"

                println("DEBUG: Exchanging code for token...")
                println("DEBUG: Code: $code")
                println("DEBUG: Client ID: $clientId")
                println("DEBUG: Redirect URI: $redirectUri")

                val tokenResponse = httpClient.post("https://oauth2.googleapis.com/token") {
                    contentType(ContentType.Application.FormUrlEncoded)
                    setBody("code=$code&" +
                            "client_id=$clientId&" +
                            "client_secret=$clientSecret&" +
                            "redirect_uri=$redirectUri&" +
                            "grant_type=authorization_code")
                }

                println("DEBUG: Token response status: ${tokenResponse.status}")

                if (tokenResponse.status != HttpStatusCode.OK) {
                    val errorBody = tokenResponse.body<String>()
                    println("ERROR: Failed to get token. Status: ${tokenResponse.status}, Body: $errorBody")
                    call.respondText("Failed to get access token: $errorBody", status = HttpStatusCode.InternalServerError)
                    return@get
                }

                @Serializable
                data class TokenResponse(
                    val access_token: String,
                    val token_type: String? = null,
                    val expires_in: Int? = null,
                    val refresh_token: String? = null,
                    val scope: String? = null
                )
                
                val tokenResponseBody = tokenResponse.body<TokenResponse>()
                val accessToken = tokenResponseBody.access_token
                
                println("DEBUG: Successfully got access token from Google")

                // Получение информации о пользователе
                val userInfoResponse = httpClient.get("https://www.googleapis.com/oauth2/v2/userinfo") {
                    header("Authorization", "Bearer $accessToken")
                }

                val googleUser = userInfoResponse.body<GoogleUserInfo>()

                // Проверяем, есть ли пользователь в БД
                var user = Users.fetchUserByGoogleId(googleUser.id)

                if (user == null) {
                    // Создаем нового пользователя
                    val userId = UUID.randomUUID().toString()
                    user = UserDTO(
                        id = userId,
                        googleId = googleUser.id,
                        email = googleUser.email,
                        login = googleUser.name,
                        password = null,
                        role = "user" // По умолчанию роль user
                    )
                    Users.insert(user)
                }

                // Генерируем JWT токен
                val jwtToken = JwtConfig.generateToken(user.id, user.email, user.role)

                // ПОСЛЕ успешного получения токена помечаем код как использованный
                usedCodes.add(code)
                println("DEBUG: Code marked as used after successful auth")

                // Возвращаем HTML-страницу, которая сохранит токен и редиректнет
                call.respondText("""
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>Авторизация...</title>
                    </head>
                    <body>
                        <script>
                            const authData = ${kotlinx.serialization.json.Json.encodeToString(AuthResponse.serializer(), AuthResponse(
                                token = jwtToken,
                                user = UserInfo(
                                    id = user.id,
                                    email = user.email,
                                    role = user.role
                                )
                            ))};
                            
                            console.log('Получены данные авторизации:', authData);
                            
                            // Сохраняем токен в localStorage
                            localStorage.setItem('authToken', authData.token);
                            console.log('Токен сохранен в localStorage');
                            
                            // Проверяем сохранение
                            const saved = localStorage.getItem('authToken');
                            console.log('Проверка сохранения:', saved ? 'OK' : 'FAILED');
                            
                            // Редиректим на dashboard
                            window.location.href = '/static/dashboard.html';
                        </script>
                    </body>
                    </html>
                """.trimIndent(), ContentType.Text.Html)
            } catch (e: Exception) {
                println("Error during Google OAuth: ${e.message}")
                e.printStackTrace()
                call.respondText(
                    "Authentication failed: ${e.message}",
                    status = HttpStatusCode.InternalServerError
                )
            }
        }
    }
}

