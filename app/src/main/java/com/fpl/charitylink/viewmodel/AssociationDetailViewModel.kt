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

data class AssociationDetailUiState(
    val isLoading: Boolean = true,
    val organization: Organization? = null,
    val campaigns: List<Campaign> = emptyList(),
    val errorMessage: String? = null
)

class AssociationDetailViewModel : ViewModel() {
    private val organizationRepository = OrganizationRepository()
    private val campaignRepository = CampaignRepository()

    private val _uiState = MutableStateFlow(AssociationDetailUiState())
    val uiState: StateFlow<AssociationDetailUiState> = _uiState

    fun loadAssociation(associationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val org = organizationRepository.getOrganization(associationId)
                if (org != null) {
                    val campaigns = campaignRepository.getAssociationCampaigns(associationId)
                    _uiState.value = AssociationDetailUiState(
                        isLoading = false,
                        organization = org,
                        campaigns = campaigns
                    )
                } else {
                    _uiState.value = AssociationDetailUiState(
                        isLoading = false,
                        errorMessage = "Association not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AssociationDetailUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load association details"
                )
            }
        }
    }
}
