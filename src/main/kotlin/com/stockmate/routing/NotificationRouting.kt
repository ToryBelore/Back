package com.stockmate.routing

import com.stockmate.repositories.NotificationRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.notificationRoutes(repo: NotificationRepository) {
    authenticate("auth-jwt") {
        route("/api/v1/notifications") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val unreadOnly = call.request.queryParameters["unread_only"]?.toBooleanStrictOrNull() ?: false
                call.respond(repo.listForUser(userId, unreadOnly))
            }
            patch("/{id}/read") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()?.toIntOrNull()
                    ?: return@patch call.respond(HttpStatusCode.Unauthorized)
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest)
                if (repo.markRead(id, userId)) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.NotFound)
            }
            patch("/read-all") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()?.toIntOrNull()
                    ?: return@patch call.respond(HttpStatusCode.Unauthorized)
                repo.markAllRead(userId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
