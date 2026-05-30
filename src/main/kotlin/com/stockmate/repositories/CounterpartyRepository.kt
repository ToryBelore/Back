package com.stockmate.repositories

import com.stockmate.models.dto.CounterpartyDto
import com.stockmate.models.dto.CreateCounterpartyRequest
import com.stockmate.models.tables.Counterparties
import com.stockmate.utils.DatabaseFactory.query
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class CounterpartyRepository {

    suspend fun listAll(type: String? = null): List<CounterpartyDto> = query {
        val q = Counterparties.selectAll()
        if (type != null) q.andWhere { Counterparties.type eq type }
        q.map { it.toDto() }
    }

    suspend fun findById(id: Int): CounterpartyDto? = query {
        Counterparties.selectAll().where { Counterparties.id eq id }
            .map { it.toDto() }.singleOrNull()
    }

    suspend fun create(req: CreateCounterpartyRequest): CounterpartyDto = query {
        val id = Counterparties.insertAndGetId {
            it[name] = req.name
            it[inn] = req.inn
            it[contactName] = req.contactName
            it[phone] = req.phone
            it[email] = req.email
            it[type] = req.type
        }
        findById(id.value)!!
    }

    suspend fun update(id: Int, req: CreateCounterpartyRequest): CounterpartyDto? = query {
        Counterparties.update({ Counterparties.id eq id }) {
            it[name] = req.name
            it[inn] = req.inn
            it[contactName] = req.contactName
            it[phone] = req.phone
            it[email] = req.email
            it[type] = req.type
        }
        findById(id)
    }

    private fun ResultRow.toDto() = CounterpartyDto(
        id = this[Counterparties.id].value,
        name = this[Counterparties.name],
        inn = this[Counterparties.inn],
        contactName = this[Counterparties.contactName],
        phone = this[Counterparties.phone],
        email = this[Counterparties.email],
        type = this[Counterparties.type]
    )
}
