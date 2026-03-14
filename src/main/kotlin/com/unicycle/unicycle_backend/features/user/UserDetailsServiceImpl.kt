package com.unicycle.unicycle_backend.features.user

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * This is the UserDetailsService Implementation
 * Used to retrieve user details from the database while a user attempts to log in
 */

@Service

class UserDetailsServiceImpl ( private val userRepository: UserRepository) : UserDetailsService {
    /**
     * First we load a users record with their email,
     * which will be used instead of a username
     */
    override fun loadUserByUsername(email: String): UserDetails {
        /**
         * Find email and return their details if not, then Throw if no email found
         */
        return userRepository.findByEmail(email)
            .orElseThrow{ UsernameNotFoundException("User not found with email: $email") }
    }
}