package com.stockmate.plugins

import com.stockmate.repositories.DocumentRepository
import com.stockmate.repositories.ProductRepository
import com.stockmate.repositories.UserRepository
import com.stockmate.repositories.WarehouseRepository
import com.stockmate.routing.authRoutes
import com.stockmate.routing.documentRoutes
import com.stockmate.routing.productRoutes
import com.stockmate.routing.warehouseRoutes
import com.stockmate.services.AuthService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val userRepo = UserRepository()
    val authService = AuthService(userRepo)
    val productRepo = ProductRepository()
    val warehouseRepo = WarehouseRepository()
    val documentRepo = DocumentRepository()

    routing {
        get("/health") {
            call.respondText("OK")
        }
        authRoutes(authService)
        productRoutes(productRepo)
        warehouseRoutes(warehouseRepo)
        documentRoutes(documentRepo)
    }
}
