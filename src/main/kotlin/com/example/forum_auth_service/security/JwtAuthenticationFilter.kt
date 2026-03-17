package com.example.forum_auth_service.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: UserDetailsServiceImpl
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        runCatching {
            request.getToken()
                ?.takeIf { jwtTokenProvider.validateToken(it) }
                ?.let { token ->
                    val username = jwtTokenProvider.getUsernameFromToken(token)
                    userDetailsService.loadUserByUsername(username)
                }
                ?.let { userDetails ->
                    createAuthentication(userDetails, request)
                }
        }.onFailure { e ->
            log.error("Cannot set user authentication: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }

    private fun createAuthentication(userDetails: UserDetails, request: HttpServletRequest) {
        UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.authorities
        ).apply {
            details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = this
        }
    }

    private fun HttpServletRequest.getToken(): String? {
        val bearerToken = getHeader("Authorization")
        return bearerToken.takeIf {
            StringUtils.hasText(it) && it.startsWith("Bearer ")
        }?.substring(7)
    }
}