package com.hsbc.portfoliomanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsbc.portfoliomanager.data.model.*
import com.hsbc.portfoliomanager.data.repository.ApiResult
import com.hsbc.portfoliomanager.data.repository.PortfolioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WatchlistUiState(
    val isLoading: Boolean = false,
    val items: List<WatchlistItem> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

class WatchlistViewModel : ViewModel() {

    private val repository = PortfolioRepository()

    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    init {
        loadWatchlist()
    }

    fun loadWatchlist() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.getWatchlist()) {
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

    fun addToWatchlist(ticker: String, assetType: AssetType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val request = CreateWatchlistItemRequest(ticker = ticker.trim().uppercase(), assetType = assetType)
            when (val result = repository.addToWatchlist(request)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "$ticker added to watchlist"
                    )
                    loadWatchlist()
                }
                is ApiResult.Error   -> _uiState.value = _uiState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }

    fun removeFromWatchlist(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.removeFromWatchlist(id)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Removed from watchlist"
                    )
                    loadWatchlist()
                }
                is ApiResult.Error   -> _uiState.value = _uiState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
