package com.carshop.android.data.dto

import com.squareup.moshi.Json

data class Category(
    val id: Int,
    val name: String,
    @Json(name = "icon_url") val iconUrl: String,
    val sort: Int,
    @Json(name = "created_at") val createdAt: String,
)
