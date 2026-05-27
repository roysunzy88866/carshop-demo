# Q4 · 视觉对齐 09 · Artifact

**Session ID**: Q4  
**日期**: 2026-05-26  
**工作性质**: 辅助 session · 视觉 only(无业务逻辑改动)

---

## 1. 实现内容(对照 session spec 打勾)

### ✅ 1.1 分类瓦片图标容器(HomeScreen.kt · CategorySection)

- 改前:图标无底盒,直接渲染在卡片背景上
- 改后:56×56 dp `Box`,`clip(RoundedCornerShape(Radius.md = 12dp))`
  - 默认:背景 `surfaceVariant`,图标 `onSurface`
  - "加油充电" 专属 accent:`tertiaryContainer(seafoam-50)`,图标 `tertiary(seafoam-600)`
- 图标大小:Sizing.iconLg = 32dp
- 贴合设计:`.cat-tile .ic` = 56×56 + `--radius-icon-tile: 12dp`
  + `.cat-tile .ic--accent` = `tertiary-container` bg + `on-tertiary-container` fg

### ✅ 1.2 "为你推荐" section 标题字号(HomeScreen.kt)

- 改前:`titleLarge`(28sp / Bold)
- 改后:`headlineSmall`(32sp / Bold)
- 贴合设计:`--fs-headline-small: 32px`

### ✅ 1.3 ProductCard 全出血图片(BrowseCommon.kt)

- 改前:CarshopCard 内置 24dp 全边距 → 图片被内缩
- 改后:换用原生 Material3 `Card(onClick=...)`
  - 图片:`fillMaxWidth().aspectRatio(4f/3f)`,`cornerRadius=0.dp`(贴卡片边)
  - 图片/body 分隔线:`HorizontalDivider` (outlineVariant · 1dp)
  - body `Column` 独立内边距:`padding(horizontal=22dp).padding(top=20dp, bottom=22dp)`
- 贴合设计:`.card--product .img` = full-bleed · 无 padding / `.card--product body` = `20px 22px 22px`

### ✅ 1.4 商品详情左图响应式尺寸(ProductDetailScreen.kt · DetailContent)

- 改前:`Modifier.width(560.dp).height(420.dp)` 写死
- 改后:`Column(Modifier.weight(1f))` + `aspectRatio(800f/560f)` → 50/50 分屏自适应
- 贴合设计:`grid-template-columns: 800px 1fr`

### ✅ 1.5 服务券装饰卡(ProductDetailScreen.kt · ServiceVoucherCard)

新增 Composable,仅对 `product.productType == "service_voucher"` 触发:

| 元素 | 实现 |
|---|---|
| 背景 | `primary`(石墨蓝 #1A2027) |
| seafoam 光晕 | 右上角 280dp `Box`,`Brush.radialGradient(tertiary 18% → Transparent)`,`CircleShape` |
| overline 文字 | "SERVICE · VOUCHER",14sp,`FontFamily.Monospace`,4sp letterSpacing,颜色 `tertiary` |
| 大图标 | 80dp,`onPrimary.copy(alpha=0.85f)`,按 title 关键词映射 |
| 中文标签 | `headlineMedium`,`onPrimary`,按 title 关键词映射 |

**图标 & 标签关键词映射**:

| 关键词 | 图标 | 标签 |
|---|---|---|
| 加油 | LocalGasStation | 加油卡 |
| 超充 / 充电月卡 | LocalGasStation | 充电月卡 |
| 充电 | LocalGasStation | 充电卡 |
| 洗车 | LocalCarWash | 洗车券 |
| 保养 | Storefront | 保养券 |
| 停车 | Storefront | 停车券 |
| (其他) | Storefront | 服务券 |

---

## 2. 未改动确认

以下内容在 session spec 中标为"视觉 only 不触碰",已确认未改动:

- ViewModels / Repositories / NavHost / Routes — **未改**
- `CarshopTopBar`, `CarshopButton`, `CarshopPrice`, `CarshopLoading`, `CarshopEmpty` — **未改**
- Skeleton 动画、LazyVerticalGrid 预加载分页逻辑 — **未改**
- `MockApiServer.kt`, fixture JSON 文件 — **未改**
- `SPEC.md`, `USER_STORIES.md`, `TEST_MATRIX.md` — **未改**

---

## 3. 主动偏离清单(Q4 新增)

| # | 偏离项 | 原型有 | Q4 处理 | 原因 |
|---|---|---|---|---|
| P-01 | 商品列表排序 / 筛选 chip 行 | ✅ | ❌ 不实现 | spec §1.2 filter UI 未排期;MVP 分类导航够用 → 记 TD-022 |
| P-02 | ServiceVoucherCard 商户 logo | ✅ | ❌ 不实现 | 需新 API 字段(brand_logo_url),当前 product DTO 没有;改用 title 关键词映射图标代替 |
| P-03 | 商品详情图片轮播缩略图条 | ✅ | ❌ 不实现 | 09 spec §1.3 已决策"单图 + 预留空间",轮播属于后续迭代 |

---

## 4. US 重跑验证证据

### US-01 · 首页浏览(5 分类 + banner + 推荐)

**场景**:Mock 模式冷启动首页

- ✅ Banner 轮播:自动播放 + 左右 chevron + "1/3" 计数器(截图见下)
- ✅ 5 个分类瓦片:加油充电 seafoam-accent 底盒 + 图标,其余 surfaceVariant 灰底
- ✅ "为你推荐" heading 字号升大(headlineSmall)
- ✅ LazyRow 推荐商品可横滑

截图路径:`/tmp/app_home.png`(4:01 AM · 重测)

---

### US-02 · 商品详情页

**US-02.1 在售商品**

- ✅ 左图右文 50/50 分屏
- ✅ ProductImage 按 `aspectRatio(800/560)` 渲染(物理产品截图:`/tmp/detail_physical.png`)
- ✅ 右列:标题(max 5 行省略)、价格(红色 Physical)、规格、描述
- ✅ 底部 CTA:"立即购买" enabled

**US-02.2 下架商品**

- ✅ 右列出现"商品已下架" surfaceVariant chip
- ✅ 底部 CTA:"商品已下架" disabled 灰色
- 商品:"便携式车载吸尘器 120W"(product-2-offsale.json)
- 截图路径:`/tmp/detail_offsale.png`

**US-02 service_voucher 变体**

- ✅ 左侧显示 ServiceVoucherCard(石墨蓝背景 + seafoam 光晕 + Storefront 图标 + "服务券")
- ✅ 价格使用 seafoam 色(ProductPriceType.ServiceVoucher)
- 商品:"超长测试…"(product-13-longtitle.json,service_voucher)
- 截图路径:`/tmp/detail_voucher.png`

---

### US-03 · 分类商品列表分页(25 条 / 3 页 / "没有更多了")

- ✅ 加油充电分类:3 页(page_size=10)共 25 条
- ✅ 第 1 页:products-cat-p1.json(10 条)
- ✅ 第 2 页:products-cat-p2.json(10 条)
- ✅ 第 3 页:products-cat-p3.json(5 条)
- ✅ 滚动到底部显示"—— 没有更多了 ——"
- 截图路径:`/tmp/catlist_nomore.png`

---

### US-24 · 超长商品 title 不崩版

- ✅ 分类列表卡片:1000+ 字符 title 显示 2 行 + 省略号,不撑开布局
- ✅ 商品详情右列:title 显示 5 行 + 省略号(maxLines=5,overflow=Ellipsis)
- ✅ TopBar:title > 20 字符时取前 20 + "…"
- 商品:product-13-longtitle.json(US-24 测试专用)
- 截图路径:`/tmp/catlist2.png`(catlist 中可见 2 行省略)/ `/tmp/detail_voucher.png`(detail 5 行省略)

---

## 5. 改动文件清单

| 文件 | 改动类型 | 改动摘要 |
|---|---|---|
| `ui/browse/home/HomeScreen.kt` | 修改 | CategorySection 图标容器(56dp·accent/default) + "为你推荐" headlineSmall |
| `ui/browse/BrowseCommon.kt` | 修改 | ProductCard 换 Material3 Card·全出血图片·22dp body padding |
| `ui/browse/detail/ProductDetailScreen.kt` | 修改 | DetailContent 左面板 weight(1f)+aspectRatio·ServiceVoucherCard 新增 |

---

## 6. 已知偏离 / 技术债

- **TD-022(新)**:商品列表排序 / 筛选 chip 行未实现 → 见 TECH_DEBT.md
- `CarshopCard.padCard` 内置 24dp 全边距的限制:如果其他 screen 需要全出血图片,同样要绕开用原生 `Card` — 可考虑给 CarshopCard 加 `noPadding` 参数还这笔债(11 兜底列表考虑)

---

**Q4 Artifact 版本**: v1.0 · 2026-05-26
