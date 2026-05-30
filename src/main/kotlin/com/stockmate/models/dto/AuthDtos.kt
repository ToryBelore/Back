package com.stockmate.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(val email: String, val password: String, val fullName: String)

@Serializable
data class RegisterResponse(val id: Int, val email: String, val fullName: String, val role: String)

@Serializable
data class LoginResponse(val accessToken: String, val refreshToken: String, val role: String)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class RefreshResponse(val accessToken: String)

@Serializable
data class UserDto(
    val id: Int,
    val email: String,
    val fullName: String,
    val role: String,
    val isActive: Boolean
)

@Serializable
data class CreateUserRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val role: String
)

@Serializable
data class UpdateUserRequest(
    val fullName: String? = null,
    val role: String? = null,
    val isActive: Boolean? = null
)
