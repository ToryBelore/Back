package com.stockmate.services

import com.stockmate.models.dto.LoginResponse
import com.stockmate.models.dto.RefreshResponse
import com.stockmate.models.dto.RegisterResponse
import com.stockmate.repositories.UserRepository
import com.stockmate.utils.JwtUtils
import java.time.LocalDateTime

class AuthService(private val userRepo: UserRepository) {

    suspend fun register(email: String, password: String, fullName: String): RegisterResponse {
        require(email.isNotBlank()) { "Email is required" }
        require(password.length >= 6) { "Password must be at least 6 characters" }
        require(fullName.isNotBlank()) { "Name is required" }
        require(userRepo.findByEmail(email) == null) { "Email already registered" }
        val user = userRepo.create(email.trim(), password, fullName.trim(), "Warehouse")
        return RegisterResponse(user.id, user.email, user.fullName, user.role)
    }

    suspend fun login(email: String, password: String): LoginResponse? {
        val user = userRepo.checkPassword(email, password) ?: return null
        val accessToken = JwtUtils.generateAccessToken(user.id, user.role)
        val refreshToken = JwtUtils.generateRefreshToken()
        val expiresAt = LocalDateTime.now().plusDays(JwtUtils.refreshTtlDays())
        userRepo.saveRefreshToken(user.id, refreshToken, expiresAt)
        return LoginResponse(accessToken, refreshToken, user.role)
    }

    suspend fun refresh(token: String): RefreshResponse? {
        val (userId, expiresAt) = userRepo.findRefreshToken(token) ?: return null
        if (expiresAt.isBefore(LocalDateTime.now())) {
            userRepo.deleteRefreshToken(token)
            return null
        }
        val user = userRepo.findById(userId) ?: return null
        val accessToken = JwtUtils.generateAccessToken(user.id, user.role)
        return RefreshResponse(accessToken)
    }

    suspend fun logout(token: String) {
        userRepo.deleteRefreshToken(token)
    }
}
