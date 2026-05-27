package com.carshop.android.data.api

import com.carshop.android.data.dto.ApiResponse
import com.carshop.android.data.dto.Category
import retrofit2.Response
import retrofit2.http.GET

interface CategoryApi {
    // GET /api/v1/categories —— 返回 List<Category>(不分页)
    @GET("categories")
    suspend fun list(): Response<ApiResponse<List<Category>>>
}
