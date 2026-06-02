package com.fpl.charitylink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpl.charitylink.data.model.Campaign
import com.fpl.charitylink.data.repository.CampaignRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AssociationHomeUiState(
    val isLoading: Boolean = true,
    val campaigns: List<Campaign> = emptyList(),
    val errorMessage: String? = null
)

class AssociationHomeViewModel : ViewModel() {
    private val campaignRepository = CampaignRepository()

    private val _uiState = MutableStateFlow(AssociationHomeUiState())
    val uiState: StateFlow<AssociationHomeUiState> = _uiState

    fun load(associationId: String?) {
        if (associationId.isNullOrBlank()) {
            _uiState.value = AssociationHomeUiState(
                isLoading = false,
                campaigns = emptyList(),
                errorMessage = "Missing association id"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val campaigns = campaignRepository.getAssociationCampaigns(associationId)
                _uiState.value = AssociationHomeUiState(
                    isLoading = false,
                    campaigns = campaigns
                )
            } catch (e: Exception) {
                _uiState.value = AssociationHomeUiState(
                    isLoading = false,
                    campaigns = emptyList(),
                    errorMessage = e.message ?: "Failed to load data"
                )
            }
        }
    }
}
