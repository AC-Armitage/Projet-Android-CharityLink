package com.fpl.charitylink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.fpl.charitylink.ui.theme.PrimaryFixedDim
import com.fpl.charitylink.ui.theme.SurfaceContainerHigh
import com.fpl.charitylink.ui.theme.SurfaceContainerLow
import com.fpl.charitylink.ui.theme.SurfaceContainerLowest
import com.fpl.charitylink.viewmodel.AuthViewModel
import com.fpl.charitylink.viewmodel.DonorHomeViewModel
import java.util.Locale
import kotlin.math.min

private data class UrgentNeed(
    val id: String,
    val title: String,
    val location: String,
    val description: String,
    val raised: String,
    val progress: Float,
    val imageUrl: String
)

private data class AssociationItem(
    val id: String,
    val name: String,
    val category: String,
    val location: String,
    val description: String,
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorHomeScreen(
    onProfileClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onExploreClick: () -> Unit = {},
    onDonationsClick: () -> Unit = {},
    onAssociationClick: (String) -> Unit = {},
    onNeedClick: (String) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onDonateClick: () -> Unit = {},
    onSeeAllClick: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    val donorHomeViewModel: DonorHomeViewModel = viewModel()
    val uiState by donorHomeViewModel.uiState.collectAsState()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                donorHomeViewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val errorMessage = uiState.errorMessage

    val cachedUser by authViewModel.cachedUser.collectAsState()
    val firebaseUser = authViewModel.currentUser
    val displayName = cachedUser["fullName"]?.ifBlank { null } ?: firebaseUser?.displayName ?: "Donor"
    val photoUrl = cachedUser["photoUrl"]?.ifBlank { null } ?: firebaseUser?.photoUrl?.toString()

    var query by remember { mutableStateOf("") }
    var selectedChip by remember { mutableStateOf("All") }
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    var showVerifiedOnly by remember { mutableStateOf(false) }

    val filteredCampaigns = if (selectedChip == "All") {
        uiState.urgentCampaigns
    } else {
        uiState.urgentCampaigns.filter {
            it.category.lowercase() == selectedChip.lowercase()
        }
    }

    val urgentNeeds = filteredCampaigns.map { campaign ->
        val progress = if (campaign.goalAmount > 0.0)
            min(1f, (campaign.raisedAmount / campaign.goalAmount).toFloat()) else 0f
        val raised = String.format(Locale.getDefault(), "$%,.0f", campaign.raisedAmount)
        UrgentNeed(
            id = campaign.id,
            title = campaign.title,
            location = if (campaign.associationName.isNotBlank()) campaign.associationName else "Association",
            description = campaign.description,
            raised = raised,
            progress = progress,
            imageUrl = campaign.imageUrl ?: ""
        )
    }

    val filteredOrganizations = uiState.organizations.filter { org ->
        val categoryMatch = selectedFilter == "All" ||
                org.description?.lowercase()?.contains(selectedFilter.lowercase()) == true
        val verifiedMatch = !showVerifiedOnly || org.verified
        categoryMatch && verifiedMatch
    }

    val associations = filteredOrganizations.map { org ->
        AssociationItem(
            id = org.uid,
            name = org.name,
            category = if (org.verified) "Verified" else "Organization",
            location = org.address ?: "Location not set",
            description = org.description ?: "No description yet.",
            imageUrl = org.logoUrl ?: ""
        )
    }

    Scaffold(
        topBar = {
            DonorTopBar(
                displayName = displayName,
                photoUrl = photoUrl,
                onNotificationsClick = onNotificationsClick,
                onChatClick = onChatClick
            )
        },
        bottomBar = {
            DonorBottomBar(
                onProfileClick = onProfileClick,
                onExploreClick = onExploreClick,
                onDonationsClick = onDonationsClick
            )
        },
        floatingActionButton = { DonateFab(onClick = onDonateClick) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 96.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Find associations...") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceContainerLow,
                        unfocusedContainerColor = SurfaceContainerLow,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("All", "Money", "Clothes", "Food")) { chip ->
                        val isSelected = chip == selectedChip
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else SurfaceContainerHigh,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(50),
                            onClick = { selectedChip = chip }
                        ) {
                            Text(
                                text = chip,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Urgent Needs", action = "See all", onActionClick = onSeeAllClick)
            }

            if (uiState.isLoading) {
                item { LoadingState() }
            } else if (errorMessage != null) {
                item { ErrorState(message = errorMessage, onRetry = { donorHomeViewModel.refresh() }) }
            }

            item {
                if (urgentNeeds.isEmpty() && !uiState.isLoading) {
                    EmptyState(message = "No urgent campaigns yet.")
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(urgentNeeds) { need ->
                            UrgentNeedCard(need = need, onClick = { onNeedClick(need.id) })
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "All Associations", style = MaterialTheme.typography.headlineMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showFilterSheet = true }
                    ) {
                        Icon(Icons.Outlined.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Filter", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                        if (selectedFilter != "All" || showVerifiedOnly) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        }
                    }
                }
            }

            if (associations.isEmpty() && !uiState.isLoading) {
                item { EmptyState(message = "No associations available yet.") }
            } else {
                items(associations) { association ->
                    AssociationCard(association = association, onViewClick = { onAssociationClick(association.id) })
                }
            }
        }
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        FilterBottomSheet(
            selectedFilter = selectedFilter,
            showVerifiedOnly = showVerifiedOnly,
            onFilterSelected = { selectedFilter = it },
            onVerifiedToggle = { showVerifiedOnly = it },
            onDismiss = { showFilterSheet = false },
            onReset = {
                selectedFilter = "All"
                showVerifiedOnly = false
                showFilterSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    selectedFilter: String,
    showVerifiedOnly: Boolean,
    onFilterSelected: (String) -> Unit,
    onVerifiedToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onReset: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val filters = listOf("All", "Money", "Clothes", "Food")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Filter Associations", style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = onReset) {
                    Text(text = "Reset", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Category", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filters.forEach { filter ->
                    val isSelected = filter == selectedFilter
                    Surface(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else SurfaceContainerHigh,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = RoundedCornerShape(50),
                        onClick = { onFilterSelected(filter) }
                    ) {
                        Text(text = filter, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Verified Only", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = "Show only verified associations", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = showVerifiedOnly,
                        onCheckedChange = onVerifiedToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Apply Filters", style = MaterialTheme.typography.labelLarge)
            }
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
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = message, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Text(text = message, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DonorTopBar(displayName: String = "", photoUrl: String? = null, onNotificationsClick: () -> Unit = {}, onChatClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().height(64.dp).background(MaterialTheme.colorScheme.surface).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (photoUrl != null) {
                AsyncImage(model = photoUrl, contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape).border(2.dp, PrimaryFixedDim, CircleShape), contentScale = ContentScale.Crop)
            } else {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer).border(2.dp, PrimaryFixedDim, CircleShape), contentAlignment = Alignment.Center) {
                    Text(text = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Welcome back,", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Hello, $displayName", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh, onClick = onChatClick) {
                Icon(Icons.AutoMirrored.Outlined.Chat, contentDescription = "Messages", modifier = Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh ,onClick = onNotificationsClick) {
                Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String, onActionClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Text(text = action, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, modifier = Modifier.clickable { onActionClick() })
    }
}

@Composable
private fun UrgentNeedCard(need: UrgentNeed, onClick: () -> Unit) {
    Card(modifier = Modifier.width(280.dp).height(380.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), onClick = onClick) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(model = need.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000)))))
            Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Surface(color = Color(0xFFF9A825), shape = RoundedCornerShape(50)) {
                    Text(text = "Urgent", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground)
                }
                Column {
                    Text(text = need.title, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = need.location, style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = need.description, style = MaterialTheme.typography.bodyMedium, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(color = Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(text = "Raised", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                Text(text = need.raised, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                            }
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(progress = { need.progress }, color = MaterialTheme.colorScheme.inversePrimary, trackColor = Color.White.copy(alpha = 0.2f), strokeWidth = 4.dp, modifier = Modifier.size(44.dp))
                                Text(text = "${(need.progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssociationCard(association: AssociationItem, onViewClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssociationLogo(
                imageUrl = association.imageUrl,
                name = association.name,
                modifier = Modifier.size(76.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = association.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = association.category,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = association.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = association.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onViewClick,
                modifier = Modifier.height(46.dp),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "View", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun AssociationLogo(imageUrl: String, name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "$name logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Business,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

@Composable
private fun DonateFab(onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), shape = RoundedCornerShape(18.dp)) {
        Icon(Icons.Outlined.VolunteerActivism, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Donate Now", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
private fun DonorBottomBar(onProfileClick: () -> Unit, onExploreClick: () -> Unit, onDonationsClick: () -> Unit) {
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
private fun BottomNavItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit = {}) {
    val background = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(color = background, shape = RoundedCornerShape(50), onClick = onClick) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = contentColor)
        }
    }
}
