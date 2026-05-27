package com.carshop.android.data.dto

import com.squareup.moshi.Json

data class Banner(
    val id: Int,
    @Json(name = "image_url") val imageUrl: String,
    // SPEC §5:"none" | "product" | "category"
    @Json(name = "link_type") val linkType: String,
    @Json(name = "link_target") val linkTarget: Int?,
    val sort: Int,
    @Json(name = "on_show") val onShow: Boolean,
)
