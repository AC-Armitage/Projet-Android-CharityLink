package com.fpl.charitylink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpl.charitylink.data.model.Organization
import com.fpl.charitylink.data.repository.OrganizationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ExploreUiState(
    val isLoading: Boolean = true,
    val organizations: List<Organization> = emptyList(),
    val filteredOrganizations: List<Organization> = emptyList(),
    val errorMessage: String? = null
)

class ExploreViewModel : ViewModel() {
    private val organizationRepository = OrganizationRepository()

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState

    init {
        loadOrganizations()
    }

    private fun loadOrganizations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val orgs = organizationRepository.getAllOrganizations() // now returns all
                _uiState.value = ExploreUiState(
                    isLoading = false,
                    organizations = orgs,
                    filteredOrganizations = orgs
                )
            } catch (e: Exception) {
                _uiState.value = ExploreUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load organizations"
                )
            }
        }
    }

    fun search(query: String) {
        val all = _uiState.value.organizations
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(filteredOrganizations = all)
        } else {
            val filtered = all.filter { 
                it.name.contains(query, ignoreCase = true) || 
                (it.description?.contains(query, ignoreCase = true) ?: false)
            }
            _uiState.value = _uiState.value.copy(filteredOrganizations = filtered)
        }
    }
}
