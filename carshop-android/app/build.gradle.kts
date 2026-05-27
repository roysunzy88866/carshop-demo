plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.carshop.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.carshop.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // 两个 buildType 覆盖 spec §5 验收 V4 / V5
    // mockDebug:USE_MOCK=true,走本地 MockWebServer(assets/mocks 加载 fixtures)
    // realDebug:USE_MOCK=false,直接打到 07 公网 carshop.hearagain.space
    // Q3 spec §1.5 USE_MOCK debug 默认 true / release 默认 false 跟 V4/V5 矛盾 ——
    // 用两个 debug buildType 才是唯一对得上的解。
    buildTypes {
        getByName("debug") {
            // 保留默认 debug 不动,避免 IDE 默认选项报错;真正用的是下面两个
            isMinifyEnabled = false
            buildConfigField("boolean", "USE_MOCK", "true")
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8000/api/v1/\"")
            // 桌面图标 label 区分(用户跑哪个 APK 一眼能看出来)
            manifestPlaceholders["appLabel"] = "商店 Debug"
        }
        create("mockDebug") {
            initWith(getByName("debug"))
            isDebuggable = true
            matchingFallbacks += listOf("debug")
            buildConfigField("boolean", "USE_MOCK", "true")
            buildConfigField("String", "API_BASE_URL", "\"http://placeholder-mock/api/v1/\"")
            applicationIdSuffix = ".mock"
            versionNameSuffix = "-mock"
            manifestPlaceholders["appLabel"] = "商店 Mock"
        }
        create("realDebug") {
            initWith(getByName("debug"))
            isDebuggable = true
            matchingFallbacks += listOf("debug")
            buildConfigField("boolean", "USE_MOCK", "false")
            buildConfigField("String", "API_BASE_URL", "\"https://carshop.hearagain.space/api/v1/\"")
            applicationIdSuffix = ".real"
            versionNameSuffix = "-real"
            manifestPlaceholders["appLabel"] = "商店 Real"
        }
        release {
            // 11 final · Demo 一次性,沿用 Android Studio 默认 debug keystore 出货
            // 日后真 release(上 Play / 真 OTA)要新建 release keystore + 记密码
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            buildConfigField("boolean", "USE_MOCK", "false")
            buildConfigField("String", "API_BASE_URL", "\"https://carshop.hearagain.space/api/v1/\"")
            manifestPlaceholders["appLabel"] = "车机商店"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.activity:activity-compose:1.9.0")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Retrofit + OkHttp + Moshi
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // MockWebServer —— 运行时启动本地 fixtures 服务器(用于 mockDebug buildType),
    // 不是测试库;放 implementation 不是 testImplementation。
    implementation("com.squareup.okhttp3:mockwebserver:4.12.0")

    // ViewModel + collectAsStateWithLifecycle(09 起 UI 层用)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")

    // Coil —— Compose AsyncImage,09 起加载 banner / 商品图 / 分类 icon
    implementation("io.coil-kt:coil-compose:2.7.0")

    // QR code —— 一行 Composable 出二维码(10 checkout PayDialog)
    // qrose 在 Maven Central(io.github.alexzhirkevich),Compose 原生支持,无需 Bitmap 转换
    implementation("io.github.alexzhirkevich:qrose:1.0.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
