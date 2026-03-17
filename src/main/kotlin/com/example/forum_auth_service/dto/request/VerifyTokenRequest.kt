package com.example.forum_auth_service.dto.request

import jakarta.validation.constraints.NotBlank

data class VerifyTokenRequest(
    @field:NotBlank(message = "Token is required")
    val token: String
)