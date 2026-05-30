package com.stockmate.repositories

import com.stockmate.models.dto.BatchDto
import com.stockmate.models.tables.Batches
import com.stockmate.models.tables.Warehouses
import com.stockmate.utils.DatabaseFactory.query
import org.jetbrains.exposed.sql.*

class BatchRepository {

    suspend fun listByProduct(productId: Int): List<BatchDto> = query {
        Batches.join(Warehouses, JoinType.INNER, Batches.warehouseId, Warehouses.id)
            .selectAll().where { Batches.productId eq productId }
            .orderBy(Batches.purchaseDate, SortOrder.DESC)
            .map {
                BatchDto(
                    id = it[Batches.id].value,
                    productId = it[Batches.productId].value,
                    warehouseId = it[Batches.warehouseId].value,
                    warehouseName = it[Warehouses.name],
                    quantity = it[Batches.quantity].toDouble(),
                    purchaseDate = it[Batches.purchaseDate].toString(),
                    expiryDate = it[Batches.expiryDate]?.toString(),
                    documentId = it[Batches.documentId]?.value
                )
            }
    }
}
