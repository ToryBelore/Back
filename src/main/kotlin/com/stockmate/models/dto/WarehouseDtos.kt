package com.stockmate.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class WarehouseDto(
    val id: Int,
    val name: String,
    val address: String?,
    val managerId: Int?,
    val managerName: String?,
    val isActive: Boolean
)

@Serializable
data class CreateWarehouseRequest(
    val name: String,
    val address: String? = null,
    val managerId: Int? = null
)

@Serializable
data class UpdateWarehouseRequest(
    val name: String? = null,
    val address: String? = null,
    val managerId: Int? = null,
    val isActive: Boolean? = null
)

@Serializable
data class ZoneDto(val id: Int, val warehouseId: Int, val name: String)

@Serializable
data class CreateZoneRequest(val name: String)

@Serializable
data class CellDto(val id: Int, val zoneId: Int, val code: String)

@Serializable
data class CreateCellRequest(val code: String)

@Serializable
data class StockItemDto(
    val productId: Int,
    val productName: String,
    val sku: String,
    val unitShortName: String,
    val warehouseId: Int,
    val cellId: Int?,
    val cellCode: String?,
    val quantity: Double,
    val minStock: Double,
    val isLow: Boolean
)

@Serializable
data class StockListResponse(
    val items: List<StockItemDto>,
    val total: Int,
    val page: Int,
    val size: Int
)
