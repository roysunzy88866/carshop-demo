package com.carshop.android

import android.app.Application
import android.util.Log
import com.carshop.android.data.ApiClient
import com.carshop.android.data.MockApiServer

// onCreate:USE_MOCK=true → 启动本地 MockWebServer 拿到 url → init ApiClient
//          USE_MOCK=false → 直接用 BuildConfig.API_BASE_URL init ApiClient
class CarshopApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val baseUrl = if (BuildConfig.USE_MOCK) {
            val url = MockApiServer.start(this)
            Log.i("CarshopApplication", "USE_MOCK=true, baseUrl=$url")
            url
        } else {
            val url = BuildConfig.API_BASE_URL
            Log.i("CarshopApplication", "USE_MOCK=false, baseUrl=$url")
            url
        }
        ApiClient.init(this, baseUrl)
    }
}
