package com.carshop.android.data.repository

import com.carshop.android.data.ApiClient
import com.carshop.android.data.ApiResult
import com.carshop.android.data.dto.ApiResponse
import com.carshop.android.data.dto.Banner
import com.carshop.android.data.dto.Category
import com.carshop.android.data.dto.CreateOrderRequest
import com.carshop.android.data.dto.Order
import com.carshop.android.data.dto.PagedList
import com.carshop.android.data.dto.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

// 09 session · 把 Retrofit Response<ApiResponse<T>> 一步转换成 ApiResult<T>
// 三种来源:
//   1. Response.isSuccessful=false → ApiError(http code, http message)
//   2. body.code != 0           → ApiError(body.code, body.message)
//   3. 抛异常(IO / 解析 / 超时) → NetworkError(throwable)
// data null 但 code=0 也按 ApiError 处理(契约违反,但前端不该崩)
private suspend fun <T : Any> callApi(call: suspend () -> Response<ApiResponse<T>>): ApiResult<T> {
    return try {
        val resp = withContext(Dispatchers.IO) { call() }
        if (!resp.isSuccessful) {
            ApiResult.ApiError(resp.code(), "HTTP ${resp.code()} ${resp.message()}")
        } else {
            val body = resp.body()
            if (body == null) {
                ApiResult.ApiError(9000, "响应体为空")
            } else if (body.code != 0) {
                ApiResult.ApiError(body.code, body.message)
            } else {
                val data = body.data
                if (data == null) {
                    ApiResult.ApiError(9000, "响应 data 为空")
                } else {
                    ApiResult.Success(data)
                }
            }
        }
    } catch (t: Throwable) {
        ApiResult.NetworkError(t)
    }
}

object CategoryRepository {
    suspend fun list(): ApiResult<List<Category>> =
        callApi { ApiClient.categoryApi.list() }
}

object ProductRepository {
    suspend fun list(categoryId: Int? = null, page: Int = 1, pageSize: Int = 20):
        ApiResult<PagedList<Product>> =
        callApi { ApiClient.productApi.list(categoryId, page, pageSize) }

    suspend fun detail(id: Int): ApiResult<Product> =
        callApi { ApiClient.productApi.detail(id) }
}

object BannerRepository {
    suspend fun list(): ApiResult<List<Banner>> =
        callApi { ApiClient.bannerApi.list() }
}

object OrderRepository {
    suspend fun create(productId: Int, quantity: Int = 1): ApiResult<Order> =
        callApi { ApiClient.orderApi.create(CreateOrderRequest(productId, quantity)) }

    suspend fun list(status: String? = null, page: Int = 1, pageSize: Int = 20): ApiResult<PagedList<Order>> =
        callApi { ApiClient.orderApi.list(status, page, pageSize) }

    suspend fun detail(id: String): ApiResult<Order> =
        callApi { ApiClient.orderApi.detail(id) }

    suspend fun mockPay(id: String): ApiResult<Order> =
        callApi { ApiClient.orderApi.mockPay(id) }
}
