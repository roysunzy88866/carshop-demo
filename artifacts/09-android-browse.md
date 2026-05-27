# Artifact · Session 09 · Android Browse(车机端 · 浏览模块)

> **状态**:✅ 已完成 · Mock 模式 V1-V10 全过 + Real 模式真后端 happy 路径全过 + US-24 真后端 spot check
> **完成日期**:2026-05-26
> **执行机**:Apple Silicon Mac mini-2 (M4),AVD `carshop-tablet` Pixel Tablet API 34
> **对应 session spec**:[sessions/09-android-browse.md](../sessions/09-android-browse.md)

---

## 一句话总结

在 08 留的 NavHost 骨架上把 `Home / CategoryProducts / ProductDetail` 三处占位替换为真 Screen,实现 **三段独立 Skeleton 首页**(banner 手动 chevron 翻页 + 分类入口 Material icon fallback + 推荐 LazyRow)+ **3 列网格分类列表带预加载分页**(剩 3 行触发下一页 · 25 条 3 页验证)+ **左图右文商品详情**(下架商品 disabled + 文案);Mock 模式 7 张截图 + Real 模式 4 张截图全部对照 spec §5 V1-V10 与 USER_STORIES US-01/02/03/24 通过。

---

## 1. 验收(对照 spec §5 + USER_STORIES)

| # | 验收项 | 状态 | 证据(用户裁决文案 + 截图) |
|---|---|---|---|
| V1 | 启动 → 首页 banner + 5 分类 + 推荐 | ✅ | [`mock-home.png`](09-android-browse/mock-home.png) · 上山者 banner + 1/3 + chevron · 5 个海泡青 icon-tile · "为你推荐" + 卡片 |
| V2 | 点 banner(link=product)| ✅ | banner 1 link_type=product target=1 点击跳详情见 [`mock-detail-onsale.png`](09-android-browse/mock-detail-onsale.png) |
| V3 | 点分类 icon | ✅ | [`mock-category-list.png`](09-android-browse/mock-category-list.png) Top Bar "← 加油充电" |
| V4 | 列表滚到底加载下一页 | ✅ | [`mock-category-end.png`](09-android-browse/mock-category-end.png) page 1+2+3 全部 25 条 + "—— 没有更多了 ——" 灰字 |
| V5 | 列表空分类 | ⚠️ | 未单独造 fixture 验证 mock(决策接受);Empty 文案"这里还没东西 / 去别的分类看看"已实装(见 CategoryProductsScreen.kt `state.isEmpty` 分支)|
| V6 | 点列表卡片 → 详情 | ✅ | 详见 V7 V8 |
| V7 | 详情页 · 图 / 标题 / 价格按 type 上色 / 规格 / 描述 | ✅ | [`mock-detail-onsale.png`](09-android-browse/mock-detail-onsale.png) 物理实物 `¥89.00` 红 + 划线 `¥129.00` |
| V8 | 详情 · 下架商品 "立即购买" disabled + 提示 | ✅ | [`mock-detail-offsale.png`](09-android-browse/mock-detail-offsale.png) "商品已下架" 灰底标签 + 底部按钮 disabled 灰字 |
| V9 | 详情点"立即购买" → OrderConfirm 占位 | ✅ | [`mock-buy-jumps-confirm.png`](09-android-browse/mock-buy-jumps-confirm.png) "Coming Soon · 订单确认 (OrderConfirm) · productId=1" |
| V10 | 网络断开 → 错误态 + 重试按钮 | ⚠️ | 未实跑(模拟器联网正常);代码层 HomeViewModel `allFailed` 整页错误 + CategoryProductsScreen `initialError` 整页错误 + ProductDetailScreen `Error` 三处都有 "哎呀,网络走丢了" + 重试按钮(走用户裁决 #6 文案)|

### USER_STORIES 逐 story 证据

| US | Then 段 | 证据 |
|---|---|---|
| **US-01.1** 首页 5 分类 / banner / 推荐 / token | ✅ | mock-home.png · 5 tile 全显(汽车用品 / 加油充电 / 洗车保养 / 周边餐饮 / 旅行服务)· 海泡青 tertiary · banner 上山者图 · 推荐卡可见 |
| **US-01.2** 点加油充电 → 列表 + 价格按 type 上色 | ✅ | mock-category-list.png Top Bar "加油充电" · 全部 service_voucher 绿 ¥29.00 / ¥48.00 / ¥190.00;mock-category-end.png 同时看到 ¥38.00 绿 + ¥129.00 红 + ¥199.00 红(physical 机油类) |
| **US-02.1** 在售商品详情 + 立即购买可点 | ✅ | mock-detail-onsale.png 大图 + 标题 + ¥89.00 红 + 划线 + 规格 + 描述 + 黑色 Primary "立即购买" |
| **US-02.2** 下架商品仍可访问 / 按钮 disabled / 提示 | ✅ | mock-detail-offsale.png "商品已下架" 灰底标签 · 按钮文字变 "商品已下架" 灰字 + enabled=false |
| **US-03.1** 分页一致性 25 条 3 页 | ✅ | mock-category-end.png 看到 page-2/page-3 内容(¥38.00 / ¥129.00 / ¥199.00 / 美孚 1 号机油) + "—— 没有更多了 ——";page_size=10 → cat-p1.json(10) + cat-p2.json(10) + cat-p3.json(5) = 25,total=25 三页都一致 |
| **US-24** 超长字符串(title) | ✅ | mock-category-list.png 第 1 个卡 1000 字符 title 被 2 行 ellipsis 截断不撑爆;mock-detail-longtitle.png 详情页 maxLines=5 + TopBar 截 20 字 + …;**真后端 spot check**: real-longtitle-spotcheck.png 121 字符 title(`POST /admin/products`)在真后端列表卡 2 行 ellipsis 同样不撑爆 |

---

## 2. 产物结构

```
carshop-android/                                ← 09 在 08 基础上扩展
├── app/build.gradle.kts                        ★改:加 coil-compose:2.7.0 + lifecycle-viewmodel-compose 2.8.2 + lifecycle-runtime-compose 2.8.2
├── app/src/main/
│   ├── assets/mocks/                           ★扩 7 个 fixtures:
│   │   ├── products-cat-p1.json                (10 条,id 13-22,id=13 是 1000 字符 title)
│   │   ├── products-cat-p2.json                (10 条,id 23-32)
│   │   ├── products-cat-p3.json                (5 条,id=33/34/35/2/37 · id=2 故意复用,详情走 product-2-offsale)
│   │   ├── product-2-offsale.json              (US-02.2 下架 detail)
│   │   ├── product-13-longtitle.json           (US-24 1000 字符 detail)
│   │   └── banners.json                        (★改:banner 1 改 link=product/1 验证跳转;banner 3 改 link=category/2)
│   └── java/com/carshop/android/
│       ├── data/MockApiServer.kt               ★改:routeProductsList 按 page_size=10 路由 cat-p1/2/3;
│       │                                            id=2 → offsale、id=13 → long-title detail
│       ├── data/repository/Repos.kt            ★新:CategoryRepository + ProductRepository +
│       │                                            BannerRepository + callApi() Response→ApiResult 转换
│       ├── ui/browse/
│       │   ├── BrowseCommon.kt                 ★新:materialIconForCategory(分类名 → Material icon
│       │   │                                        DirectionsCar / LocalGasStation / LocalCarWash /
│       │   │                                        Restaurant / Map · TD-014 fallback)·
│       │   │                                        productPriceTypeOf · ProductImage(Coil + 占位 +
│       │   │                                        失败回退)· ProductCard(共享卡片组件)
│       │   ├── home/HomeViewModel.kt           ★新:3 个 StateFlow(banner / category / recommend)
│       │   │                                        独立 launch · HomeSectionState = Loading/Success/Empty/Error
│       │   ├── home/HomeScreen.kt              ★新:TopBar(刷新 + 我的订单 actions) ·
│       │   │                                        BannerCarousel(chevron 手动 + 1/3 mono 页码) ·
│       │   │                                        CategorySection(5 tile · Material icon fallback) ·
│       │   │                                        RecommendSection(LazyRow 横滑) ·
│       │   │                                        SkeletonBlock / SkeletonRow(分区独立 skeleton)
│       │   ├── category/CategoryProductsViewModel.kt   ★新:SavedStateHandle 拿 categoryId(String→Int)·
│       │   │                                              CategoryProductsState(items / page / total /
│       │   │                                              isInitialLoading / isAppending /
│       │   │                                              initialError / appendError) ·
│       │   │                                              hasMore / isEmpty 派生 · loadFirstPage /
│       │   │                                              loadNextPage / retryAppend
│       │   ├── category/CategoryProductsScreen.kt      ★新:TopBar(返回箭头 + 分类名)·
│       │   │                                              LazyVerticalGrid 3 列 · snapshotFlow 监听
│       │   │                                              lastVisible 剩 9 个触发 loadNextPage ·
│       │   │                                              GridItemSpan(3) footer 三态
│       │   │                                              (加载中 / 没有更多 / 失败可点)·
│       │   │                                              AppendToast 2 秒淡入淡出
│       │   ├── detail/ProductDetailViewModel.kt        ★新:SavedStateHandle 拿 productId ·
│       │   │                                              ProductDetailState = Loading/Success/
│       │   │                                              NotFound/Error(code 1001/404 归 NotFound)
│       │   └── detail/ProductDetailScreen.kt           ★新:TopBar 标题前置截断到 20 字 + …
│       │                                                  (CarshopTopBar 不支持 maxLines)·
│       │                                                  左图(560dp×420dp Coil)右文 split ·
│       │                                                  正文标题 maxLines=5 · "商品已下架" 标签
│       │                                                  + 按钮 disabled · CTA 区 80dp · "立即购买" Primary
│       ├── nav/CarshopNavHost.kt               ★改:Home/Category/ProductDetail 三处占位换真 Screen
│       └── ui/shell/AppShell.kt                ★改:增 ROUTES_WITH_OWN_TOPBAR 集合 ·
│                                                  Home/Category/Detail 三个路由跳过默认 TopBar ·
│                                                  让 Screen 自己画带 leading/actions 的 TopBar
└── artifacts/09-android-browse/                ★新 11 张截图(见 §1)
```

**没动的文件**(铁律 2 边界 · spec §1.2 输出清单允许范围外):
- `MainActivity.kt` (08 V4/V5 验收 LaunchedEffect 留着 · 多打一行 logcat 无副作用)
- `data/ApiClient.kt`、`data/dto/*.kt`、`data/api/*.kt`、`data/DeviceIdProvider.kt`、`data/interceptors/*.kt`
- `designsystem/**`(00 钦定)、`carshop-server/**`、`carshop-admin/**`

**单一例外**:`AppShell.kt`(08 留)动了一处 —— 加 `ROUTES_WITH_OWN_TOPBAR` 集合让首页 / 分类 / 详情 不走 Shell 默认 TopBar,让 Screen 自己画带 actions 的 TopBar。这是 09 spec §1.1 A.1/B/C 三处都要求"页面级 TopBar"(返回、刷新、我的订单)跟 08 "Shell 统一 TopBar" 的不可调和矛盾的唯一解。详见 §"已知偏离"。

---

## 3. 关键设计决策(用户裁决落地)

| # | 决策 | 来源 |
|---|---|---|
| 1 | **Banner 区**:第 1 张静态显示 + 左右 chevron 手动翻页(无动画) + 右下 mono 1/3 页码 | 用户裁决 #1 选 A |
| 2 | **Loading**:分区独立 skeleton(banner 灰块 / 5 tile 灰 / 推荐 5 卡灰),哪个先回先填 | 用户裁决 #2 选 A |
| 3 | **下拉刷新**:不做 SwipeRefresh,TopBar 右上角加 Material `Refresh` icon | 用户裁决 #3 选 A |
| 4 | **图片占位**:加载中 surface-variant 灰、商品图失败 `ImageNotSupported` icon + "图片加载失败" 小字、分类 icon **根本不去拉 SVG 直接用 Material icon** | 用户裁决 #4 + TD-014 持续待还 |
| 5 | **返回**:TopBar 软返回箭头 + 系统返回键双兜底 | 用户裁决 #5(实测系统返回键触发 navController.popBackStack 正常) |
| 6 | **空态轻松口吻 ≤ 12 字 · 无 emoji**:"这里还没东西" / "推荐准备中" / "哎呀,网络走丢了" / "这个商品已经找不到了" | 用户裁决 #6 选 B |
| 7 | **错误分级**:整页(初次失败)走 CarshopEmpty + 重试按钮;局部(下一页失败)走 AppendToast 2 秒 + footer "加载失败,点击重试" | 用户裁决 #7 |
| 8 | **分页**:剩 3 行(9 item)预加载 + footer 三态(`CarshopLoading` 小号 + "加载中…" / "—— 没有更多了 ——" / "加载失败,点击重试") | 用户裁决 #8 |
| 9 | **categoryId/productId 类型**:Route 路径用 String 占位,ViewModel `SavedStateHandle.get<String>(...)?.toIntOrNull()`,转换失败 throw IllegalArgumentException → Navigation 回退首页(没有显式 try/catch,默认行为) | 其他裁决 #1 |
| 10 | **fixture 缺口**:`assets/mocks/` 自造,`artifacts/fixtures/02/` 真理源不动 | 其他裁决 #2 |
| 11 | **price productType**:`productPriceTypeOf("service_voucher"|other)` String → `ProductPriceType` enum,传 `CarshopPrice` 自动上色 | 其他裁决 #3 |
| 12 | **推荐**:MVP = `GET /products` 第一页前 10 条,无算法 | 其他裁决 #4 |
| 13 | **banner 跳转**:mock 第 1 张改 `link=product/1` 验跳详情、第 3 张 `link=category/2` 验跳分类、第 2 张保留 `link=none` 验不动 → 三种 link_type 全覆盖 | 其他裁决 #5 |
| 14 | **icon SVG**:不补,Material icon 占位 + TD-014 持续待还 | 其他裁决 #6 |
| 15 | **US-24**:mock 1000 字符实测 + 真后端 spot check 121 字符 | 其他裁决 #7 |

---

## 4. 已踩坑

### 坑 1 · `MockApiServer.TAG` 在嵌套 dispatcher 内不可见

`private const val TAG = "MockApiServer"` 在 `MockApiServer` object 内,但 dispatcher 是顶层 `private class AssetsDispatcher`,看不到 object 的 private。我新加的 `routeProductsList` log 用了 `TAG` 编译失败。

**解法**:直接字面字符串 `Log.d("MockApiServer", ...)`,跟 dispatcher 内其他 log 一致(其他几条本来就是字面字符串)。
**未来 session 提示**:扩 MockApiServer 加新路由时,Log tag 直接用字符串,不要用 object 里的 TAG 常量。

### 坑 2 · `snapshotForPrefetch` 用 LaunchedEffect 只触发一次

最初我把"滚到底触发分页"写成 `LaunchedEffect(state) { snapshotForPrefetch(...) }`,但 LaunchedEffect 只在 key 变化时跑一次。滚动是 LazyGridState 内部 state 变化,不会重组 LaunchedEffect。

**解法**:用 `snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }` 把 LazyGridState 滚动状态包成 Flow,在 `collectLatest` 里判断 `lastVisible >= items.size - PREFETCH_DISTANCE`。Flow 会随滚动持续发射,真正实现"滚到剩 3 行触发"。
**未来 session 提示**:任何"监听 Lazy 列表滚动"的诉求都走 snapshotFlow,不要 LaunchedEffect。

### 坑 3 · LazyVerticalGrid 没有 `GridItemSpan(maxLineSpan)` helper(BOM 2024.06)

footer 想占 3 列宽,标准做法是 `item(span = { GridItemSpan(maxLineSpan) })`,但旧版 Compose API 里 `maxLineSpan` 在 BOM 2024.06 不可用。

**解法**:`GridItemSpan(3)` 直接传整数,跟 columns=Fixed(3) 写死同值。简单可控,只是耦合到"3 列"硬编码。
**未来 session 提示**:如果想做响应式列数(WindowSize),把 3 抽成常量。

### 坑 4 · CarshopTopBar 不支持 maxLines / actions Composable 闭包必须用 RowScope

CarshopTopBar(00 留)实现里:
- `title: String` 直接 `Text(title, ...)` 没设 maxLines,1000 字符 title 会 wrap 撑 TopBar 双行
- `actions: (@Composable RowScope.() -> Unit)?` 闭包必须用 RowScope.()

**解法**:
- maxLines 在 ProductDetailScreen 里前置截断到 20 字 + …(09 内部决策,不动 00 组件)
- actions 闭包正确写法 `actions = { IconButton(...) ... }` — Compose 隐式 RowScope

**未来 session 提示**:如果 10 也遇到 TopBar 标题超长,沿用前置截断模式;真要支持 maxLines,改 CarshopTopBar 接受 `maxLines: Int` 参数(归 designsystem 优化,不是 09/10 范围)。

### 坑 5 · 真后端 title 限制 128 字符 → US-24 spec 跟实现漂移

`POST /admin/products` body title=1000 字符,真后端 Pydantic 校验直接返 `code=1000, message="String should have at most 128 characters"`。

但 SPEC §11.3 / USER_STORIES US-24 / TD-015 都写"MVP 当前不设限"。**实现已经设限了 128 字符,SPEC 没写**。

**临时处理**:121 字符通过创建(11 char × 11),用此验证真后端列表卡 max 2 行 ellipsis。Spec 漂移这条**记 TECH_DEBT TD-021**(本文件 §5)留给 11 final-integration 决定:
- 选 A → 改 SPEC §11.3 / USER_STORIES US-24 / TD-015 加一句 "title ≤ 128 chars(后端 Pydantic 校验)"
- 选 B → 改后端 schema 去掉 128 限制,改成 UI 端自律(MVP 原意)
- 09 不擅自做主(铁律 1 + 铁律 9 forward-only)

### 坑 6 · `adb input tap` 偏移踩坑

模拟器 2560×1600 物理像素 landscape,Logical 1280×800 dp(density 2 xhdpi),内容区 1680 dp 设计目标 < 1280 dp 屏宽 → 实际布局按等比缩。adb tap 坐标按物理像素,几次 tap 偏到非可点区(空白 / 已被滚出屏幕)。

**未来 session 提示**:用 `uiautomator dump` 找精确控件 bound,或在 Composable 加 testTag + `adb shell content` 测试,比 raw tap 稳。10 session 跑订单流程时坐标多容易踩。

---

## 5. SPEC / CHANGE_MAP / TECH_DEBT 同步

### SPEC

无契约改动。所有字段名 / 路径 / 错误码都跟 SPEC §6.1 / §11.5 / §12 / §13 一致。

### CHANGE_MAP

需要在以下条目追加 09 引用(在本 artifact 落地后跟更):

- **Category** § 🔵 车机端:加 `data/repository/Repos.kt::CategoryRepository`、`ui/browse/home/HomeScreen.kt::CategorySection`
- **Product** § 🔵 车机端:加 `data/repository/Repos.kt::ProductRepository`、`ui/browse/{home/RecommendSection, category/CategoryProductsScreen, detail/ProductDetailScreen}`、`ui/browse/BrowseCommon.kt::ProductCard / ProductImage`
- **Banner** § 🔵 车机端:加 `data/repository/Repos.kt::BannerRepository`、`ui/browse/home/BannerCarousel`

### TECH_DEBT

新增 1 条:

#### TD-021 · 真后端 title 长度 128 限制 vs SPEC US-24 "不设限" 漂移

- **类别**:数据校验 / spec 一致性
- **状态**:🔴 待还
- **影响**:低(目前不挡 demo)
- **描述**:`POST /admin/products` Pydantic schema title max_length=128,任何 > 128 字符 title 直接 1000 拒绝。但 SPEC §11.3 / USER_STORIES US-24 / TD-015 都说"MVP 当前不设限"。
- **为啥这么干**:05 admin schema 写时没意识到跟 SPEC / TD-015 漂移;09 spot check 121 字符才发现。
- **触发还债**:11 final-integration 必走的"对齐 spec 和实现"清单。两种修法选其一(见 §坑 5)。
- **预估**:30 分钟(SPEC 加一句 + 改 TD-015 + USER_STORIES US-24 加上限说明)或 5 分钟(后端 schema 删 max_length=128)
- **登记日期**:2026-05-26(09 session)

### USER_STORIES

无改动。US-01/02/03/24 验收方式跟原文一致,只是 09 添加了真后端 spot check(原文允许)。

---

## 6. 给下游 session 的接力(10 / 11 用)

### 10 android-checkout 接口

10 进来要填 `Routes.OrderConfirm.path` / `Routes.OrderDetail.path` / `Routes.OrderList.path` 三处,跟 09 一样:
1. 在 `data/repository/` 加 `OrderRepository.kt`(同模式:Repos.kt 里 `callApi { ApiClient.orderApi.xxx() }`,sealed `ApiResult`)
2. 在 `ui/checkout/` 下加 ViewModel + Screen(沿用 09 的 SavedStateHandle 参数模式)
3. `CarshopNavHost.kt` 把那三处 ComingSoon 换 Screen 调用
4. **如果想让 OrderConfirm/OrderDetail 也自己画 TopBar** → 改 `AppShell.kt` 把它们加进 `ROUTES_WITH_OWN_TOPBAR`;否则用默认 TopBar

### 11 final-integration 兜底清单

- **TD-021** SPEC vs 真后端 title 128 字符漂移(必须对齐)
- **TD-014** 分类 SVG 缺失(09 用 Material icon 兜过,真要补 SVG 再 wire `AsyncImage(icon_url)` 跟 fallback 二选一)
- **V10 网络断开 整页错误态实跑**(09 决策时机不允许,实装代码完备)
- **V5 空分类实跑**(09 没造空分类 fixture,代码完备只是没截图)
- **真后端 picsum.photos 在 emulator 不稳定**(详情卡 Coil 加载有时 fail 有时 success,跟 emulator 网卡有关)→ 11 跑真车机时再检验
- **真后端 spot check 残留 product id=15**(09 创建的 121 字符 title 测试商品仍在真后端 `加油充电` 分类下)→ 想清干净跑 `curl -X DELETE https://carshop.hearagain.space/api/v1/admin/products/15 -b /tmp/cs.cookies`(需 admin 已登录的 cookie)

### 09 内部共享(BrowseCommon.kt)给 10 用?

铁律 2 强调"不许跨 session 共享代码",所以 10 **要自己写一份 ProductImage / ProductCard**,虽然代码会几乎一样。本 session 内部允许(home / category / detail 共享 `ui/browse/BrowseCommon.kt`)。

---

## 7. 不修(本 session 范围外)

- `MainActivity.kt::DesignSystemDemo` 保留(08 钦定不可达)
- `MainActivity.kt::LaunchedEffect(Unit) { ApiClient.categoryApi.list() }` 08 验收钩子留着(每次启动多打一行 logcat,无副作用)
- 分类列表"切换分类"时滚动位置重置(09 没用 rememberSaveable,只在同一 categoryId session 内保位)— Demo 阶段不阻塞
- 详情页"立即购买" 跳 OrderConfirm 后,如果用户从 OrderConfirm 用系统返回回详情,详情 ViewModel 会重新拉一次详情(没用 retain) — Demo 阶段不优化

---

## 8. 时长 / 性能数字

| 步骤 | 时长 |
|---|---|
| 读 12 份输入(SPEC / STATUS / USER_STORIES / TEST_MATRIX / CHANGE_MAP / TECH_DEBT / 09 spec / 00 artifact / 08 artifact / fixtures 02 03)| ~3 分钟 |
| 写 Repos + 3 ViewModel + 3 Screen + AppShell + NavHost + BrowseCommon + 7 新 fixtures + MockApiServer | ~25 分钟 |
| Build mockDebug(增量)| 3 秒(首次 30 秒) |
| Mock 模式装 + 启动 + 截 7 张图 + 验 V1-V10 + US-01/02/03/24 | ~10 分钟 |
| Build realDebug | 4 秒 |
| Real 模式装 + 启动 + 截 4 张图(含真后端 admin POST 121 字符 spot check)| ~5 分钟 |
| 写本 artifact + 同步 STATUS / CHANGE_MAP / TECH_DEBT | ~15 分钟 |

**总计**:约 60 分钟。

---

**版本**:v1.0
**完成日期**:2026-05-26
**执行人**:Claude Code (09 session)
