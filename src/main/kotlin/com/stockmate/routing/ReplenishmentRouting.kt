package com.stockmate.routing

import com.stockmate.models.dto.*
import com.stockmate.repositories.InventoryRepository
import com.stockmate.repositories.ReplenishmentRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.replenishmentRoutes(repo: ReplenishmentRepository, inventoryRepo: InventoryRepository) {
    authenticate("auth-jwt") {

        route("/api/v1/requests") {
            get {
                val status = call.request.queryParameters["status"]
                call.respond(repo.list(status))
            }
            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val req = call.receive<CreateReplenishmentRequest>()
                call.respond(HttpStatusCode.Created, repo.create(userId, req))
            }
            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(repo.findById(id) ?: return@get call.respond(HttpStatusCode.NotFound))
            }
            patch("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest)
                val req = call.receive<UpdateReplenishmentRequest>()
                call.respond(repo.updateStatus(id, req) ?: return@patch call.respond(HttpStatusCode.NotFound))
            }
        }

        route("/api/v1/inventory") {
            post("/start") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val req = call.receive<StartInventoryRequest>()
                call.respond(HttpStatusCode.Created, inventoryRepo.start(userId, req.warehouseId))
            }
            get("/{docId}") {
                val docId = call.parameters["docId"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(inventoryRepo.getSheet(docId) ?: return@get call.respond(HttpStatusCode.NotFound))
            }
            post("/{docId}/conduct") {
                val docId = call.parameters["docId"]?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest)
                val req = call.receive<SubmitInventoryRequest>()
                try {
                    call.respond(inventoryRepo.conduct(docId, req.items))
                } catch (e: IllegalStateException) {
                    call.respond(HttpStatusCode.Conflict, mapOf("message" to e.message))
                }
            }
        }
    }
}
