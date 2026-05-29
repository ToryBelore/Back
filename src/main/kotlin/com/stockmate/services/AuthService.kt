package com.stockmate.services

import com.stockmate.models.dto.LoginResponse
import com.stockmate.models.dto.RefreshResponse
import com.stockmate.repositories.UserRepository
import com.stockmate.utils.JwtUtils
import java.time.LocalDateTime

class AuthService(private val userRepo: UserRepository) {

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
