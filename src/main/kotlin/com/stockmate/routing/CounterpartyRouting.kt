package com.stockmate.routing

import com.stockmate.models.dto.CreateCounterpartyRequest
import com.stockmate.repositories.CounterpartyRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.counterpartyRoutes(repo: CounterpartyRepository) {
    authenticate("auth-jwt") {
        route("/api/v1/counterparties") {
            get {
                val type = call.request.queryParameters["type"]
                call.respond(repo.listAll(type))
            }
            post {
                val req = call.receive<CreateCounterpartyRequest>()
                require(req.name.isNotBlank()) { "Название обязательно" }
                call.respond(HttpStatusCode.Created, repo.create(req))
            }
            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(repo.findById(id) ?: return@get call.respond(HttpStatusCode.NotFound))
            }
            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest)
                val req = call.receive<CreateCounterpartyRequest>()
                call.respond(repo.update(id, req) ?: return@put call.respond(HttpStatusCode.NotFound))
            }
        }
    }
}
