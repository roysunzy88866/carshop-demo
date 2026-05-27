# Change Map · 变更地图

> **目的**:任何时候想改一个东西,在这里查一次"它定义在哪、被谁引用",就**只读那几个文件**,不用全 grep。
>
> **维护规则**:任何 session 改了新增 / 改名 / 增字段,**必须**同步更新本文件相关条目。漏一条 = 留坑。
>
> 格式约定:
> - 🟢 **定义**:这个东西的"真理之源",改这里其他人才跟
> - 🔵 **引用**:消费方,改定义后必须扫一遍这些
> - 🟡 **测试**:覆盖它的 User Story
> - 🔴 **债务**:跟它相关的技术债

---

## 一、数据实体(改 schema 看这里)

### Category(分类)

- 🟢 **定义**:`SPEC.md §5` + `carshop-server/app/models/category.py`
- 🔵 **后端**:
  - `app/schemas/category.py`(Pydantic)
  - `app/routers/category.py`(路由)
  - `app/seeds/initial.py`(seed 5 个分类名 · 07 改:icon_url 走相对路径让 absolutize_url 跟 BASE_URL)
- 🔵 **后台 Web**:
  - `carshop-admin/src/api/category.ts`(类型 + 调用)
  - `carshop-admin/src/pages/CategoryList.tsx`(UI)
- 🔵 **车机端**(08 起):`data/dto/Category.kt`、`data/repository/Repos.kt::CategoryRepository`(09 落地)、`ui/browse/home/HomeScreen.kt::CategorySection`(09)、`ui/browse/BrowseCommon.kt::materialIconForCategory`(09 fallback)、`ui/browse/category/CategoryProductsViewModel.kt`(09 TopBar 标题来源)
- 🔵 **Fixtures**:`artifacts/fixtures/02/get-categories.json`(07 重导:icon_url → `https://carshop.hearagain.space/...`)+ `post-category-success.json` + `delete-category-conflict.json`
- 🟡 **User Stories**:US-01(首页 5 分类显示)、US-10(删分类约束)
- 🔴 **债务**:TD-014(SVG 文件缺失,渲染 404)

### Product(商品)

- 🟢 **定义**:`SPEC.md §5` + `carshop-server/app/models/product.py`(注意 `product_type` enum + `price` 是分)
- 🔵 **后端**:
  - `app/schemas/product.py`
  - `app/routers/product.py`(公开 list/detail + 后台 CRUD + on_sale 切换)
  - `app/seeds/initial.py`(seed 12 个商品)
- 🔵 **后台 Web**:
  - `carshop-admin/src/api/product.ts`
  - `carshop-admin/src/pages/ProductList.tsx`、`ProductEdit.tsx`
  - `carshop-admin/src/components/PriceInput.tsx`、`PriceDisplay.tsx`(分↔元转换)
- 🔵 **设计系统**:`carshop-android/.../components/CarshopPrice.kt`(按 `ProductPriceType` 上色)
- 🔵 **车机端**(09 落地):`data/repository/Repos.kt::ProductRepository`、`ui/browse/home/HomeScreen.kt::RecommendSection`(横滑 LazyRow)、`ui/browse/category/CategoryProductsScreen.kt`(LazyVerticalGrid 3 列 + 预加载分页 + footer 三态)、`ui/browse/detail/ProductDetailScreen.kt`(左图右文 + CTA + 下架态)、`ui/browse/BrowseCommon.kt::{ProductCard, ProductImage, productPriceTypeOf}`
- 🔵 **Fixtures**:`artifacts/fixtures/02/get-products*.json`、`get-product-1.json` 等
- 🟡 **User Stories**:US-02(详情)、US-03(分页)、US-05(下架拒下单)、US-09(新建)
- 🔴 **债务**:无

### Banner(首页轮播)

- 🟢 **定义**:`SPEC.md §5` + `carshop-server/app/models/banner.py`(`link_type` 枚举 + `link_target` 联动)
- 🔵 **后端**:
  - `app/schemas/banner.py`、`app/routers/banner.py`
  - 关键校验:`_validate_link_target_exists`(product/category 必须真存在)
- 🔵 **后台 Web**:`src/api/banner.ts`、`src/pages/BannerList.tsx`
- 🔵 **车机端**(09 落地):`data/repository/Repos.kt::BannerRepository`、`ui/browse/home/HomeScreen.kt::BannerCarousel`(chevron 手动翻页 + 1/3 mono 页码 + 三种 link_type 跳转处理)
- 🔵 **Fixtures**:`artifacts/fixtures/03/*`
- 🟡 **User Stories**:US-01(首页 banner)
- 🔴 **债务**:无

### Order + OrderItem(订单)

- 🟢 **定义**:`SPEC.md §5` + `carshop-server/app/models/order.py`
- 🔵 **后端**:
  - `app/schemas/order.py`、`app/routers/order.py`、`app/routers/admin_order.py`
  - `app/services/order_service.py`(订单号生成 + 商品快照)
- 🔵 **后台 Web**:`src/api/order.ts`、`src/pages/OrderList.tsx`
- 🔵 **车机端**(10 落地):
  - `data/repository/Repos.kt::OrderRepository`(create / list / detail / mockPay)
  - `ui/checkout/confirm/OrderConfirmViewModel.kt`(state machine + countdown + triggerPay 1 秒延迟 + popUpTo(Home))、`ui/checkout/confirm/OrderConfirmScreen.kt`(2 列布局 + `PayDialog` M3 Dialog 480dp + qrose QR 320dp + 02:00 mono 倒计时)
  - `ui/checkout/detail/OrderDetailViewModel.kt`、`ui/checkout/detail/OrderDetailScreen.kt`(海泡青已支付徽章 + product_snapshot 渲染)
  - `ui/checkout/list/OrderListViewModel.kt`(OrderListTab enum)、`ui/checkout/list/OrderListScreen.kt`(M3 PrimaryTabRow contentColor=tertiary)
  - `data/MockApiServer.kt::routeOrderList`(status=pending → orders-empty.json)、`assets/mocks/orders-empty.json` 新增
  - `nav/CarshopNavHost.kt`(3 处占位换真 Screen)、`ui/shell/AppShell.kt::ROUTES_WITH_OWN_TOPBAR`(加 OrderConfirm/Detail/List)
- 🔵 **Fixtures**:`artifacts/fixtures/04/*`(11 个)
- 🟡 **User Stories**:US-04(下单)、US-05(下架拒单)、US-06(幂等支付)、US-07(列表)、US-11(后台看单)、US-13(端到端)、US-17/18(并发)
- 🔴 **债务**:TD-007(订单号高并发碰撞)、TD-008(shipping_info 写死)、**TD-023(US-18 真后端并发 paid_at 抖动 · 10 发现)**

### Admin + AdminSession(后台账号 + 会话)

- 🟢 **定义**:`SPEC.md §5` + `app/models/admin.py`、`app/models/admin_session.py`
- 🔵 **后端**:`app/routers/admin_auth.py`、`app/services/auth_service.py`、`app/deps.py::require_admin`、`app/main.py`(CORS)、`app/settings.py`(`CORS_ALLOW_ORIGINS` / `SESSION_COOKIE_SAMESITE` / `SESSION_COOKIE_SECURE` env)
- 🔵 **后台 Web**:`src/auth/AuthContext.tsx`、`src/pages/Login.tsx`、`src/api/auth.ts`、`src/api/client.ts`(401 拦截 + `VITE_API_BASE_URL` 切目标)
- 🔵 **Fixtures**:`artifacts/fixtures/05/*`
- 🟡 **User Stories**:US-08(登录)、US-12(后台改 / 车机端看)、US-13(车机下 / 后台看)
- 🔴 **债务**:TD-003(session 不签名)、TD-004(cookie 跨域 · 07 部分还了 env 化)、TD-016(logout 缺 Secure)

### Image Upload(图片)

- 🟢 **定义**:`SPEC.md §6.2`(`POST /api/v1/admin/upload`)
- 🔵 **后端**:`app/routers/upload.py`、`app/settings.py::UPLOAD_DIR`、`app/utils/url.py::absolutize_url`
- 🔵 **后台 Web**:`src/api/upload.ts`、`src/components/ImageUpload.tsx`
- 🔵 **Fixtures**:`artifacts/fixtures/05/post-upload-*.json`
- 🟡 **User Stories**:US-09(上传 + 错误场景)
- 🔴 **债务**:TD-005(同图多副本)、TD-006(path-traversal 无显式校验)

---

## 二、跨切关注(改一个地方影响很多)

### 错误码 · `code`

- 🟢 **定义**:`SPEC.md §12` 错误码表 + `carshop-server/app/errors.py::ErrorCode`
- 🔵 **后端抛出点**:几乎所有 routers 都会抛 `CarshopException(ErrorCode.XXX)`
- 🔵 **前端处理**:`carshop-admin/src/api/client.ts`(全局解包 + ApiError)、`AuthContext.tsx`(401 拦截)
- 🔵 **车机端**(08+):`data/ApiResult.kt`(sealed class:Success/ApiError/NetworkError)
- 🟡 **典型场景**:US-02 场景 2(下架商品)、US-05、US-08 场景 2、US-09 场景 2/3、US-10 场景 1
- **改这里要做**:
  1. 改 SPEC.md §12 加新 code
  2. 改 errors.py 加 enum
  3. 抛出方加 raise
  4. 改 fixtures 加错误样例
  5. 改 USER_STORIES 加测试场景

### 金额 · 单位「分」

- 🟢 **定义**:`SPEC.md §11.3` + `CLAUDE.md` 共享约定 #1
- 🔵 **后端**:所有 `price` / `total_amount` / `original_price` 字段在 model / schema / API 全是 int
- 🔵 **后台 Web**:`PriceInput.tsx`(元→分)、`PriceDisplay.tsx`(分→元)是**唯一**两个换算点
- 🔵 **车机端**:`CarshopPrice.kt`(分→元)
- 🟡 **User Stories**:US-04(总价计算)、US-09(价格边界)
- **改这里要做**:任何新加的金额字段必须保持 int(分);如果想加"美元"或其他币种,要先开新讨论(本项目假设人民币)

### 分页 envelope · `{list, total, page, page_size}`

- 🟢 **定义**:`SPEC.md §13` URL 规范 + `app/schemas/common.py::Page<T>`
- 🔵 **后端列表接口**:products / banners(admin)/ orders 都用
- 🔵 **后台 Web**:`api/client.ts::Page<T>` 类型 + 各列表页 `useState<Page<T>>`
- 🔵 **车机端**:`data/dto/PagedList.kt`
- 🟡 **User Stories**:US-03(分页一致性)、US-07(订单列表)
- **约定**:`page` 从 1 起、`page_size` 默认 20、最大 100;非法值返 1000

### 图片 URL · 完整 URL 不是相对路径

- 🟢 **定义**:`SPEC.md §11.6` + `app/utils/url.py::absolutize_url`
- 🔵 **后端响应**:任何含 `image_url` / `icon_url` / `main_image_url` 的 schema 必须用 `absolutize_url` 包一层
- 🔵 **后台 Web**:`<img src={url} />` 直接用
- 🔵 **车机端**:Coil `AsyncImage(url)` 直接用
- **约定**:`/static/...` 开头 → 自动拼 BASE_URL;`http://` / `https://` 开头 → 原样返回

### 设备 ID · `X-Device-Id` header

- 🟢 **定义**:`SPEC.md §11.5` + `app/deps.py::get_device_id`
- 🔵 **后端**:`POST /orders` / `GET /orders` / `POST /orders/:id/mock_pay` 必须有
- 🔵 **车机端**:`data/interceptors/DeviceIdInterceptor.kt` 全局注入
- 🔵 **后台 Web**:**不用**(后台是运营操作,跟设备无关)
- 🟡 **User Stories**:US-04 场景 2(跨设备)
- **改这里要做**:加新接口要订单关联设备时,记得 `Depends(get_device_id)`

### 时间格式 · ISO 8601 字符串

- 🟢 **定义**:`SPEC.md §11.8` + `app/utils/datetime.py::to_iso`
- 🔵 **后端**:所有 `created_at` / `paid_at` / `updated_at` 字段必须走 `to_iso(dt)` 序列化
- 🔵 **前端**:用 `dayjs(s).format(...)` 直接消费
- **改这里要做**:任何新加的时间字段必须 ISO 8601 带时区(`2026-05-26T12:00:00+08:00`)

---

## 三、设计系统(改视觉看这里)

### 设计 Token(颜色 / 字号 / 间距 / 圆角)

- 🟢 **定义**:`design/安卓商城/tokens.json` v1.0.0
- 🔵 **车机端**:`carshop-android/.../designsystem/tokens/{Colors,Typography,Spacing,Radius,Sizing,Elevation}.kt`
- 🔵 **后台 Web**:`carshop-admin/src/theme/tokens.ts` + `antdTheme.ts`
- 🟡 **User Stories**:US-01 场景 1("字体颜色间距走 token")
- 🔴 **债务**:TD-002(思源黑体未打包)
- **改这里要做**:**先改 tokens.json**,然后**两端各自翻译**(不许共享代码,跨技术栈)

### 价格上色规则(physical 红 / service_voucher 绿)

- 🟢 **定义**:`SPEC.md §5 Product.product_type` + `tokens.json` 的 `TextPrice` / `TextPriceEnergy`
- 🔵 **车机端**:`CarshopPrice.kt` 自动按 type 上色
- 🔵 **后台 Web**:`PriceDisplay.tsx`(看后续是否需要)
- 🟡 **User Stories**:US-01 场景 2 ("price 颜色按 product_type")
- **改这里要做**:加新 product_type → 改 enum + 改 CarshopPrice + 改 SPEC + 加 fixture

---

## 四、Session 之间的依赖

### Session → 涉及实体 / 关注 → 干扰半径

| Session | 主要改的实体 | 改哪些公共文件 | 干扰谁 |
|---|---|---|---|
| 01 server-foundation | 全部 model | `app/db.py` / `app/main.py` / `app/seeds/initial.py` | 全部下游 |
| 02 catalog | Product + Category | `app/main.py`(挂路由)/ `app/deps.py`(require_admin 桩)/ `app/schemas/__init__.py` | 03/04/05/06/09 |
| 03 banner | Banner | 同上 | 06/09 |
| 04 order-pay | Order + OrderItem | 同上 + `app/services/order_service.py` | 05/06/10/11 |
| 05 admin-auth-upload | Admin + AdminSession | `app/deps.py`(替换 require_admin)/ `static/uploads/` | 06 |
| 06 admin-web | 不动后端 | `carshop-admin/src/*` 全是 06 的 | 07 |
| 07 early-integration | 不写新代码 | Mac mini launchd / cloudflared / DNS / SPEC §14 | 08~11 |
| 09 android-browse | 不动后端 | `carshop-android/app/src/main/java/com/carshop/android/{data/repository, ui/browse/{home,category,detail,BrowseCommon}}` + 7 新 mock fixtures + AppShell.kt(一处增 ROUTES_WITH_OWN_TOPBAR) | 10 / 11 |
| 10 android-checkout | 不动后端 | `carshop-android/app/src/main/java/com/carshop/android/{ui/checkout/{confirm,detail,list}, data/repository/Repos.kt(扩 OrderRepository), data/MockApiServer.kt(routeOrderList), nav/CarshopNavHost.kt(3 处占位换真), ui/shell/AppShell.kt(ROUTES_WITH_OWN_TOPBAR 扩 3 条)}` + `assets/mocks/orders-empty.json` + `build.gradle.kts`(qrose) | 11 |
| 08~10 android | 不动后端 | `carshop-android/src/*` 全是各 session 的 | 11 |
| 11 final-integration | 不写新代码 | APK 签名 / 部署 / 录屏 | (无下游) |

---

## 五、常见改动场景速查

### 我想加一个新的商品字段(比如 `weight_kg`)

读这些文件:
1. `SPEC.md §5 Product` ← 改这里加字段
2. `app/models/product.py` ← Migration + 字段
3. `app/schemas/product.py` ← Pydantic
4. `app/seeds/initial.py` ← seed 数据补
5. `artifacts/fixtures/02/get-product-1.json` 等 ← 更新示例
6. `carshop-admin/src/pages/ProductEdit.tsx` ← 加表单字段
7. `carshop-admin/src/api/product.ts` ← 类型
8. `carshop-android/.../data/dto/Product.kt` ← 类型
9. `carshop-android/.../ui/browse/detail/*` ← 展示
10. `USER_STORIES.md` ← 加场景验证新字段

### 我想加一个新的错误码(比如 4000 库存不足)

1. `SPEC.md §12` ← 加 code 表
2. `app/errors.py::ErrorCode` ← enum
3. 抛出点
4. `app/errors.py::http_status_for` ← HTTP 状态映射
5. `CHANGE_MAP.md` ← 本文件错误码节加一句
6. `USER_STORIES.md` ← 加触发场景
7. `TECH_DEBT.md` ← 看是否引入新债务

### 我想加一个新的页面(比如商品评价)

1. `SPEC.md §4` 功能清单 + §5 加 model + §6 加接口 ← 改这里
2. 走新 session 加文档:`sessions/XX-feature-name.md`
3. 改 `STATUS.md` 加 row
4. 改 `sessions/README.md` 加进依赖图
5. 改 `USER_STORIES.md` 加 stories
6. 改 `TEST_MATRIX.md` 加覆盖目标

---

## 六、知道这份文件是怎么过期的

任何下面情况发生时,本文件**必须更新**:

- 新增 / 删除 / 改名一个 model
- 新增 / 删除一个 API 路由
- 新增 / 删除一个错误码
- 新增 / 删除一个 session
- 新增 / 删除一个 cross-cutting 约定(分页规则、时间格式等)
- 新增 / 删除一个组件(车机端 / 后台)

CLAUDE.md 已经加了铁律 8:技术债登记 TECH_DEBT。本文件也是同理 —— **不维护就废**。

---

**版本**:v1.2
**最后更新**:2026-05-26(10 完成 · 车机端 checkout 模块入 Order/OrderItem 实体引用 + TD-023)
