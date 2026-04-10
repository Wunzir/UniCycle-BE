package com.unicycle.unicycle_backend.features.listing

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ListingRepository : JpaRepository<Listing, Long> {

    // Get all listings for a specific user
    fun findAllByUserId(userId: Long): List<Listing>

    // Get all listings for a specific university using an implicit join
    @Query("SELECT l FROM Listing l JOIN l.user u WHERE u.university = :university")
    fun findAllByUniversity(@Param("university") university: String): List<Listing>
}