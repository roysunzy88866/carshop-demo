package com.carshop.android.ui.browse.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carshop.android.data.ApiResult
import com.carshop.android.data.dto.Product
import com.carshop.android.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 09 · 商品详情 state
sealed class ProductDetailState {
    data object Loading : ProductDetailState()
    data class Success(val product: Product) : ProductDetailState()
    data object NotFound : ProductDetailState()                  // code=1001 / HTTP 404
    data class Error(val message: String, val isNetwork: Boolean) : ProductDetailState()
}

class ProductDetailViewModel(savedState: SavedStateHandle) : ViewModel() {
    private val productId: Int =
        savedState.get<String>("productId")?.toIntOrNull()
            ?: throw IllegalArgumentException("productId 缺失或非整数")

    private val _state = MutableStateFlow<ProductDetailState>(ProductDetailState.Loading)
    val state: StateFlow<ProductDetailState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = ProductDetailState.Loading
        viewModelScope.launch {
            _state.value = when (val r = ProductRepository.detail(productId)) {
                is ApiResult.Success -> ProductDetailState.Success(r.data)
                is ApiResult.ApiError -> {
                    if (r.code == 1001 || r.code == 404) ProductDetailState.NotFound
                    else ProductDetailState.Error(r.message, false)
                }
                is ApiResult.NetworkError -> ProductDetailState.Error(
                    r.throwable.message ?: "网络错误", true
                )
            }
        }
    }
}
