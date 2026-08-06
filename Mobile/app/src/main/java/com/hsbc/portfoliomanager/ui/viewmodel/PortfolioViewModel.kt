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
import java.math.BigDecimal

data class PortfolioUiState(
    val isLoading: Boolean = false,
    val items: List<PortfolioItem> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

class PortfolioViewModel : ViewModel() {

    private val repository = PortfolioRepository()

    private val _uiState = MutableStateFlow(PortfolioUiState())
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getPortfolioFlow().collect { items ->
                _uiState.value = _uiState.value.copy(
                    items = items, isLoading = false, error = null
                )
            }
        }
        loadPortfolioItems()
    }

    fun loadPortfolioItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.getPortfolioItems()) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false, items = result.data, error = null
                )
                is ApiResult.Error   -> _uiState.value = _uiState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }

    fun createPortfolioItem(request: CreatePortfolioItemRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.createPortfolioItem(request)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "${request.ticker} added to portfolio"
                    )
                    loadPortfolioItems()
                }
                is ApiResult.Error   -> _uiState.value = _uiState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }

    fun updatePortfolioItem(id: Long, request: UpdatePortfolioItemRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.updatePortfolioItem(id, request)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Holding updated successfully"
                    )
                    loadPortfolioItems()
                }
                is ApiResult.Error   -> _uiState.value = _uiState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }

    fun deletePortfolioItem(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.deletePortfolioItem(id)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Holding deleted"
                    )
                    loadPortfolioItems()
                }
                is ApiResult.Error   -> _uiState.value = _uiState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }

    fun sellPortfolioItem(id: Long, pricePerUnit: BigDecimal, quantity: BigDecimal) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val request = SellHoldingRequest(pricePerUnit = pricePerUnit, quantity = quantity)
            when (val result = repository.sellPortfolioItem(id, request)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Sold successfully at ₹${pricePerUnit}/unit"
                    )
                    loadPortfolioItems()
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
