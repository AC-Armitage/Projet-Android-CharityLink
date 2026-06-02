package com.fpl.charitylink.data.model

data class Organization(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val description: String? = null,
    val logoUrl: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val verified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)