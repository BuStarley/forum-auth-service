package com.example.forum_auth_service.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class RefreshTokenTest {

    @Test
    fun `should create refresh token`() {
        val user = User(
            email = "test@example.com",
            passwordHash = "hash123"
        )
        val token = "test-refresh-token"
        val expiresAt = LocalDateTime.now().plusDays(7)

        val refreshToken = RefreshToken(
            user = user,
            token = token,
            expiresAt = expiresAt
        )

        assertNull(refreshToken.id)
        assertEquals(user, refreshToken.user)
        assertEquals(token, refreshToken.token)
        assertEquals(expiresAt, refreshToken.expiresAt)
        assertNull(refreshToken.createdAt)
    }

    @Test
    fun `should detect expired token`() {
        val user = User(
            email = "test@example.com",
            passwordHash = "hash123"
        )
        val expiredTime = LocalDateTime.now().minusDays(1)

        val refreshToken = RefreshToken(
            user = user,
            token = "expired-token",
            expiresAt = expiredTime
        )

        assertTrue(refreshToken.isExpired())
    }

    @Test
    fun `should detect non-expired token`() {
        val user = User(
            email = "test@example.com",
            passwordHash = "hash123"
        )
        val futureTime = LocalDateTime.now().plusDays(7)

        val refreshToken = RefreshToken(
            user = user,
            token = "valid-token",
            expiresAt = futureTime
        )

        assertFalse(refreshToken.isExpired())
    }

    @Test
    fun `equals should work based on id`() {
        val user = User(
            email = "test@example.com",
            passwordHash = "hash123"
        )
        val id = UUID.randomUUID()

        val token1 = RefreshToken(
            user = user,
            token = "token1",
            expiresAt = LocalDateTime.now()
        ).apply { this.id = id }

        val token2 = RefreshToken(
            user = user,
            token = "token2", // разные токены, но одинаковый id
            expiresAt = LocalDateTime.now()
        ).apply { this.id = id }

        val token3 = RefreshToken(
            user = user,
            token = "token3",
            expiresAt = LocalDateTime.now()
        ).apply { this.id = UUID.randomUUID() }

        assertEquals(token1, token2)
        assertNotEquals(token1, token3)
    }
}