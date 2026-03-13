package com.unicycle.unicycle_backend.features.auth

import com.unicycle.unicycle_backend.features.user.UserRepository
import org.springframework.stereotype.Component

@Component
class SignupValidator(
    private val userRepository: UserRepository
) {

    fun validate(request: SignupRequest) {
        validateEmail(request.email)
        validatePassword(request.password)
        validateNames(request.firstName, request.lastName)
    }

    private fun validateEmail(email: String) {
        if (email.isBlank()) {
            throw IllegalArgumentException("Email cannot be empty.")
        }

        if (!email.contains("@")) {
            throw IllegalArgumentException("Email must be valid.")
        }

        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("An account with this email already exists.")
        }
    }

    private fun validatePassword(password: String) {
        if (password.length < 8) {
            throw IllegalArgumentException("Password must be at least 8 characters long.")
        }

        if (!password.any { it.isUpperCase() }) {
            throw IllegalArgumentException("Password must contain at least one uppercase letter.")
        }

        if (!password.any { it.isDigit() }) {
            throw IllegalArgumentException("Password must contain at least one digit.")
        }
    }

    private fun validateNames(firstName: String, lastName: String) {
        if (firstName.isBlank()) {
            throw IllegalArgumentException("First name cannot be empty.")
        }

        if (lastName.isBlank()) {
            throw IllegalArgumentException("Last name cannot be empty.")
        }
    }
}

