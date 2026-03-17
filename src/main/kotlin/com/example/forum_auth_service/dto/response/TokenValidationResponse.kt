package com.example.forum_auth_service.dto.response

data class TokenValidationResponse(
    val valid: Boolean,
    val email: String? = null
)