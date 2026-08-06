package com.hsbc.portfoliomanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsbc.portfoliomanager.data.model.AssetType
import com.hsbc.portfoliomanager.data.model.DashboardResponse
import com.hsbc.portfoliomanager.data.repository.ApiResult
import com.hsbc.portfoliomanager.data.repository.PortfolioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val dashboard: DashboardResponse? = null,
    val error: String? = null,
    val selectedAssetType: AssetType? = null
)

class DashboardViewModel : ViewModel() {
    
    private val repository = PortfolioRepository()
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadDashboard()
    }
    
    fun loadDashboard(assetType: AssetType? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                error = null,
                selectedAssetType = assetType
            )
            
            val result = if (assetType != null) {
                repository.getDashboardByAssetType(assetType)
            } else {
                repository.getDashboard()
            }
            
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        dashboard = result.data,
                        error = null
                    )
                }
                is ApiResult.Error -> {
                    val fallback = if (assetType != null) {
                        repository.getLocalDashboardByAssetType(assetType)
                    } else {
                        repository.getLocalDashboard()
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        dashboard = fallback,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

