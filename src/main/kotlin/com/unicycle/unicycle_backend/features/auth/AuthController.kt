package com.unicycle.unicycle_backend.features.auth

import com.unicycle.unicycle_backend.features.user.UserDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/signup")
    fun registerUser(@Valid @RequestBody signupRequest: SignupRequest): ResponseEntity<UserDto.Readonly> {
        // The service now returns the DTO directly
        val userDto = authService.registerUser(signupRequest)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(userDto)
    }
}