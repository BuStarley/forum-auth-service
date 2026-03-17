package com.example.forum_auth_service.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class AuthResponse(
    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("refresh_token")
    val refreshToken: String,

    @JsonProperty("token_type")
    val tokenType: String = "Bearer",

    @JsonProperty("expires_in")
    val expiresIn: Long
)