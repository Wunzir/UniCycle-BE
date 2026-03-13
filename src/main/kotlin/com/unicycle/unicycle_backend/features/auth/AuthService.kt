package com.unicycle.unicycle_backend.features.auth

import com.unicycle.unicycle_backend.features.user.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userMapper: UserMapper,
    private val signupValidator: SignupValidator
) {

    @Transactional
    fun registerUser(request: SignupRequest): UserDto.Readonly {
        signupValidator.validate(request)

        val userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow { RuntimeException("Error: Default Role not found.") }

        val user = User(
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password)!!,
            university = request.university,
            roles = setOf(userRole)
        )

        val savedUser = userRepository.save(user)

        // Map to DTO here before returning to the Controller
        return userMapper.toUserReadonlyDto(savedUser)
    }
}