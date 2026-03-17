package com.example.forum_auth_service.model

import jakarta.persistence.*
import org.hibernate.Hibernate
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "refresh_tokens")
@EntityListeners(AuditingEntityListener::class)
class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = false, unique = true)
    var token: String = "",

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,
) {
    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        other as RefreshToken

        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String {
        return "RefreshToken(" +
                "id=$id, " +
                "token='$token', " +
                "expiresAt=$expiresAt, " +
                "createdAt=$createdAt)"
    }
}