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

data class TransactionUiState(
    val isLoading: Boolean = false,
    val transactions: List<TransactionResponse> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

class TransactionViewModel : ViewModel() {

    private val repository = PortfolioRepository()

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    fun loadTransactions(ticker: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.getTransactions(ticker)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false, transactions = result.data
                )
                is ApiResult.Error   -> _uiState.value = _uiState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }

    fun createTransaction(request: CreateTransactionRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.createTransaction(request)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Transaction recorded successfully"
                    )
                    loadTransactions()
                }
                is ApiResult.Error   -> _uiState.value = _uiState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.deleteTransaction(id)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Transaction deleted"
                    )
                    loadTransactions()
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
