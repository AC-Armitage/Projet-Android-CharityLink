package com.fpl.charitylink.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fpl.charitylink.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fpl.charitylink.data.model.Campaign
import com.fpl.charitylink.data.repository.CampaignRepository
import com.fpl.charitylink.viewmodel.DonateViewModel
import java.util.Locale

private val quickAmounts = listOf(10.0, 25.0, 50.0, 100.0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonateScreen(
    campaignId: String,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: DonateViewModel = viewModel()
) {
    // Local screen state for loading the campaign this donation targets.
    var campaign by remember { mutableStateOf<Campaign?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var isLoadingCampaign by remember { mutableStateOf(true) }

    val campaignRepository = remember { CampaignRepository() }

    LaunchedEffect(campaignId) {
        isLoadingCampaign = true
        loadError = null
        try {
            campaign = campaignRepository.getCampaign(campaignId)
            if (campaign == null) {
                loadError = "Campaign not found"
            }
        } catch (e: Exception) {
            loadError = e.message ?: "Failed to load campaign"
        } finally {
            isLoadingCampaign = false
        }
    }

    var selectedQuickAmount by rememberSaveable { mutableStateOf<Double?>(25.0) }
    var customAmountText by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    // Navigate away once the donation succeeds.
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetState()
            onSuccess()
        }
    }

    val enteredAmount: Double? = customAmountText.toDoubleOrNull() ?: selectedQuickAmount
    val isAmountValid = enteredAmount != null && enteredAmount > 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donate") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            isLoadingCampaign -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            loadError != null || campaign == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = loadError ?: "Campaign not found",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {
                val activeCampaign = campaign!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    // What you're donating to
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.donating_to),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeCampaign.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = activeCampaign.associationName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Amount selection
                    Text(
                        text = stringResource(R.string.choose_amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        quickAmounts.forEach { amount ->
                            val selected = selectedQuickAmount == amount && customAmountText.isBlank()
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedQuickAmount = amount
                                    customAmountText = ""
                                },
                                label = {
                                    Text(String.format(Locale.getDefault(), "$%,.0f", amount))
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.or_custom_amount),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customAmountText,
                        onValueChange = {
                            customAmountText = it
                            if (it.isNotBlank()) selectedQuickAmount = null
                        },
                        singleLine = true,
                        leadingIcon = { Text("$", style = MaterialTheme.typography.titleMedium) },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.add_message_optional),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        placeholder = { Text("Say something encouraging...") },
                        modifier = Modifier.fillMaxWidth().height(96.dp)
                    )

                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            val amount = enteredAmount ?: return@Button
                            viewModel.donate(
                                campaignId = activeCampaign.id,
                                campaignTitle = activeCampaign.title,
                                associationId = activeCampaign.associationId,
                                associationName = activeCampaign.associationName,
                                amount = amount,
                                message = message.ifBlank { null }
                            )
                        },
                        enabled = isAmountValid && !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            val amountLabel = enteredAmount?.let {
                                String.format(Locale.getDefault(), "$%,.2f", it)
                            } ?: "Amount"
                            Text(
                                text = "Donate $amountLabel",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
