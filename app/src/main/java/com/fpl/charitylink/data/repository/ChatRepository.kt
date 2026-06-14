package com.fpl.charitylink.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.fpl.charitylink.data.model.Chat
import com.fpl.charitylink.data.model.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val db = FirebaseDatabase.getInstance()
    private val chatsRef = db.getReference("chats")
    private val userChatsRef = db.getReference("userChats")

    // Generate consistent chat ID from two user IDs
    fun getChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}"
        else "${userId2}_${userId1}"
    }

    // Send a message
    suspend fun sendMessage(
        chatId: String,
        message: Message,
        donorId: String,
        donorName: String,
        associationId: String,
        associationName: String
    ) {
        // Save message
        val msgRef = chatsRef.child(chatId).child("messages").push()
        val msgWithId = message.copy(id = msgRef.key ?: "")
        msgRef.setValue(msgWithId).await()

        // Update chat metadata for both users
        val chatData = mapOf(
            "id" to chatId,
            "donorId" to donorId,
            "donorName" to donorName,
            "associationId" to associationId,
            "associationName" to associationName,
            "lastMessage" to message.text,
            "lastMessageTime" to message.timestamp
        )
        userChatsRef.child(donorId).child(chatId).setValue(chatData).await()
        userChatsRef.child(associationId).child(chatId).setValue(chatData).await()
    }

    // Listen to messages in real time
    fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull {
                    it.getValue(Message::class.java)
                }.sortedBy { it.timestamp }
                trySend(messages)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = chatsRef.child(chatId).child("messages")
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // Get all chats for a user in real time
    fun getUserChats(userId: String): Flow<List<Chat>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chats = snapshot.children.mapNotNull {
                    it.getValue(Chat::class.java)
                }.sortedByDescending { it.lastMessageTime }
                trySend(chats)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = userChatsRef.child(userId)
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // Mark messages as read
    suspend fun markMessagesAsRead(chatId: String, userId: String) {
        val snapshot = chatsRef.child(chatId).child("messages").get().await()
        snapshot.children.forEach { msgSnapshot ->
            val msg = msgSnapshot.getValue(Message::class.java)
            if (msg != null && msg.senderId != userId && !msg.read) {
                msgSnapshot.ref.child("read").setValue(true).await()
            }
        }
    }
}