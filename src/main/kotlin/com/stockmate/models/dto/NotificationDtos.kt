package com.stockmate.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: Int,
    val type: String,
    val title: String,
    val body: String,
    val entityType: String?,
    val entityId: Int?,
    val isRead: Boolean,
    val createdAt: String
)
