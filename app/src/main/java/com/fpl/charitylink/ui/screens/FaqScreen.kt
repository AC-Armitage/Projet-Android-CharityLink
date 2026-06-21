package com.fpl.charitylink.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class FaqItem(val question: String, val answer: String)

private val faqItems = listOf(
    FaqItem(
        "How do I make a donation?",
        "Open a campaign from the Explore tab or your home feed, tap Donate, enter an amount, and confirm. You'll see the donation reflected in your donation history right away."
    ),
    FaqItem(
        "How do I create a campaign or post a need?",
        "If you're registered as an association, use Post Need from your home screen to create a new campaign with a title, description, goal amount, and optional image."
    ),
    FaqItem(
        "Can I delete my donation history?",
        "Yes. Go to Donation History, tap the checkmark icon to select individual donations or the sweep icon to delete everything at once. Deleting is permanent and can't be undone."
    ),
    FaqItem(
        "How do I contact an association?",
        "Open the association's profile and tap Message to start a chat with them directly."
    ),
    FaqItem(
        "How do I change my password?",
        "Go to Settings > Change Password. A password reset link will be sent to your account email."
    ),
    FaqItem(
        "Is my donation information private?",
        "Your donation history is only visible to you and, for transparency, to the association you donated to. It is never shown publicly."
    ),
    FaqItem(
        "How do I switch the app language?",
        "Go to Settings > Language and choose English, French, or Arabic."
    ),
    FaqItem(
        "I found a bug or have a suggestion, what do I do?",
        "Head to the Support page from Settings or your Profile and reach out to us by email. We read every message."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("FAQ") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Frequently Asked Questions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            faqItems.forEach { item ->
                FaqExpandableItem(item)
            }
        }
    }
}

@Composable
private fun FaqExpandableItem(item: FaqItem) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.question,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
