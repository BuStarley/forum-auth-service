package com.example.forum_auth_service.dto.request

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RegisterRequestTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        val factory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    @Test
    fun `should validate valid request`() {
        val request = RegisterRequest(
            email = "test@example.com",
            password = "password123"
        )

        val violations = validator.validate(request)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `should fail when email is invalid`() {
        val request = RegisterRequest(
            email = "invalid-email",
            password = "password123"
        )

        val violations = validator.validate(request)
        assertFalse(violations.isEmpty())
        assertTrue(violations.any { it.propertyPath.toString() == "email" })
    }

    @Test
    fun `should fail when password is too short`() {
        val request = RegisterRequest(
            email = "test@example.com",
            password = "123"
        )

        val violations = validator.validate(request)
        assertFalse(violations.isEmpty())
        assertTrue(violations.any { it.propertyPath.toString() == "password" })
    }

    @Test
    fun `should allow null fullName`() {
        val request = RegisterRequest(
            email = "test@example.com",
            password = "password123"
        )

        val violations = validator.validate(request)
        assertTrue(violations.isEmpty())
    }
}