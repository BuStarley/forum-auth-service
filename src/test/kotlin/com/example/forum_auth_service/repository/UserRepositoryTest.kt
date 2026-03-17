package com.example.forum_auth_service.repository

import com.example.forum_auth_service.model.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles

@EnableJpaRepositories
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
    }

    @Test
    fun `should save user`() {
        val user = User(
            email = "test@example.com",
            passwordHash = "hash123"
        )

        val saved = userRepository.save(user)

        assertNotNull(saved.id)
        assertEquals("test@example.com", saved.email)
        assertEquals("hash123", saved.passwordHash)
    }

    @Test
    fun `should find user by email`() {
        val user = User(
            email = "find@example.com",
            passwordHash = "hash123"
        )
        userRepository.save(user)

        val found = userRepository.findByEmail("find@example.com")

        assertTrue(found.isPresent)
        assertEquals("find@example.com", found.get().email)
    }

    @Test
    fun `should return empty when email not found`() {
        val found = userRepository.findByEmail("nonexistent@example.com")

        assertFalse(found.isPresent)
    }

    @Test
    fun `should check if email exists`() {
        val user = User(
            email = "exists@example.com",
            passwordHash = "hash123"
        )
        userRepository.save(user)

        assertTrue(userRepository.existsByEmail("exists@example.com"))
        assertFalse(userRepository.existsByEmail("not@example.com"))
    }

    @Test
    fun `should update user`() {
        val user = User(
            email = "update@example.com",
            passwordHash = "oldHash"
        )
        val saved = userRepository.save(user)

        saved.email = "updated@example.com"
        saved.passwordHash = "newHash"

        val updated = userRepository.save(saved)

        assertEquals("updated@example.com", updated.email)
        assertEquals("newHash", updated.passwordHash)
    }

    @Test
    fun `should delete user`() {
        val user = User(
            email = "delete@example.com",
            passwordHash = "hash"
        )
        val saved = userRepository.save(user)

        userRepository.delete(saved)

        assertFalse(userRepository.findByEmail("delete@example.com").isPresent)
    }
}