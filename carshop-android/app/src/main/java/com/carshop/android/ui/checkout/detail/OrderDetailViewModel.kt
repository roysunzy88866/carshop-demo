package com.carshop.android.ui.checkout.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carshop.android.data.ApiResult
import com.carshop.android.data.dto.Order
import com.carshop.android.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OrderDetailUiState {
    object Loading : OrderDetailUiState()
    data class Success(val order: Order) : OrderDetailUiState()
    object NotFound : OrderDetailUiState()
    data class Error(val message: String) : OrderDetailUiState()
}

class OrderDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val orderId: String = savedStateHandle.get<String>("orderId").orEmpty()

    private val _state = MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Loading)
    val state: StateFlow<OrderDetailUiState> = _state.asStateFlow()

    init { load() }

    fun retry() = load()

    private fun load() {
        if (orderId.isBlank()) {
            _state.value = OrderDetailUiState.NotFound
            return
        }
        viewModelScope.launch {
            _state.value = OrderDetailUiState.Loading
            when (val r = OrderRepository.detail(orderId)) {
                is ApiResult.Success -> _state.value = OrderDetailUiState.Success(r.data)
                is ApiResult.ApiError -> _state.value =
                    if (r.code == 1001 || r.code == 404) OrderDetailUiState.NotFound
                    else OrderDetailUiState.Error(r.message)
                is ApiResult.NetworkError -> _state.value = OrderDetailUiState.Error("网络异常,请重试")
            }
        }
    }
}
