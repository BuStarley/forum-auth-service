package com.example.forum_auth_service.mapper

import com.example.forum_auth_service.dto.request.RegisterRequest
import com.example.forum_auth_service.dto.response.UserResponse
import com.example.forum_auth_service.model.User
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.ReportingPolicy

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", constant = "USER")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    fun toEntity(request: RegisterRequest): User

    @Mapping(source = "passwordHash", target = "passwordHash", ignore = true)
    fun toResponse(user: User): UserResponse
}