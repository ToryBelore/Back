package com.stockmate.routing

import com.stockmate.models.dto.*
import com.stockmate.repositories.WarehouseRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.warehouseRoutes(repo: WarehouseRepository) {
    authenticate("auth-jwt") {
        route("/api/v1/warehouses") {
            get {
                call.respond(repo.listWarehouses())
            }
            post {
                val req = call.receive<CreateWarehouseRequest>()
                require(req.name.isNotBlank()) { "Warehouse name is required" }
                call.respond(HttpStatusCode.Created, repo.createWarehouse(req))
            }
            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(repo.findWarehouse(id) ?: return@get call.respond(HttpStatusCode.NotFound))
            }
            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest)
                val req = call.receive<UpdateWarehouseRequest>()
                call.respond(repo.updateWarehouse(id, req) ?: return@put call.respond(HttpStatusCode.NotFound))
            }
            get("/{id}/stock") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull()?.coerceIn(1, 100) ?: 30
                val search = call.request.queryParameters["search"]
                val lowStock = call.request.queryParameters["low_stock"]?.toBooleanStrictOrNull() ?: false
                call.respond(repo.getStock(id, page, size, search, lowStock))
            }
            get("/{id}/zones") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(repo.listZones(id))
            }
            post("/{id}/zones") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest)
                val req = call.receive<CreateZoneRequest>()
                call.respond(HttpStatusCode.Created, repo.createZone(id, req.name))
            }
        }

        route("/api/v1/zones/{zoneId}/cells") {
            get {
                val zoneId = call.parameters["zoneId"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(repo.listCells(zoneId))
            }
            post {
                val zoneId = call.parameters["zoneId"]?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest)
                val req = call.receive<CreateCellRequest>()
                call.respond(HttpStatusCode.Created, repo.createCell(zoneId, req.code))
            }
        }
    }
}
