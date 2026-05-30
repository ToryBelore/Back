package com.stockmate.routing

import com.stockmate.repositories.BatchRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.batchRoutes(repo: BatchRepository) {
    authenticate("auth-jwt") {
        get("/api/v1/products/{id}/batches") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond(repo.listByProduct(id))
        }
    }
}
