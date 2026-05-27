package com.carshop.android.data

import android.content.Context
import android.provider.Settings

// SPEC §11.5:用 Settings.Secure.ANDROID_ID 作为 X-Device-Id
// Android 8+ 同一应用稳定,wipe data 后会变(预期内,Demo 不解决)
object DeviceIdProvider {
    @Volatile private var cached: String? = null

    fun get(context: Context): String {
        cached?.let { return it }
        synchronized(this) {
            cached?.let { return it }
            @Suppress("HardwareIds")
            val id = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID,
            ) ?: "unknown-device"
            cached = id
            return id
        }
    }
}
