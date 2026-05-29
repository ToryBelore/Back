package com.stockmate.plugins

import com.stockmate.repositories.*
import com.stockmate.routing.*
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
    val replenishmentRepo = ReplenishmentRepository()
    val inventoryRepo = InventoryRepository()

    routing {
        get("/health") {
            call.respondText("OK")
        }
        authRoutes(authService)
        productRoutes(productRepo)
        warehouseRoutes(warehouseRepo)
        documentRoutes(documentRepo)
        replenishmentRoutes(replenishmentRepo, inventoryRepo)
    }
}
