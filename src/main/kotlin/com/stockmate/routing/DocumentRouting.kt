package com.stockmate.routing

import com.stockmate.models.dto.CreateDocumentRequest
import com.stockmate.repositories.DocumentRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.documentRoutes(repo: DocumentRepository) {
    authenticate("auth-jwt") {
        route("/api/v1/documents") {
            get {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull()?.coerceIn(1, 100) ?: 30
                val type = call.request.queryParameters["type"]
                val status = call.request.queryParameters["status"]
                val from = call.request.queryParameters["from"]
                val to = call.request.queryParameters["to"]
                call.respond(repo.list(page, size, type, status, from, to))
            }
            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val req = call.receive<CreateDocumentRequest>()
                require(req.items.isNotEmpty()) { "Document must have at least one item" }
                call.respond(HttpStatusCode.Created, repo.create(userId, req))
            }
            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(repo.findById(id) ?: return@get call.respond(HttpStatusCode.NotFound))
            }
            post("/{id}/conduct") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest)
                try {
                    call.respond(repo.conduct(id))
                } catch (e: IllegalStateException) {
                    call.respond(HttpStatusCode.Conflict, mapOf("message" to e.message))
                }
            }
            post("/{id}/cancel") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest)
                try {
                    call.respond(repo.cancel(id))
                } catch (e: IllegalStateException) {
                    call.respond(HttpStatusCode.Conflict, mapOf("message" to e.message))
                }
            }
        }
    }
}
