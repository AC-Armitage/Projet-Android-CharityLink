package com.fpl.charitylink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpl.charitylink.data.model.Donation
import com.fpl.charitylink.data.repository.CampaignRepository
import com.fpl.charitylink.data.repository.DonationRepository
import com.fpl.charitylink.data.repository.NotificationRepository
import com.fpl.charitylink.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DonateUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class DonateViewModel : ViewModel() {
    private val donationRepository = DonationRepository()
    private val campaignRepository = CampaignRepository()
    private val notificationRepository = NotificationRepository()
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(DonateUiState())
    val uiState: StateFlow<DonateUiState> = _uiState

    fun donate(
        campaignId: String,
        campaignTitle: String,
        associationId: String,
        associationName: String,
        amount: Double,
        message: String? = null
    ) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            _uiState.value = DonateUiState(isLoading = true)
            try {
                // Get donor name
                val donor = userRepository.getUser(currentUser.uid)
                val donorName = donor?.fullName ?: currentUser.displayName ?: "Anonymous"

                // 1. Create donation document
                val donation = Donation(
                    donorId = currentUser.uid,
                    donorName = donorName,
                    campaignId = campaignId,
                    campaignTitle = campaignTitle,
                    associationId = associationId,
                    amount = amount,
                    message = message,
                    createdAt = System.currentTimeMillis()
                )
                donationRepository.createDonation(donation)

                // 2. Update campaign raised amount
                campaignRepository.updateRaisedAmount(campaignId, amount)

                // 3. Create notification for association
                notificationRepository.createDonationNotification(
                    associationId = associationId,
                    donorName = donorName,
                    amount = amount,
                    campaignTitle = campaignTitle
                )

                // 4. Check if campaign goal is reached
                val updatedCampaign = campaignRepository.getCampaign(campaignId)
                if (updatedCampaign != null &&
                    updatedCampaign.goalAmount > 0 &&
                    updatedCampaign.raisedAmount >= updatedCampaign.goalAmount
                ) {
                    // Notify association campaign is fulfilled
                    notificationRepository.createCampaignFulfilledNotification(
                        associationId = associationId,
                        campaignTitle = campaignTitle
                    )
                    // Update campaign status
                    campaignRepository.updateCampaignStatus(campaignId, "completed")
                }

                _uiState.value = DonateUiState(isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = DonateUiState(errorMessage = e.message ?: "Donation failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = DonateUiState()
    }
}