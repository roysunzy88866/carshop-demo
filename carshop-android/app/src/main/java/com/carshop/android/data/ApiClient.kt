package com.carshop.android.data

import android.content.Context
import com.carshop.android.BuildConfig
import com.carshop.android.data.api.BannerApi
import com.carshop.android.data.api.CategoryApi
import com.carshop.android.data.api.OrderApi
import com.carshop.android.data.api.ProductApi
import com.carshop.android.data.interceptors.DeviceIdInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

// 单例 ApiClient,由 CarshopApplication.onCreate 调 init() 启动。
// USE_MOCK=true 时 baseUrl 来自 MockApiServer 启动后的 URL(不是 BuildConfig)。
// USE_MOCK=false 时 baseUrl 来自 BuildConfig.API_BASE_URL(realDebug 走公网)。
object ApiClient {
    @Volatile private var retrofit: Retrofit? = null
    @Volatile private var initOnce = false

    lateinit var categoryApi: CategoryApi private set
    lateinit var productApi: ProductApi private set
    lateinit var bannerApi: BannerApi private set
    lateinit var orderApi: OrderApi private set

    fun init(appContext: Context, baseUrl: String) {
        synchronized(this) {
            if (initOnce) return
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val logging = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }

            val okHttp = OkHttpClient.Builder()
                .addInterceptor(DeviceIdInterceptor(appContext.applicationContext))
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()

            val r = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttp)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            retrofit = r
            categoryApi = r.create(CategoryApi::class.java)
            productApi = r.create(ProductApi::class.java)
            bannerApi = r.create(BannerApi::class.java)
            orderApi = r.create(OrderApi::class.java)
            initOnce = true
        }
    }
}
