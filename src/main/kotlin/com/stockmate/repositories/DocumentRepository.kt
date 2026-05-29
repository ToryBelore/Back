package com.stockmate.repositories

import com.stockmate.models.dto.*
import com.stockmate.models.tables.*
import com.stockmate.utils.DatabaseFactory.query
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DocumentRepository {

    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    suspend fun list(
        page: Int, size: Int, type: String?, status: String?, from: String?, to: String?
    ): DocumentListResponse = query {
        val q = Documents
            .join(Users, JoinType.INNER, Documents.createdBy, Users.id)
            .join(Warehouses.alias("wf"), JoinType.LEFT, Documents.warehouseFromId, Warehouses.id)
            .join(Warehouses.alias("wt"), JoinType.LEFT, Documents.warehouseToId, Warehouses.id)
            .selectAll()
        if (type != null) q.andWhere { Documents.type eq type }
        if (status != null) q.andWhere { Documents.status eq status }
        if (from != null) q.andWhere { Documents.createdAt greaterEq LocalDateTime.parse(from) }
        if (to != null) q.andWhere { Documents.createdAt lessEq LocalDateTime.parse(to) }
        val total = q.count().toInt()
        val docs = q.orderBy(Documents.createdAt to SortOrder.DESC)
            .limit(size).offset(((page - 1) * size).toLong())
            .map { buildDocumentDto(it, emptyList()) }
        DocumentListResponse(docs, total, page, size)
    }

    suspend fun findById(id: Int): DocumentDto? = query {
        val docRow = Documents
            .join(Users, JoinType.INNER, Documents.createdBy, Users.id)
            .selectAll().where { Documents.id eq id }
            .singleOrNull() ?: return@query null

        val items = DocumentItems
            .join(Products, JoinType.INNER, DocumentItems.productId, Products.id)
            .join(Cells, JoinType.LEFT, DocumentItems.cellId, Cells.id)
            .selectAll().where { DocumentItems.documentId eq id }
            .map {
                DocumentItemDto(
                    id = it[DocumentItems.id].value,
                    productId = it[Products.id].value,
                    productName = it[Products.name],
                    sku = it[Products.sku],
                    quantity = it[DocumentItems.quantity].toDouble(),
                    price = it[DocumentItems.price].toDouble(),
                    cellId = it[DocumentItems.cellId]?.value,
                    cellCode = it.getOrNull(Cells.code)
                )
            }
        buildDocumentDto(docRow, items)
    }

    suspend fun create(userId: Int, req: CreateDocumentRequest): DocumentDto = query {
        val docId = Documents.insertAndGetId {
            it[type] = req.type
            it[status] = "DRAFT"
            if (req.warehouseFromId != null) it[warehouseFromId] = req.warehouseFromId
            if (req.warehouseToId != null) it[warehouseToId] = req.warehouseToId
            if (req.counterpartyId != null) it[counterpartyId] = req.counterpartyId
            it[createdBy] = userId
            it[createdAt] = LocalDateTime.now()
            if (req.comment != null) it[comment] = req.comment
        }
        req.items.forEach { item ->
            DocumentItems.insert {
                it[documentId] = docId
                it[productId] = item.productId
                it[quantity] = BigDecimal.valueOf(item.quantity)
                it[price] = BigDecimal.valueOf(item.price)
                if (item.cellId != null) it[cellId] = item.cellId
            }
        }
        findById(docId.value)!!
    }

    suspend fun conduct(id: Int): DocumentDto = query {
        val doc = findById(id) ?: error("Document not found")
        require(doc.status == "DRAFT") { "Only drafts can be conducted" }

        when (doc.type) {
            "RECEIPT" -> {
                val warehouseId = doc.warehouseToId ?: error("Warehouse required for receipt")
                doc.items.forEach { item ->
                    upsertStock(item.productId, warehouseId, item.cellId, BigDecimal.valueOf(item.quantity), add = true)
                    Batches.insert {
                        it[productId] = item.productId
                        it[Batches.warehouseId] = warehouseId
                        it[quantity] = BigDecimal.valueOf(item.quantity)
                        it[purchaseDate] = LocalDateTime.now()
                        it[documentId] = id
                    }
                }
            }
            "SHIPMENT" -> {
                val warehouseId = doc.warehouseFromId ?: error("Source warehouse required")
                doc.items.forEach { item ->
                    deductFifo(item.productId, warehouseId, BigDecimal.valueOf(item.quantity))
                }
            }
            "TRANSFER" -> {
                val fromId = doc.warehouseFromId ?: error("Source warehouse required")
                val toId = doc.warehouseToId ?: error("Destination warehouse required")
                doc.items.forEach { item ->
                    deductFifo(item.productId, fromId, BigDecimal.valueOf(item.quantity))
                    upsertStock(item.productId, toId, item.cellId, BigDecimal.valueOf(item.quantity), add = true)
                }
            }
        }

        Documents.update({ Documents.id eq id }) {
            it[status] = "CONDUCTED"
            it[conductedAt] = LocalDateTime.now()
        }
        findById(id)!!
    }

    suspend fun cancel(id: Int): DocumentDto = query {
        val doc = findById(id) ?: error("Document not found")
        require(doc.status == "DRAFT") { "Only drafts can be cancelled" }
        Documents.update({ Documents.id eq id }) { it[status] = "CANCELLED" }
        findById(id)!!
    }

    private fun upsertStock(productId: Int, warehouseId: Int, cellId: Int?, amount: BigDecimal, add: Boolean) {
        val existing = Stock.selectAll()
            .where { (Stock.productId eq productId) and (Stock.warehouseId eq warehouseId) }
            .singleOrNull()
        if (existing == null) {
            Stock.insert {
                it[Stock.productId] = productId
                it[Stock.warehouseId] = warehouseId
                if (cellId != null) it[Stock.cellId] = cellId
                it[quantity] = if (add) amount else BigDecimal.ZERO
                it[updatedAt] = LocalDateTime.now()
            }
        } else {
            val current = existing[Stock.quantity]
            val newQty = if (add) current + amount else (current - amount).coerceAtLeast(BigDecimal.ZERO)
            Stock.update({ (Stock.productId eq productId) and (Stock.warehouseId eq warehouseId) }) {
                it[quantity] = newQty
                it[updatedAt] = LocalDateTime.now()
            }
        }
    }

    private fun deductFifo(productId: Int, warehouseId: Int, needed: BigDecimal) {
        var remaining = needed
        val batches = Batches.selectAll()
            .where { (Batches.productId eq productId) and (Batches.warehouseId eq warehouseId) and (Batches.quantity greater BigDecimal.ZERO) }
            .orderBy(Batches.purchaseDate to SortOrder.ASC)
            .toList()
        for (batch in batches) {
            if (remaining <= BigDecimal.ZERO) break
            val batchQty = batch[Batches.quantity]
            val deduct = remaining.min(batchQty)
            Batches.update({ Batches.id eq batch[Batches.id] }) {
                it[quantity] = batchQty - deduct
            }
            remaining -= deduct
        }
        upsertStock(productId, warehouseId, null, needed, add = false)
    }

    private fun buildDocumentDto(row: ResultRow, items: List<DocumentItemDto>): DocumentDto {
        return DocumentDto(
            id = row[Documents.id].value,
            type = row[Documents.type],
            status = row[Documents.status],
            warehouseFromId = row[Documents.warehouseFromId]?.value,
            warehouseFromName = null,
            warehouseToId = row[Documents.warehouseToId]?.value,
            warehouseToName = null,
            counterpartyId = row[Documents.counterpartyId]?.value,
            counterpartyName = null,
            createdBy = row[Documents.createdBy].value,
            createdByName = row[Users.fullName],
            createdAt = row[Documents.createdAt].format(fmt),
            conductedAt = row[Documents.conductedAt]?.format(fmt),
            comment = row[Documents.comment],
            items = items
        )
    }
}
