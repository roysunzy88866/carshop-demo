package com.carshop.android.ui.browse.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carshop.android.data.ApiResult
import com.carshop.android.data.dto.Product
import com.carshop.android.data.repository.CategoryRepository
import com.carshop.android.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 09 · 分类商品列表 page_size=10,触发 US-03 分页验证
// PageSize 10:首页推荐走 20(MockApiServer routeProductsList 也按这点区分 fixtures)
private const val PAGE_SIZE = 10

data class CategoryProductsState(
    val categoryName: String = "",
    val items: List<Product> = emptyList(),
    val page: Int = 0,                  // 已加载到第几页(0=还没开始)
    val total: Int = 0,
    val isInitialLoading: Boolean = false,
    val isAppending: Boolean = false,
    val initialError: SectionError? = null,    // 整页错误(首屏失败)
    val appendError: SectionError? = null,     // 局部错误(下一页失败,Toast 用)
) {
    val hasMore: Boolean get() = items.size < total && page > 0
    val isEmpty: Boolean get() = page > 0 && items.isEmpty() && total == 0
}

data class SectionError(val code: Int, val message: String, val isNetwork: Boolean)

class CategoryProductsViewModel(
    savedState: SavedStateHandle,
) : ViewModel() {
    private val categoryId: Int =
        savedState.get<String>("categoryId")?.toIntOrNull()
            ?: throw IllegalArgumentException("categoryId 缺失或非整数")

    private val _state = MutableStateFlow(CategoryProductsState())
    val state: StateFlow<CategoryProductsState> = _state.asStateFlow()

    init {
        loadCategoryNameThenFirstPage()
    }

    /** TopBar 标题需要分类名,先请求 categories list 拿到名字(沿用 CategoryRepository,不新增接口) */
    private fun loadCategoryNameThenFirstPage() {
        viewModelScope.launch {
            val r = CategoryRepository.list()
            if (r is ApiResult.Success) {
                val name = r.data.firstOrNull { it.id == categoryId }?.name ?: "分类 $categoryId"
                _state.value = _state.value.copy(categoryName = name)
            }
            loadFirstPage()
        }
    }

    fun loadFirstPage() {
        _state.value = _state.value.copy(
            isInitialLoading = true,
            initialError = null,
            appendError = null,
        )
        viewModelScope.launch {
            when (val r = ProductRepository.list(categoryId, page = 1, pageSize = PAGE_SIZE)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        items = r.data.list,
                        page = 1,
                        total = r.data.total,
                        isInitialLoading = false,
                    )
                }
                is ApiResult.ApiError -> _state.value = _state.value.copy(
                    isInitialLoading = false,
                    initialError = SectionError(r.code, r.message, false),
                )
                is ApiResult.NetworkError -> _state.value = _state.value.copy(
                    isInitialLoading = false,
                    initialError = SectionError(-1, r.throwable.message ?: "网络错误", true),
                )
            }
        }
    }

    fun loadNextPage() {
        val cur = _state.value
        if (cur.isAppending || cur.isInitialLoading || !cur.hasMore) return
        _state.value = cur.copy(isAppending = true, appendError = null)
        val nextPage = cur.page + 1
        viewModelScope.launch {
            when (val r = ProductRepository.list(categoryId, page = nextPage, pageSize = PAGE_SIZE)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        items = _state.value.items + r.data.list,
                        page = nextPage,
                        total = r.data.total,
                        isAppending = false,
                    )
                }
                is ApiResult.ApiError -> _state.value = _state.value.copy(
                    isAppending = false,
                    appendError = SectionError(r.code, r.message, false),
                )
                is ApiResult.NetworkError -> _state.value = _state.value.copy(
                    isAppending = false,
                    appendError = SectionError(-1, r.throwable.message ?: "网络错误", true),
                )
            }
        }
    }

    fun retryAppend() {
        _state.value = _state.value.copy(appendError = null)
        loadNextPage()
    }
}
