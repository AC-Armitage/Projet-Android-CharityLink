package com.fpl.charitylink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpl.charitylink.data.model.Campaign
import com.fpl.charitylink.data.repository.CampaignRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class NeedDetailUiState(
    val isLoading: Boolean = true,
    val campaign: Campaign? = null,
    val errorMessage: String? = null
)

class NeedDetailViewModel : ViewModel() {
    private val campaignRepository = CampaignRepository()

    private val _uiState = MutableStateFlow(NeedDetailUiState())
    val uiState: StateFlow<NeedDetailUiState> = _uiState

    fun loadNeed(needId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val campaign = campaignRepository.getCampaign(needId)
                if (campaign != null) {
                    _uiState.value = NeedDetailUiState(
                        isLoading = false,
                        campaign = campaign
                    )
                } else {
                    _uiState.value = NeedDetailUiState(
                        isLoading = false,
                        errorMessage = "Campaign not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = NeedDetailUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load campaign details"
                )
            }
        }
    }
}
