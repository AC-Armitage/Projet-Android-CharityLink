package com.fpl.charitylink.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fpl.charitylink.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fpl.charitylink.data.model.Chat
import com.fpl.charitylink.viewmodel.AuthViewModel
import com.fpl.charitylink.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onBack: () -> Unit,
    onChatClick: (chatId: String, otherUserId: String, otherUserName: String) -> Unit,
    onNewChatClick: () -> Unit = {},
    viewModel: ChatViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId = viewModel.currentUserId
    val role by authViewModel.cachedRole.collectAsState()
    // Only donors can currently browse a list of associations to start a chat with —
    // there's no equivalent "browse donors" screen yet for the association side.
    val canStartNewChat = role != "association"

    LaunchedEffect(Unit) {
        viewModel.loadChats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (canStartNewChat) {
                FloatingActionButton(onClick = onNewChatClick) {
                    Icon(Icons.Filled.Add, contentDescription = "Start a new conversation")
                }
            }
        }
    ) { innerPadding ->
        if (uiState.chats.isEmpty()) {
            EmptyChatsState(
                canStartNewChat = canStartNewChat,
                onNewChatClick = onNewChatClick,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.chats) { chat ->
                    ChatListItem(
                        chat = chat,
                        currentUserId = currentUserId,
                        onClick = {
                            val isCurrentUserDonor = currentUserId == chat.donorId
                            val otherUserId = if (isCurrentUserDonor) chat.associationId else chat.donorId
                            val otherUserName = if (isCurrentUserDonor) chat.associationName else chat.donorName
                            onChatClick(chat.id, otherUserId, otherUserName)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatListItem(chat: Chat, currentUserId: String, onClick: () -> Unit) {
    val isCurrentUserDonor = currentUserId == chat.donorId
    val otherPartyName = if (isCurrentUserDonor) chat.associationName else chat.donorName
    val displayName = otherPartyName.ifBlank { "Unknown" }
    val timeLabel = remember(chat.lastMessageTime) {
        if (chat.lastMessageTime > 0) {
            SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(chat.lastMessageTime))
        } else ""
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = displayName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = chat.lastMessage.ifBlank { "No messages yet" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (timeLabel.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyChatsState(
    canStartNewChat: Boolean,
    onNewChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Filled.Forum,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.no_conversations),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (canStartNewChat) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onNewChatClick,
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Find an association to message")
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.donors_will_appear),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
