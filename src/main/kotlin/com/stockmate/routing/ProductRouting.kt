package com.stockmate.routing

import com.stockmate.models.dto.CreateCategoryRequest
import com.stockmate.models.dto.CreateProductRequest
import com.stockmate.models.dto.CreateUnitRequest
import com.stockmate.models.dto.UpdateProductRequest
import com.stockmate.repositories.ProductRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.productRoutes(repo: ProductRepository) {
    authenticate("auth-jwt") {
        route("/api/v1") {

            // Categories
            get("/categories") {
                call.respond(repo.listCategories())
            }
            post("/categories") {
                val req = call.receive<CreateCategoryRequest>()
                require(req.name.isNotBlank()) { "Category name is required" }
                call.respond(HttpStatusCode.Created, repo.createCategory(req.name, req.parentId))
            }
            delete("/categories/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)
                if (repo.deleteCategory(id)) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.NotFound)
            }

            // Units
            get("/units") {
                call.respond(repo.listUnits())
            }
            post("/units") {
                val req = call.receive<CreateUnitRequest>()
                call.respond(HttpStatusCode.Created, repo.createUnit(req.name, req.shortName))
            }

            // Products
            get("/products") {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull()?.coerceIn(1, 100) ?: 30
                val search = call.request.queryParameters["search"]
                val categoryId = call.request.queryParameters["category_id"]?.toIntOrNull()
                val sortBy = call.request.queryParameters["sort"]
                call.respond(repo.listProducts(page, size, search, categoryId, sortBy))
            }
            post("/products") {
                val req = call.receive<CreateProductRequest>()
                require(req.name.isNotBlank()) { "Product name is required" }
                require(req.sku.isNotBlank()) { "SKU is required" }
                val product = repo.create(req)
                call.respond(HttpStatusCode.Created, product)
            }
            get("/products/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)
                val product = repo.findById(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(product)
            }
            put("/products/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest)
                val req = call.receive<UpdateProductRequest>()
                val product = repo.update(id, req)
                    ?: return@put call.respond(HttpStatusCode.NotFound)
                call.respond(product)
            }
            delete("/products/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)
                if (repo.delete(id)) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
