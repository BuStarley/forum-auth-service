package com.example.forum_auth_service.dto.response

import com.example.forum_auth_service.model.Role
import java.time.LocalDateTime
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val email: String,
    val role: Role,
    val enabled: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)