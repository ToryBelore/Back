package com.stockmate.utils

import com.stockmate.models.tables.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init(application: Application) {
        val config = application.environment.config
        val url = System.getenv("DATABASE_URL")
            ?: config.propertyOrNull("database.url")?.getString()
            ?: "jdbc:postgresql://localhost:5432/stockmate"
        val user = System.getenv("DATABASE_USER")
            ?: config.propertyOrNull("database.user")?.getString()
            ?: "postgres"
        val password = System.getenv("DATABASE_PASSWORD")
            ?: config.propertyOrNull("database.password")?.getString()
            ?: "postgres"

        val hikariConfig = HikariConfig().apply {
            jdbcUrl = url
            username = user
            this.password = password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        Database.connect(HikariDataSource(hikariConfig))

        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                Users, RefreshTokens,
                Categories, Units, Products,
                Warehouses, Zones, Cells, Stock,
                Counterparties,
                Documents, DocumentItems, Batches,
                ReplenishmentRequests, Notifications
            )
        }

        application.environment.log.info("Database connected and schema created")
    }

    suspend fun <T> query(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
