package com.carshop.android.data.api

import com.carshop.android.data.dto.ApiResponse
import com.carshop.android.data.dto.PagedList
import com.carshop.android.data.dto.Product
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {
    // GET /api/v1/products?category_id=&page=&page_size= (只返 on_sale=true)
    @GET("products")
    suspend fun list(
        @Query("category_id") categoryId: Int? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): Response<ApiResponse<PagedList<Product>>>

    // GET /api/v1/products/{id}(下架商品也能拿到,前端处理)
    @GET("products/{id}")
    suspend fun detail(@Path("id") id: Int): Response<ApiResponse<Product>>
}
