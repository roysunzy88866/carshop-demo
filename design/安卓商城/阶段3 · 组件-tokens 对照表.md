# 阶段 3 · 组件 → Tokens 对照表

> **用途**:工程消费用 · 每个组件的每个状态映射到哪些 token。
> **方向**:A · 极简黑白(锋利克制)
> **源文件**:`tokens.json`(SSOT)+ `components/_tokens.css`(CSS 变量)+ `components/_components.css`(组件类)
> **状态**:`normal`(默认) / `pressed` / `disabled` / `focused` / `loading` / `error` / `selected`
> **约定**:所有 token 名走 kebab-case · 字号单位 sp · 间距/圆角单位 dp

---

## 1 · BUTTONS

通用规则:`height = space-12 (48dp)` · `radius = radius-button (4dp)` · `text = label-large (14sp / 500)` · `gap = space-2 (8dp)`

### 1.1 Primary · `.btn.btn--primary`

| 状态 | 背景 | 文字 | 描边 | 备注 |
|---|---|---|---|---|
| normal | `color.primary` `#0F0F0F` | `color.on-primary` `#FFFFFF` | — | 主 CTA |
| pressed | `color.primitive.neutral-800` `#2E2E2E` | `color.on-primary` | — | 12% state-layer 折算后近似值 |
| disabled | `color.primitive.neutral-200` `#EAEAEA` | `color.text-disabled` `#B8B8B8` | — | container opacity 12% / content 38% |
| focused | `color.primary` | `color.on-primary` | outline 2dp `color.border-focus` offset 2px | M3 focus ring |
| loading | `color.primary` | `color.on-primary` (隐藏) | — | spinner 16dp · color = on-primary |

**Compose 映射**: `Button(colors = ButtonDefaults.buttonColors(containerColor = primary, contentColor = onPrimary))`

### 1.2 Secondary · `.btn.btn--secondary`

| 状态 | 背景 | 文字 | 描边 |
|---|---|---|---|
| normal | transparent | `color.on-surface` `#0F0F0F` | 1dp `color.outline` `#D6D6D6` |
| pressed | `color.surface-variant` `#F5F5F5` | `color.on-surface` | 1dp `color.outline` |
| disabled | transparent | `color.text-disabled` | 1dp `color.outline-variant` `#EAEAEA` |
| focused | transparent | `color.on-surface` | 2dp `color.border-focus` `#0F0F0F` |
| loading | transparent | (隐藏) | 1dp `color.outline` · spinner color = on-surface |

**Compose 映射**: `OutlinedButton(colors = ButtonDefaults.outlinedButtonColors(contentColor = onSurface), border = BorderStroke(1.dp, outline))`

### 1.3 Text · `.btn.btn--text`

| 状态 | 背景 | 文字 |
|---|---|---|
| normal | transparent | `color.primary` `#0F0F0F` |
| pressed | `color.surface-variant` | `color.primary` |
| disabled | transparent | `color.text-disabled` |
| focused | `color.surface-variant` | `color.primary` |
| loading | transparent | (隐藏) · spinner color = primary |

**Compose 映射**: `TextButton(colors = ButtonDefaults.textButtonColors(contentColor = primary))`
**特殊**: 内边距收紧 `padding-h = space-3 (12dp)`

### 1.4 Icon Button · `.btn.btn--icon`

| 状态 | 背景 | 图标 |
|---|---|---|
| normal | transparent | `color.on-surface` |
| pressed | `color.surface-variant` | `color.on-surface` |
| disabled | transparent | `color.text-disabled` |
| focused | `color.surface-variant` + outline 2dp `border-focus` | `color.on-surface` |
| loading | transparent | spinner 20dp |

**尺寸**: hit-area 40×40dp(`radius-full`)· 图标 22×22dp · stroke 1.8
**Compose 映射**: `IconButton(modifier = Modifier.size(40.dp)) { Icon(...) }`

---

## 2 · INPUTS

通用规则:`height = space-12 (48dp)` 单行 / `min-height = 96dp` 多行 · `radius = radius-input (4dp)` · `text = body-medium (14sp)` · `padding = pad-input-h pad-input-v (16dp 12dp)`

| 状态 | 背景 | 文字 | 描边 | 占位符 | helper |
|---|---|---|---|---|---|
| normal | `color.surface` | `color.text-primary` | 1dp `color.border-default` `#D6D6D6` | `color.text-tertiary` `#8A8A8A` | `color.text-tertiary` |
| focused | `color.surface` | `color.text-primary` | 2dp `color.border-focus` `#0F0F0F` | — | `color.text-tertiary` |
| filled | `color.surface` | `color.text-primary` | 1dp `color.border-default` | — | `color.text-tertiary` |
| disabled | `color.surface-variant` `#F5F5F5` | `color.text-disabled` | 1dp `color.outline-variant` `#EAEAEA` | `color.text-disabled` | `color.text-disabled` |
| error | `color.surface` | `color.text-primary` | 2dp `color.error` `#E63946` | — | `color.error` |

**Label**: `body-small (12sp)` · `color.text-secondary`
**Icon (prefix/suffix)**: 20×20 · `color.text-secondary`
**Clear / Eye button**: 24×24 hit · `color.text-tertiary`

**Compose 映射**: `OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(...))` · 通过 `BorderStroke` 切换 1dp/2dp。

---

## 3 · CARDS

通用规则:`bg = color.surface` · `border = 1dp color.border-default` · `radius = radius-card (4dp)` · `shadow = elevation-0`(不用阴影)

### 3.1 商品卡 · 双列 · `.card.card--product-grid`

| 元素 | Token | 备注 |
|---|---|---|
| 卡片底 | `color.surface` · 1dp `color.border-default` | radius 4dp · 无阴影 |
| 商品图区 | 占位 striped · `color.surface-variant` / `surface-low` | aspect-ratio 3/4 · radius 2dp(`radius-image`) |
| 标题 | `fs-product-title-card` (13sp) · `color.text-primary` | 2 行 max · webkit-line-clamp |
| 现价 | `fs-price-card` (16sp / 700) · `color.text-price` `#E63946` | yuan 符号小 0.65em |
| 划线原价 | `fs-price-strike` (12sp) · `color.text-price-strike` `#B8B8B8` | line-through |
| 销量 | `body-small` (12sp) · `color.text-tertiary` | 例:"已售 6.2k" |
| 角标 overlay | top-left · `space-2` from edge | tag 用 4.1 状态标 |

**状态**:
- `pressed` → bg `color.surface-low`
- `selected` → border 2dp `color.border-focus`
- `disabled` → opacity 38% (`opacity-disabled-content`)

### 3.2 商品卡 · 横版 · `.card.card--product-horizontal`

| 元素 | Token |
|---|---|
| 图片 | 100×100 固定 · `radius-image` 2dp |
| 标题 | `body-medium` (14sp) · 2 行 max · `color.text-primary` |
| 规格 | `body-small` (12sp) · `color.text-tertiary` |
| 价格 | 同 3.1 |

### 3.3 订单卡 · `.card.card--order`

| 元素 | Token |
|---|---|
| 订单号 | `font-mono` `body-small` · `color.text-tertiary` |
| 状态 | `label-medium` (12sp / 500) · 待付款 = `color.error`,其他 = `color.on-surface` |
| 商品缩略 | 56×56 · `radius-image` |
| 商品名 | `body-small` · 2 行 max · `color.text-primary` |
| 数量 | `font-mono` `body-small` · `color.text-tertiary` |
| 分割线 | head 下 / foot 上 · 1dp `color.divider` `#EAEAEA` |
| 总价 | `price-line-item` (14sp / 700) |

### 3.4 地址卡 · `.card.card--address`

| 元素 | Token |
|---|---|
| 姓名 | `title-medium` (16sp / 600) · `color.on-surface` |
| 手机 | `font-mono` `body-medium` · `color.text-secondary` |
| 默认标 | `tag.tag--new`(借用新品标样式) |
| 地址 | `body-medium` (14sp) · `color.text-secondary` · line-height 1.5 |

**状态**:`selected` → border 2dp `color.border-focus`

---

## 4 · CHIPS & TAGS

### 4.1 状态标 · `.tag` · `radius-tag (2dp)` · `label-small (11sp / 500)`

| 变体 | 背景 | 文字 |
|---|---|---|
| `.tag--new` 新品 | `color.tag-new-bg` `#0F0F0F` | `color.tag-new-text` `#FFFFFF` |
| `.tag--hot` 热卖 / 在售 | `color.tag-hot-bg` `#F5F5F5` | `color.tag-hot-text` `#1A1A1A` |
| `.tag--preorder` 预售 | `color.tag-preorder-bg` `#F5F5F5` | `color.tag-preorder-text` `#1A1A1A` |
| `.tag--sale` SALE | `color.tag-sale-bg` `#FEF2F3` | `color.tag-sale-text` `#A91823` |
| `.tag--sale-block` 大 SALE | 同 sale · 字号升至 `label-medium` (12sp / 700) · letter-spacing 1px | |

**高度**:20dp(`label-small`)/ 24dp(`tag--sale-block`)
**内边距**:`space-2 (8dp)` 水平

### 4.2 筛选 Chip · `.chip` · `radius-pill (9999)` · `label-large (14sp / 500)`

| 状态 | 背景 | 文字 | 描边 |
|---|---|---|---|
| normal | `color.surface` | `color.on-surface` | 1dp `color.outline` |
| pressed | `color.surface-variant` | `color.on-surface` | 1dp `color.outline` |
| selected | `color.primary` | `color.on-primary` | 1dp `color.primary` |
| focused | `color.surface` | `color.on-surface` | 2dp `color.border-focus` |
| disabled | `color.surface` | `color.text-disabled` | 1dp `color.outline-variant` |

**高度**:32dp · **内边距**:`space-3 (12dp)` 水平 · **gap**:`space-1 (4dp)` · 选中带 ✓ 图标 14×14

**Compose 映射**: `FilterChip(...)` 或 `InputChip(...)` · 通过 `selected` 参数切换样式

### 4.3 品牌 Chip
样式同 4.2 筛选 Chip。

---

## 5 · DIALOG & OVERLAY

| 元素 | Token | 备注 |
|---|---|---|
| Scrim 蒙层 | `color.scrim` `#000000` · `opacity-scrim (0.6)` | 全屏 |
| Dialog 容器 | `color.surface` · `radius-dialog (8dp)` · `elevation-3` | 宽 = 100% - space-12 · max 320 |
| Dialog 标题 | `title-large` (22sp / 600) · `color.on-surface` | |
| Dialog 正文 | `body-medium` · `color.text-secondary` · line-height 1.5 | |
| Dialog 操作区 | flex end · gap `space-2` | Text Button + Primary Button(40dp 高) |
| BottomSheet 容器 | `color.surface` · `radius-bottomsheet (12dp)` 仅顶部两角 · `elevation-3` | 内边距 `space-4 space-4 space-6` |
| BottomSheet handle | 32×4 · `color.outline` · `radius-full` · 居中 | |
| BottomSheet 标题 | `title-large` | |
| Toast | `color.inverse-surface` `#2E2E2E` / `color.inverse-on-surface` · `radius-md (8dp)` · `elevation-3` | 居中 · 2 秒 · max-width 280 |
| Snackbar | 同 Toast · 含 action 按钮 · 底部居中 · 4 秒 | |
| Snackbar Action | `color.inverse-primary` `#FFFFFF` · `label-large` · 反色 Text Button | |

**Compose 映射**:
- `AlertDialog` / `BasicAlertDialog`
- `ModalBottomSheet`(Material3)
- 自定义 Snackbar / Toast(Compose 没有 native Toast,需自实现或用 SnackbarHost)

---

## 6 · LIST ITEMS · `.list / .list__item`

通用:`min-height = space-14 (56dp)` · `padding-h = space-4 (16dp)` · `gap = space-3 (12dp)`

| 元素 | Token | 备注 |
|---|---|---|
| 容器 bg | `color.surface` | 外层 1dp `color.divider` border · radius 4dp |
| Item 分割线 | 1dp `color.divider` · 最后一条无 | |
| Avatar 头像 | 40×40 · `radius-full` · bg `color.surface-container` · 文字 `body-small` `font-mono` | 缩写或图标 |
| Icon 图标 | 24×24 · `color.on-surface-variant` `#4A4A4A` · stroke 1.6 | |
| 标题 | `body-large` (16sp) · `color.on-surface` | line-height 1.4 |
| 副标题 | `body-small` (12sp) · `color.text-tertiary` | |
| Chevron 箭头 | 20×20 · `color.text-tertiary` | 跳转指示 |
| Badge 数字 | min-w 16 · h 16 · `radius-full` · bg `color.error` · text `color.on-error` · `font-mono 10px` | |
| Switch 开关 | 44×24 · bg `color.surface-container-high` 关 / `color.primary` 开 · 圆把 20×20 | |

**状态**:
- `pressed` → bg `color.surface-variant`
- `disabled` → 全文字降级 `color.text-disabled` · pointer-events none
- `selected` → bg `color.surface-low` + 右侧 ✓ 图标(20×20 · `color.on-surface`)

**Compose 映射**: `ListItem(...)` · M3 内置组件

---

## 7 · TOP APP BAR

通用:`height = space-14 (56dp)` · `bg = color.surface` · 底部 1dp `color.divider`

| 变体 | 结构 | Token |
|---|---|---|
| 普通 | back icon · title · action icons | title `title-large` 22sp / 600 · `color.on-surface` |
| 搜索 Bar | back icon · 搜索框 · action icons | 搜索框 bg `color.surface-container` · `radius-pill` · 36dp 高 · 占位 `color.text-tertiary` |
| 沉浸式 | back · spacer · actions | bg `transparent` · icon `color.on-primary` `#FFFFFF` · 配渐变蒙层覆盖图片 |

**Icon Button**: 40×40 · stroke 1.8

**Compose 映射**:
- `TopAppBar` / `CenterAlignedTopAppBar` / `LargeTopAppBar`
- `SearchBar` (M3)
- 沉浸式:`TopAppBar(colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent))`

---

## 8 · BOTTOM TAB · `.tabbar / .tab`

通用:`height = 80dp` · 顶部 1dp `color.divider` · `bg = color.surface` · `padding = space-2 0 space-3`

| 状态 | 颜色 | 字重 | Icon 样式 |
|---|---|---|---|
| selected | `color.on-surface` | 600 | filled |
| idle | `color.text-tertiary` `#8A8A8A` | 500 | stroke 1.6 |
| pressed | `color.on-surface-variant` `#4A4A4A` | 500 | stroke |

**Icon**: 24×24 · **Label**: 11sp · `font-weight-medium` · letter-spacing 0.5px
**Badge**: min-w 16 · h 16 · `radius-full` · bg `color.error` · 1dp `color.surface` 描边(避免贴底色)· `font-mono 10px`
**Dot**: 8×8 · `color.error` · 1dp `color.surface` 描边 · 不带数字时用

**Compose 映射**: `NavigationBar { NavigationBarItem(selected, ...) }` · M3 内置

---

## 9 · STATE PAGES · `.state-page`

通用:`bg = color.surface` · `padding = space-16 space-6 (64dp 24dp)` · 居中 · `gap = space-4`

| 元素 | Token |
|---|---|
| Icon | 72×72 · `color.text-tertiary` 默认 · `color.on-surface` 强调态 · stroke 1.4-1.6 |
| 404 大数字 | `font-mono` 64px / 700 · `color.on-surface` · letter-spacing -0.04em |
| Title | `title-medium` (16sp / 600) · `color.on-surface` · margin-top `space-2` |
| Description | `body-small` · `color.text-tertiary` · line-height 1.5 · max-width 240 |
| Action | 主 CTA / Secondary · 40dp 高 |

**Skeleton 骨架**:
- bg = `color.surface-variant` `#F5F5F5`
- radius = `radius-xs (2dp)`
- 静态展示 · **不闪烁**(符合 no-motion 约束)· 加载完成后真实内容替换

| 类型 | Icon 风格 | 标题 |
|---|---|---|
| 9.1 骨架屏 Loading | — | (无,直接占位骨架) |
| 9.2 空状态 | 购物袋 outline | 上下文相关("购物车空空如也"等) |
| 9.3 错误 | 圆 + ! | "加载失败" |
| 9.4 网络异常 | wifi-slash | "网络连接已断开" |
| 9.5 404 | 64px 数字 "404" | "页面不存在" |

---

## 10 · LOADERS

| 类型 | Token | 备注 |
|---|---|---|
| 圆形 Loading · `.spinner` | border 2dp `currentColor` · `border-right: transparent` | 270° 弧 · 静态展示 · 实际旋转由 Compose 提供 |
| 顶部进度条 · `.loader--top` | h 2px · bg `color.surface-variant` · indicator `color.primary` (35% 宽) | M3 `LinearProgressIndicator` |
| 下拉刷新 · `.loader--pull` | spinner 20dp `color.text-tertiary` + 文字 `body-small` `color.text-tertiary` | |
| 上拉加载更多 · `.loader--more` | spinner 14dp `color.text-tertiary` + 文字 `body-small` | 触底触发 |

**Compose 映射**:
- `CircularProgressIndicator(color = primary)`
- `LinearProgressIndicator(color = primary, trackColor = surfaceVariant)`
- 下拉刷新:`PullRefreshIndicator`
- 上拉:`LazyColumn` + 自定义 footer

---

## 附录 A · 价格组件 · `.price`

| 变体 | Token | 用途 |
|---|---|---|
| 默认 | `fs-price-card (16sp / 700)` · `color.text-price` | 商品卡 / 通用 |
| `.price--display` | `fs-price-display (28sp / 700)` · letter-spacing -0.5px | 商品详情主价 |
| `.price--line-item` | `fs-price-line-item (14sp / 700)` | 订单行 / 购物车 |
| `.price__strike` | `fs-price-strike (12sp / 400)` · `color.text-price-strike` · line-through | 划线原价 |
| `.yuan` | font-size 0.65em · weight 700 | ¥ 符号缩小 |

---

## 附录 B · 关键约束清单(给后续阶段)

| 约束 | Token / 规则 | 违反后果 |
|---|---|---|
| 只浅色模式 | 不引入 dark scheme · 无 `--*-dark` 变体 | v1 推倒重来 |
| 无动效 | 不写 `transition` / `animation` · 骨架屏不闪烁 · loader 旋转由 Compose 实现 | UI 走的视觉不接受 |
| Hardcode 色值 | 必须引用 token · `_components.css` 内禁止出现 `#hex` 字面量(本文件已审) | code review 拒绝合并 |
| 卡片不加阴影 | `elevation = elevation-0` + 1dp border | 风格走样 |
| 圆角默认 4dp | `radius = radius-sm` · Dialog 8dp · Sheet 12dp · 不要 Material 默认 12dp | 风格走软 |
| 字号下限 | 11sp(`label-small`)· 不要更小 | 可读性问题 |
| 按钮高度 | 48dp 主按钮 · 40dp 紧凑按钮 · 不要更矮 | 触控不达标 |
| 强调色滥用 | `signal-500` 只用于价格 / 错误 / SALE / 角标 · **CTA 用黑** | 失去语义分工 |

---

**END · 等用户审核 · 通过后调用阶段 4 · 核心页面 mockup**
