package com.stockmate.plugins

import com.stockmate.repositories.UserRepository
import com.stockmate.routing.authRoutes
import com.stockmate.services.AuthService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val userRepo = UserRepository()
    val authService = AuthService(userRepo)

    routing {
        get("/health") {
            call.respondText("OK")
        }
        authRoutes(authService)
    }
}
