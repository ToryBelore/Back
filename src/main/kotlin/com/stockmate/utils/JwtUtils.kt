package com.stockmate.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import java.util.*

object JwtUtils {

    private lateinit var secret: String
    private lateinit var issuer: String
    private lateinit var audience: String
    private var accessTtlMs: Long = 3600_000L
    private var refreshTtlMs: Long = 2592000_000L

    fun init(application: Application) {
        val config = application.environment.config
        secret = System.getenv("JWT_SECRET")
            ?: config.propertyOrNull("jwt.secret")?.getString()
            ?: "stockmate-secret-key-change-in-production"
        issuer = config.propertyOrNull("jwt.issuer")?.getString() ?: "stockmate"
        audience = config.propertyOrNull("jwt.audience")?.getString() ?: "stockmate-users"
        accessTtlMs = (System.getenv("JWT_EXPIRATION")?.toLongOrNull()
            ?: config.propertyOrNull("jwt.expiration")?.getString()?.toLong()
            ?: 3600) * 1000L
        refreshTtlMs = (System.getenv("JWT_REFRESH_EXPIRATION")?.toLongOrNull()
            ?: config.propertyOrNull("jwt.refreshExpiration")?.getString()?.toLong()
            ?: 2592000) * 1000L
    }

    fun generateAccessToken(userId: Int, role: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId.toString())
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + accessTtlMs))
            .sign(Algorithm.HMAC256(secret))
    }

    fun generateRefreshToken(): String {
        return UUID.randomUUID().toString() + UUID.randomUUID().toString()
    }

    fun refreshTtlDays(): Long = refreshTtlMs / 86400_000L
}
