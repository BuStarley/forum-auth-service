package com.example.forum_auth_service.service

import com.example.forum_auth_service.config.JwtConfig
import com.example.forum_auth_service.exception.TokenRefreshException
import com.example.forum_auth_service.model.RefreshToken
import com.example.forum_auth_service.model.Role
import com.example.forum_auth_service.model.User
import com.example.forum_auth_service.repository.RefreshTokenRepository
import com.example.forum_auth_service.security.JwtTokenProvider
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime
import java.util.*

class TokenServiceTest {

    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var jwtConfig: JwtConfig
    private lateinit var tokenService: TokenService

    @BeforeEach
    fun setup() {
        refreshTokenRepository = mockk()
        jwtTokenProvider = mockk()
        jwtConfig = mockk()
        tokenService = TokenService(refreshTokenRepository, jwtTokenProvider, jwtConfig)
    }

    @Test
    fun `should create refresh token`() {
        // given
        val user = User(
            email = "test@example.com",
            passwordHash = "hash",
            role = Role.USER
        ).apply { id = UUID.randomUUID() }

        every { jwtConfig.refreshTokenExpiration } returns 604800000L
        every { refreshTokenRepository.deleteByUser(user) } just Runs
        every { refreshTokenRepository.save(any()) } answers { firstArg() }

        val result = tokenService.createRefreshToken(user)

        assertNotNull(result.token)
        assertEquals(user, result.user)
        assertFalse(result.isExpired())
        verify(exactly = 1) { refreshTokenRepository.deleteByUser(user) }
        verify(exactly = 1) { refreshTokenRepository.save(any()) }
    }

    @Test
    fun `should verify valid refresh token`() {
        val user = User(
            email = "test@example.com",
            passwordHash = "hash"
        ).apply { id = UUID.randomUUID() }

        val token = RefreshToken(
            user = user,
            token = "valid-token",
            expiresAt = LocalDateTime.now().plusDays(1)
        )

        every { refreshTokenRepository.findByToken("valid-token") } returns Optional.of(token)

        val result = tokenService.verifyRefreshToken("valid-token")

        assertEquals(token, result)
        assertFalse(result.isExpired())
    }

    @Test
    fun `should throw exception when token not found`() {
        every { refreshTokenRepository.findByToken("invalid-token") } returns Optional.empty()

        assertThrows<TokenRefreshException> {
            tokenService.verifyRefreshToken("invalid-token")
        }
    }

    @Test
    fun `should throw exception and delete expired token`() {
        val user = User(
            email = "test@example.com",
            passwordHash = "hash"
        ).apply { id = UUID.randomUUID() }

        val expiredToken = RefreshToken(
            user = user,
            token = "expired-token",
            expiresAt = LocalDateTime.now().minusDays(1)
        )

        every { refreshTokenRepository.findByToken("expired-token") } returns Optional.of(expiredToken)
        every { refreshTokenRepository.delete(expiredToken) } just Runs

        assertThrows<TokenRefreshException> {
            tokenService.verifyRefreshToken("expired-token")
        }

        verify(exactly = 1) { refreshTokenRepository.delete(expiredToken) }
    }

    @Test
    fun `should delete tokens by user`() {
        val user = User(
            email = "test@example.com",
            passwordHash = "hash"
        )
        every { refreshTokenRepository.deleteByUser(user) } just Runs

        tokenService.deleteByUser(user)

        verify(exactly = 1) { refreshTokenRepository.deleteByUser(user) }
    }

    @Test
    fun `should generate access and refresh tokens`() {
        val userDetails = mockk<UserDetails>()
        every { userDetails.username } returns "test@example.com"

        val authentication = UsernamePasswordAuthenticationToken(
            userDetails, null, listOf()
        )

        every { jwtTokenProvider.generateAccessToken(authentication) } returns "access-token"
        every { jwtTokenProvider.generateRefreshToken(authentication) } returns "refresh-token"

        val (accessToken, refreshToken) = tokenService.generateTokens(authentication)

        assertEquals("access-token", accessToken)
        assertEquals("refresh-token", refreshToken)
    }
}