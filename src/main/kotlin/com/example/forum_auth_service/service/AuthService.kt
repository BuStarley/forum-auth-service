package com.example.forum_auth_service.service

import com.example.forum_auth_service.dto.request.LoginRequest
import com.example.forum_auth_service.dto.request.RefreshTokenRequest
import com.example.forum_auth_service.dto.request.RegisterRequest
import com.example.forum_auth_service.dto.request.VerifyTokenRequest
import com.example.forum_auth_service.dto.response.AuthResponse
import com.example.forum_auth_service.dto.response.TokenValidationResponse
import com.example.forum_auth_service.dto.response.UserResponse
import com.example.forum_auth_service.exception.InvalidCredentialsException
import com.example.forum_auth_service.exception.UserAlreadyExistsException
import com.example.forum_auth_service.mapper.UserMapper
import com.example.forum_auth_service.model.Role
import com.example.forum_auth_service.model.User
import com.example.forum_auth_service.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
    private val userMapper: UserMapper,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: com.example.forum_auth_service.security.JwtTokenProvider
) {

    private val log = LoggerFactory.getLogger(AuthService::class.java)

    @Transactional
    fun register(request: RegisterRequest): UserResponse {
        log.info("Attempting to register user with email: {}", request.email)

        if (userRepository.existsByEmail(request.email)) {
            log.warn("Registration failed: email {} already exists", request.email)
            throw UserAlreadyExistsException("User with email ${request.email} already exists")
        }

        val user = User(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password)!!,
            role = Role.USER,
            enabled = true
        )

        val savedUser = userRepository.save(user)
        log.info("User registered successfully with email: {}, id: {}", savedUser.email, savedUser.id)

        return userMapper.toResponse(savedUser)
    }

    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        log.info("Login attempt for email: {}", request.email)

        try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    request.email,
                    request.password
                )
            )

            val (accessToken) = tokenService.generateTokens(authentication)

            val user = userRepository.findByEmail(request.email)
                .orElseThrow { InvalidCredentialsException("User not found") }

            val savedRefreshToken = tokenService.createRefreshToken(user)

            log.info("User logged in successfully: {}", request.email)

            return AuthResponse(
                accessToken = accessToken,
                refreshToken = savedRefreshToken.token,
                expiresIn = jwtTokenProvider.getAccessTokenExpiration()
            )

        } catch (e: AuthenticationException) {
            log.warn("Failed login attempt for email: {}", request.email)
            throw InvalidCredentialsException("Invalid email or password")
        }
    }

    @Transactional
    fun refresh(request: RefreshTokenRequest): AuthResponse {
        log.info("Attempting to refresh token")

        val refreshToken = tokenService.verifyRefreshToken(request.refreshToken)
        val user = refreshToken.user

        log.info("Refresh token valid for user: {}", user.email)

        val authentication = UsernamePasswordAuthenticationToken(
            user.email,
            null,
            listOf(org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_${user.role.name}"))
        )

        val (newAccessToken, newRefreshToken) = tokenService.generateTokens(authentication)

        tokenService.deleteByUser(user)
        val savedRefreshToken = tokenService.createRefreshToken(user)

        log.info("Tokens refreshed successfully for user: {}", user.email)

        return AuthResponse(
            accessToken = newAccessToken,
            refreshToken = savedRefreshToken.token,
            expiresIn = jwtTokenProvider.getAccessTokenExpiration()
        )
    }

    fun verify(request: VerifyTokenRequest): TokenValidationResponse {
        log.debug("Verifying token")

        val isValid = jwtTokenProvider.validateToken(request.token)

        return if (isValid) {
            val email = jwtTokenProvider.getUsernameFromToken(request.token)
            log.debug("Token valid for user: {}", email)
            TokenValidationResponse(valid = true, email = email)
        } else {
            log.debug("Token invalid")
            TokenValidationResponse(valid = false)
        }
    }

    @Transactional
    fun logout(email: String) {
        log.info("Logout attempt for user: {}", email)

        val user = userRepository.findByEmail(email)
            .orElseThrow { InvalidCredentialsException("User not found") }

        tokenService.deleteByUser(user)
        log.info("User logged out successfully: {}", email)
    }

    fun getAccessTokenExpiration(): Long = jwtTokenProvider.getAccessTokenExpiration()
}