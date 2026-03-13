package com.unicycle.unicycle_backend.features.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignupRequest(
    @field:Email(message = "Invalid email format")
    @field:NotBlank
    val email: String,

    @field:Size(min = 8, message = "Password too short")
    val password: String,

    @field:NotBlank
    val firstName: String,

    @field:NotBlank
    val lastName: String,

    @field:NotBlank
    val university: String
)