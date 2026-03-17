package com.example.forum_auth_service.mapper

import com.example.forum_auth_service.dto.request.RegisterRequest
import com.example.forum_auth_service.model.Role
import com.example.forum_auth_service.model.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mapstruct.factory.Mappers
import java.time.LocalDateTime
import java.util.UUID

class UserMapperTest {

    private val userMapper = Mappers.getMapper(UserMapper::class.java)

    @Test
    fun `should map RegisterRequest to User entity`() {
        val request = RegisterRequest(
            email = "test@example.com",
            password = "password123"
        )

        val user = userMapper.toEntity(request)

        assertEquals(request.email, user.email)
        assertNull(user.passwordHash)
        assertEquals(Role.USER, user.role)
        assertTrue(user.enabled)
        assertNull(user.id)
        assertNull(user.createdAt)
        assertNull(user.updatedAt)
    }

    @Test
    fun `should map User entity to UserResponse`() {
        val user = User(
            email = "test@example.com",
            passwordHash = "hashedPassword123",
            role = Role.ADMIN,
            enabled = true
        ).apply {
            id = UUID.randomUUID()
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
        }

        val response = userMapper.toResponse(user)

        assertEquals(user.id, response.id)
        assertEquals(user.email, response.email)
        assertEquals(user.role, response.role)
        assertEquals(user.enabled, response.enabled)
        assertEquals(user.createdAt, response.createdAt)
        assertEquals(user.updatedAt, response.updatedAt)
    }

    @Test
    fun `should handle null fullName in RegisterRequest`() {
        val request = RegisterRequest(
            email = "test@example.com",
            password = "password123"
        )

        val user = userMapper.toEntity(request)

        assertEquals(request.email, user.email)
    }
}