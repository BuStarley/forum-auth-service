package com.example.forum_auth_service.repository

import com.example.forum_auth_service.model.RefreshToken
import com.example.forum_auth_service.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {

    fun findByToken(token: String): Optional<RefreshToken>

    fun deleteByUser(user: User)

    fun deleteAllByExpiresAtBefore(time: LocalDateTime)

    fun findAllByUser(user: User): List<RefreshToken>
}