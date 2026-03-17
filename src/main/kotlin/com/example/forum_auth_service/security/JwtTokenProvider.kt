package com.example.forum_auth_service.security

import com.example.forum_auth_service.config.JwtConfig
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.Authentication
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date


@Component
class JwtTokenProvider(
    private val jwtConfig: JwtConfig
) {
    private val log = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    private val signingKey by lazy {
        Keys.hmacShaKeyFor(jwtConfig.secret.toByteArray(StandardCharsets.UTF_8))
    }

    fun generateAccessToken(authentication: Authentication): String {
        val userDetails = authentication.principal as UserDetails
        return generateToken(userDetails.username, jwtConfig.accessTokenExpiration)
    }

    fun generateRefreshToken(authentication: Authentication): String {
        val userDetails = authentication.principal as UserDetails
        return generateToken(userDetails.username, jwtConfig.refreshTokenExpiration)
    }

    private fun generateToken(username: String, expirationMs: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationMs)

        return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact()
    }

    fun getUsernameFromToken(token: String): String =
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject

    fun validateToken(token: String): Boolean =
        runCatching {
            Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
            true
        }.getOrElse { exception ->
            when (exception) {
                is SecurityException -> log.error("Invalid JWT signature: ${exception.message}")
                is MalformedJwtException -> log.error("Invalid JWT token: ${exception.message}")
                is ExpiredJwtException -> log.error("JWT token is expired: ${exception.message}")
                is UnsupportedJwtException -> log.error("JWT token is unsupported: ${exception.message}")
                is IllegalArgumentException -> log.error("JWT claims string is empty: ${exception.message}")
                else -> log.error("JWT validation error: ${exception.message}")
            }
            false
        }
}