package com.fpl.charitylink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpl.charitylink.data.model.Campaign
import com.fpl.charitylink.data.model.Organization
import com.fpl.charitylink.data.repository.CampaignRepository
import com.fpl.charitylink.data.repository.OrganizationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DonorHomeUiState(
    val isLoading: Boolean = true,
    val urgentCampaigns: List<Campaign> = emptyList(),
    val organizations: List<Organization> = emptyList(),
    val errorMessage: String? = null
)

class DonorHomeViewModel : ViewModel() {
    private val campaignRepository = CampaignRepository()
    private val organizationRepository = OrganizationRepository()

    private val _uiState = MutableStateFlow(DonorHomeUiState())
    val uiState: StateFlow<DonorHomeUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val campaigns = campaignRepository.getActiveCampaigns()
                val organizations = organizationRepository.getAllOrganizations()
                _uiState.value = DonorHomeUiState(
                    isLoading = false,
                    urgentCampaigns = campaigns,
                    organizations = organizations
                )
            } catch (e: Exception) {
                _uiState.value = DonorHomeUiState(
                    isLoading = false,
                    urgentCampaigns = emptyList(),
                    organizations = emptyList(),
                    errorMessage = e.message ?: "Failed to load data"
                )
            }
        }
    }
}
