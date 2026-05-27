# Session 08 · Android Foundation(车机端地基)

> 车机端项目骨架,**消费 00 design-system 的主题 + 07 修过的契约**。本 session 不实现业务页面,只搭框架 + API client + 导航 + Mock。

---

## 1. 你要做什么

### 1.1 项目骨架

> `carshop-android/` 已经在 00 session 创建过(只有 design-system 部分)。本 session 在此基础上扩展。

补充结构:

```
carshop-android/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml      (横屏锁、网络权限、Application 类)
│       └── java/com/carshop/android/
│           ├── CarshopApplication.kt
│           ├── MainActivity.kt        (替换 00 的 demo,改为 NavHost 入口)
│           ├── designsystem/         (00 已建)
│           ├── data/
│           │   ├── DeviceIdProvider.kt  (Settings.Secure.ANDROID_ID)
│           │   ├── ApiResult.kt         (Sealed class:Success<T> / ApiError(code, msg) / NetworkError)
│           │   ├── ApiClient.kt         (Retrofit + OkHttp 配置)
│           │   ├── interceptors/
│           │   │   └── DeviceIdInterceptor.kt
│           │   ├── api/                 (Retrofit interfaces,本 session 全部定义)
│           │   │   ├── CategoryApi.kt
│           │   │   ├── ProductApi.kt
│           │   │   ├── BannerApi.kt
│           │   │   └── OrderApi.kt
│           │   ├── dto/                 (响应 DTO)
│           │   │   ├── ApiResponse.kt   (envelope {code, data, message})
│           │   │   ├── PagedList.kt     (分页 envelope)
│           │   │   ├── Category.kt
│           │   │   ├── Product.kt
│           │   │   ├── Banner.kt
│           │   │   └── Order.kt
│           │   └── repository/          (空 stub,09/10 实现)
│           ├── nav/
│           │   ├── Routes.kt            (路由定义)
│           │   └── CarshopNavHost.kt    (NavHost + 占位页面)
│           └── ui/
│               └── shell/
│                   └── AppShell.kt      (整体壳:左 Rail + 主内容区,navi 选中态联动)
└── app/src/main/assets/mocks/         (本地 fixtures,见 1.5)
```

### 1.2 网络层(Retrofit + OkHttp)

- `ApiClient`:
  - `baseUrl`:从 BuildConfig 读(开发=本地、生产=Mac mini 公网),**默认指向 07 部署后的公网 URL**
  - `OkHttpClient`:加 `DeviceIdInterceptor`(自动注入 `X-Device-Id` header)+ HttpLoggingInterceptor(BODY,仅 Debug)
  - Moshi(或 Kotlinx Serialization)反序列化
- `ApiResponse<T>` envelope:
  ```kotlin
  data class ApiResponse<T>(val code: Int, val data: T?, val message: String)
  ```
- 所有接口方法返回 `Result<T>`(自定义 sealed class):
  - Success(T)
  - ApiError(code: Int, message: String)  ← code != 0
  - NetworkError(throwable)
- **数据类必须对照 `artifacts/fixtures/`**,不要凭空写字段

### 1.3 接口定义(全部覆盖,本 session 不实现 UI 用)

按 SPEC.md §6.1 把公开接口全部声明(8 个端点):
- `GET /api/v1/categories`
- `GET /api/v1/products?category_id&page&page_size`
- `GET /api/v1/products/{id}`
- `GET /api/v1/banners`
- `POST /api/v1/orders`
- `GET /api/v1/orders?status&page&page_size`
- `GET /api/v1/orders/{id}`
- `POST /api/v1/orders/{id}/mock_pay`

### 1.4 导航(AppShell)

按设计稿:左侧 Rail(240dp)+ 主内容区(1680dp)。

`AppShell` Composable:
- Rail 列出 3 个根入口:**首页 / 我的订单 / (占位 about)**
- 选中态用 3dp 海泡青强调条(00 已实现 `CarshopRail` 组件)
- 主内容区显示当前路由的 Composable

Routes 暂时定义但用空页面占位:
```kotlin
sealed class Routes(val path: String) {
    object Home : Routes("home")
    object CategoryProducts : Routes("category/{categoryId}")
    object ProductDetail : Routes("product/{productId}")
    object OrderConfirm : Routes("order/confirm/{productId}")
    object OrderDetail : Routes("order/detail/{orderId}")
    object OrderList : Routes("orders")
}
```

> 实际页面内容由 09 / 10 填,本 session 每个路由放一个"Coming Soon"占位即可。

### 1.5 Mock 模式(MockWebServer)

本 session **必须**实现 Mock 切换机制(在 09/10 没起来真后端时也能跑):

- 创建 `MockApiServer.kt`,用 OkHttp `MockWebServer`
- 启动一个本地 MockWebServer 加载 `app/src/main/assets/mocks/*.json`
- BuildConfig 加 flag `USE_MOCK`(`debug` flavor 默认 true,`release` 默认 false)
- USE_MOCK=true 时 baseUrl 指向 MockWebServer.url("/api/v1")

**Mock fixtures 必须从 `artifacts/fixtures/02~05` 复制到 `app/src/main/assets/mocks/`**,不许自己写。

### 1.6 应用清单 / 权限

`AndroidManifest.xml`:
- `<uses-permission android:name="android.permission.INTERNET"/>`
- Application:
  - `android:supportsRtl="true"`
  - `android:theme="@style/Theme.Carshop"`(00 session 已建)
- MainActivity:
  - `android:screenOrientation="landscape"`
  - `android:configChanges="orientation|screenSize"`
  - `android:exported="true"`

---

## 2. 你不要做什么

- ❌ 实现首页 / 商品列表 / 详情 / 订单页(09/10 的事)
- ❌ 真实下单 / 支付(09/10)
- ❌ 改 design-system(00 的事,有问题先改 00 artifact)
- ❌ 改 SPEC.md(除非发现真漂移)
- ❌ 自己写测试数据(用 fixtures)
- ❌ 实现搜索 / 购物车 / 优惠券(MVP 不做)
- ❌ 实现登录(无登录)
- ❌ 加权限请求 dialog(不需要)

---

## 3. 输入

- `CLAUDE.md`、`SPEC.md`(尤其 §6.1、§11.5 设备 ID、§13 URL 规范)
- `artifacts/00-design-system.md`(主题 + 组件 API)
- `artifacts/fixtures/02~05/`(**关键!**所有 fixture 文件)
- `artifacts/07-early-integration.md`(知道部署的公网 URL,作为生产 baseUrl)

---

## 4. 输出

- `carshop-android/` 扩展产物(见 1.1)
- `app/src/main/assets/mocks/*.json`(从 fixtures 拷过来)
- `artifacts/08-android-foundation.md`:
  - 项目结构图
  - 接口清单 + 对应 fixture 链接
  - AppShell 截图(只有空 Rail + 占位主内容)
  - Mock 模式 / 真模式如何切换
  - 已踩坑

---

## 5. 验收

1. ✅ `./gradlew assembleDebug` 编译通过,生成 APK
2. ✅ 装到模拟器或真机,启动后**横屏显示**,看到左侧 Rail + 主内容区"Coming Soon",**主题色正确**
3. ✅ Rail 三个 item 可点击切换(虽然内容都是 Coming Soon)
4. ✅ Mock 模式:能调通 `categoryApi.list()`,Logcat 看到 Retrofit 拿到 fixtures 数据
5. ✅ 真模式(指向 07 公网 URL):能调通,Logcat 看到真后端响应,设备 ID 自动注入(从 logcat 的 OkHttp log 验证)

> 验收 4 / 5 不用做 UI,在 MainActivity 的 LaunchedEffect 里调一次接口,打 log 即可。

---

## 6. 依赖

- **上游**:00 design-system、07 早集成(必须先完成,拿到公网 URL 和修过的契约)
- **下游**:09、10、11

## 7. Mock 策略

USE_MOCK BuildConfig 切换,fixtures 从 07 修过的版本复制。

## 8. 已知坑

1. **Settings.Secure.ANDROID_ID**:Android 8+ 在不同应用 / 不同用户下值不同,但同一应用稳定 → 我们用就够
2. **`X-Device-Id` 必须每个请求带**:用 Interceptor 全局注入,不要每个 API 调用手动加
3. **横屏锁定**:Manifest 写 `screenOrientation="landscape"`,但避免 `Activity` 重建 → 加 `configChanges`
4. **设计稿 1920×1080**:真机/模拟器实测分辨率可能不同,**用 dp 不用 px**,关键尺寸走 `Sizing` token(80dp/240dp/1680dp)
5. **Compose Navigation**:用 `androidx.navigation:navigation-compose`,不要用 fragment 那一套
6. **MockWebServer 启动**:在 Application.onCreate 启动,baseUrl 用 `mockServer.url("/api/v1/").toString()`
7. **assets/mocks 加载**:在 MockWebServer dispatcher 里读 assets,根据 request path 路由到对应 JSON
8. **网络明文 HTTP**:开发期连本地 / Mac mini 可能用 HTTP,Android 9+ 默认禁止明文 → `AndroidManifest` 配 `usesCleartextTraffic="true"`(仅 debug flavor)或者全程用 HTTPS
