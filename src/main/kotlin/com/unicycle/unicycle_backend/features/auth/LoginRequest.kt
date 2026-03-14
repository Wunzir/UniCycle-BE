package com.unicycle.unicycle_backend.features.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
/**
 * This is the LoginRequest DTO, Data Transfer Object
 * Handles User log in requests
 */
data class LoginRequest (
    /**
     * First is checking if the given email is an actual email, before checking database
     * Then for password we check if it's not blank
     */
    @field:Email
    val email: String,

    @field:NotBlank
    val password: String

)