package com.stockmate.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReplenishmentRequestDto(
    val id: Int,
    val productId: Int,
    val productName: String,
    val warehouseId: Int,
    val warehouseName: String,
    val quantity: Double,
    val status: String,
    val createdBy: Int,
    val createdByName: String,
    val assignedTo: Int?,
    val assignedToName: String?,
    val comment: String?,
    val createdAt: String
)

@Serializable
data class CreateReplenishmentRequest(
    val productId: Int,
    val warehouseId: Int,
    val quantity: Double,
    val comment: String? = null
)

@Serializable
data class UpdateReplenishmentRequest(
    val status: String,
    val assignedTo: Int? = null,
    val comment: String? = null
)

@Serializable
data class InventoryItemDto(
    val productId: Int,
    val productName: String,
    val sku: String,
    val unitShortName: String,
    val currentQty: Double,
    val actualQty: Double?,
    val diff: Double?
)

@Serializable
data class InventorySheetDto(
    val documentId: Int,
    val warehouseId: Int,
    val warehouseName: String,
    val status: String,
    val items: List<InventoryItemDto>
)

@Serializable
data class InventoryActualInput(
    val productId: Int,
    val actualQty: Double
)

@Serializable
data class StartInventoryRequest(val warehouseId: Int)

@Serializable
data class SubmitInventoryRequest(val items: List<InventoryActualInput>)
