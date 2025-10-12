package com.example.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    private val secret = System.getenv("JWT_SECRET") ?: "my-super-secret-key-change-in-production"
    private const val issuer = "fitbalance-server"
    private const val audience = "fitbalance-users"
    const val realm = "FitBalance App"
    
    private val algorithm = Algorithm.HMAC256(secret)
    
    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()
    
    /**
     * Генерация JWT токена для пользователя
     */
    fun generateToken(userId: String, email: String, role: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + 86400000 * 7)) // 7 дней
            .sign(algorithm)
    }
}

