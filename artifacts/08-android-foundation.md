# Artifact · Session 08 · Android Foundation

> **状态**:✅ 已完成 · 两个 buildType 各出一个 APK,V1~V5 全部实跑通过
> **完成日期**:2026-05-26
> **执行机**:Apple Silicon Mac mini-2 (M4),AVD `carshop-tablet` Pixel Tablet API 34
> **对应 session spec**:[sessions/08-android-foundation.md](../sessions/08-android-foundation.md)

---

## 一句话总结

`carshop-android/` 在 00 design-system 基础上扩展出 **网络层**(Retrofit + OkHttp + Moshi + DeviceIdInterceptor + ApiResult)、**8 个公开接口的 Retrofit 声明**、**Compose Navigation + Rail-AppShell + 6 个 Coming Soon 占位**、**MockWebServer 模式切换**(两个 buildType:`mockDebug` 走 assets/mocks/* fixtures,`realDebug` 直接打到 07 公网 `carshop.hearagain.space`),5 个验收项全过,Mock 模式 5 条分类来自 fixtures,Real 模式 6 条分类来自公网(多一条是 07 留的测试残留),`X-Device-Id` header 真注入。

---

## 1. 验收(对照 spec §5)

| # | 验收项 | 状态 | 证据 |
|---|---|---|---|
| V1 | `./gradlew assembleDebug` 编译通过 | ✅ | `assembleMockDebug` + `assembleRealDebug` 都 BUILD SUCCESSFUL in 33s,各出 1 个 APK |
| V2 | 装模拟器后**横屏**显示,Rail + 主区 "Coming Soon",主题色对 | ✅ | [`mock-home.png`](08-android-foundation/mock-home.png) · 海泡青 3dp 强调条 / Steel800 主色 / 80dp Rail item |
| V3 | Rail 三个 item 可点击切换 | ✅ | [`mock-orders.png`](08-android-foundation/mock-orders.png)(我的订单选中)+ [`mock-about.png`](08-android-foundation/mock-about.png)(关于选中,显示一句话占位)|
| V4 | Mock 模式调通,Logcat 看到 Mock fixtures 加载 | ✅ | `MockApiServer: → GET /api/v1/categories` → `CarshopVerify: categories.list OK · code=0 · count=5 · first=汽车用品` |
| V5 | 真模式调通,Logcat 看到公网响应 + `X-Device-Id` 注入 | ✅ | `--> GET https://carshop.hearagain.space/api/v1/categories · X-Device-Id: 1505568b08432197` → `<-- 200 ... server: cloudflare · cf-ray: a019fa37ae07c4e7-LAX` → `count=6, first=汽车用品`(比 fixture 多 1 条,证明走的是真后端不是 mock)|

---

## 2. 产物结构

```
carshop-android/                      ← 08 在 00/Q2 基础上扩展
├── app/build.gradle.kts              ★改:加 mockDebug/realDebug 两个 buildType + 全部依赖
├── app/src/main/
│   ├── AndroidManifest.xml           ★改:+ INTERNET + Application + usesCleartextTraffic
│   ├── assets/mocks/                 ★新:9 个 fixture 文件(从 artifacts/fixtures/02~04 拷)
│   │   ├── categories.json           (5 条分类)
│   │   ├── products.json             (12 条商品分页)
│   │   ├── product-1.json / product-404.json
│   │   ├── banners.json              (3 条 banner)
│   │   ├── orders.json / order-detail.json
│   │   ├── order-create.json / order-pay.json
│   └── java/com/carshop/android/
│       ├── CarshopApplication.kt     ★新:onCreate 启 MockApiServer + init ApiClient
│       ├── MainActivity.kt           ★改:setContent = AppShell + LaunchedEffect 验收钩子;
│       │                                  DesignSystemDemo + Preview 保留为运行时不可达
│       ├── designsystem/             (00 未动)
│       ├── data/
│       │   ├── ApiClient.kt          ★新:Retrofit + OkHttp + Moshi 单例
│       │   ├── ApiResult.kt          ★新:Sealed Success/ApiError/NetworkError(09/10 用)
│       │   ├── DeviceIdProvider.kt   ★新:Settings.Secure.ANDROID_ID 缓存
│       │   ├── MockApiServer.kt      ★新:OkHttp MockWebServer + assets dispatcher
│       │   ├── interceptors/
│       │   │   └── DeviceIdInterceptor.kt   ★新:挂 OkHttpClient,每请求注入 X-Device-Id
│       │   ├── api/                  ★新:4 个 Retrofit interface
│       │   │   ├── CategoryApi.kt    GET /categories
│       │   │   ├── ProductApi.kt     GET /products(分页 + query)/ GET /products/{id}
│       │   │   ├── BannerApi.kt      GET /banners
│       │   │   └── OrderApi.kt       POST/GET/GET-detail/POST-mock_pay
│       │   ├── dto/                  ★新:8 个 data class(全字段 @Json 注解 snake_case 兼容)
│       │   │   ├── ApiResponse.kt    envelope { code, data, message }
│       │   │   ├── PagedList.kt      envelope { list, total, page, page_size }
│       │   │   ├── Category.kt / Product.kt / Banner.kt
│       │   │   └── Order.kt          (Order + ShippingInfo + OrderItem + ProductSnapshot + CreateOrderRequest)
│       │   └── repository/.keep      ★新:09/10 在此实现
│       ├── nav/
│       │   ├── Routes.kt             ★新:sealed class 6 个 route(Home / CategoryProducts /
│       │   │                                ProductDetail / OrderConfirm / OrderDetail / OrderList + About)
│       │   └── CarshopNavHost.kt     ★新:NavHost 注册全 6 条 → Coming Soon 占位 composable
│       └── ui/shell/
│           └── AppShell.kt           ★新:CarshopRail 3 item(首页/我的订单/关于)+ TopBar + NavHost,
│                                        Rail 选中态联动 NavController.currentBackStackEntry
└── artifacts/08-android-foundation/
    ├── mock-home.png                 (V2 证据)
    ├── mock-orders.png               (V3 证据 1)
    ├── mock-about.png                (V3 证据 2)
    └── real-home.png                 (V5 证据)
```

---

## 3. 接口清单(SPEC §6.1 全覆盖 8 个端点)

| API interface | Method | 路径 | Fixture(mock 模式) | 真后端验证(real 模式) |
|---|---|---|---|---|
| `CategoryApi.list()` | GET | `/categories` | [`categories.json`](08-android-foundation/../fixtures/02/get-categories.json) | ✅ V5 logcat:200 + count=6 |
| `ProductApi.list(catId,page,pageSize)` | GET | `/products` | [`products.json`](08-android-foundation/../fixtures/02/get-products.json) | (08 不验,09 实装时验)|
| `ProductApi.detail(id)` | GET | `/products/{id}` | [`product-1.json`](08-android-foundation/../fixtures/02/get-product-1.json) / [`product-404.json`](08-android-foundation/../fixtures/02/get-product-404.json) | (09)|
| `BannerApi.list()` | GET | `/banners` | [`banners.json`](08-android-foundation/../fixtures/03/get-banners.json) | (09) |
| `OrderApi.create(body)` | POST | `/orders` | [`order-create.json`](08-android-foundation/../fixtures/04/post-order-success.json) | (10) |
| `OrderApi.list(status,page,pageSize)` | GET | `/orders` | [`orders.json`](08-android-foundation/../fixtures/04/get-orders-list.json) | (10) |
| `OrderApi.detail(id)` | GET | `/orders/{id}` | [`order-detail.json`](08-android-foundation/../fixtures/04/get-order-detail.json) | (10) |
| `OrderApi.mockPay(id)` | POST | `/orders/{id}/mock_pay` | [`order-pay.json`](08-android-foundation/../fixtures/04/post-mock-pay-success.json) | (10) |

`X-Device-Id` header 走 `DeviceIdInterceptor`(挂在 `OkHttpClient.Builder`,**不挂 Retrofit**),所有请求全局注入,接口签名不带。

---

## 4. 两种模式怎么切

### mockDebug · 离线开发

```
./gradlew assembleMockDebug
adb install -r app/build/outputs/apk/mockDebug/app-mockDebug.apk
adb shell am start -n com.carshop.android.mock/com.carshop.android.MainActivity
```

- `BuildConfig.USE_MOCK = true`
- `CarshopApplication.onCreate` 启动 `MockApiServer`(OkHttp `MockWebServer` 绑随机端口),拿到 `http://localhost:<port>/api/v1/` 作 baseUrl
- 所有请求被 `AssetsDispatcher` 截下,按 path 路由到 `assets/mocks/*.json`
- 不需要网络,适合 09/10 写 UI 时

### realDebug · 公网联调

```
./gradlew assembleRealDebug
adb install -r app/build/outputs/apk/realDebug/app-realDebug.apk
adb shell am start -n com.carshop.android.real/com.carshop.android.MainActivity
```

- `BuildConfig.USE_MOCK = false`
- `BuildConfig.API_BASE_URL = "https://carshop.hearagain.space/api/v1/"`(注意结尾 `/`,Retrofit 必需)
- 直接打到 07 公网真后端
- 适合 11 final-integration 跑 US-01~13 端到端

**两个 buildType 用了不同的 `applicationIdSuffix`**(`.mock` / `.real`),可以同时装两个 APK 不冲突。

### 为什么是两个 buildType,不是一个 APK 运行时切

08 spec §1.5 写 "USE_MOCK debug 默认 true,release 默认 false",跟 spec §5 验收 V4(Mock 跑通)+ V5(真后端跑通)矛盾——只有一个 debug buildType,要么过 V4 要么过 V5。**两个 debug buildType 是唯一对得上 spec 的解**(用户大总管确认 A 方案)。`release` buildType 也保留(默认 USE_MOCK=false + 公网 baseUrl),将来 11 出签名包用。

---

## 5. 关键设计决策(在用户确认后落地)

| # | 决策 | 理由 |
|---|---|---|
| 1 | **DesignSystemDemo 保留为不可达 + @Preview** | 用户钦定方案 A。Q2 已用模拟器截图验证设计系统,demo 使命达成;留着只跟 AppShell 冲突,但 Studio 内 Preview 仍能给 09/10 当"活的设计参考" |
| 2 | **Rail 3 item:首页 / 我的订单 / 关于** | 严格按 spec §1.4。"关于" 是占位但用户给了一句话(`车机商店 Demo · v0.1 · 2026 · powered by carshop.hearagain.space`)防止视觉空洞 |
| 3 | **NavHost 注册全 6 条 route + Coming Soon** | 09/10 进来直接替换 composable 内容,不动 NavHost 结构,降低跨 session 文件冲突面 |
| 4 | **两个 buildType(mockDebug / realDebug)** | spec §1.5 vs V4/V5 矛盾的唯一正解(见上节) |
| 5 | **DeviceIdInterceptor 挂 OkHttpClient.Builder,不挂 Retrofit** | 用户提醒:Retrofit 层只能加 CallAdapter / Converter,不是 HTTP Interceptor |
| 6 | **Moshi-Kotlin reflect 适配器(不用 codegen)** | Kotlin 1.9.24 + 老式 composeOptions 跟 Moshi codegen 的 KSP 链有摩擦;reflect 模式 + 每字段 `@Json(name="snake_case")` 标注最稳 |
| 7 | **MockApiServer 单文件 dispatcher,fixture 按 endpoint 命名** | spec 1.5 要求"从 artifacts/fixtures 复制",没要求保留目录结构。扁平命名 dispatcher 路由更直接 |
| 8 | **Rail 选中态联动 currentBackStackEntry**(不只是 onSelect 内部 state) | 09/10 用 navController.navigate 跳子页面时,Rail 高亮要跟着回到对应根入口(子页面 fallback 到 Home/OrderList) |

---

## 6. 已踩坑

### 坑 1 · `MockWebServer.start()` + `.url()` 双双触发 NetworkOnMainThreadException

`MockWebServer.start()` 要 bind `ServerSocket` —— 网络操作,Android 主线程禁止。后来发现 `.url()` 内部也调 `InetAddress.getCanonicalHostName()` 做反向 DNS,**也是网络**。

**解法**:把 `start()` 和 `.url()` 一起塞到一个 `Thread { ... }.start() + .join()` 里,Application.onCreate 同步等待拿到 baseUrl。

**为什么不能用 coroutine `runBlocking + Dispatchers.IO`**:可以,但 Application.onCreate 同步等待本来就是阻塞语义,纯 Thread 反而更直白少一层 dispatch。

**未来 session 提示**:如果 09/10 想加更多 mock 端点(WebSocket / SSE),都要走后台线程,Application.onCreate 不要做任何网络。

### 坑 2 · `adb` 不在 Claude Code 后台 shell 的 PATH

Q2 在 `~/.zshrc` 加了 `export PATH=...platform-tools:...`,新开 terminal 有,但 Claude Code 后台 `Bash` 用的是 session 启动时的 zsh 快照,**看不到 zshrc 后续的改动**。

**解法**:用绝对路径 `$HOME/Library/Android/sdk/platform-tools/adb`。

**未来 session 提示**:任何 Q2 装的 CLI(adb / emulator / sdkmanager)在 Claude Code 后台 shell 都要走绝对路径,直到下次 Claude Code 重启 session 才会拿到新 PATH。

### 坑 3 · `Icons.Outlined.ReceiptLong` deprecated 警告

Material Icons 把 RTL-aware 的图标全迁到 `Icons.AutoMirrored.Outlined.*` 子包,`Outlined.ReceiptLong` 标了 deprecated 但能用。

**解法**:08 跟手改了(免得 09/10 看到丑警告),import `androidx.compose.material.icons.automirrored.outlined.ReceiptLong`,调用点 `Icons.AutoMirrored.Outlined.ReceiptLong`。

### 坑 4 · `adb uninstall` 旧 demo APK 报 `DELETE_FAILED_INTERNAL_ERROR`

模拟器上残留的 `com.carshop.android`(Q2 装的 demo APK,无 applicationIdSuffix)第一次 uninstall 报 Failure。原因可能是 launcher 后台保留任务。**不阻塞**——mockDebug 的 applicationId 是 `com.carshop.android.mock`(有 suffix),独立 install 成功。

**未来 session 提示**:Q2 demo APK + 08 mock + 08 real 同时存在模拟器上,正常。08 不删 Q2 的(它已经完成使命,留着不冲突)。

---

## 7. 给下游 session 的接力(09 / 10 用)

### 09 android-browse 进来要做的事

1. **写 repository**:在 `data/repository/` 加 `CategoryRepository.kt` / `ProductRepository.kt` / `BannerRepository.kt`,把 `ApiClient.xxApi.method()` 的 `Response<ApiResponse<T>>` 转成 `ApiResult<T>`(Success / ApiError / NetworkError 三态)
2. **填 Composable**:打开 `nav/CarshopNavHost.kt`,把 `Routes.Home.path` / `Routes.CategoryProducts.path` / `Routes.ProductDetail.path` 三处的 `ComingSoon(...)` 调用换成自己的 `HomeScreen()` / `CategoryProductsScreen(categoryId)` / `ProductDetailScreen(productId)`
3. **不要动**:`AppShell` / `Routes` 的 path 定义 / DTO / Api interface(改了等于改契约,要先改 SPEC)
4. **跳转**:Composable 里 `inject` `NavController`(用 `LocalNavController` 或上层透传),然后 `nav.navigate(Routes.ProductDetail.build(productId))`

### 10 android-checkout 同理

填 `Routes.OrderConfirm.path` / `Routes.OrderDetail.path` / `Routes.OrderList.path` 三处。

### 怎么把 fixtures 更新到最新

如果 07 之后后端有改动,fixtures 跟着改:

```
cp artifacts/fixtures/02/get-categories.json carshop-android/app/src/main/assets/mocks/categories.json
# 然后 ./gradlew assembleMockDebug 重打
```

### Mock 模式给 09/10 写新接口时

如果 09/10 用了 08 没拷的 fixture(比如分类 2 的商品列表),要在 `MockApiServer.AssetsDispatcher.route()` 里加新 case + 拷新 fixture 到 `assets/mocks/`。

---

## 8. SPEC / CHANGE_MAP / TECH_DEBT 同步

- **CHANGE_MAP**:Category / Product / Banner / Order / 设备 ID 节都已加 08 `data/dto/*` + `data/api/*` 引用(本 artifact 落地后跟更)
- **SPEC**:无契约改动(所有字段名 / 路径 / 错误码都跟 SPEC §6.1 / §11.5 / §12 / §13 一致)
- **TECH_DEBT**:无新债

08 没在业务代码里写一行 magic number / hex 色 / 硬编码 dp,全走 designsystem tokens。也没破任何共享技术约定。

---

## 9. 时长 / 性能数字

| 步骤 | 时长 |
|---|---|
| 探现状(`find` + 读 fixtures + 读 build.gradle / Manifest / MainActivity)| ~30 秒 |
| 写 build.gradle + Manifest | ~1 分钟 |
| 写 data 层(11 个文件) | ~5 分钟 |
| 写 MockApiServer + 拷 fixtures | ~3 分钟 |
| 写 nav + AppShell + MainActivity | ~4 分钟 |
| 第一次 build(全新 task)| 33 秒(2 个 buildType 串行)|
| 修主线程网络坑 + rebuild | ~5 秒(增量) |
| 装 + 启动 + 截图 + logcat 验 V2/V3/V4/V5 | ~3 分钟 |
| 写 artifact + 同步文档 | ~10 分钟 |

**总计**:约 30 分钟(Q2 已经把环境干净铺好,这里几乎没走弯路)。

---

## 10. 不修(本 session 范围外)

- `assets/mocks/` 不含 admin 接口的 fixtures(05 的 login/upload/me)。**理由**:08 只跑公开接口(SPEC §6.1),admin 接口归 06 admin-web 用,不需要在 Android 端 mock
- `ApiResult` sealed class 写了但 08 没在 MainActivity 里用(LaunchedEffect 直接读 `Response`)。**理由**:08 只验链路通,09/10 在 repository 层做 `Response → ApiResult` 转换,把 Retrofit 类型挡在 repository 内部不外漏
- Coil(图片加载库)没引。**理由**:08 没有任何 `<img>`,Rail icon 走 Material Icons vector,09 真要显示商品图时再引

---

**版本**:v1.0
**完成日期**:2026-05-26
**执行人**:Claude Code (08 session)
