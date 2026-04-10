package com.unicycle.unicycle_backend.features.listing

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController

@RestController
@PreAuthorize("hasRole('ROLE_USER')")
class ListingController {
}