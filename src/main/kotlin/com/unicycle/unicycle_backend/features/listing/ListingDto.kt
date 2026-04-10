package com.unicycle.unicycle_backend.features.listing

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "Listing Domain DTO Container")
class ListingDto private constructor() {

    @Schema(name = "ListingReadonly", description = "Immutable view of a listing")
    data class Readonly(
        val id: Long?,
        val userId: Long?,
        val name: String,
        val category: String,
        val picture: String?,
        val description: String?,
        val price: BigDecimal,
        val status: String
    )

    @Schema(name = "ListingCreate", description = "Data to create a listing")
    data class Create(
        val name: String,
        val category: String,
        val picture: String?,
        val description: String?,
        val price: BigDecimal
    )
}