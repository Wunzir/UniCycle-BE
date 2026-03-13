package com.unicycle.unicycle_backend.features.user

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Auth Domain DTO Container")
class UserDto private constructor() {

    @Schema(name = "UserReadonly", description = "Immutable view of a user")
    data class Readonly(
        val id: Long?,
        val email: String,
        val firstName: String,
        val lastName: String,
        val university: String,
        val isVerified: Boolean
    )
}