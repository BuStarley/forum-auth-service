package com.example.forum_auth_service.mapper

import com.example.forum_auth_service.dto.request.RegisterRequest
import com.example.forum_auth_service.dto.response.UserResponse
import com.example.forum_auth_service.model.Role
import com.example.forum_auth_service.model.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class UserMapperTest {

    private val userMapper = object : UserMapper {
        override fun toEntity(request: RegisterRequest): User {
            return User(
                email = request.email,
                passwordHash = "",
                role = Role.USER,
                enabled = true
            )
        }

        override fun toResponse(user: User): UserResponse {
            return UserResponse(
                id = user.id!!,
                email = user.email,
                role = user.role,
                enabled = user.enabled,
                createdAt = user.createdAt!!,
                updatedAt = user.updatedAt!!
            )
        }
    }

    @Test
    fun `should map RegisterRequest to User entity`() {
        val request = RegisterRequest(
            email = "test@example.com",
            password = "password123"
        )

        val user = userMapper.toEntity(request)

        assertEquals(request.email, user.email)
        assertEquals("", user.passwordHash)  // Ожидаем пустую строку
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
}