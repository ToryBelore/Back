package com.stockmate.repositories

import com.stockmate.models.dto.*
import com.stockmate.models.tables.Categories
import com.stockmate.models.tables.Products
import com.stockmate.models.tables.Units
import com.stockmate.utils.DatabaseFactory.query
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ProductRepository {

    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    suspend fun listCategories(): List<CategoryDto> = query {
        Categories.selectAll().map {
            CategoryDto(it[Categories.id].value, it[Categories.name], it[Categories.parentId]?.value)
        }
    }

    suspend fun createCategory(name: String, parentId: Int?): CategoryDto = query {
        val id = Categories.insertAndGetId {
            it[Categories.name] = name
            if (parentId != null) it[Categories.parentId] = parentId
        }
        CategoryDto(id.value, name, parentId)
    }

    suspend fun deleteCategory(id: Int): Boolean = query {
        Categories.deleteWhere { Categories.id eq id } > 0
    }

    suspend fun listUnits(): List<UnitDto> = query {
        Units.selectAll().map { UnitDto(it[Units.id].value, it[Units.name], it[Units.shortName]) }
    }

    suspend fun createUnit(name: String, shortName: String): UnitDto = query {
        val id = Units.insertAndGetId {
            it[Units.name] = name
            it[Units.shortName] = shortName
        }
        UnitDto(id.value, name, shortName)
    }

    suspend fun listProducts(
        page: Int, size: Int, search: String?, categoryId: Int?, sortBy: String?
    ): ProductListResponse = query {
        val query = Products.join(Units, JoinType.INNER, Products.unitId, Units.id)
            .join(Categories, JoinType.LEFT, Products.categoryId, Categories.id)
            .selectAll()
        if (!search.isNullOrBlank()) {
            query.andWhere {
                (Products.name.lowerCase() like "%${search.lowercase()}%") or
                        (Products.sku.lowerCase() like "%${search.lowercase()}%")
            }
        }
        if (categoryId != null) query.andWhere { Products.categoryId eq categoryId }

        val total = query.count().toInt()

        when (sortBy) {
            "sku" -> query.orderBy(Products.sku to SortOrder.ASC)
            "stock" -> query.orderBy(Products.name to SortOrder.ASC)
            else -> query.orderBy(Products.name to SortOrder.ASC)
        }

        val items = query.limit(size).offset(((page - 1) * size).toLong()).map { it.toDto() }
        ProductListResponse(items, total, page, size)
    }

    suspend fun findById(id: Int): ProductDto? = query {
        Products.join(Units, JoinType.INNER, Products.unitId, Units.id)
            .join(Categories, JoinType.LEFT, Products.categoryId, Categories.id)
            .selectAll().where { Products.id eq id }
            .map { it.toDto() }
            .singleOrNull()
    }

    suspend fun findBySku(sku: String): ProductDto? = query {
        Products.join(Units, JoinType.INNER, Products.unitId, Units.id)
            .join(Categories, JoinType.LEFT, Products.categoryId, Categories.id)
            .selectAll().where { Products.sku eq sku }
            .map { it.toDto() }
            .singleOrNull()
    }

    suspend fun create(req: CreateProductRequest): ProductDto = query {
        require(findBySku(req.sku) == null) { "SKU already exists" }
        val id = Products.insertAndGetId {
            it[sku] = req.sku
            if (req.barcode != null) it[barcode] = req.barcode
            it[name] = req.name
            if (req.description != null) it[description] = req.description
            if (req.categoryId != null) it[categoryId] = req.categoryId
            it[unitId] = req.unitId
            it[minStock] = BigDecimal.valueOf(req.minStock)
            it[purchasePrice] = BigDecimal.valueOf(req.purchasePrice)
            it[sellPrice] = BigDecimal.valueOf(req.sellPrice)
            it[createdAt] = LocalDateTime.now()
        }
        findById(id.value)!!
    }

    suspend fun update(id: Int, req: UpdateProductRequest): ProductDto? = query {
        Products.update({ Products.id eq id }) {
            if (req.sku != null) it[sku] = req.sku
            if (req.barcode != null) it[barcode] = req.barcode
            if (req.name != null) it[name] = req.name
            if (req.description != null) it[description] = req.description
            if (req.categoryId != null) it[categoryId] = req.categoryId
            if (req.unitId != null) it[unitId] = req.unitId
            if (req.minStock != null) it[minStock] = BigDecimal.valueOf(req.minStock)
            if (req.purchasePrice != null) it[purchasePrice] = BigDecimal.valueOf(req.purchasePrice)
            if (req.sellPrice != null) it[sellPrice] = BigDecimal.valueOf(req.sellPrice)
            if (req.photoUrl != null) it[photoUrl] = req.photoUrl
        }
        findById(id)
    }

    suspend fun delete(id: Int): Boolean = query {
        Products.deleteWhere { Products.id eq id } > 0
    }

    private fun ResultRow.toDto() = ProductDto(
        id = this[Products.id].value,
        sku = this[Products.sku],
        barcode = this[Products.barcode],
        name = this[Products.name],
        description = this[Products.description],
        categoryId = this[Products.categoryId]?.value,
        categoryName = this.getOrNull(Categories.name),
        unitId = this[Products.unitId].value,
        unitShortName = this[Units.shortName],
        minStock = this[Products.minStock].toDouble(),
        purchasePrice = this[Products.purchasePrice].toDouble(),
        sellPrice = this[Products.sellPrice].toDouble(),
        photoUrl = this[Products.photoUrl],
        createdAt = this[Products.createdAt].format(fmt)
    )
}
