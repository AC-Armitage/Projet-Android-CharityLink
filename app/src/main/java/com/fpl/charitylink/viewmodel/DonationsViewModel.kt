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
    val errorMessage: String? = null,
    val selectionMode: Boolean = false,
    val selectedIds: Set<String> = emptySet(),
    val isDeleting: Boolean = false
)

class DonationsViewModel : ViewModel() {
    private val donationRepository = DonationRepository()
    private val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }

    private val _uiState = MutableStateFlow(DonationsUiState())
    val uiState: StateFlow<DonationsUiState> = _uiState

    // Remember which load function was used so we can refresh after deleting
    private var isAssociationContext = false

    fun loadDonorDonations() {
        isAssociationContext = false
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
        isAssociationContext = true
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

    private fun refresh() {
        if (isAssociationContext) loadAssociationDonations() else loadDonorDonations()
    }

    fun setSelectionMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            selectionMode = enabled,
            selectedIds = if (enabled) _uiState.value.selectedIds else emptySet()
        )
    }

    fun toggleSelected(donationId: String) {
        val current = _uiState.value.selectedIds
        val updated = if (current.contains(donationId)) current - donationId else current + donationId
        _uiState.value = _uiState.value.copy(selectedIds = updated)
    }

    fun selectAll() {
        _uiState.value = _uiState.value.copy(
            selectedIds = _uiState.value.donations.map { it.id }.toSet()
        )
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedIds = emptySet())
    }

    fun deleteSelected() {
        val ids = _uiState.value.selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)
            try {
                donationRepository.deleteDonations(ids)
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    selectionMode = false,
                    selectedIds = emptySet()
                )
                refresh()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = e.message ?: "Failed to delete donations"
                )
            }
        }
    }

    fun deleteAll() {
        val userId = auth?.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)
            try {
                donationRepository.deleteAllDonorDonations(userId)
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    selectionMode = false,
                    selectedIds = emptySet()
                )
                refresh()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = e.message ?: "Failed to delete donations"
                )
            }
        }
    }
}
