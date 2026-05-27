package com.carshop.android.data.interceptors

import android.content.Context
import com.carshop.android.data.DeviceIdProvider
import okhttp3.Interceptor
import okhttp3.Response

// 用户钦点:挂在 OkHttpClient.Builder 上,不挂 Retrofit
// 全局每个请求注入 X-Device-Id header(SPEC §11.5)
class DeviceIdInterceptor(private val appContext: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val deviceId = DeviceIdProvider.get(appContext)
        val newRequest = chain.request().newBuilder()
            .header("X-Device-Id", deviceId)
            .build()
        return chain.proceed(newRequest)
    }
}
