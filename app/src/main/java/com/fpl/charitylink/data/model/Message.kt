package com.fpl.charitylink.data.model

data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false
)

data class Chat(
    val id: String = "",
    val donorId: String = "",
    val donorName: String = "",
    val associationId: String = "",
    val associationName: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0
)