package com.unicycle.unicycle_backend.features.user

import jakarta.persistence.*

@Entity
@Table(name = "roles")
class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val name: String // e.g., "ROLE_USER", "ROLE_ADMIN"
)