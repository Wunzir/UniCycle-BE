package com.unicycle.unicycle_backend.features.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {

    // Spring parses the name "findByEmail" and generates:
    // SELECT * FROM users WHERE email = ?
    fun findByEmail(email: String): Optional<User>

    // Useful for signup validation to prevent duplicate accounts
    fun existsByEmail(email: String): Boolean

    // If you ever need to find all students from a specific school (like RPI)
    fun findAllByUniversity(university: String): List<User>
}
