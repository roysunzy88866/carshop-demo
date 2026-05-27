# Session 09 · Android Browse(车机端 · 浏览模块)

> 实现车机端的 **首页 + 分类商品列表 + 商品详情**,完成"从打开应用 → 看到商品"的浏览链路。

---

## 1. 你要做什么

### 1.1 三个页面

#### A. 首页(`Routes.Home`)

布局(主内容区 1680dp,从上到下):
1. **顶部 Top Bar**(80dp 高):标题"商店",右上角"我的订单"按钮(跳 `Routes.OrderList`)
2. **Banner 区**(高 240dp):横向轮播 banner(无动画,纯切换;或者就显示第一张,**no-motion 设计稿要求**)。点击 banner:
   - `link_type=none`:不动
   - `link_type=product`:跳详情
   - `link_type=category`:跳分类列表
3. **分类入口栏**(高 176dp,5 个 icon-tile,横向排):点击跳 `Routes.CategoryProducts/{id}`,使用海泡青 accent
4. **推荐商品**(横滑列表):服务端 `GET /api/v1/products` 第一页前 10 个

#### B. 分类商品列表(`Routes.CategoryProducts/{categoryId}`)

- Top Bar:返回按钮 + 分类名(标题)
- 主区域:**3 列网格**商品卡片
  - 卡片用 00 session 的 `CarshopCard` + 商品卡布局(主图 + 标题 max 2 行 + 价格 + 划线原价 + 销量?用占位 "已售 X")
  - 价格颜色按 `product_type`:`physical` 红、`service_voucher` 绿(00 session 已提供 `CarshopPrice`)
- 分页:滚到底拉下一页,直到 `total` 加载完
- 空态:用 00 session 的 `CarshopEmpty` 组件,文案"该分类下暂无商品"
- 点击卡片:跳 `Routes.ProductDetail/{productId}`

#### C. 商品详情(`Routes.ProductDetail/{productId}`)

- Top Bar:返回 + 商品标题(超长省略)
- 主区域(左右 split,左图右文):
  - **左**:商品主图(560dp 宽,16:9 或 1:1)
  - **右**:
    - 商品标题(`product-title-detail` 28sp Bold)
    - 价格(`price-display` 44sp,按 type 上色)
    - 划线原价(若有)
    - 规格(若有,如"100 元面值")
    - 描述(`body-large` 20sp,可滚)
- 底部固定 CTA 区(80dp 高):
  - **"立即购买"** Primary Button(走 00 的 `CarshopButton`)→ 跳 `Routes.OrderConfirm/{productId}`
  - **下架状态**:Button disabled + 显示"商品已下架"

### 1.2 数据层(Repository + ViewModel)

- `CategoryRepository`、`ProductRepository`、`BannerRepository`
- 各有 `fun list(): Result<...>` / `fun get(id): Result<...>`
- 用 Kotlin coroutines + Flow
- 每个页面有一个 ViewModel(`HomeViewModel` / `CategoryProductsViewModel` / `ProductDetailViewModel`)
- State 用 `StateFlow<UiState>` 表达 Loading / Success / Error / Empty

### 1.3 错误处理

- 网络错 → 整页 Empty + "重试"按钮
- 商品不存在(code 1001) → 整页 Empty + "商品已失效"
- 商品下架(详情还是返回) → 显示商品,但 "立即购买" disabled + 提示

---

## 2. 你不要做什么

- ❌ 实现下单 / 支付 / 我的订单(10 的事,只跳 route 占位)
- ❌ 实现搜索
- ❌ 实现购物车 / 收藏
- ❌ 改 design-system(00 的事)
- ❌ 改 API client(08 的事)
- ❌ 改 SPEC(除非发现真漂移)
- ❌ 实现 banner 自动轮播动画(no-motion)
- ❌ 实现 ImageLoader 自己写一套(用 Coil)

---

## 3. 输入

- `CLAUDE.md`、`SPEC.md`
- `carshop-android/`(08 产物)
- `artifacts/00-design-system.md`(组件 API)
- `artifacts/fixtures/02-banner-categories-products/`
- `design/安卓商城/阶段4 · 屏2 · 首页.html` 等(参考视觉,不照搬代码)

---

## 4. 输出

- `carshop-android/app/src/main/java/com/carshop/android/ui/browse/`
  - `home/` HomeScreen + HomeViewModel
  - `category/` CategoryProductsScreen + ViewModel
  - `detail/` ProductDetailScreen + ViewModel
- `carshop-android/app/src/main/java/com/carshop/android/data/repository/`
  - `CategoryRepository.kt`、`ProductRepository.kt`、`BannerRepository.kt`
- 在 `CarshopNavHost` 把这三个路由替换为真页面
- `artifacts/09-android-browse.md`:
  - 截图 3 张(首页 / 分类列表 / 详情)
  - ViewModel 逻辑摘要
  - 已踩坑

---

## 5. 验收

跑 Mock 模式 **和** 真模式各一遍:

| # | 场景 | 期望 |
|---|---|---|
| 1 | 启动 → 首页 | banner + 5 个分类 + 推荐商品列表显示正常,字体/颜色/间距走 token |
| 2 | 点 banner(link=product)| 跳详情页 |
| 3 | 点分类 icon | 跳列表页,标题=分类名 |
| 4 | 列表滚到底 | 加载下一页(若有);total 到了显示"没有更多" |
| 5 | 列表空分类 | 显示 Empty 组件 |
| 6 | 点列表卡片 | 跳详情页 |
| 7 | 详情页 | 显示图、标题、价格(按 type 上色)、规格、描述 |
| 8 | 详情页 - 下架商品 | "立即购买" disabled + 提示 |
| 9 | 详情页点"立即购买" | 跳 OrderConfirm 占位(10 来填) |
| 10 | 网络断开 | 显示错误态 + 重试按钮 |

---

## 6. 依赖

- **上游**:08 android-foundation
- **下游**:10、11

## 7. Mock 策略

继续用 08 已建的 MockWebServer + assets/mocks,fixtures 来自 07 修过的版本。

## 8. 已知坑

1. **图片加载**:用 [Coil](https://coil-kt.github.io/coil/) (`io.coil-kt.coil-compose`),不要自己写 ImageLoader
2. **图片占位**:加载中用 `surface-variant` 灰底,失败用 placeholder 图
3. **价格颜色**:**必须**用 `CarshopPrice` 组件,传 `productType` 让它自动选色,**不要**自己写 `if (type == physical) red else green`
4. **分页 state**:LazyVerticalGrid 滚到底触发,注意防抖(避免短时间重复触发)
5. **banner 占位**:如果后台没配 banner,首页空着 / 隐藏掉,不要崩
6. **导航参数**:用 navigation-compose 的 typed args,categoryId 必须是 String → 转 Int
7. **State preservation**:用 `rememberSaveable` 保住滚动位置,旋转(虽然锁横屏)或返回时不丢
8. **不要在 Composable 里直接调 API**:走 ViewModel + Repository + coroutines
