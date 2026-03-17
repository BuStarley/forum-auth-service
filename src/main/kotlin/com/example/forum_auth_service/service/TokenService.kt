package com.example.forum_auth_service.service

import com.example.forum_auth_service.config.JwtConfig
import com.example.forum_auth_service.exception.TokenRefreshException
import com.example.forum_auth_service.model.RefreshToken
import com.example.forum_auth_service.model.User
import com.example.forum_auth_service.repository.RefreshTokenRepository
import com.example.forum_auth_service.security.JwtTokenProvider
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class TokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtConfig: JwtConfig
) {

    private val log = LoggerFactory.getLogger(TokenService::class.java)

    @Transactional
    fun createRefreshToken(user: User): RefreshToken {
        log.debug("Creating refresh token for user: {}", user.email)

        refreshTokenRepository.deleteByUser(user)

        val refreshToken = RefreshToken(
            user = user,
            token = UUID.randomUUID().toString(),
            expiresAt = LocalDateTime.now().plusSeconds(jwtConfig.refreshTokenExpiration / 1000)
        )

        val saved = refreshTokenRepository.save(refreshToken)
        log.debug("Refresh token created for user: {}, expires at: {}", user.email, saved.expiresAt)

        return saved
    }

    @Transactional
    fun verifyRefreshToken(token: String): RefreshToken {
        log.debug("Verifying refresh token")

        val refreshToken = refreshTokenRepository.findByToken(token)
            .orElseThrow {
                log.warn("Invalid refresh token attempt")
                TokenRefreshException("Invalid refresh token")
            }

        if (refreshToken.isExpired()) {
            log.warn("Expired refresh token for user: {}", refreshToken.user.email)
            refreshTokenRepository.delete(refreshToken)
            throw TokenRefreshException("Refresh token expired")
        }

        log.debug("Refresh token valid for user: {}", refreshToken.user.email)
        return refreshToken
    }

    @Transactional
    fun deleteByUser(user: User) {
        log.debug("Deleting all refresh tokens for user: {}", user.email)
        refreshTokenRepository.deleteByUser(user)
    }

    fun generateTokens(authentication: Authentication): Pair<String, String> {
        log.debug("Generating token pair")
        val accessToken = jwtTokenProvider.generateAccessToken(authentication)
        val refreshToken = jwtTokenProvider.generateRefreshToken(authentication)
        return Pair(accessToken, refreshToken)
    }
}