package com.unicycle.unicycle_backend.features.user

import org.springframework.stereotype.Component

@Component
class UserMapper {

    // The "Read-Only" version for public or general use
    fun toUserReadonlyDto(user: User): UserDto.Readonly {
        return UserDto.Readonly(
            id = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            university = user.university,
            isVerified = user.isVerified
        )
    }

}