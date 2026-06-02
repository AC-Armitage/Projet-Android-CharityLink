package com.fpl.charitylink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpl.charitylink.data.model.Donation
import com.fpl.charitylink.data.repository.DonationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DonationsUiState(
    val isLoading: Boolean = true,
    val donations: List<Donation> = emptyList(),
    val errorMessage: String? = null
)

class DonationsViewModel : ViewModel() {
    private val donationRepository = DonationRepository()
    private val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }

    private val _uiState = MutableStateFlow(DonationsUiState())
    val uiState: StateFlow<DonationsUiState> = _uiState

    fun loadDonorDonations() {
        val userId = auth?.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val donations = donationRepository.getDonorDonations(userId)
                _uiState.value = DonationsUiState(isLoading = false, donations = donations)
            } catch (e: Exception) {
                _uiState.value = DonationsUiState(
                    isLoading = false, 
                    errorMessage = e.message ?: "Failed to load donations"
                )
            }
        }
    }

    fun loadAssociationDonations() {
        val userId = auth?.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // For association, we might need a different repository method or filter
                // Assuming associationId matches current user for now
                val donations = donationRepository.getCampaignDonationsByAssociation(userId)
                _uiState.value = DonationsUiState(isLoading = false, donations = donations)
            } catch (e: Exception) {
                _uiState.value = DonationsUiState(
                    isLoading = false, 
                    errorMessage = e.message ?: "Failed to load donations"
                )
            }
        }
    }
}
