package com.carshop.android.data.dto

import com.squareup.moshi.JsonClass

// SPEC §6 envelope:{ code, data, message }。code=0 表示成功
@JsonClass(generateAdapter = false)
data class ApiResponse<T>(
    val code: Int,
    val data: T?,
    val message: String,
)
