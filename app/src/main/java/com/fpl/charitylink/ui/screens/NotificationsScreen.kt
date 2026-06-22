package com.fpl.charitylink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fpl.charitylink.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fpl.charitylink.data.model.Notification
import com.fpl.charitylink.data.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// ── ViewModel ──────────────────────────────────────────────
data class NotificationsUiState(
    val isLoading: Boolean = true,
    val notifications: List<Notification> = emptyList(),
    val errorMessage: String? = null
)

class NotificationsViewModel : ViewModel() {
    private val repo = NotificationRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState

    init { load() }

    fun load() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val notifications = repo.getUserNotifications(uid)
                _uiState.value = NotificationsUiState(isLoading = false, notifications = notifications)
            } catch (e: Exception) {
                _uiState.value = NotificationsUiState(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                repo.markAsRead(notificationId)
                _uiState.value = _uiState.value.copy(
                    notifications = _uiState.value.notifications.map {
                        if (it.id == notificationId) it.copy(read = true) else it
                    }
                )
            } catch (_: Exception) {}
        }
    }

    fun markAllAsRead() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                repo.markAllAsRead(uid)
                _uiState.value = _uiState.value.copy(
                    notifications = _uiState.value.notifications.map { it.copy(read = true) }
                )
            } catch (_: Exception) {}
        }
    }
}

// ── Screen ─────────────────────────────────────────────────
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Donations", "Updates")

    val filteredNotifications = if (selectedFilter == "All") {
        uiState.notifications
    } else {
        uiState.notifications.filter {
            it.type.lowercase() == selectedFilter.lowercase() ||
                    (selectedFilter == "Donations" && it.type == "donation") ||
                    (selectedFilter == "Updates" && it.type in listOf("campaign", "system"))
        }
    }

    // Group by today vs yesterday vs older
    val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
    val yesterday = today - TimeUnit.DAYS.toMillis(1)

    val todayNotifs = filteredNotifications.filter { it.createdAt >= today }
    val yesterdayNotifs = filteredNotifications.filter { it.createdAt in yesterday until today }
    val olderNotifs = filteredNotifications.filter { it.createdAt < yesterday }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = stringResource(R.string.notifications),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (uiState.notifications.any { !it.read }) {
                    TextButton(onClick = { viewModel.markAllAsRead() }) {
                        Text(
                            text = stringResource(R.string.mark_all_read),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Filter chips
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filters) { filter ->
                                val isSelected = filter == selectedFilter
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent,
                                    border = ButtonDefaults.outlinedButtonBorder,
                                    onClick = { selectedFilter = filter }
                                ) {
                                    Text(
                                        text = filter,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Empty state
                    if (filteredNotifications.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Outlined.Notifications,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.outlineVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(R.string.no_notifications),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.no_notifications_subtitle),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Today
                    if (todayNotifs.isNotEmpty()) {
                        items(todayNotifs) { notification ->
                            NotificationCard(
                                notification = notification,
                                onClick = { if (!notification.read) viewModel.markAsRead(notification.id) }
                            )
                        }
                    }

                    // Yesterday divider
                    if (yesterdayNotifs.isNotEmpty()) {
                        item { DateDivider(label = "Yesterday") }
                        items(yesterdayNotifs) { notification ->
                            NotificationCard(
                                notification = notification,
                                onClick = { if (!notification.read) viewModel.markAsRead(notification.id) }
                            )
                        }
                    }

                    // Older divider
                    if (olderNotifs.isNotEmpty()) {
                        item { DateDivider(label = "Older") }
                        items(olderNotifs) { notification ->
                            NotificationCard(
                                notification = notification,
                                onClick = { if (!notification.read) viewModel.markAsRead(notification.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: Notification, onClick: () -> Unit) {
    val (icon, iconBg, iconTint) = when (notification.type) {
        "donation" -> Triple(Icons.Outlined.VolunteerActivism, Color(0xFF2E7D32), Color(0xFFCBFFC2))
        "campaign" -> Triple(Icons.Outlined.Campaign, Color(0xFFB14B6F), Color(0xFFFFEDF0))
        else -> Triple(Icons.Outlined.Notifications, Color(0xFF835400), Color(0xFFFFDDB5))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (!notification.read)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeAgo(notification.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Unread dot
            if (!notification.read) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .align(Alignment.Top)
                )
            }
        }
    }
}

@Composable
private fun DateDivider(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Divider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

private fun timeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} minutes ago"
        diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} hours ago"
        diff < TimeUnit.DAYS.toMillis(2) -> "Yesterday"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}