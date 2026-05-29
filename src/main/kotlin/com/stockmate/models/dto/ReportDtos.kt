package com.stockmate.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class DashboardDto(
    val totalStockValue: Double,
    val lowStockCount: Int,
    val todayOperationsCount: Int,
    val stockByCategory: List<CategoryStock>
)

@Serializable
data class CategoryStock(val categoryName: String, val value: Double)

@Serializable
data class StockReportItem(
    val productId: Int,
    val productName: String,
    val sku: String,
    val categoryName: String?,
    val warehouseName: String,
    val quantity: Double,
    val unitShortName: String,
    val sellPrice: Double,
    val totalValue: Double
)

@Serializable
data class MovementReportItem(
    val date: String,
    val documentId: Int,
    val docType: String,
    val productName: String,
    val sku: String,
    val quantity: Double,
    val warehouseName: String?
)

@Serializable
data class TurnoverReportItem(
    val productId: Int,
    val productName: String,
    val sku: String,
    val totalOut: Double,
    val avgStock: Double,
    val turnoverCoeff: Double
)
