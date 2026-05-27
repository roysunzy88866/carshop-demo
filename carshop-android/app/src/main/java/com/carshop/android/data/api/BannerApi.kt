package com.carshop.android.data.api

import com.carshop.android.data.dto.ApiResponse
import com.carshop.android.data.dto.Banner
import retrofit2.Response
import retrofit2.http.GET

interface BannerApi {
    // GET /api/v1/banners (sort 升序,只返 on_show=true)
    @GET("banners")
    suspend fun list(): Response<ApiResponse<List<Banner>>>
}
