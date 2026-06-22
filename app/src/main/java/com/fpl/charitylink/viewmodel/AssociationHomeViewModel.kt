package com.fpl.charitylink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpl.charitylink.data.model.Campaign
import com.fpl.charitylink.data.repository.CampaignRepository
import com.fpl.charitylink.data.repository.DonationRepository
import com.fpl.charitylink.data.repository.OrganizationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AssociationHomeUiState(
    val isLoading: Boolean = true,
    val campaigns: List<Campaign> = emptyList(),
    val totalDonations: Double = 0.0,
    val donorsCount: Int = 0,
    val logoUrl: String? = null,
    val errorMessage: String? = null
)

class AssociationHomeViewModel : ViewModel() {
    private val campaignRepository = CampaignRepository()
    private val donationRepository = DonationRepository()
    private val organizationRepository = OrganizationRepository()

    private val _uiState = MutableStateFlow(AssociationHomeUiState())
    val uiState: StateFlow<AssociationHomeUiState> = _uiState

    fun load(associationId: String?) {
        if (associationId.isNullOrBlank()) {
            _uiState.value = AssociationHomeUiState(
                isLoading = false,
                errorMessage = "Missing association id"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // Load all data in parallel
                val campaignsDeferred = async {
                    campaignRepository.getAssociationCampaigns(associationId)
                }
                val donationsDeferred = async {
                    donationRepository.getAssociationDonations(associationId)
                }
                val orgDeferred = async {
                    organizationRepository.getOrganization(associationId)
                }

                val campaigns = campaignsDeferred.await()
                val donations = donationsDeferred.await()
                val org = orgDeferred.await()

                val totalDonations = donations.sumOf { it.amount }
                val donorsCount = donations.map { it.donorId }.distinct().count()

                _uiState.value = AssociationHomeUiState(
                    isLoading = false,
                    campaigns = campaigns,
                    totalDonations = totalDonations,
                    donorsCount = donorsCount,
                    logoUrl = org?.logoUrl
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load data"
                )
            }
        }
    }
    fun deleteCampaign(campaignId: String) {
        viewModelScope.launch {
            try {
                campaignRepository.deleteCampaign(campaignId)
                // Remove from local state immediately
                _uiState.value = _uiState.value.copy(
                    campaigns = _uiState.value.campaigns.filter { it.id != campaignId }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }
}