package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

object JwtConfig {
    private const val secret = "my-very-secure-secret-key-12345"
    private const val issuer = "fitbalance_server"
    private const val audience = "fitbalance"
    const val realm = "ktor app"

    var verifier = JWT.require(Algorithm.HMAC256(secret))
        .withIssuer(issuer)
        .withAudience(audience)
        .build()
}