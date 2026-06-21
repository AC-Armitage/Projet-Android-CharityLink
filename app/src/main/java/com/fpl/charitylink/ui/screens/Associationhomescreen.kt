package com.fpl.charitylink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.fpl.charitylink.ui.theme.SurfaceContainerHigh
import com.fpl.charitylink.ui.theme.SurfaceContainerHighest
import com.fpl.charitylink.ui.theme.SurfaceContainerLowest
import com.fpl.charitylink.viewmodel.AssociationHomeViewModel
import com.fpl.charitylink.viewmodel.AuthViewModel
import java.util.Locale
import kotlin.math.min

private data class NeedItem(
    val id: String,
    val title: String,
    val status: String,
    val statusColor: Color,
    val progressLabel: String,
    val progress: Float,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun AssociationHomeScreen(
    authViewModel: AuthViewModel = viewModel(),
    onProfileClick: () -> Unit = {},
    onExploreClick: () -> Unit = {},
    onDonationsClick: () -> Unit = {},
    onPostNeedClick: () -> Unit = {},
    onNeedClick: (String) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onEditNeedClick: (String) -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val associationHomeViewModel: AssociationHomeViewModel = viewModel()
    val uiState by associationHomeViewModel.uiState.collectAsState()
    val associationId = authViewModel.currentUser?.uid
    val errorMessage = uiState.errorMessage

    var campaignToDelete by remember { mutableStateOf<String?>(null) }

// Initial load
    LaunchedEffect(associationId) {
        associationHomeViewModel.load(associationId)
    }

// Refresh every time the screen resumes (e.g. after editing/deleting/donating)
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                associationHomeViewModel.load(associationId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val needs = uiState.campaigns.map { campaign ->
        val status = campaign.status.ifBlank { "active" }
        val statusColor = when (status) {
            "open" -> colorScheme.secondary
            "in_progress" -> colorScheme.tertiary
            "fulfilled" -> colorScheme.primary
            "active" -> colorScheme.secondary
            else -> colorScheme.primary
        }
        val progress = if (campaign.goalAmount > 0.0)
            min(1f, (campaign.raisedAmount / campaign.goalAmount).toFloat()) else 0f
        val raised = String.format(Locale.getDefault(), "%,.0f", campaign.raisedAmount)
        val goal = String.format(Locale.getDefault(), "%,.0f", campaign.goalAmount)
        val progressLabel = if (campaign.goalAmount > 0.0) "$raised / $goal" else "No goal set"
        val icon = when (campaign.category.lowercase()) {
            "food" -> Icons.Outlined.Fastfood
            "clothes" -> Icons.Outlined.Checkroom
            else -> Icons.Outlined.Payments
        }
        NeedItem(
            id = campaign.id,
            title = campaign.title,
            status = status.replaceFirstChar { it.uppercase() },
            statusColor = statusColor,
            progressLabel = progressLabel,
            progress = progress,
            icon = icon
        )
    }

    // Delete confirmation dialog
    if (campaignToDelete != null) {
        AlertDialog(
            onDismissRequest = { campaignToDelete = null },
            title = { Text("Delete Campaign") },
            text = { Text("Are you sure you want to delete this campaign? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        associationHomeViewModel.deleteCampaign(campaignToDelete!!)
                        campaignToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { campaignToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = { AssociationTopBar(onNotificationsClick = onNotificationsClick, onChatClick = onChatClick) },
        bottomBar = {
            AssociationBottomBar(
                onProfileClick = onProfileClick,
                onExploreClick = onExploreClick,
                onDonationsClick = onDonationsClick
            )
        },
        floatingActionButton = { PostNeedFab(onClick = onPostNeedClick) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 96.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SummaryGrid(
                    activeNeeds = needs.size,
                    totalDonations = uiState.totalDonations,
                    donorsCount = uiState.donorsCount
                )
            }

            if (uiState.isLoading) {
                item { LoadingState() }
            } else if (errorMessage != null) {
                item { ErrorState(message = errorMessage) }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "My Needs", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = "View All",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            if (needs.isEmpty() && !uiState.isLoading) {
                item { EmptyState(message = "No needs yet. Create your first campaign.") }
            } else {
                items(needs) { need ->
                    NeedCard(
                        need = need,
                        onClick = { onNeedClick(need.id) },
                        onEditClick = { onEditNeedClick(need.id) },
                        onDeleteClick = { campaignToDelete = need.id }
                    )
                }
            }
        }
    }
}

@Composable
private fun AssociationTopBar(onNotificationsClick: () -> Unit = {}, onChatClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.VolunteerActivism,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "CharityLink",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                onClick = onChatClick
            ) {
                Icon(Icons.AutoMirrored.Outlined.Chat, contentDescription = "Messages", modifier = Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                onClick = onNotificationsClick
            ) {
                Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Icon(Icons.Outlined.Settings, contentDescription = null, modifier = Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SummaryGrid(activeNeeds: Int, totalDonations: Double = 0.0, donorsCount: Int = 0) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Total Donations", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Icon(Icons.Outlined.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = String.format(Locale.getDefault(), "$%,.2f", totalDonations),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(text = "Total received donations", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(label = "Active Needs", value = activeNeeds.toString(), icon = Icons.Outlined.AssignmentLate, tint = MaterialTheme.colorScheme.secondary)
            SummaryCard(label = "Donors", value = donorsCount.toString(), icon = Icons.Outlined.Groups, tint = MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun RowScope.SummaryCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = tint)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun NeedCard(
    need: NeedItem,
    onClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier.size(48.dp).background(need.statusColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = need.icon, contentDescription = null, tint = need.statusColor)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = need.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(need.statusColor, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = need.status, style = MaterialTheme.typography.labelSmall, color = need.statusColor)
                        }
                    }
                }
                Row {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(4.dp).clickable { onEditClick() }
                    )
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(4.dp).clickable { onDeleteClick() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = need.progressLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "${(need.progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { need.progress },
                color = MaterialTheme.colorScheme.primary,
                trackColor = SurfaceContainerHighest,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50))
            )
        }
    }
}

@Composable
private fun PostNeedFab(onClick: () -> Unit) {
    Button(onClick = onClick, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Icon(Icons.Outlined.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Post a Need", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun AssociationBottomBar(onProfileClick: () -> Unit, onExploreClick: () -> Unit, onDonationsClick: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(label = "Home", icon = Icons.Outlined.Home, selected = true, onClick = {})
            BottomNavItem(label = "Explore", icon = Icons.Outlined.Explore, selected = false, onClick = onExploreClick)
            BottomNavItem(label = "Donations", icon = Icons.Outlined.VolunteerActivism, selected = false, onClick = onDonationsClick)
            BottomNavItem(label = "Profile", icon = Icons.Outlined.Person, selected = false, onClick = onProfileClick)
        }
    }
}

@Composable
private fun BottomNavItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(color = background, shape = RoundedCornerShape(50), onClick = onClick) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = contentColor)
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Text(text = message, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
    }
}

@Composable
private fun EmptyState(message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Text(text = message, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
