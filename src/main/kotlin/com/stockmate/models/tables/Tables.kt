package com.stockmate.models.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime

object Users : IntIdTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val fullName = varchar("full_name", 255)
    val role = varchar("role", 50)
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at")
}

object RefreshTokens : IntIdTable("refresh_tokens") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val token = varchar("token", 512).uniqueIndex()
    val expiresAt = datetime("expires_at")
    val createdAt = datetime("created_at")
}

object Categories : IntIdTable("categories") {
    val name = varchar("name", 255)
    val parentId = reference("parent_id", Categories, onDelete = ReferenceOption.SET_NULL).nullable()
}

object Units : IntIdTable("units") {
    val name = varchar("name", 100)
    val shortName = varchar("short_name", 20)
}

object Products : IntIdTable("products") {
    val sku = varchar("sku", 100).uniqueIndex()
    val barcode = varchar("barcode", 50).nullable()
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val categoryId = reference("category_id", Categories, onDelete = ReferenceOption.SET_NULL).nullable()
    val unitId = reference("unit_id", Units, onDelete = ReferenceOption.RESTRICT)
    val minStock = decimal("min_stock", 12, 3).default(java.math.BigDecimal.ZERO)
    val purchasePrice = decimal("purchase_price", 12, 2).default(java.math.BigDecimal.ZERO)
    val sellPrice = decimal("sell_price", 12, 2).default(java.math.BigDecimal.ZERO)
    val photoUrl = varchar("photo_url", 512).nullable()
    val createdAt = datetime("created_at")
}

object Warehouses : IntIdTable("warehouses") {
    val name = varchar("name", 255)
    val address = text("address").nullable()
    val managerId = reference("manager_id", Users, onDelete = ReferenceOption.SET_NULL).nullable()
    val isActive = bool("is_active").default(true)
}

object Zones : IntIdTable("zones") {
    val warehouseId = reference("warehouse_id", Warehouses, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 100)
}

object Cells : IntIdTable("cells") {
    val zoneId = reference("zone_id", Zones, onDelete = ReferenceOption.CASCADE)
    val code = varchar("code", 100)
}

object Stock : IntIdTable("stock") {
    val productId = reference("product_id", Products, onDelete = ReferenceOption.CASCADE)
    val warehouseId = reference("warehouse_id", Warehouses, onDelete = ReferenceOption.CASCADE)
    val cellId = reference("cell_id", Cells, onDelete = ReferenceOption.SET_NULL).nullable()
    val quantity = decimal("quantity", 12, 3).default(java.math.BigDecimal.ZERO)
    val updatedAt = datetime("updated_at")
}

object Counterparties : IntIdTable("counterparties") {
    val name = varchar("name", 255)
    val inn = varchar("inn", 20).nullable()
    val contactName = varchar("contact_name", 255).nullable()
    val phone = varchar("phone", 50).nullable()
    val email = varchar("email", 255).nullable()
    val type = varchar("type", 20)
}

object Documents : IntIdTable("documents") {
    val type = varchar("type", 30)
    val status = varchar("status", 20)
    val warehouseFromId = reference("warehouse_from_id", Warehouses, onDelete = ReferenceOption.RESTRICT).nullable()
    val warehouseToId = reference("warehouse_to_id", Warehouses, onDelete = ReferenceOption.RESTRICT).nullable()
    val counterpartyId = reference("counterparty_id", Counterparties, onDelete = ReferenceOption.SET_NULL).nullable()
    val createdBy = reference("created_by", Users, onDelete = ReferenceOption.RESTRICT)
    val createdAt = datetime("created_at")
    val conductedAt = datetime("conducted_at").nullable()
    val comment = text("comment").nullable()
}

object DocumentItems : IntIdTable("document_items") {
    val documentId = reference("document_id", Documents, onDelete = ReferenceOption.CASCADE)
    val productId = reference("product_id", Products, onDelete = ReferenceOption.RESTRICT)
    val quantity = decimal("quantity", 12, 3)
    val price = decimal("price", 12, 2).default(java.math.BigDecimal.ZERO)
    val cellId = reference("cell_id", Cells, onDelete = ReferenceOption.SET_NULL).nullable()
}

object Batches : IntIdTable("batches") {
    val productId = reference("product_id", Products, onDelete = ReferenceOption.CASCADE)
    val warehouseId = reference("warehouse_id", Warehouses, onDelete = ReferenceOption.CASCADE)
    val quantity = decimal("quantity", 12, 3)
    val purchaseDate = datetime("purchase_date")
    val expiryDate = datetime("expiry_date").nullable()
    val documentId = reference("document_id", Documents, onDelete = ReferenceOption.SET_NULL).nullable()
}

object ReplenishmentRequests : IntIdTable("replenishment_requests") {
    val productId = reference("product_id", Products, onDelete = ReferenceOption.CASCADE)
    val warehouseId = reference("warehouse_id", Warehouses, onDelete = ReferenceOption.CASCADE)
    val quantity = decimal("quantity", 12, 3)
    val status = varchar("status", 20)
    val createdBy = reference("created_by", Users, onDelete = ReferenceOption.RESTRICT)
    val assignedTo = reference("assigned_to", Users, onDelete = ReferenceOption.SET_NULL).nullable()
    val comment = text("comment").nullable()
    val createdAt = datetime("created_at")
}

object Notifications : IntIdTable("notifications") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val type = varchar("type", 50)
    val title = varchar("title", 255)
    val body = text("body")
    val entityType = varchar("entity_type", 50).nullable()
    val entityId = integer("entity_id").nullable()
    val isRead = bool("is_read").default(false)
    val createdAt = datetime("created_at")
}
