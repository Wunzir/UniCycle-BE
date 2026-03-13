package com.unicycle.unicycle_backend.features.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface RoleRepository : JpaRepository<Role, Long> {

    // This allows you to find "ROLE_USER" or "ROLE_ADMIN"
    // precisely as they are stored in your 'roles' table.
    fun findByName(name: String): Optional<Role>
}