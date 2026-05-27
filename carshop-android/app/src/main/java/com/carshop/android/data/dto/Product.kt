package com.carshop.android.data.dto

import com.squareup.moshi.Json

data class Product(
    val id: Int,
    @Json(name = "category_id") val categoryId: Int,
    @Json(name = "category_name") val categoryName: String?,
    val title: String,
    // SPEC §5:"physical" 或 "service_voucher"
    @Json(name = "product_type") val productType: String,
    val price: Int,
    @Json(name = "original_price") val originalPrice: Int?,
    val spec: String?,
    @Json(name = "main_image_url") val mainImageUrl: String,
    val description: String,
    @Json(name = "on_sale") val onSale: Boolean,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
)
