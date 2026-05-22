package com.fpl.charitylink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fpl.charitylink.ui.theme.PrimaryFixedDim
import com.fpl.charitylink.ui.theme.SurfaceContainerHigh
import com.fpl.charitylink.ui.theme.SurfaceContainerLow
import com.fpl.charitylink.ui.theme.SurfaceContainerLowest

private data class UrgentNeed(
    val title: String,
    val location: String,
    val description: String,
    val raised: String,
    val progress: Float,
    val imageUrl: String
)

private data class AssociationItem(
    val name: String,
    val category: String,
    val location: String,
    val description: String,
    val imageUrl: String
)

@Composable
fun DonorHomeScreen(
    onProfileClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val urgentNeeds = remember {
        listOf(
            UrgentNeed(
                title = "Global Aid Network",
                location = "Casablanca, Morocco",
                description = "Crisis relief for families affected by the recent drought. Immediate food and water needed.",
                raised = "$12,450",
                progress = 0.75f,
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBsh4FO-2sTWmLywnBFSR2pBxNf2dM13ZqTbi63lmuPz2I2i0qxnfMU9tqbk-m7BTlymurv-2e1RdNU6uW96tZot6tqsx2bwFGZ6LOJTi6M9sr7YxC5VGia3ZnWs-cSlouR2FcnmVByylBrcCR0XeR0hFFNRA5xmb67ej1tMwGRkLSZhrqJAVxzFVjIwgI7hM9NaGNd6C6VquJYqYyrlGG8LD0SUrRT2HO8FV27jihiRE5Z1Wvtp3VZN-wg3huSt6YiMR_a05F-Wvsu"
            ),
            UrgentNeed(
                title = "EcoCare Foundation",
                location = "Rabat, Morocco",
                description = "Reforestation project to restore local ecosystems after forest fires.",
                raised = "$4,200",
                progress = 0.4f,
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBWyNOcANsndy6gsEX_LJqrTjE2z3k4WK6XrBOBSJYzko-s1-2NjoY4sZMrxDaaWWEDJ4-mrzIAbLxuwqTdqsh_Crxc3icLR0gMTp9I8bZAnURMj4E1FqaxVK6mK_f9xmac9ZvGAZUbrzH72d3NC4FIR4cfZ9Idlh6QG2Pk2anmKvedqlOzqyMnSOo1fOSZvBO2_Ei9iI5swu3QAh1ZgC2aTyUzGnNnnJsgd2bhXFNepbk50nCD9SuYXAHaoC5hZULlQRNFHm0kWI0r"
            )
        )
    }

    val associations = remember {
        listOf(
            AssociationItem(
                name = "Hearts For All",
                category = "Humanitarian",
                location = "Tangier, Morocco",
                description = "Providing education and healthcare to underserved youth.",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDbv7dCsVCSwwyodtU63jfUpMQyRrFZQVzP-zi8WTgb53b-7m4tF17OU9A_vknqhCW4YJxOidBjwWoqDZgdLooOIHED3WaNuiMZxGdgVI-866c1tZDOeFBbOesfDyI04SgdhMr8CPhPfYSQzqyAfYHLkzZcTFrQq09sTnP3AvYpShhyMlKO5imMiA7gplG6yom4EVY1BFl-bW2pRgvPpAYKsf0OPFJbG3ys_gylveJyJfk5Df7sFPXCiVaTHEqCo-EeBELbbj5LfRTi"
            ),
            AssociationItem(
                name = "Green Roots",
                category = "Environment",
                location = "Ifrane, Morocco",
                description = "Protecting national parks and promoting sustainable farming.",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCMMrQjbtrvYhVbrRiuVYUZgdhSWNblc2RB8zR54TP5sdJOuxhomMA9pK7AiP29eY8MEVYiPq3bpWnoxn899KNfKsoAABCK5jeR7v-nZ3S5T98etXq6jtOtXTv-TGnKi3wASnxTFadx2FmzF7XD7xZGMljFZcrhxM0YqfCRGBFMnOjaV0nuZlCv-947B8vMcwlHzDhNt-k3j1W2vQ_MkUrIOxF2vQ0IGBmkMXPA6upmVJp64HpyY99-4TYgarccarwCUYI9V3hcXIgA"
            ),
            AssociationItem(
                name = "Medi-Care Morocco",
                category = "Health",
                location = "Marrakech, Morocco",
                description = "Mobile clinics providing free medical checkups in rural areas.",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCRn5pdgTY1X2szsYA5zBVpnzksCbVN-QOAaw_cy1J8fd5m6YjFXR_jaBk2U_JU08ztF9TlW-v1WFx4qSyN7SEztUaG2Z_hEu_RYvvBFpz4tg7uWjSi43mgIpdXDU32FxN3HbeCVIvIN273buv3Bph3FH2XAyTw65hziOt9gee3VoX7l9Li5RtUmuEETCBZEigaBH-BC0S35tVheUnXYL9_GEd9I9DkIt618sxtIgD7JEHRw9g9MhgigON8R3SsNcFWjpIxEgeN5YWA"
            )
        )
    }

    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = { DonorTopBar() },
        bottomBar = { DonorBottomBar(onProfileClick = onProfileClick) },
        floatingActionButton = { DonateFab() },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 96.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(text = "Find associations...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline
                        )
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
                        val isSelected = chip == "All"
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else SurfaceContainerHigh,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(50)
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
                SectionHeader(title = "Urgent Needs", action = "See all")
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(urgentNeeds) { need ->
                        UrgentNeedCard(need)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Associations",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Filter",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            items(associations) { association ->
                AssociationCard(association)
            }
        }
    }
}

@Composable
private fun DonorTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAwOrG5iKIjHUQF2u2Z9WqMKSDFIQFJEZ_V_g3W7A2n09-OSbJv3t0vk42cPDu-udWBgZEK50B0Gi6JhCaU442Rbwwhr4dC1qchcQaZa947x7qeXCxhg6hxhQpLtJKP_lcn8DvgOu1w3R63Eg42qdMeDqq52AYHf1Qj-SekghdII0-UwxgwUCqG8znR89s9D-QrcZclK4FatCqTaYtOI-gr-f-YD3u6Pj3bZL3p6b2Nh5X3xlPcY1bCgIursmmqTWLzNvJVwF4dDY3U",
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, PrimaryFixedDim, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Welcome back,",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Hello, Abderrahim",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                modifier = Modifier.padding(10.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = action,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun UrgentNeedCard(need: UrgentNeed) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(380.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = need.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC000000))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = Color(0xFFF9A825),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "Urgent",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Column {
                    Text(
                        text = need.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = need.location,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = need.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Raised",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = need.raised,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White
                                )
                            }
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { need.progress },
                                    color = MaterialTheme.colorScheme.inversePrimary,
                                    trackColor = Color.White.copy(alpha = 0.2f),
                                    strokeWidth = 4.dp,
                                    modifier = Modifier.size(44.dp)
                                )
                                Text(
                                    text = "${(need.progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssociationCard(association: AssociationItem) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = association.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = association.name,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = association.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = association.location,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = association.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "View", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun DonateFab() {
    Button(
        onClick = { },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(18.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.VolunteerActivism,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Donate Now",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun DonorBottomBar(onProfileClick: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(label = "Home", icon = Icons.Outlined.Home, selected = true, onClick = {})
            BottomNavItem(label = "Explore", icon = Icons.Outlined.Explore, selected = false, onClick = {})
            BottomNavItem(label = "Donations", icon = Icons.Outlined.VolunteerActivism, selected = false, onClick = {})
            BottomNavItem(label = "Profile", icon = Icons.Outlined.Person, selected = false, onClick = onProfileClick)
        }
    }

}

@Composable
private fun BottomNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit = {}
) {
    val background = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = background,
        shape = RoundedCornerShape(50),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = contentColor)
        }
    }
}


