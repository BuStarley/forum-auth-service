package com.example.forum_auth_service.repository

import com.example.forum_auth_service.config.TestJpaAuditingConfig
import com.example.forum_auth_service.model.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaAuditingConfig::class)
class UserRepositoryTest {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var testEntityManager: TestEntityManager

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

        val saved = testEntityManager.persistAndFlush(user)  // 👈 Используем

        assertNotNull(saved.id)
        assertEquals("test@example.com", saved.email)
        assertEquals("hash123", saved.passwordHash)
        assertNotNull(saved.createdAt)  // 👈 Теперь должно быть не null
        assertNotNull(saved.updatedAt)
    }

    @Test
    fun `should find user by email`() {
        val user = User(
            email = "find@example.com",
            passwordHash = "hash123"
        )
        testEntityManager.persistAndFlush(user)

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
        testEntityManager.persistAndFlush(user)

        assertTrue(userRepository.existsByEmail("exists@example.com"))
        assertFalse(userRepository.existsByEmail("not@example.com"))
    }

    @Test
    fun `should update user`() {
        val user = User(
            email = "update@example.com",
            passwordHash = "oldHash"
        )
        val saved = testEntityManager.persistAndFlush(user)

        saved.email = "updated@example.com"
        saved.passwordHash = "newHash"

        val updated = userRepository.save(saved)
        testEntityManager.flush()  // Принудительно применяем изменения

        assertEquals("updated@example.com", updated.email)
        assertEquals("newHash", updated.passwordHash)
    }

    @Test
    fun `should delete user`() {
        val user = User(
            email = "delete@example.com",
            passwordHash = "hash"
        )
        val saved = testEntityManager.persistAndFlush(user)

        userRepository.delete(saved)
        testEntityManager.flush()

        assertFalse(userRepository.findByEmail("delete@example.com").isPresent)
    }
}