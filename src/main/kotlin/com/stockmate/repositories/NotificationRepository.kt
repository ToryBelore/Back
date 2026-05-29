package com.stockmate.repositories

import com.stockmate.models.dto.NotificationDto
import com.stockmate.models.tables.Notifications
import com.stockmate.utils.DatabaseFactory.query
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NotificationRepository {

    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    suspend fun listForUser(userId: Int, unreadOnly: Boolean): List<NotificationDto> = query {
        val q = Notifications.selectAll().where { Notifications.userId eq userId }
        if (unreadOnly) q.andWhere { Notifications.isRead eq false }
        q.orderBy(Notifications.createdAt to SortOrder.DESC).map { it.toDto() }
    }

    suspend fun markRead(notificationId: Int, userId: Int): Boolean = query {
        Notifications.update({
            (Notifications.id eq notificationId) and (Notifications.userId eq userId)
        }) {
            it[isRead] = true
        } > 0
    }

    suspend fun markAllRead(userId: Int) = query {
        Notifications.update({ Notifications.userId eq userId }) {
            it[isRead] = true
        }
    }

    suspend fun create(
        userId: Int, type: String, title: String, body: String,
        entityType: String? = null, entityId: Int? = null
    ) = query {
        Notifications.insert {
            it[Notifications.userId] = userId
            it[Notifications.type] = type
            it[Notifications.title] = title
            it[Notifications.body] = body
            if (entityType != null) it[Notifications.entityType] = entityType
            if (entityId != null) it[Notifications.entityId] = entityId
            it[isRead] = false
            it[createdAt] = LocalDateTime.now()
        }
    }

    private fun ResultRow.toDto() = NotificationDto(
        id = this[Notifications.id].value,
        type = this[Notifications.type],
        title = this[Notifications.title],
        body = this[Notifications.body],
        entityType = this[Notifications.entityType],
        entityId = this[Notifications.entityId],
        isRead = this[Notifications.isRead],
        createdAt = this[Notifications.createdAt].format(fmt)
    )
}
