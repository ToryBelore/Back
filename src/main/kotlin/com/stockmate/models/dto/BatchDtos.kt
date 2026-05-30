package com.stockmate.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class BatchDto(
    val id: Int,
    val productId: Int,
    val warehouseId: Int,
    val warehouseName: String,
    val quantity: Double,
    val purchaseDate: String,
    val expiryDate: String?,
    val documentId: Int?
)
