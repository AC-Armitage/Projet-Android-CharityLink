package com.fpl.charitylink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpl.charitylink.data.model.Campaign
import com.fpl.charitylink.data.repository.CampaignRepository
import com.fpl.charitylink.data.repository.OrganizationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PostNeedUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class PostNeedViewModel : ViewModel() {
    private val campaignRepository = CampaignRepository()
    private val organizationRepository = OrganizationRepository()
    private val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }

    private val _uiState = MutableStateFlow(PostNeedUiState())
    val uiState: StateFlow<PostNeedUiState> = _uiState

    fun postNeed(
        title: String,
        description: String,
        goalAmount: Double,
        category: String,
        imageUrl: String? = null
    ) {
        val currentUser = auth?.currentUser ?: return
        
        viewModelScope.launch {
            _uiState.value = PostNeedUiState(isLoading = true)
            try {
                // Get organization name
                val org = organizationRepository.getOrganization(currentUser.uid)
                val orgName = org?.name ?: "Association"

                val campaign = Campaign(
                    title = title,
                    description = description,
                    goalAmount = goalAmount,
                    category = category,
                    imageUrl = imageUrl,
                    associationId = currentUser.uid,
                    associationName = orgName,
                    createdAt = System.currentTimeMillis()
                )
                
                campaignRepository.createCampaign(campaign)
                _uiState.value = PostNeedUiState(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = PostNeedUiState(
                    isLoading = false, 
                    errorMessage = e.message ?: "Failed to post need"
                )
            }
        }
    }
    
    fun resetState() {
        _uiState.value = PostNeedUiState()
    }
}
