package com.example.forum_auth_service.repository

import com.example.forum_auth_service.config.TestJpaAuditingConfig
import com.example.forum_auth_service.model.RefreshToken
import com.example.forum_auth_service.model.Role
import com.example.forum_auth_service.model.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.UUID

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaAuditingConfig::class)
class RefreshTokenRepositoryTest {

    @Autowired
    lateinit var refreshTokenRepository: RefreshTokenRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        refreshTokenRepository.deleteAll()
        userRepository.deleteAll()

        testUser = User(
            email = "test@example.com",
            passwordHash = "hash123",
            role = Role.USER
        )
        testUser = testEntityManager.persistAndFlush(testUser)
    }

    @Test
    fun `should save refresh token`() {
        val token = RefreshToken(
            user = testUser,
            token = "test-token-${UUID.randomUUID()}",
            expiresAt = LocalDateTime.now().plusDays(7)
        )

        val saved = refreshTokenRepository.save(token)

        assertNotNull(saved.id)
        assertEquals(testUser.id, saved.user.id)
        assertEquals(token.token, saved.token)
        assertEquals(token.expiresAt.toLocalDate(), saved.expiresAt.toLocalDate())
        assertNotNull(saved.createdAt)
    }

    @Test
    fun `should find token by token string`() {
        val tokenString = "unique-token-${UUID.randomUUID()}"
        val token = RefreshToken(
            user = testUser,
            token = tokenString,
            expiresAt = LocalDateTime.now().plusDays(7)
        )
        refreshTokenRepository.save(token)

        val found = refreshTokenRepository.findByToken(tokenString)

        assertTrue(found.isPresent)
        assertEquals(tokenString, found.get().token)
        assertEquals(testUser.id, found.get().user.id)
    }

    @Test
    fun `should return empty when token not found`() {
        val found = refreshTokenRepository.findByToken("non-existent-token")

        assertFalse(found.isPresent)
    }

    @Test
    fun `should delete tokens by user`() {
        val token1 = RefreshToken(
            user = testUser,
            token = "token1-${UUID.randomUUID()}",
            expiresAt = LocalDateTime.now().plusDays(7)
        )
        val token2 = RefreshToken(
            user = testUser,
            token = "token2-${UUID.randomUUID()}",
            expiresAt = LocalDateTime.now().plusDays(7)
        )
        refreshTokenRepository.save(token1)
        refreshTokenRepository.save(token2)

        refreshTokenRepository.deleteByUser(testUser)

        val tokens = refreshTokenRepository.findAllByUser(testUser)
        assertTrue(tokens.isEmpty())
    }

    @Test
    fun `should delete expired tokens`() {
        val expiredToken = RefreshToken(
            user = testUser,
            token = "expired-${UUID.randomUUID()}",
            expiresAt = LocalDateTime.now().minusDays(1)
        )
        val validToken = RefreshToken(
            user = testUser,
            token = "valid-${UUID.randomUUID()}",
            expiresAt = LocalDateTime.now().plusDays(7)
        )
        refreshTokenRepository.save(expiredToken)
        refreshTokenRepository.save(validToken)

        refreshTokenRepository.deleteAllByExpiresAtBefore(LocalDateTime.now())

        val remainingTokens = refreshTokenRepository.findAllByUser(testUser)
        assertEquals(1, remainingTokens.size)
        assertEquals(validToken.token, remainingTokens[0].token)
    }

    @Test
    fun `should find all tokens by user`() {
        val token1 = RefreshToken(
            user = testUser,
            token = "token1-${UUID.randomUUID()}",
            expiresAt = LocalDateTime.now().plusDays(7)
        )
        val token2 = RefreshToken(
            user = testUser,
            token = "token2-${UUID.randomUUID()}",
            expiresAt = LocalDateTime.now().plusDays(7)
        )
        refreshTokenRepository.save(token1)
        refreshTokenRepository.save(token2)

        val tokens = refreshTokenRepository.findAllByUser(testUser)

        assertEquals(2, tokens.size)
        assertTrue(tokens.any { it.token == token1.token })
        assertTrue(tokens.any { it.token == token2.token })
    }
}