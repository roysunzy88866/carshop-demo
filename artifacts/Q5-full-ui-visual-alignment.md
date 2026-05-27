# Q5 · full-ui-visual-alignment 产物文件

**Session 类型**:辅助 (⚙️)  
**完成日期**:2026-05-27  
**目标**:全量 UI 视觉对齐——所有已实现的屏幕必须与 `design/安卓商城/` HTML 原型保持一致

---

## 一、Session 背景

用户明确反馈：
- 侧边栏(Rail)字体/字号与 HTML 设计稿完全不同
- 分类推荐区字体字号也不对
- 商品列表页看起来和设计图完全不一样

扩大范围为**全 APP 对齐**，不只是首页。

---

## 二、改动文件清单

| 文件 | 改动描述 |
|---|---|
| `designsystem/components/CarshopRail.kt` | ① 新增 "EV·MALL." 品牌区 + 分隔线；② icon 28dp→24dp；③ 文字 labelMedium(20sp)→labelLarge(22sp)；④ 选中色 primary→tertiary(seafoam) |
| `ui/browse/category/CategoryProductsScreen.kt` | ① TopBar 下方加 HorizontalDivider；② Column 加 background；③ TopBar actions 加筛选图标 |
| `ui/browse/BrowseCommon.kt`(ProductCard) | 图片区改为 Box 叠加，服务券商品左下角显示 seafoam 类型角标（加油卡/充电卡/洗车券/保养券） |
| `ui/browse/detail/ProductDetailScreen.kt` | ① TopBar 下方加 HorizontalDivider；② CTA 底栏高度 touchTarget+s3(96dp)→touchTarget(80dp) |
| `ui/checkout/confirm/OrderConfirmScreen.kt` | TopBar 下方加 HorizontalDivider |
| `ui/checkout/detail/OrderDetailScreen.kt` | TopBar 下方加 HorizontalDivider |

**未改动**：ViewModel / Repository / NavHost / fixtures / SPEC / 接口契约

---

## 三、对齐点明细

### R-01 ~ R-04 · CarshopRail

| # | HTML 原型 | 改动前 | 改动后 |
|---|---|---|---|
| R-01 | `.brand { EV·MALL. }` 品牌区 | 无品牌区 | 80dp 高 Row, "EV·MALL." titleLarge/Bold, "·" 用 tertiary(seafoam) |
| R-02 | `.rail__item font-size:22px` | labelMedium 20sp | labelLarge 22sp/Medium |
| R-03 | `.rail__item--active { color: seafoam-600 }` | primary 色(steel-800) | tertiary 色(seafoam-600) |
| R-04 | `.rail__icon { 24px }` | iconMd 28dp | 24.dp |

### D-01 · TopBar 分隔线（全屏一致性）

所有 Screen 自带 TopBar 的页面（CategoryProducts / ProductDetail / OrderConfirm / OrderDetail）均加 1dp `outlineVariant` HorizontalDivider，与首页 HomeScreen 保持一致（HomeScreen 已在 Q5 初期加过）。

### C-01 · CategoryProductsScreen 筛选图标

TopBar actions 加 `FilterList` 图标按钮，对齐原型 AppBar 右侧筛选图标。功能待后续实现（点击无响应）。

### P-01 · ProductCard 服务券类型角标

productType=="service_voucher" 时，在图片左下角叠加 seafoam 底色角标，按 product.title 关键词映射：
- 含"加油" → "加油卡"
- 含"充电" → "充电卡"
- 含"洗车" → "洗车券"
- 含"保养" → "保养券"
- 其余 → "服务券"

### E-01 · ProductDetailScreen CTA 栏高度

底部 CTA 栏从 `touchTarget + s3`(80+16=96dp) 修正为 `touchTarget`(80dp)，对齐 HTML `--h-cta-bar: 80px`。

---

## 四、截图验证记录

| 屏幕 | 验收结果 |
|---|---|
| 首页 | ✅ EV·MALL. 品牌区可见，"·" seafoam，首页 active seafoam 色，字号 22sp，banner 分页/chevron/分类瓦片/推荐均正常 |
| 商品列表（汽车用品） | ✅ TopBar 分隔线可见，右侧筛选图标，3列卡片网格，服务券角标（加油卡/充电卡）正确渲染 |
| 商品详情（车载磁吸手机支架） | ✅ TopBar 分隔线，左图右文 50/50，价格+划线价，底部 80dp CTA 栏 |
| 确认订单 | ✅ TopBar 分隔线，1.4fr/1fr 两列（收货信息+商品卡 / 订单金额），底部合计+提交订单 |
| 我的订单 | ✅ Rail "我的订单" seafoam active，PrimaryTabRow 全部/已支付/待支付，订单卡片正常 |

---

## 五、已知偏离（不影响本 session 完成状态）

| # | 描述 | 原因 | 处理方式 |
|---|---|---|---|
| P-02 | 商品列表无商品数量 meta 行（"共 X 件"） | ViewModel/API 未暴露 total_count | 待后续 session 追加；记 TECH_DEBT |
| P-03 | 商品列表无 filter chip 行 | 筛选功能未实现 | MVP 已知偏离；筛选图标已到位，功能待后续 |
| D-02 | 商品详情无 stats block（已售/评价/好评率） | Product DTO 无该字段 | 待后续 session 追加；记 TECH_DEBT |

---

## 六、收尾自检

- [x] 6 个文件改动，全部在自己 session 允许范围内
- [x] 不涉及 SPEC 接口契约，无需更新 SPEC
- [x] 编译通过（compileMockDebugKotlin BUILD SUCCESSFUL）
- [x] APK 安装到 carshop-tablet AVD 实跑验证，5 个屏幕截图均 PASS
- [x] 已更新 STATUS.md（Q5 行添加）
- [x] 本 artifact 已创建
