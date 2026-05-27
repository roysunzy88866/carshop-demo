package com.carshop.android.ui.checkout.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carshop.android.data.ApiResult
import com.carshop.android.data.dto.Order
import com.carshop.android.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class OrderListTab(val label: String, val status: String?) {
    All("全部", null),
    Paid("已支付", "paid"),
    Pending("待支付", "pending"),
}

sealed class OrderListUiState {
    object Loading : OrderListUiState()
    data class Success(val orders: List<Order>) : OrderListUiState()
    object Empty : OrderListUiState()
    data class Error(val message: String) : OrderListUiState()
}

class OrderListViewModel : ViewModel() {

    private val _selectedTab = MutableStateFlow(OrderListTab.All)
    val selectedTab: StateFlow<OrderListTab> = _selectedTab.asStateFlow()

    private val _state = MutableStateFlow<OrderListUiState>(OrderListUiState.Loading)
    val state: StateFlow<OrderListUiState> = _state.asStateFlow()

    init { load(OrderListTab.All) }

    fun selectTab(tab: OrderListTab) {
        if (tab == _selectedTab.value) return
        _selectedTab.value = tab
        load(tab)
    }

    fun refresh() = load(_selectedTab.value)

    private fun load(tab: OrderListTab) {
        viewModelScope.launch {
            _state.value = OrderListUiState.Loading
            when (val r = OrderRepository.list(status = tab.status, page = 1, pageSize = 100)) {
                is ApiResult.Success -> {
                    val list = r.data.list
                    _state.value = if (list.isEmpty()) OrderListUiState.Empty
                    else OrderListUiState.Success(list)
                }
                is ApiResult.ApiError -> _state.value = OrderListUiState.Error(r.message)
                is ApiResult.NetworkError -> _state.value = OrderListUiState.Error("网络异常,请重试")
            }
        }
    }
}
