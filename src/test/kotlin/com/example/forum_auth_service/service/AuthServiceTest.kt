package com.example.forum_auth_service.service

import com.example.forum_auth_service.dto.request.LoginRequest
import com.example.forum_auth_service.dto.request.RefreshTokenRequest
import com.example.forum_auth_service.dto.request.RegisterRequest
import com.example.forum_auth_service.dto.request.VerifyTokenRequest
import com.example.forum_auth_service.dto.response.UserResponse
import com.example.forum_auth_service.exception.InvalidCredentialsException
import com.example.forum_auth_service.exception.UserAlreadyExistsException
import com.example.forum_auth_service.mapper.UserMapper
import com.example.forum_auth_service.model.RefreshToken
import com.example.forum_auth_service.model.Role
import com.example.forum_auth_service.model.User
import com.example.forum_auth_service.repository.UserRepository
import com.example.forum_auth_service.security.JwtTokenProvider
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.*

class AuthServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var tokenService: TokenService
    private lateinit var userMapper: UserMapper
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var authenticationManager: AuthenticationManager
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var authService: AuthService

    @BeforeEach
    fun setup() {
        userRepository = mockk()
        tokenService = mockk()
        userMapper = mockk()
        passwordEncoder = mockk()
        authenticationManager = mockk()
        jwtTokenProvider = mockk()

        authService = AuthService(
            userRepository = userRepository,
            tokenService = tokenService,
            userMapper = userMapper,
            passwordEncoder = passwordEncoder,
            authenticationManager = authenticationManager,
            jwtTokenProvider = jwtTokenProvider
        )
    }

    @Test
    fun `should register new user`() {
        val request = RegisterRequest(
            email = "test@example.com",
            password = "password123"
        )

        val savedUser = User(
            email = request.email,
            passwordHash = "encoded-password",
            role = Role.USER,
            enabled = true
        ).apply {
            id = UUID.randomUUID()
        }

        val expectedResponse = UserResponse(
            id = savedUser.id!!,
            email = savedUser.email,
            role = savedUser.role,
            enabled = savedUser.enabled,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { userRepository.existsByEmail(request.email) } returns false
        every { passwordEncoder.encode(request.password) } returns "encoded-password"
        every { userRepository.save(any()) } returns savedUser
        every { userMapper.toResponse(savedUser) } returns expectedResponse

        val result = authService.register(request)

        assertEquals(expectedResponse, result)
        assertEquals(request.email, result.email)

        verify(exactly = 1) { userRepository.existsByEmail(request.email) }
        verify(exactly = 1) { passwordEncoder.encode(request.password) }
        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `should throw exception when email already exists`() {
        val request = RegisterRequest(
            email = "existing@example.com",
            password = "password123"
        )

        every { userRepository.existsByEmail(request.email) } returns true

        assertThrows<UserAlreadyExistsException> {
            authService.register(request)
        }

        verify(exactly = 1) { userRepository.existsByEmail(request.email) }
        verify(exactly = 0) { passwordEncoder.encode(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should login user successfully`() {
        val request = LoginRequest(
            email = "test@example.com",
            password = "password123"
        )

        val user = User(
            email = request.email,
            passwordHash = "encoded-password",
            role = Role.USER
        ).apply { id = UUID.randomUUID() }

        val authentication = mockk<Authentication>()
        val refreshToken = RefreshToken(
            user = user,
            token = "refresh-token",
            expiresAt = LocalDateTime.now().plusDays(7)
        ).apply { id = UUID.randomUUID() }

        every { authenticationManager.authenticate(any()) } returns authentication
        every { tokenService.generateTokens(authentication) } returns Pair("access-token", "jwt-refresh-token")
        every { userRepository.findByEmail(request.email) } returns Optional.of(user)
        every { tokenService.createRefreshToken(user) } returns refreshToken
        every { jwtTokenProvider.getAccessTokenExpiration() } returns 900000L

        val result = authService.login(request)

        assertEquals("access-token", result.accessToken)
        assertEquals("refresh-token", result.refreshToken)
        assertEquals(900000L, result.expiresIn)

        verify(exactly = 1) { authenticationManager.authenticate(any()) }
        verify(exactly = 1) { tokenService.generateTokens(authentication) }
        verify(exactly = 1) { userRepository.findByEmail(request.email) }
        verify(exactly = 1) { tokenService.createRefreshToken(user) }
    }

    @Test
    fun `should throw exception when login fails`() {
        val request = LoginRequest(
            email = "test@example.com",
            password = "wrong-password"
        )

        every { authenticationManager.authenticate(any()) } throws BadCredentialsException("Bad credentials")

        assertThrows<InvalidCredentialsException> {
            authService.login(request)
        }
    }

    @Test
    fun `should refresh token`() {
        val request = RefreshTokenRequest(refreshToken = "old-refresh-token")

        val user = User(
            email = "test@example.com",
            passwordHash = "hash",
            role = Role.ADMIN
        ).apply { id = UUID.randomUUID() }

        val refreshToken = RefreshToken(
            user = user,
            token = "old-refresh-token",
            expiresAt = LocalDateTime.now().plusDays(1)
        ).apply { id = UUID.randomUUID() }

        val newRefreshToken = RefreshToken(
            user = user,
            token = "new-refresh-token",
            expiresAt = LocalDateTime.now().plusDays(7)
        ).apply { id = UUID.randomUUID() }

        every { tokenService.verifyRefreshToken(request.refreshToken) } returns refreshToken
        every { tokenService.generateTokens(any()) } returns Pair("new-access-token", "new-jwt-refresh-token")
        every { tokenService.deleteByUser(user) } just Runs
        every { tokenService.createRefreshToken(user) } returns newRefreshToken
        every { jwtTokenProvider.getAccessTokenExpiration() } returns 900000L

        val result = authService.refresh(request)

        assertEquals("new-access-token", result.accessToken)
        assertEquals("new-refresh-token", result.refreshToken)
        assertEquals(900000L, result.expiresIn)

        verify(exactly = 1) { tokenService.verifyRefreshToken(request.refreshToken) }
        verify(exactly = 1) { tokenService.generateTokens(any()) }
        verify(exactly = 1) { tokenService.deleteByUser(user) }
        verify(exactly = 1) { tokenService.createRefreshToken(user) }
    }

    @Test
    fun `should verify valid token`() {
        val request = VerifyTokenRequest(token = "valid-token")

        every { jwtTokenProvider.validateToken(request.token) } returns true
        every { jwtTokenProvider.getUsernameFromToken(request.token) } returns "test@example.com"

        val result = authService.verify(request)

        assertTrue(result.valid)
        assertEquals("test@example.com", result.email)
    }

    @Test
    fun `should verify invalid token`() {
        val request = VerifyTokenRequest(token = "invalid-token")

        every { jwtTokenProvider.validateToken(request.token) } returns false

        val result = authService.verify(request)

        assertFalse(result.valid)
        assertNull(result.email)
    }

    @Test
    fun `should logout user`() {
        val email = "test@example.com"
        val user = User(
            email = email,
            passwordHash = "hash"
        ).apply { id = UUID.randomUUID() }

        every { userRepository.findByEmail(email) } returns Optional.of(user)
        every { tokenService.deleteByUser(user) } just Runs

        authService.logout(email)

        verify(exactly = 1) { userRepository.findByEmail(email) }
        verify(exactly = 1) { tokenService.deleteByUser(user) }
    }
}