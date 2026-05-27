package com.carshop.android.data.api

import com.carshop.android.data.dto.ApiResponse
import com.carshop.android.data.dto.CreateOrderRequest
import com.carshop.android.data.dto.Order
import com.carshop.android.data.dto.PagedList
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApi {
    // POST /api/v1/orders body { product_id, quantity }
    // X-Device-Id 由 DeviceIdInterceptor 全局注入,不用在签名里
    @POST("orders")
    suspend fun create(@Body body: CreateOrderRequest): Response<ApiResponse<Order>>

    // GET /api/v1/orders?status=&page=&page_size= (按 X-Device-Id 过滤)
    @GET("orders")
    suspend fun list(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): Response<ApiResponse<PagedList<Order>>>

    // GET /api/v1/orders/{id} (仅限当前设备的订单)
    @GET("orders/{id}")
    suspend fun detail(@Path("id") id: String): Response<ApiResponse<Order>>

    // POST /api/v1/orders/{id}/mock_pay
    @POST("orders/{id}/mock_pay")
    suspend fun mockPay(@Path("id") id: String): Response<ApiResponse<Order>>
}
