package com.fpl.charitylink.data.model

data class Campaign(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val goalAmount: Double = 0.0,
    val raisedAmount: Double = 0.0,
    val associationId: String = "",
    val associationName: String = "",
    val category: String = "",
    val status: String = "active",
    val createdAt: Long = System.currentTimeMillis(),
    val deadline: Long? = null
)