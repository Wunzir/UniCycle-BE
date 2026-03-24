package com.unicycle.unicycle_backend.features.auth

data class LoginResponse(
    val jwt: String,
    val email: String,
    val roles: List<String>
)
