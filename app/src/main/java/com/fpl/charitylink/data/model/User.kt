package com.fpl.charitylink.data.model

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = "donor",
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)