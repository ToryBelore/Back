package com.stockmate.repositories

import com.stockmate.models.dto.*
import com.stockmate.models.tables.*
import com.stockmate.utils.DatabaseFactory.query
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class WarehouseRepository {

    suspend fun listWarehouses(): List<WarehouseDto> = query {
        Warehouses.join(Users, JoinType.LEFT, Warehouses.managerId, Users.id)
            .selectAll()
            .map { it.toWarehouseDto() }
    }

    suspend fun findWarehouse(id: Int): WarehouseDto? = query {
        Warehouses.join(Users, JoinType.LEFT, Warehouses.managerId, Users.id)
            .selectAll().where { Warehouses.id eq id }
            .map { it.toWarehouseDto() }
            .singleOrNull()
    }

    suspend fun createWarehouse(req: CreateWarehouseRequest): WarehouseDto = query {
        val id = Warehouses.insertAndGetId {
            it[name] = req.name
            if (req.address != null) it[address] = req.address
            if (req.managerId != null) it[managerId] = req.managerId
            it[isActive] = true
        }
        findWarehouse(id.value)!!
    }

    suspend fun updateWarehouse(id: Int, req: UpdateWarehouseRequest): WarehouseDto? = query {
        Warehouses.update({ Warehouses.id eq id }) {
            if (req.name != null) it[name] = req.name
            if (req.address != null) it[address] = req.address
            if (req.managerId != null) it[managerId] = req.managerId
            if (req.isActive != null) it[isActive] = req.isActive
        }
        findWarehouse(id)
    }

    suspend fun listZones(warehouseId: Int): List<ZoneDto> = query {
        Zones.selectAll().where { Zones.warehouseId eq warehouseId }
            .map { ZoneDto(it[Zones.id].value, warehouseId, it[Zones.name]) }
    }

    suspend fun createZone(warehouseId: Int, name: String): ZoneDto = query {
        val id = Zones.insertAndGetId {
            it[Zones.warehouseId] = warehouseId
            it[Zones.name] = name
        }
        ZoneDto(id.value, warehouseId, name)
    }

    suspend fun listCells(zoneId: Int): List<CellDto> = query {
        Cells.selectAll().where { Cells.zoneId eq zoneId }
            .map { CellDto(it[Cells.id].value, zoneId, it[Cells.code]) }
    }

    suspend fun createCell(zoneId: Int, code: String): CellDto = query {
        val id = Cells.insertAndGetId {
            it[Cells.zoneId] = zoneId
            it[Cells.code] = code
        }
        CellDto(id.value, zoneId, code)
    }

    suspend fun getStock(
        warehouseId: Int, page: Int, size: Int,
        search: String?, lowStockOnly: Boolean
    ): StockListResponse = query {
        val q = Stock
            .join(Products, JoinType.INNER, Stock.productId, Products.id)
            .join(Units, JoinType.INNER, Products.unitId, Units.id)
            .join(Cells, JoinType.LEFT, Stock.cellId, Cells.id)
            .selectAll().where { Stock.warehouseId eq warehouseId }

        if (!search.isNullOrBlank()) {
            q.andWhere {
                (Products.name.lowerCase() like "%${search.lowercase()}%") or
                        (Products.sku.lowerCase() like "%${search.lowercase()}%")
            }
        }
        if (lowStockOnly) {
            q.andWhere { Stock.quantity lessEq Products.minStock }
        }

        val total = q.count().toInt()
        val items = q.limit(size).offset(((page - 1) * size).toLong()).map {
            val qty = it[Stock.quantity].toDouble()
            val minQty = it[Products.minStock].toDouble()
            StockItemDto(
                productId = it[Products.id].value,
                productName = it[Products.name],
                sku = it[Products.sku],
                unitShortName = it[Units.shortName],
                warehouseId = warehouseId,
                cellId = it[Stock.cellId]?.value,
                cellCode = it.getOrNull(Cells.code),
                quantity = qty,
                minStock = minQty,
                isLow = qty <= minQty
            )
        }
        StockListResponse(items, total, page, size)
    }

    private fun ResultRow.toWarehouseDto() = WarehouseDto(
        id = this[Warehouses.id].value,
        name = this[Warehouses.name],
        address = this[Warehouses.address],
        managerId = this[Warehouses.managerId]?.value,
        managerName = this.getOrNull(Users.fullName),
        isActive = this[Warehouses.isActive]
    )
}
