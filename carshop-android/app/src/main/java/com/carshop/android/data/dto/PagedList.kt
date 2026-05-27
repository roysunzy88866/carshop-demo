package com.carshop.android.data.dto

import com.squareup.moshi.Json

// SPEC §13 分页 envelope:{ list, total, page, page_size }
data class PagedList<T>(
    val list: List<T>,
    val total: Int,
    val page: Int,
    @Json(name = "page_size") val pageSize: Int,
)
