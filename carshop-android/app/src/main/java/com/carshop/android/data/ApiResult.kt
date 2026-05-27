package com.carshop.android.data

// sealed class:Success<T> / ApiError(code, msg) / NetworkError(throwable)
// 包一层让上层 (09/10) 不直接处理 retrofit Call/Response,只关注业务
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class ApiError(val code: Int, val message: String) : ApiResult<Nothing>()
    data class NetworkError(val throwable: Throwable) : ApiResult<Nothing>()
}
