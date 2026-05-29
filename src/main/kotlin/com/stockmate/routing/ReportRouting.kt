package com.stockmate.routing

import com.stockmate.repositories.ReportRepository
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.reportRoutes(repo: ReportRepository) {
    authenticate("auth-jwt") {
        route("/api/v1/reports") {
            get("/dashboard") {
                call.respond(repo.dashboard())
            }
            get("/stock") {
                val warehouseId = call.request.queryParameters["warehouse_id"]?.toIntOrNull()
                call.respond(repo.stockReport(warehouseId))
            }
            get("/movement") {
                val productId = call.request.queryParameters["product_id"]?.toIntOrNull()
                val from = call.request.queryParameters["from"]
                val to = call.request.queryParameters["to"]
                call.respond(repo.movementReport(productId, from, to))
            }
            get("/turnover") {
                val warehouseId = call.request.queryParameters["warehouse_id"]?.toIntOrNull()
                val from = call.request.queryParameters["from"]
                val to = call.request.queryParameters["to"]
                call.respond(repo.turnoverReport(warehouseId, from, to))
            }
        }
    }
}
