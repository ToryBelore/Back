package com.stockmate.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

fun Application.configureAuth() {
    val jwtSecret = System.getenv("JWT_SECRET")
        ?: environment.config.propertyOrNull("jwt.secret")?.getString()
        ?: "stockmate-secret-key-change-in-production"
    val jwtIssuer = environment.config.propertyOrNull("jwt.issuer")?.getString() ?: "stockmate"
    val jwtAudience = environment.config.propertyOrNull("jwt.audience")?.getString() ?: "stockmate-users"

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}
