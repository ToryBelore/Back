package com.stockmate.repositories

import com.stockmate.models.dto.*
import com.stockmate.models.tables.*
import com.stockmate.utils.DatabaseFactory.query
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ReplenishmentRepository {

    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    suspend fun list(status: String?): List<ReplenishmentRequestDto> = query {
        val q = ReplenishmentRequests
            .join(Products, JoinType.INNER, ReplenishmentRequests.productId, Products.id)
            .join(Warehouses, JoinType.INNER, ReplenishmentRequests.warehouseId, Warehouses.id)
            .join(Users.alias("cb"), JoinType.INNER, ReplenishmentRequests.createdBy, Users.id)
            .join(Users.alias("at"), JoinType.LEFT, ReplenishmentRequests.assignedTo, Users.id)
            .selectAll()
        if (status != null) q.andWhere { ReplenishmentRequests.status eq status }
        q.orderBy(ReplenishmentRequests.createdAt to SortOrder.DESC).map { it.toDto() }
    }

    suspend fun create(userId: Int, req: CreateReplenishmentRequest): ReplenishmentRequestDto = query {
        val id = ReplenishmentRequests.insertAndGetId {
            it[productId] = req.productId
            it[warehouseId] = req.warehouseId
            it[quantity] = BigDecimal.valueOf(req.quantity)
            it[status] = "NEW"
            it[createdBy] = userId
            if (req.comment != null) it[comment] = req.comment
            it[createdAt] = LocalDateTime.now()
        }
        findById(id.value)!!
    }

    suspend fun findById(id: Int): ReplenishmentRequestDto? = query {
        ReplenishmentRequests
            .join(Products, JoinType.INNER, ReplenishmentRequests.productId, Products.id)
            .join(Warehouses, JoinType.INNER, ReplenishmentRequests.warehouseId, Warehouses.id)
            .join(Users.alias("cb"), JoinType.INNER, ReplenishmentRequests.createdBy, Users.id)
            .join(Users.alias("at"), JoinType.LEFT, ReplenishmentRequests.assignedTo, Users.id)
            .selectAll().where { ReplenishmentRequests.id eq id }
            .map { it.toDto() }
            .singleOrNull()
    }

    suspend fun updateStatus(id: Int, req: UpdateReplenishmentRequest): ReplenishmentRequestDto? = query {
        ReplenishmentRequests.update({ ReplenishmentRequests.id eq id }) {
            it[status] = req.status
            if (req.assignedTo != null) it[assignedTo] = req.assignedTo
            if (req.comment != null) it[comment] = req.comment
        }
        findById(id)
    }

    private fun ResultRow.toDto(): ReplenishmentRequestDto {
        val cbAlias = Users.alias("cb")
        val atAlias = Users.alias("at")
        return ReplenishmentRequestDto(
            id = this[ReplenishmentRequests.id].value,
            productId = this[Products.id].value,
            productName = this[Products.name],
            warehouseId = this[Warehouses.id].value,
            warehouseName = this[Warehouses.name],
            quantity = this[ReplenishmentRequests.quantity].toDouble(),
            status = this[ReplenishmentRequests.status],
            createdBy = this[ReplenishmentRequests.createdBy].value,
            createdByName = this[cbAlias[Users.fullName]],
            assignedTo = this[ReplenishmentRequests.assignedTo]?.value,
            assignedToName = this.getOrNull(atAlias[Users.fullName]),
            comment = this[ReplenishmentRequests.comment],
            createdAt = this[ReplenishmentRequests.createdAt].format(fmt)
        )
    }
}
