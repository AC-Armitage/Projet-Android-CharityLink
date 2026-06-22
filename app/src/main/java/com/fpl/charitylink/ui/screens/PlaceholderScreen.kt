package com.fpl.charitylink.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fpl.charitylink.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(
    title: String,
    message: String,
    onBack: (() -> Unit)? = null
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = message, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.screen_not_implemented),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
