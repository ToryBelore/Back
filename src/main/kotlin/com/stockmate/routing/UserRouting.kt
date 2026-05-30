package com.stockmate.routing

import com.stockmate.models.dto.CreateUserRequest
import com.stockmate.models.dto.UpdateUserRequest
import com.stockmate.repositories.UserRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(repo: UserRepository) {
    authenticate("auth-jwt") {
        route("/api/v1/users") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                if (role != "Admin") return@get call.respond(HttpStatusCode.Forbidden)
                call.respond(repo.listAll())
            }

            post {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                if (role != "Admin") return@post call.respond(HttpStatusCode.Forbidden)
                val req = call.receive<CreateUserRequest>()
                val existing = repo.findByEmail(req.email)
                if (existing != null) return@post call.respond(
                    HttpStatusCode.Conflict, mapOf("message" to "Пользователь уже существует")
                )
                val user = repo.create(req.email, req.password, req.fullName, req.role)
                call.respond(HttpStatusCode.Created, user)
            }

            patch("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                if (role != "Admin") return@patch call.respond(HttpStatusCode.Forbidden)
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest)
                val req = call.receive<UpdateUserRequest>()
                val updated = repo.update(id, req.fullName, req.role, req.isActive)
                call.respond(updated ?: return@patch call.respond(HttpStatusCode.NotFound))
            }
        }
    }
}
