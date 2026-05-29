package com.stockmate.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: Int,
    val name: String,
    val parentId: Int?
)

@Serializable
data class CreateCategoryRequest(val name: String, val parentId: Int? = null)

@Serializable
data class UnitDto(val id: Int, val name: String, val shortName: String)

@Serializable
data class CreateUnitRequest(val name: String, val shortName: String)

@Serializable
data class ProductDto(
    val id: Int,
    val sku: String,
    val barcode: String?,
    val name: String,
    val description: String?,
    val categoryId: Int?,
    val categoryName: String?,
    val unitId: Int,
    val unitShortName: String,
    val minStock: Double,
    val purchasePrice: Double,
    val sellPrice: Double,
    val photoUrl: String?,
    val createdAt: String
)

@Serializable
data class CreateProductRequest(
    val sku: String,
    val barcode: String? = null,
    val name: String,
    val description: String? = null,
    val categoryId: Int? = null,
    val unitId: Int,
    val minStock: Double = 0.0,
    val purchasePrice: Double = 0.0,
    val sellPrice: Double = 0.0
)

@Serializable
data class UpdateProductRequest(
    val sku: String? = null,
    val barcode: String? = null,
    val name: String? = null,
    val description: String? = null,
    val categoryId: Int? = null,
    val unitId: Int? = null,
    val minStock: Double? = null,
    val purchasePrice: Double? = null,
    val sellPrice: Double? = null,
    val photoUrl: String? = null
)

@Serializable
data class ProductListResponse(
    val items: List<ProductDto>,
    val total: Int,
    val page: Int,
    val size: Int
)
