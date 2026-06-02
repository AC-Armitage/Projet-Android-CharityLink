package com.fpl.charitylink.data.model

data class Donation(
    val id: String = "",
    val donorId: String = "",
    val donorName: String = "",
    val campaignId: String = "",
    val campaignTitle: String = "",
    val associationId: String = "",
    val amount: Double = 0.0,
    val message: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)