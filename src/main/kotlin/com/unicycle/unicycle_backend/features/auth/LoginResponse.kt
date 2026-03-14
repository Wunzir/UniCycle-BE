package com.unicycle.unicycle_backend.features.auth

import com.unicycle.unicycle_backend.features.user.UserDto

/*This is for a response to a successful login
* and will be sent to Front End*/
data class LoginResponse (
    /*This is for a response to a successful login
* and will be sent to Front End
* UserDto basically has name and email so it does not send the full user info
* Excludes Password hash*/
    val user: UserDto.Readonly,
    val message: String = "Login successful:"
)
