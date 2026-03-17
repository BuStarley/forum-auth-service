package com.example.forum_auth_service.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class UserTest {

    @Test
    fun `should create user with default values`() {
        val user = User(
            email = "test@example.com",
            passwordHash = "hashedPassword123"
        )

        assertEquals("test@example.com", user.email)
        assertEquals("hashedPassword123", user.passwordHash)
        assertEquals(Role.USER, user.role)
        assertTrue(user.enabled)
        assertNull(user.id)
        assertNull(user.createdAt)
        assertNull(user.updatedAt)
    }

    @Test
    fun `should create user with custom role`() {
        val user = User(
            email = "admin@example.com",
            passwordHash = "adminHash",
            role = Role.ADMIN
        )

        assertEquals(Role.ADMIN, user.role)
    }

    @Test
    fun `should update email and password`() {
        val user = User(
            email = "old@example.com",
            passwordHash = "oldHash"
        )

        user.email = "new@example.com"
        user.passwordHash = "newHash"

        assertEquals("new@example.com", user.email)
        assertEquals("newHash", user.passwordHash)
    }

    @Test
    fun `should disable user`() {
        val user = User(
            email = "test@example.com",
            passwordHash = "hash"
        )

        user.enabled = false

        assertFalse(user.enabled)
    }

    @Test
    fun `equals should work based on id`() {
        val user1 = User(
            email = "test@example.com",
            passwordHash = "hash"
        ).apply { id = UUID.randomUUID() }

        val user2 = User(
            email = "test@example.com",
            passwordHash = "hash"
        ).apply { id = user1.id }

        val user3 = User(
            email = "other@example.com",
            passwordHash = "hash"
        ).apply { id = UUID.randomUUID() }

        assertEquals(user1, user2)
        assertNotEquals(user1, user3)
    }
}