package com.fpl.charitylink.data.model

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "system",
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)