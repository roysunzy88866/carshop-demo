package com.carshop.android.ui.browse.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carshop.android.data.ApiResult
import com.carshop.android.data.dto.Banner
import com.carshop.android.data.dto.Category
import com.carshop.android.data.dto.Product
import com.carshop.android.data.repository.BannerRepository
import com.carshop.android.data.repository.CategoryRepository
import com.carshop.android.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 09 · 首页分区独立 skeleton:三块 state 各自加载,哪个先回来先填
sealed class HomeSectionState<out T> {
    data object Loading : HomeSectionState<Nothing>()
    data class Success<T>(val data: T) : HomeSectionState<T>()
    data object Empty : HomeSectionState<Nothing>()         // 接口成功但 list 为空(banner / 推荐空时整区隐藏)
    data class Error(val code: Int, val message: String, val isNetwork: Boolean) : HomeSectionState<Nothing>()
}

class HomeViewModel : ViewModel() {
    private val _banners = MutableStateFlow<HomeSectionState<List<Banner>>>(HomeSectionState.Loading)
    val banners: StateFlow<HomeSectionState<List<Banner>>> = _banners.asStateFlow()

    private val _categories = MutableStateFlow<HomeSectionState<List<Category>>>(HomeSectionState.Loading)
    val categories: StateFlow<HomeSectionState<List<Category>>> = _categories.asStateFlow()

    private val _recommend = MutableStateFlow<HomeSectionState<List<Product>>>(HomeSectionState.Loading)
    val recommend: StateFlow<HomeSectionState<List<Product>>> = _recommend.asStateFlow()

    init { refreshAll() }

    fun refreshAll() {
        loadBanners()
        loadCategories()
        loadRecommend()
    }

    private fun loadBanners() {
        _banners.value = HomeSectionState.Loading
        viewModelScope.launch {
            _banners.value = when (val r = BannerRepository.list()) {
                is ApiResult.Success -> {
                    val visible = r.data.filter { it.onShow }
                    if (visible.isEmpty()) HomeSectionState.Empty else HomeSectionState.Success(visible)
                }
                is ApiResult.ApiError -> HomeSectionState.Error(r.code, r.message, isNetwork = false)
                is ApiResult.NetworkError -> HomeSectionState.Error(-1, r.throwable.message ?: "网络错误", isNetwork = true)
            }
        }
    }

    private fun loadCategories() {
        _categories.value = HomeSectionState.Loading
        viewModelScope.launch {
            _categories.value = when (val r = CategoryRepository.list()) {
                is ApiResult.Success -> {
                    if (r.data.isEmpty()) HomeSectionState.Empty else HomeSectionState.Success(r.data)
                }
                is ApiResult.ApiError -> HomeSectionState.Error(r.code, r.message, isNetwork = false)
                is ApiResult.NetworkError -> HomeSectionState.Error(-1, r.throwable.message ?: "网络错误", isNetwork = true)
            }
        }
    }

    private fun loadRecommend() {
        _recommend.value = HomeSectionState.Loading
        viewModelScope.launch {
            // 推荐 = 首页 GET /products 第一页前 10(MVP · 用户拍板)
            _recommend.value = when (val r = ProductRepository.list(page = 1, pageSize = 20)) {
                is ApiResult.Success -> {
                    val take = r.data.list.take(10)
                    if (take.isEmpty()) HomeSectionState.Empty else HomeSectionState.Success(take)
                }
                is ApiResult.ApiError -> HomeSectionState.Error(r.code, r.message, isNetwork = false)
                is ApiResult.NetworkError -> HomeSectionState.Error(-1, r.throwable.message ?: "网络错误", isNetwork = true)
            }
        }
    }
}
