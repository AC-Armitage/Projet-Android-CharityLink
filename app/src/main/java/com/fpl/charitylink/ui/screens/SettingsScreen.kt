package com.fpl.charitylink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fpl.charitylink.MainActivity
import com.fpl.charitylink.viewmodel.AuthViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSupport: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current

    // Language — read from persisted state, not throwaway remember
    var showLanguageDialog by remember { mutableStateOf(false) }
    val persistedLanguage by authViewModel.cachedLanguage.collectAsState()
    val languages = listOf("English", "French", "Arabic")

    // Notification states
    var donationNotifs by remember { mutableStateOf(true) }
    var campaignNotifs by remember { mutableStateOf(true) }
    var systemNotifs by remember { mutableStateOf(false) }

    // Change password dialog
    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordMessage by remember { mutableStateOf<String?>(null) }

    // About dialog
    var showAboutDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(passwordMessage) {
        passwordMessage?.let {
            snackbarHostState.showSnackbar(it)
            passwordMessage = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Language ──────────────────────────────────
            SettingsSectionHeader(title = "General")
            SettingsItem(
                icon = Icons.Outlined.Language,
                title = "Language",
                subtitle = persistedLanguage,
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = persistedLanguage,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                },
                onClick = { showLanguageDialog = true }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Notifications ─────────────────────────────
            SettingsSectionHeader(title = "Notifications")
            SettingsToggleItem(
                icon = Icons.Outlined.VolunteerActivism,
                title = "Donation Alerts",
                subtitle = "Get notified about donation activity",
                checked = donationNotifs,
                onCheckedChange = { donationNotifs = it }
            )
            Spacer(modifier = Modifier.height(4.dp))
            SettingsToggleItem(
                icon = Icons.Outlined.Campaign,
                title = "Campaign Updates",
                subtitle = "New campaigns and milestones",
                checked = campaignNotifs,
                onCheckedChange = { campaignNotifs = it }
            )
            Spacer(modifier = Modifier.height(4.dp))
            SettingsToggleItem(
                icon = Icons.Outlined.Notifications,
                title = "System Notifications",
                subtitle = "App updates and announcements",
                checked = systemNotifs,
                onCheckedChange = { systemNotifs = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Account ───────────────────────────────────
            SettingsSectionHeader(title = "Account")
            SettingsItem(
                icon = Icons.Outlined.Lock,
                title = "Change Password",
                subtitle = "Update your account password",
                trailingContent = {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                },
                onClick = { showPasswordDialog = true }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── About ─────────────────────────────────────
            SettingsSectionHeader(title = "About")
            SettingsItem(
                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                title = "About CharityLink",
                subtitle = "Version 1.0.0",
                trailingContent = {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                },
                onClick = { showAboutDialog = true }
            )
            Spacer(modifier = Modifier.height(4.dp))
            SettingsItem(
                icon = Icons.Outlined.SupportAgent,
                title = "Help & Support",
                subtitle = "FAQ and contact us",
                trailingContent = {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                },
                onClick = onSupport
            )
        }
    }

    // ── Language Dialog ───────────────────────────────────
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language") },
            text = {
                Column {
                    languages.forEach { language ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = persistedLanguage == language,
                                onClick = {
                                    // Persist to DataStore
                                    authViewModel.saveLanguage(language)
                                    // Apply locale immediately — activity will recreate
                                    MainActivity.applyLocale(language)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = language, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // ── Change Password Dialog ────────────────────────────
    if (showPasswordDialog) {
        val user = authViewModel.currentUser
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Change Password") },
            text = {
                Text(
                    text = "A password reset email will be sent to:\n${user?.email}",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = {
                    user?.email?.let { email ->
                        com.google.firebase.auth.FirebaseAuth.getInstance()
                            .sendPasswordResetEmail(email)
                    }
                    showPasswordDialog = false
                    passwordMessage = "Reset email sent to ${user?.email}"
                }) {
                    Text("Send Email")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ── About Dialog ──────────────────────────────────────
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About CharityLink") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Version: 1.0.0", style = MaterialTheme.typography.bodyMedium)
                    Text("CharityLink connects donors with associations to make giving simple, transparent, and impactful.", style = MaterialTheme.typography.bodyMedium)
                    Text("Built with ❤️ by the CharityLink team.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

// ── Reusable Components ───────────────────────────────────

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailingContent: @Composable () -> Unit = {},
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            trailingContent()
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
