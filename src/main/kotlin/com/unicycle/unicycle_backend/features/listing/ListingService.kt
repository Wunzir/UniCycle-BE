package com.unicycle.unicycle_backend.features.listing

import com.unicycle.unicycle_backend.features.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListingService(
    private val listingRepository: ListingRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createListing(userId: Long, dto: ListingDto.Create): ListingDto.Readonly {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("User not found")
        }

        val listing = Listing(
            user = user,
            name = dto.name,
            category = dto.category,
            picture = dto.picture,
            description = dto.description,
            price = dto.price
        )

        val saved = listingRepository.save(listing)
        return mapToReadonly(saved)
    }

    fun getListingById(id: Long): ListingDto.Readonly {
        val listing = listingRepository.findById(id).orElseThrow {
            IllegalArgumentException("Listing not found")
        }
        return mapToReadonly(listing)
    }

    fun getAllListingsByUniversity(university: String): List<ListingDto.Readonly> {
        return listingRepository.findAllByUniversity(university).map { mapToReadonly(it) }
    }

    fun getListingsByUserId(userId: Long): List<ListingDto.Readonly> {
        return listingRepository.findAllByUserId(userId).map { mapToReadonly(it) }
    }

    @Transactional
    fun deleteListing(id: Long) {
        listingRepository.deleteById(id)
    }

    private fun mapToReadonly(listing: Listing) = ListingDto.Readonly(
        id = listing.id,
        userId = listing.user.id,
        name = listing.name,
        category = listing.category,
        picture = listing.picture,
        description = listing.description,
        price = listing.price,
        status = listing.status
    )
}