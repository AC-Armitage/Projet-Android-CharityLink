package com.fpl.charitylink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpl.charitylink.data.model.Chat
import com.fpl.charitylink.data.model.Message
import com.fpl.charitylink.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ChatViewModel : ViewModel() {
    private val chatRepository = ChatRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    val currentUserId get() = auth.currentUser?.uid ?: ""
    val currentUserName get() = auth.currentUser?.displayName ?: ""

    // Load all chats for current user
    fun loadChats() {
        val uid = currentUserId
        if (uid.isEmpty()) return
        viewModelScope.launch {
            chatRepository.getUserChats(uid).collect { chats ->
                _uiState.value = _uiState.value.copy(chats = chats)
            }
        }
    }

    // Load messages for a specific chat
    fun loadMessages(chatId: String) {
        viewModelScope.launch {
            chatRepository.getMessages(chatId).collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
    }

    // Send a message
    fun sendMessage(
        text: String,
        chatId: String,
        donorId: String,
        donorName: String,
        associationId: String,
        associationName: String
    ) {
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                val message = Message(
                    senderId = currentUserId,
                    senderName = currentUserName,
                    text = text.trim(),
                    timestamp = System.currentTimeMillis()
                )
                chatRepository.sendMessage(
                    chatId = chatId,
                    message = message,
                    donorId = donorId,
                    donorName = donorName,
                    associationId = associationId,
                    associationName = associationName
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    // Mark messages as read when opening chat
    fun markAsRead(chatId: String) {
        viewModelScope.launch {
            chatRepository.markMessagesAsRead(chatId, currentUserId)
        }
    }

    // Get or create chat ID
    fun getChatId(otherUserId: String): String {
        return chatRepository.getChatId(currentUserId, otherUserId)
    }
}