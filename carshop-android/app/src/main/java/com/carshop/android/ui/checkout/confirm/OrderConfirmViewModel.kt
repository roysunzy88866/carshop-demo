package com.carshop.android.ui.checkout.confirm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carshop.android.data.ApiResult
import com.carshop.android.data.dto.Order
import com.carshop.android.data.dto.Product
import com.carshop.android.data.repository.OrderRepository
import com.carshop.android.data.repository.ProductRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OrderConfirmUiState {
    object Loading : OrderConfirmUiState()
    data class Ready(
        val product: Product,
        val isSubmitting: Boolean = false,
        val payDialog: PayDialogUiState? = null,
    ) : OrderConfirmUiState()
    data class Error(val message: String) : OrderConfirmUiState()
}

data class PayDialogUiState(
    val order: Order,
    val remainingSeconds: Int = 120,
    val isPaying: Boolean = false,
)

class OrderConfirmViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val productId: Int =
        savedStateHandle.get<String>("productId")?.toIntOrNull() ?: -1

    private val _state = MutableStateFlow<OrderConfirmUiState>(OrderConfirmUiState.Loading)
    val state: StateFlow<OrderConfirmUiState> = _state.asStateFlow()

    // 支付成功后发射订单号,Screen 收到后 navigate
    private val _paySuccessEvent = MutableSharedFlow<String>()
    val paySuccessEvent: SharedFlow<String> = _paySuccessEvent.asSharedFlow()

    // 超时或错误 Toast 消息
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    private var countdownJob: Job? = null

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _state.value = OrderConfirmUiState.Loading
            when (val r = ProductRepository.detail(productId)) {
                is ApiResult.Success -> _state.value = OrderConfirmUiState.Ready(r.data)
                is ApiResult.ApiError -> _state.value = OrderConfirmUiState.Error(
                    when (r.code) {
                        1001 -> "商品已失效"
                        3000 -> "商品已下架"
                        else -> r.message
                    }
                )
                is ApiResult.NetworkError -> _state.value = OrderConfirmUiState.Error("网络异常,请重试")
            }
        }
    }

    fun submitOrder() {
        val current = _state.value as? OrderConfirmUiState.Ready ?: return
        if (current.isSubmitting) return
        _state.value = current.copy(isSubmitting = true)

        viewModelScope.launch {
            when (val r = OrderRepository.create(productId)) {
                is ApiResult.Success -> {
                    _state.value = current.copy(isSubmitting = false)
                    showPayDialog(r.data)
                }
                is ApiResult.ApiError -> {
                    _state.value = current.copy(isSubmitting = false)
                    val msg = when (r.code) {
                        1001 -> "商品已失效"
                        3000 -> "商品已下架"
                        else -> r.message
                    }
                    _toastEvent.emit(msg)
                }
                is ApiResult.NetworkError -> {
                    _state.value = current.copy(isSubmitting = false)
                    _toastEvent.emit("网络异常,请重试")
                }
            }
        }
    }

    private fun showPayDialog(order: Order) {
        val current = _state.value as? OrderConfirmUiState.Ready ?: return
        _state.value = current.copy(payDialog = PayDialogUiState(order))
        startCountdown(order.id)
    }

    private fun startCountdown(orderId: String) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (remaining in 119 downTo 0) {
                delay(1000L)
                val s = _state.value
                if (s is OrderConfirmUiState.Ready && s.payDialog?.order?.id == orderId) {
                    if (s.payDialog.isPaying) break  // 已在支付中,停止倒计时
                    _state.value = s.copy(payDialog = s.payDialog.copy(remainingSeconds = remaining))
                    if (remaining == 0) {
                        _state.value = s.copy(payDialog = null)
                        _toastEvent.emit("支付超时")
                        break
                    }
                } else {
                    break
                }
            }
        }
    }

    // 用户点击支付弹层任意位置触发
    fun triggerPay() {
        val s = _state.value as? OrderConfirmUiState.Ready ?: return
        val dialog = s.payDialog ?: return
        if (dialog.isPaying) return

        countdownJob?.cancel()
        _state.value = s.copy(payDialog = dialog.copy(isPaying = true))

        viewModelScope.launch {
            delay(1000L)  // spec §1.1.B: 1 秒延迟模拟扫码
            when (val r = OrderRepository.mockPay(dialog.order.id)) {
                is ApiResult.Success -> {
                    _state.value = (_state.value as? OrderConfirmUiState.Ready)
                        ?.copy(payDialog = null) ?: _state.value
                    _paySuccessEvent.emit(dialog.order.id)
                }
                is ApiResult.ApiError -> {
                    _state.value = (_state.value as? OrderConfirmUiState.Ready)
                        ?.copy(payDialog = null) ?: _state.value
                    _toastEvent.emit(r.message)
                }
                is ApiResult.NetworkError -> {
                    _state.value = (_state.value as? OrderConfirmUiState.Ready)
                        ?.copy(payDialog = null) ?: _state.value
                    _toastEvent.emit("网络异常,请重试")
                }
            }
        }
    }

    fun dismissPayDialog() {
        countdownJob?.cancel()
        val s = _state.value as? OrderConfirmUiState.Ready ?: return
        _state.value = s.copy(payDialog = null)
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
