package com.example.forum_auth_service.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "jwt")
class JwtConfig {
    var secret: String = ""
    var accessTokenExpiration: Long = 0
    var refreshTokenExpiration: Long = 0
}