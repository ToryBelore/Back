package com.stockmate.repositories

import com.stockmate.models.dto.*
import com.stockmate.models.tables.*
import com.stockmate.utils.DatabaseFactory.query
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ReportRepository {

    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    suspend fun dashboard(): DashboardDto = query {
        val totalValue = Stock
            .join(Products, JoinType.INNER, Stock.productId, Products.id)
            .select(Stock.quantity, Products.sellPrice)
            .sumOf { it[Stock.quantity].toDouble() * it[Products.sellPrice].toDouble() }

        val lowStock = Stock
            .join(Products, JoinType.INNER, Stock.productId, Products.id)
            .selectAll()
            .count { it[Stock.quantity] <= it[Products.minStock] }

        val todayStart = LocalDateTime.now().toLocalDate().atStartOfDay()
        val todayOps = Documents.selectAll()
            .where { Documents.createdAt greaterEq todayStart }
            .count().toInt()

        val byCat = Stock
            .join(Products, JoinType.INNER, Stock.productId, Products.id)
            .join(Categories, JoinType.LEFT, Products.categoryId, Categories.id)
            .select(Categories.name, Stock.quantity, Products.sellPrice)
            .groupBy { it.getOrNull(Categories.name) ?: "Без категории" }
            .map { (cat, rows) ->
                val val_ = rows.sumOf { it[Stock.quantity].toDouble() * it[Products.sellPrice].toDouble() }
                CategoryStock(cat, val_)
            }
            .sortedByDescending { it.value }
            .take(10)

        DashboardDto(totalValue, lowStock, todayOps, byCat)
    }

    suspend fun stockReport(warehouseId: Int?): List<StockReportItem> = query {
        val q = Stock
            .join(Products, JoinType.INNER, Stock.productId, Products.id)
            .join(Units, JoinType.INNER, Products.unitId, Units.id)
            .join(Categories, JoinType.LEFT, Products.categoryId, Categories.id)
            .join(Warehouses, JoinType.INNER, Stock.warehouseId, Warehouses.id)
            .selectAll()
        if (warehouseId != null) q.andWhere { Stock.warehouseId eq warehouseId }
        q.map {
            val qty = it[Stock.quantity].toDouble()
            val price = it[Products.sellPrice].toDouble()
            StockReportItem(
                productId = it[Products.id].value,
                productName = it[Products.name],
                sku = it[Products.sku],
                categoryName = it.getOrNull(Categories.name),
                warehouseName = it[Warehouses.name],
                quantity = qty,
                unitShortName = it[Units.shortName],
                sellPrice = price,
                totalValue = qty * price
            )
        }
    }

    suspend fun movementReport(productId: Int?, from: String?, to: String?): List<MovementReportItem> = query {
        val q = DocumentItems
            .join(Documents, JoinType.INNER, DocumentItems.documentId, Documents.id)
            .join(Products, JoinType.INNER, DocumentItems.productId, Products.id)
            .join(Warehouses, JoinType.LEFT, Documents.warehouseFromId, Warehouses.id)
            .selectAll()
            .where { Documents.status eq "CONDUCTED" }
        if (productId != null) q.andWhere { DocumentItems.productId eq productId }
        if (from != null) q.andWhere { Documents.conductedAt greaterEq LocalDateTime.parse(from) }
        if (to != null) q.andWhere { Documents.conductedAt lessEq LocalDateTime.parse(to) }
        q.orderBy(Documents.conductedAt to SortOrder.DESC).map {
            MovementReportItem(
                date = it[Documents.conductedAt]?.format(fmt) ?: it[Documents.createdAt].format(fmt),
                documentId = it[Documents.id].value,
                docType = it[Documents.type],
                productName = it[Products.name],
                sku = it[Products.sku],
                quantity = it[DocumentItems.quantity].toDouble(),
                warehouseName = it.getOrNull(Warehouses.name)
            )
        }
    }

    suspend fun turnoverReport(warehouseId: Int?, from: String?, to: String?): List<TurnoverReportItem> = query {
        val fromDt = if (from != null) LocalDateTime.parse(from) else LocalDateTime.now().minusDays(30)
        val toDt = if (to != null) LocalDateTime.parse(to) else LocalDateTime.now()

        val outByProduct = DocumentItems
            .join(Documents, JoinType.INNER, DocumentItems.documentId, Documents.id)
            .select(DocumentItems.productId, DocumentItems.quantity)
            .where {
                (Documents.type inList listOf("SHIPMENT", "TRANSFER")) and
                        (Documents.status eq "CONDUCTED") and
                        (Documents.conductedAt greaterEq fromDt) and
                        (Documents.conductedAt lessEq toDt)
            }
            .also { q -> if (warehouseId != null) q.andWhere { Documents.warehouseFromId eq warehouseId } }
            .groupBy { it[DocumentItems.productId].value }
            .mapValues { (_, rows) -> rows.sumOf { it[DocumentItems.quantity].toDouble() } }

        Products.selectAll().mapNotNull { productRow ->
            val productId = productRow[Products.id].value
            val totalOut = outByProduct[productId] ?: return@mapNotNull null
            val stockQty = Stock
                .selectAll().where { Stock.productId eq productId }
                .also { q -> if (warehouseId != null) q.andWhere { Stock.warehouseId eq warehouseId } }
                .sumOf { it[Stock.quantity].toDouble() }
            val avgStock = (stockQty + totalOut) / 2.0
            val coeff = if (avgStock > 0) totalOut / avgStock else 0.0
            TurnoverReportItem(productId, productRow[Products.name], productRow[Products.sku], totalOut, avgStock, coeff)
        }.sortedByDescending { it.turnoverCoeff }
    }
}
