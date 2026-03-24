package com.unicycle.unicycle_backend.features.auth

import com.unicycle.unicycle_backend.features.user.*
import com.unicycle.unicycle_backend.util.JwtUtil
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userMapper: UserMapper,
    private val signupValidator: SignupValidator,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JwtUtil
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

    fun loginUser(request: LoginRequest): LoginResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )

        val user = userRepository.findByEmail(request.email)
            .orElseThrow { UsernameNotFoundException("User not found with email: \${request.email}") }

        val roles = user.roles.map { it.name }.toList()
        val jwt = jwtUtil.generateToken(user.email, roles)

        return LoginResponse(jwt, user.email, roles)
    }
}