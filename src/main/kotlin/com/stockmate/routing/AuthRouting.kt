package com.stockmate.routing

import com.stockmate.models.dto.LoginRequest
import com.stockmate.models.dto.RefreshRequest
import com.stockmate.services.AuthService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {
    route("/api/v1/auth") {
        post("/login") {
            val req = call.receive<LoginRequest>()
            require(req.email.isNotBlank()) { "Email is required" }
            require(req.password.isNotBlank()) { "Password is required" }
            val response = authService.login(req.email.trim(), req.password)
                ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Invalid credentials"))
            call.respond(HttpStatusCode.OK, response)
        }

        post("/refresh") {
            val req = call.receive<RefreshRequest>()
            val response = authService.refresh(req.refreshToken)
                ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Invalid or expired token"))
            call.respond(HttpStatusCode.OK, response)
        }

        authenticate("auth-jwt") {
            post("/logout") {
                val req = call.receive<RefreshRequest>()
                authService.logout(req.refreshToken)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
