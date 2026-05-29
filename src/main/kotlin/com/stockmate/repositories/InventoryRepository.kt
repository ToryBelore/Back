package com.stockmate.repositories

import com.stockmate.models.dto.*
import com.stockmate.models.tables.*
import com.stockmate.utils.DatabaseFactory.query
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.math.BigDecimal
import java.time.LocalDateTime

class InventoryRepository {

    suspend fun start(userId: Int, warehouseId: Int): InventorySheetDto = query {
        val docId = Documents.insertAndGetId {
            it[type] = "INVENTORY"
            it[status] = "DRAFT"
            it[warehouseFromId] = warehouseId
            it[createdBy] = userId
            it[createdAt] = LocalDateTime.now()
        }

        val warehouseName = Warehouses.selectAll().where { Warehouses.id eq warehouseId }
            .single()[Warehouses.name]

        val stockItems = Stock
            .join(Products, JoinType.INNER, Stock.productId, Products.id)
            .join(Units, JoinType.INNER, Products.unitId, Units.id)
            .selectAll().where { Stock.warehouseId eq warehouseId }
            .map {
                DocumentItems.insert { di ->
                    di[documentId] = docId
                    di[productId] = it[Products.id].value
                    di[quantity] = it[Stock.quantity]
                    di[price] = BigDecimal.ZERO
                }
                InventoryItemDto(
                    productId = it[Products.id].value,
                    productName = it[Products.name],
                    sku = it[Products.sku],
                    unitShortName = it[Units.shortName],
                    currentQty = it[Stock.quantity].toDouble(),
                    actualQty = null,
                    diff = null
                )
            }

        InventorySheetDto(docId.value, warehouseId, warehouseName, "DRAFT", stockItems)
    }

    suspend fun getSheet(docId: Int): InventorySheetDto? = query {
        val docRow = Documents.selectAll().where { Documents.id eq docId }.singleOrNull() ?: return@query null
        if (docRow[Documents.type] != "INVENTORY") return@query null

        val warehouseId = docRow[Documents.warehouseFromId]?.value ?: return@query null
        val warehouseName = Warehouses.selectAll().where { Warehouses.id eq warehouseId }
            .single()[Warehouses.name]

        val items = DocumentItems
            .join(Products, JoinType.INNER, DocumentItems.productId, Products.id)
            .join(Units, JoinType.INNER, Products.unitId, Units.id)
            .selectAll().where { DocumentItems.documentId eq docId }
            .map {
                val current = it[DocumentItems.quantity].toDouble()
                InventoryItemDto(
                    productId = it[Products.id].value,
                    productName = it[Products.name],
                    sku = it[Products.sku],
                    unitShortName = it[Units.shortName],
                    currentQty = current,
                    actualQty = null,
                    diff = null
                )
            }

        InventorySheetDto(docId, warehouseId, warehouseName, docRow[Documents.status], items)
    }

    suspend fun conduct(docId: Int, inputs: List<InventoryActualInput>): InventorySheetDto = query {
        val sheet = getSheet(docId) ?: error("Inventory not found")
        require(sheet.status == "DRAFT") { "Already conducted" }

        val warehouseId = sheet.warehouseId
        val actualMap = inputs.associateBy { it.productId }

        sheet.items.forEach { item ->
            val actual = actualMap[item.productId]?.actualQty ?: item.currentQty
            Stock.update({ (Stock.productId eq item.productId) and (Stock.warehouseId eq warehouseId) }) {
                it[quantity] = BigDecimal.valueOf(actual)
                it[updatedAt] = LocalDateTime.now()
            }
        }

        Documents.update({ Documents.id eq docId }) {
            it[status] = "CONDUCTED"
            it[conductedAt] = LocalDateTime.now()
        }
        getSheet(docId)!!
    }
}
