package com.hsbc.portfoliomanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsbc.portfoliomanager.data.model.PerformanceItem
import com.hsbc.portfoliomanager.data.repository.ApiResult
import com.hsbc.portfoliomanager.data.repository.PortfolioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PerformanceUiState(
    val isLoading: Boolean = false,
    val items: List<PerformanceItem> = emptyList(),
    val error: String? = null
)

class PerformanceViewModel : ViewModel() {

    private val repository = PortfolioRepository()

    private val _uiState = MutableStateFlow(PerformanceUiState())
    val uiState: StateFlow<PerformanceUiState> = _uiState.asStateFlow()

    init {
        loadPerformance()
    }

    fun loadPerformance() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.getPerformance()) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false, items = result.data
                )
                is ApiResult.Error   -> _uiState.value = _uiState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
