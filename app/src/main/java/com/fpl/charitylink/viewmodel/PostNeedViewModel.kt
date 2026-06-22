package com.fpl.charitylink.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpl.charitylink.data.model.Campaign
import com.fpl.charitylink.data.repository.CampaignRepository
import com.fpl.charitylink.data.repository.NotificationRepository
import com.fpl.charitylink.data.repository.OrganizationRepository
import com.fpl.charitylink.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


data class PostNeedUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val existingCampaign: Campaign? = null
)

class PostNeedViewModel : ViewModel() {
    private val campaignRepository = CampaignRepository()
    private val organizationRepository = OrganizationRepository()
    private val notificationRepository = NotificationRepository()
    private val userRepository = UserRepository()
    private val storage = FirebaseStorage.getInstance()
    private val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }

    private val _uiState = MutableStateFlow(PostNeedUiState())
    val uiState: StateFlow<PostNeedUiState> = _uiState

    fun loadCampaign(campaignId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val campaign = campaignRepository.getCampaign(campaignId)
                _uiState.value = _uiState.value.copy(isLoading = false, existingCampaign = campaign)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun postNeed(
        title: String,
        description: String,
        goalAmount: Double,
        category: String,
        selectedImageUri: Uri? = null,
        imageUrl: String? = null
    ) {
        val currentUser = auth?.currentUser ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val existing = _uiState.value.existingCampaign
                val finalImageUrl = when {
                    selectedImageUri != null -> uploadCampaignImage(
                        associationId = currentUser.uid,
                        campaignId = existing?.id ?: "new_${System.currentTimeMillis()}",
                        uri = selectedImageUri
                    )
                    !imageUrl.isNullOrBlank() -> imageUrl
                    existing != null -> existing.imageUrl
                    else -> null
                }

                if (existing != null) {
                    // Edit mode — update existing campaign
                    val updated = existing.copy(
                        title = title,
                        description = description,
                        goalAmount = goalAmount,
                        category = category,
                        imageUrl = finalImageUrl
                    )
                    campaignRepository.updateCampaign(updated)
                } else {
                    // Create mode — new campaign
                    val org = organizationRepository.getOrganization(currentUser.uid)
                    val orgName = org?.name ?: "Association"
                    val campaign = Campaign(
                        title = title,
                        description = description,
                        goalAmount = goalAmount,
                        category = category,
                        imageUrl = finalImageUrl,
                        associationId = currentUser.uid,
                        associationName = orgName,
                        createdAt = System.currentTimeMillis()
                    )
                    campaignRepository.createCampaign(campaign)

                    // Notify all donors about the new campaign
                    try {
                        val allDonors = userRepository.getAllDonors()
                        allDonors.forEach { donor ->
                            notificationRepository.createNewNeedNotification(
                                donorId = donor.uid,
                                associationName = orgName,
                                campaignTitle = title
                            )
                        }
                    } catch (_: Exception) {
                        // Notifications are non-critical, don't fail the post
                    }
                }
                _uiState.value = PostNeedUiState(isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = PostNeedUiState(errorMessage = e.message ?: "Failed to save campaign")
            }
        }
    }

    private suspend fun uploadCampaignImage(associationId: String, campaignId: String, uri: Uri): String {
        val ref = storage.reference.child("campaign_images/$associationId/$campaignId.jpg")
        val metadata = storageMetadata { contentType = "image/jpeg" }
        ref.putFile(uri, metadata).await()
        return ref.downloadUrl.await().toString()
    }

    fun resetState() {
        _uiState.value = PostNeedUiState()
    }
}
