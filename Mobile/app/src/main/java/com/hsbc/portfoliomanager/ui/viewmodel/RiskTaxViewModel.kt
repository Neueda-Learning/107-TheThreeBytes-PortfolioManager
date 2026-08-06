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

data class RiskTaxUiState(
    val isLoading: Boolean = false,
    val riskAnalysis: RiskAnalysisResponse? = null,
    val taxItems: List<TaxItem> = emptyList(),
    val dividends: List<DividendResponse> = emptyList(),
    val totalDividends: BigDecimal = BigDecimal.ZERO,
    val error: String? = null,
    val successMessage: String? = null
)

class RiskTaxViewModel : ViewModel() {

    private val repository = PortfolioRepository()

    private val _uiState = MutableStateFlow(RiskTaxUiState())
    val uiState: StateFlow<RiskTaxUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        loadRiskAnalysis()
        loadTaxEstimate()
        loadDividends()
    }

    fun loadRiskAnalysis() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.getRiskAnalysis()) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false, riskAnalysis = result.data
                )
                is ApiResult.Error   -> _uiState.value = _uiState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }

    fun loadTaxEstimate() {
        viewModelScope.launch {
            when (val result = repository.getTaxEstimate()) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(taxItems = result.data)
                is ApiResult.Error   -> _uiState.value = _uiState.value.copy(error = result.message)
                else -> {}
            }
        }
    }

    fun loadDividends() {
        viewModelScope.launch {
            when (val result = repository.getDividends()) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(dividends = result.data)
                is ApiResult.Error   -> {}
                else -> {}
            }
            when (val result = repository.getTotalDividends()) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(totalDividends = result.data)
                else -> {}
            }
        }
    }

    fun createDividend(request: CreateDividendRequest) {
        viewModelScope.launch {
            when (val result = repository.createDividend(request)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Dividend recorded")
                    loadDividends()
                }
                is ApiResult.Error   -> _uiState.value = _uiState.value.copy(error = result.message)
                else -> {}
            }
        }
    }

    fun deleteDividend(id: Long) {
        viewModelScope.launch {
            when (val result = repository.deleteDividend(id)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Dividend deleted")
                    loadDividends()
                }
                is ApiResult.Error   -> _uiState.value = _uiState.value.copy(error = result.message)
                else -> {}
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
