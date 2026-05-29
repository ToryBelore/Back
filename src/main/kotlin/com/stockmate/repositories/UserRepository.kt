package com.stockmate.repositories

import com.stockmate.models.dto.UserDto
import com.stockmate.models.tables.RefreshTokens
import com.stockmate.models.tables.Users
import com.stockmate.utils.DatabaseFactory.query
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime

class UserRepository {

    suspend fun findByEmail(email: String): UserDto? = query {
        Users.selectAll()
            .where { Users.email eq email }
            .map { it.toDto() }
            .singleOrNull()
    }

    suspend fun findById(id: Int): UserDto? = query {
        Users.selectAll()
            .where { Users.id eq id }
            .map { it.toDto() }
            .singleOrNull()
    }

    suspend fun checkPassword(email: String, password: String): UserDto? = query {
        val row = Users.selectAll()
            .where { (Users.email eq email) and (Users.isActive eq true) }
            .singleOrNull() ?: return@query null
        if (BCrypt.checkpw(password, row[Users.passwordHash])) row.toDto() else null
    }

    suspend fun create(email: String, password: String, fullName: String, role: String): UserDto = query {
        val hash = BCrypt.hashpw(password, BCrypt.gensalt(12))
        val id = Users.insertAndGetId {
            it[Users.email] = email
            it[Users.passwordHash] = hash
            it[Users.fullName] = fullName
            it[Users.role] = role
            it[Users.isActive] = true
            it[createdAt] = LocalDateTime.now()
        }
        Users.selectAll().where { Users.id eq id }.map { it.toDto() }.single()
    }

    suspend fun update(id: Int, fullName: String?, role: String?, isActive: Boolean?): UserDto? = query {
        Users.update({ Users.id eq id }) {
            if (fullName != null) it[Users.fullName] = fullName
            if (role != null) it[Users.role] = role
            if (isActive != null) it[Users.isActive] = isActive
        }
        Users.selectAll().where { Users.id eq id }.map { it.toDto() }.singleOrNull()
    }

    suspend fun listAll(): List<UserDto> = query {
        Users.selectAll().map { it.toDto() }
    }

    suspend fun saveRefreshToken(userId: Int, token: String, expiresAt: LocalDateTime) = query {
        RefreshTokens.insert {
            it[RefreshTokens.userId] = userId
            it[RefreshTokens.token] = token
            it[RefreshTokens.expiresAt] = expiresAt
            it[createdAt] = LocalDateTime.now()
        }
    }

    suspend fun findRefreshToken(token: String): Pair<Int, LocalDateTime>? = query {
        RefreshTokens.selectAll()
            .where { RefreshTokens.token eq token }
            .map { it[RefreshTokens.userId].value to it[RefreshTokens.expiresAt] }
            .singleOrNull()
    }

    suspend fun deleteRefreshToken(token: String) = query {
        RefreshTokens.deleteWhere { RefreshTokens.token eq token }
    }

    suspend fun deleteAllRefreshTokensForUser(userId: Int) = query {
        RefreshTokens.deleteWhere { RefreshTokens.userId eq userId }
    }

    private fun ResultRow.toDto() = UserDto(
        id = this[Users.id].value,
        email = this[Users.email],
        fullName = this[Users.fullName],
        role = this[Users.role],
        isActive = this[Users.isActive]
    )
}
