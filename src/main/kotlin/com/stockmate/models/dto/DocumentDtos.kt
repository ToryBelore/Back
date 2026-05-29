package com.stockmate.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class DocumentItemDto(
    val id: Int,
    val productId: Int,
    val productName: String,
    val sku: String,
    val quantity: Double,
    val price: Double,
    val cellId: Int?,
    val cellCode: String?
)

@Serializable
data class DocumentDto(
    val id: Int,
    val type: String,
    val status: String,
    val warehouseFromId: Int?,
    val warehouseFromName: String?,
    val warehouseToId: Int?,
    val warehouseToName: String?,
    val counterpartyId: Int?,
    val counterpartyName: String?,
    val createdBy: Int,
    val createdByName: String,
    val createdAt: String,
    val conductedAt: String?,
    val comment: String?,
    val items: List<DocumentItemDto>
)

@Serializable
data class DocumentItemRequest(
    val productId: Int,
    val quantity: Double,
    val price: Double = 0.0,
    val cellId: Int? = null
)

@Serializable
data class CreateDocumentRequest(
    val type: String,
    val warehouseFromId: Int? = null,
    val warehouseToId: Int? = null,
    val counterpartyId: Int? = null,
    val comment: String? = null,
    val items: List<DocumentItemRequest>
)

@Serializable
data class DocumentListResponse(
    val items: List<DocumentDto>,
    val total: Int,
    val page: Int,
    val size: Int
)
