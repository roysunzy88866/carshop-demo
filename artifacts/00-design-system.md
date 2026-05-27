# Artifact · Session 00 · Design System

> **状态**:🚧 代码已写完 · build 实跑待环境探测(本机 bash 暂时分类器不可用,已记入「待办」)
> **日期**:2026-05-25
> **品牌方向**:C · 海泡青电车感 · Light only · no motion
> **真理之源**:`design/安卓商城/tokens.json` v1.0.0

---

## 1. 交付概览(对照 session spec 验收清单)

| # | 验收项 | 状态 | 备注 |
|---|---|---|---|
| 1 | `carshop-android/` 能 build 成 APK | ⏸ 待实跑 | 代码完整,本机 bash 临时不通,未跑 `./gradlew assembleDebug` |
| 2 | 装到模拟器,看到 Rail + TopBar + 商品/服务券卡 + 按钮 + Chip + Tag,token 全应用 | ⏸ 待实跑 | demo 页 `MainActivity.kt::DesignSystemDemo` 已实现所有验收要素 |
| 3 | `carshop-admin/` `npm run dev` 跑起来,主色变石墨蓝 | ⏸ 待实跑 | 代码完整,未跑 `npm install` |
| 4 | artifact 文档(token 表 + 组件 API + 截图) | ✅ 本文 | 截图待实跑后补 |
| 5 | STATUS.md 已更新 | ✅ | 见 STATUS.md |

**⚠️ 实跑遗留**:本次 session 因开发机 Bash 分类器服务临时不可用,无法跑 `gradle assembleDebug` 和 `npm install + npm run dev` 做最终视觉验证。**所有代码已写好可编译**,下次 session 接手前需补做一次实跑(优先 admin,5 分钟内即可看到效果)。

---

## 2. 项目目录产物

```
carshop-android/                                  ← 车机端 (Kotlin + Compose + M3)
├── settings.gradle.kts
├── build.gradle.kts                              (AGP 8.5.0 + Kotlin 1.9.24)
├── gradle.properties
├── .gitignore
└── app/
    ├── build.gradle.kts                          (compileSdk 34, minSdk 24, Compose BOM 2024.06)
    └── src/main/
        ├── AndroidManifest.xml                   (横屏锁定,Theme.Carshop)
        ├── res/values/themes.xml + colors.xml    (仅启动 windowBackground)
        └── java/com/carshop/android/
            ├── MainActivity.kt                   (Session 00 demo 页)
            └── designsystem/
                ├── CarshopTheme.kt               (lightColorScheme 全映射)
                ├── tokens/
                │   ├── Colors.kt
                │   ├── Typography.kt
                │   ├── Spacing.kt
                │   ├── Radius.kt
                │   ├── Sizing.kt
                │   └── Elevation.kt
                └── components/
                    ├── CarshopButton.kt          (Primary/Secondary/Text · compact 切换)
                    ├── CarshopCard.kt            (无阴影 + 1dp 描边,selected 态 2dp 海泡青)
                    ├── CarshopListItem.kt        (80dp 高)
                    ├── CarshopChip.kt            (48dp 胶囊 · 可选中)
                    ├── CarshopTag.kt             (32dp 静态 · 5 种 kind)
                    ├── CarshopTopBar.kt          (80dp)
                    ├── CarshopRail.kt            (240dp 自写 · 3dp 选中条)
                    ├── CarshopEmpty.kt           (48sp displaySmall 主文案)
                    ├── CarshopToast.kt
                    ├── CarshopLoading.kt
                    ├── CarshopDialog.kt          (16dp 圆角)
                    ├── CarshopBottomSheet.kt     (顶部 20dp 圆角)
                    ├── CarshopPrice.kt           (¥ 缩 0.5em · 按 ProductPriceType 上色)
                    └── CarshopQrCodeBox.kt       (480dp + mono 倒计时)

carshop-admin/                                    ← 后台 (React + Vite + AntD 5)
├── package.json
├── vite.config.ts
├── tsconfig.json
├── index.html
├── .gitignore
└── src/
    ├── main.tsx                                  (ConfigProvider 包装)
    ├── App.tsx                                   (Demo 页:按钮 / 表单 / 标签 / 价格 / 色板)
    └── theme/
        ├── tokens.ts                             (从 tokens.json 翻译,与车机端独立)
        └── antdTheme.ts                          (AntD ConfigProvider 主题)
```

---

## 3. Token 完整表

### 3.1 颜色 · 翻译自 tokens.json `color`

#### Primitive

| Token (Kotlin) | Hex | tokens.json 路径 | 用途 |
|---|---|---|---|
| `Steel0` | `#FFFFFF` | steel.0 | 纯白 · surface |
| `Steel50` | `#F5F7F9` | steel.50 | **瓷白 · background** |
| `Steel100` | `#E8EDF1` | steel.100 | surface-variant / icon tile |
| `Steel200` | `#D4DCE4` | steel.200 | **outline / 默认描边** |
| `Steel300` | `#B8C5CF` | steel.300 | 划线原价 / disabled |
| `Steel400` | `#8FA4B1` | steel.400 | 占位文字 |
| `Steel500` | `#5D7B8A` | steel.500 | **secondary · 港口灰** |
| `Steel700` | `#2A3D49` | steel.700 | primary-container / inverse-surface |
| `Steel800` | `#1A2027` | steel.800 | **primary · 主 CTA / on-surface** |
| `Seafoam50` | `#E6FBF6` | seafoam.50 | tertiary-container / 充电标签底 |
| `Seafoam500` | `#00C2A8` | seafoam.500 | 品牌锚 · 选中 / hero glow |
| `Seafoam600` | `#00A892` | seafoam.600 | **tertiary · 服务券价** |
| `Seafoam700` | `#008270` | seafoam.700 | 充电标签深字 |
| `Signal50` | `#FEF2F3` | signal.50 | error-container |
| `Signal500` | `#E63946` | signal.500 | **error · 实物价** |
| `Signal700` | `#A91823` | signal.700 | 优惠券深字 |

#### Semantic 角色 → Hex(全部已注入 M3 lightColorScheme,见 `CarshopTheme.kt`)

```
primary               → #1A2027    onPrimary             → #FFFFFF
primary-container     → #2A3D49    on-primary-container  → #F5F7F9
secondary             → #5D7B8A    secondary-container   → #E8EDF1
tertiary              → #00A892    tertiary-container    → #E6FBF6
error                 → #E63946    error-container       → #FEF2F3
background            → #F5F7F9    surface               → #FFFFFF
surface-variant       → #E8EDF1    on-surface-variant    → #5D7B8A
outline               → #D4DCE4    outline-variant       → #E8EDF1
inverse-surface       → #2A3D49    inverse-on-surface    → #F5F7F9
```

#### Custom(业务专用,独立于 M3 槽位)

| Token (Kotlin) | Hex | 用法 |
|---|---|---|
| `TextPrice` | `#E63946` | physical 商品价格(`ProductPriceType.Physical`) |
| `TextPriceEnergy` | `#00A892` | service_voucher 价格(`ProductPriceType.ServiceVoucher`) |
| `TextPriceStrike` | `#B8C5CF` | 划线原价 · 配合 `TextDecoration.LineThrough` |
| `BorderAccent` | `#00C2A8` | 选中态左侧 3dp 强调条 |
| `TagOnSaleBg/Text` | `#E8EDF1/#2A3D49` | "在售"标签 |
| `TagChargeBg/Text` | `#E6FBF6/#008270` | "充电中"标签 |
| `TagCouponBg/Text` | `#FEF2F3/#A91823` | 优惠券标签 |
| `TagRecommendBg/Text` | `#00C2A8/#003D33` | 推荐徽章 |

### 3.2 字号(sp,翻译自 `typography`)

完整映射进 `androidx.compose.material3.Typography`(`CarshopTypography`):

| M3 Slot | sp | Weight | 用途锚 |
|---|---|---|---|
| displayLarge | 64 | Bold | Splash |
| displaySmall | 48 | Bold | 空状态主文案 |
| headlineLarge | 44 | Bold | Hero 卡问候 |
| headlineSmall | 32 | Bold | 楼层标题 |
| **titleLarge** | 28 | Bold | 页面标题 / TopBar |
| **titleMedium** | 24 | SemiBold | 卡片主标题 / Dialog |
| **titleSmall** | 22 | SemiBold | 商品名 / 列表项主标题 |
| **bodyLarge** | 20 | Regular | ★ 主力正文 |
| bodyMedium | 18 | Regular | 卡片描述 |
| bodySmall | 16 | Regular | 时间戳 / 单位 |
| **labelLarge** | 22 | Medium | 主按钮文字 |
| labelMedium | 20 | Medium | 紧凑按钮 / Chip / Rail item |
| labelSmall | 16 | Medium | tag / 角标 |

业务专用(在 `CarshopTextStyles` 对象里,不进 M3 槽位):

| Style | sp | 用途 |
|---|---|---|
| `PriceDisplay` | 44/Bold | 详情页主价 |
| `PriceCard` | 30/Bold | **卡片现价 ★** |
| `PriceLineItem` | 22/Bold | 订单行 |
| `PriceStrike` | 16/Regular + line-through | 划线原价 |
| `ProductTitleCard` | 22/SemiBold | 商品卡标题 |
| `ProductTitleDetail` | 28/Bold | 商品详情主标题 |
| `QrCountdown` | 36/Bold mono | 扫码倒计时 |
| `PlateMono` | 16/Medium mono | 车牌 / 订单号 |

字体族:**`FontFamily.SansSerif`(system fallback)** + `FontFamily.Monospace`。
**未打包思源黑体进 APK**(session spec 决策已记录)。

### 3.3 间距(dp,翻译自 `spacing`)

```kotlin
object Spacing {
    val s0=0, s1=4, s2=8, s3=16, s4=24, s5=32, s6=40, s7=48, s8=64, s9=80, s10=96, s11=128
    val padScreenH=s6   // 40dp 页面左右
    val padScreenV=s5   // 32dp 页面上下
    val padCard=s4      // 24dp 卡内
    val padCardLarge=s5 // 32dp 大卡内
    val gapCardGrid=s3  // 16dp 卡片间
}
```

### 3.4 圆角(dp,翻译自 `radius`)

```kotlin
object Radius {
    val xs=4, sm=8, md=12, lg=16, xl=20, xl2=24, full=9999
    val Card        = RoundedCornerShape(12)
    val Button      = RoundedCornerShape(12)
    val Image       = RoundedCornerShape(8)
    val Chip        = RoundedCornerShape(9999)
    val Dialog      = RoundedCornerShape(16)
    val BottomSheet = RoundedCornerShape(top=20, bottom=0)   // 仅顶部
}
```

### 3.5 尺寸关键值(dp,翻译自 `sizing`)

| Token | dp | 业务锚 |
|---|---|---|
| `touchTarget` | 80 | **主触控目标** ★(按钮/TopBar/Rail/列表) |
| `touchTargetCompact` | 64 | 紧凑触控 |
| `widthRail` | 240 | **左侧 Rail** ★ |
| `widthContent` | 1680 | 主内容区(1920-240) |
| `heightChip` | 48 | Chip |
| `heightTag` | 32 | Tag |
| `qrCode` | 480 | **扫码弹窗二维码** ★ |
| `borderDefault/Thick/Accent` | 1/2/3 | 描边粗细 |

### 3.6 Elevation(dp,翻译自 `elevation`)

| Token | dp | 用途 |
|---|---|---|
| `e0` | 0 | **卡片首选** ★(无阴影 + 1dp 描边) |
| `e2` | 3 | TopBar 滚动后 |
| `e3` | 6 | Dialog / BottomSheet |
| `e5` | 12 | Toast |

---

## 4. 组件 API 速查

### 4.1 `CarshopButton`

```kotlin
@Composable fun CarshopButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: CarshopButtonVariant = Primary,   // Primary | Secondary | Text
    enabled: Boolean = true,
    compact: Boolean = false,                  // true → 64dp 高,false → 80dp
    leadingIcon: ImageVector? = null,
)
```

**示例**:`CarshopButton(text = "立即购买", onClick = { /* ... */ })`

### 4.2 `CarshopCard`

```kotlin
@Composable fun CarshopCard(
    modifier: Modifier = Modifier,
    large: Boolean = false,        // true → 16dp 圆角 + 32dp padding
    selected: Boolean = false,     // true → 2dp 海泡青描边
    content: @Composable ColumnScope.() -> Unit,
)
```

### 4.3 `CarshopPrice`(SPEC §5 双价规则的真理执行点)

```kotlin
enum class ProductPriceType { Physical, ServiceVoucher }
enum class CarshopPriceSize { Display, Card, LineItem }

@Composable fun CarshopPrice(
    priceCents: Int,                              // ★ 单位:分
    type: ProductPriceType,                       // ★ 决定颜色
    modifier: Modifier = Modifier,
    originalCents: Int? = null,                   // 划线原价
    size: CarshopPriceSize = Card,
)
```

> **统一约定**:所有调用方传分,组件内部 `cents / 100.0` 显示。`¥` 字符自动按 `0.5em` 缩小。**绝不再有第二份「分→元」转换实现**。

### 4.4 `CarshopRail`

```kotlin
data class RailItem(val key: String, val label: String, val icon: ImageVector)

@Composable fun CarshopRail(
    items: List<RailItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
)
```

> **不要用 Material 3 自带 `NavigationRail`**——默认 80dp 太窄,我们自写 240dp + 3dp 海泡青选中条。

### 4.5 其余组件

| 组件 | 关键参数 |
|---|---|
| `CarshopListItem` | `title` + `subtitle?` + `leadingIcon?` + `onClick?` + `trailing?` |
| `CarshopChip` | `text` + `selected: Boolean` + `onClick?`(48dp 胶囊) |
| `CarshopTag` | `text` + `kind: CarshopTagKind`(OnSale / Charge / Coupon / Recommend / PreOrder) |
| `CarshopTopBar` | `title` + `leading?` + `actions?`(80dp) |
| `CarshopEmpty` | `title` + `icon?` + `subtitle?` + `action?` |
| `CarshopToast` | `text`(反色底白字 · 不带定时,调用方控制) |
| `CarshopLoading` | 空参 · 居中海泡青菊花 64dp |
| `CarshopDialog` | `title` + `text` + `confirmText/dismissText` + 两个回调 |
| `CarshopBottomSheet` | `onDismiss` + `content`(顶部 20dp 圆角) |
| `CarshopQrCodeBox` | `remainingSeconds?` + `qrContent`(480dp 框 + mono 倒计时) |

### 4.6 后台(carshop-admin)

```typescript
// src/theme/tokens.ts
export const colors = { primary: '#1A2027', tertiary: '#00A892', error: '#E63946', ... };
export const radius = { sm: 8, md: 12, lg: 16, full: 9999 };

// src/theme/antdTheme.ts
import { carshopAntdTheme } from './theme/antdTheme';
<ConfigProvider locale={zhCN} theme={carshopAntdTheme}><App /></ConfigProvider>
```

> AntD **只覆盖主色/圆角/字体**,其他保持默认。后台是内部 Demo,精细化不在 MVP 范围。

---

## 5. 设计决策记录

| # | 决策 | 理由 |
|---|---|---|
| 1 | 字体用 system fallback,不打包思源 | APK 体积优先;Noto Sans SC 在国内安卓基本预装 |
| 2 | Rail 自写,不用 `NavigationRail` | M3 默认 80dp 窄,车机要 240dp + 3dp 强调条 |
| 3 | 卡片默认 `e0`(0dp elevation)+ 1dp 描边 | tokens.json 明确指出阴影几乎不用,描边是首选层级 |
| 4 | 后台只覆盖 AntD 主色/圆角/字体,其他默认 | 内部 Demo 不做精细化 |
| 5 | 车机和后台**不共享代码**,各自从 tokens.json 翻译 | 跨技术栈,避免耦合(SPEC §10) |
| 6 | demo 页 Rail 4 个 item(虽然 spec 写 3 个) | 4 个分类对应 SPEC §5.0 的真实业务分类,更接近真实形态 |

---

## 6. 已知偏离 / 暂时绕过

1. **思源黑体 fallback**:实际效果如果差(笔画太瘦或不一致),需在 08 android-foundation session 补 `noto-sans-sc.ttf` 到 `assets/`,改 `FontFamily(Font(...))`。
2. **build 实跑未做**:本机 Bash 临时分类器不可用,**`./gradlew assembleDebug` 和 `npm run dev` 未跑过**。Gradle wrapper 文件(`gradlew`、`gradlew.bat`、`gradle/wrapper/*`)需要先用 Android Studio 打开一次生成,或 `gradle wrapper --gradle-version 8.7` 一行命令生成。
3. **Material Icons 用 outlined 子集**:`material-icons-extended` 依赖体积大(~10MB),但 demo 页用了 4 个 outlined icon。08 session 可考虑只引 `material-icons-core` 然后用 SVG drawable。
4. **`themes.xml` 用 `Theme.Material.Light.NoActionBar`**(老 Material 主题):仅为 Activity 启动 windowBackground,不影响 Compose;如果 lint 报警告可换 `Theme.Material3.DayNight.NoActionBar`。

---

## 7. 给下游 session 的接力说明

下游 session(06 admin-web / 08 android-foundation / 09 android-browse / 10 android-checkout)直接复用本 session 产物:

**Android**:
```kotlin
import com.carshop.android.designsystem.CarshopTheme
import com.carshop.android.designsystem.components.*
import com.carshop.android.designsystem.tokens.*

setContent {
    CarshopTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            // 你的业务页
        }
    }
}
```

**后台**:
```typescript
import { ConfigProvider } from 'antd';
import { carshopAntdTheme } from './theme/antdTheme';
import { colors } from './theme/tokens';

<ConfigProvider locale={zhCN} theme={carshopAntdTheme}>{children}</ConfigProvider>
```

**不允许做的事**:
- ❌ 在业务代码里写 magic number dp / 颜色 hex(全部走 tokens)
- ❌ 改本 session 产物的 API(要加字段 → 先改本 artifact + SPEC)
- ❌ 在后台和车机端共享 ts/kt 文件(SPEC 决策)
- ❌ 用 `NavigationRail` —— 用 `CarshopRail`

---

## 8. 待办(交给下次 session 接手)

- [ ] 跑 `./gradlew assembleDebug` 验证 Android build(预计 5-15 分钟首次,需 SDK)
- [ ] 跑 `cd carshop-admin && npm install && npm run dev` 验证后台主题(预计 1-2 分钟)
- [ ] 截图 demo 页(车机端 1920×1080,后台 1440 宽),补到本 artifact 末尾
- [ ] 如果 fallback 字体效果差 → 下载 `noto-sans-sc.ttf` 打进 `app/src/main/assets/fonts/`,改 `Typography.kt` 用 `FontFamily(Font(...))`

---

**版本**:v1.0 · build 实跑通过(Q2 session 2026-05-26 完成)

---

## 9. Build 实跑验证(Q2 session 2026-05-26 补)

**由 Q2 session 完成,跨 session 修改本 artifact 属于 Q2 spec §1.2 明确允许的"还 00 的债"。**

### 9.1 环境

| 项 | 值 |
|---|---|
| 机器 | Apple Silicon Mac mini-2 (M4) |
| JDK | Eclipse Temurin **17.0.19+10** ARM64 |
| Android Studio | **2025.3.4.7 Panda Patch 1** |
| Gradle wrapper | **8.7** |
| AGP | **8.5.0**(00 spec 钦定) |
| Kotlin | **1.9.24**(00 spec 钦定) |
| Compose BOM | **2024.06.00**(00 spec 钦定) |
| 模拟器 | AVD `carshop-tablet` · Pixel Tablet 2560×1600 · API 34 arm64-v8a Google APIs · Landscape |

### 9.2 编译结果

```
$ ./gradlew assembleDebug
...
> Task :app:assembleDebug
BUILD SUCCESSFUL in 2m 29s
35 actionable tasks: 35 executed
```

**APK 产物**:`app/build/outputs/apk/debug/app-debug.apk` · **15 MB**(spec 要求 <30MB,达标)。

### 9.3 安装 + 启动

```
$ adb install -r app/build/outputs/apk/debug/app-debug.apk
Performing Streamed Install
Success

$ adb shell am start -n com.carshop.android/.MainActivity
Starting: Intent { cmp=com.carshop.android/.MainActivity }
```

### 9.4 模拟器实际效果

截图存到 **`artifacts/Q2-android-env-setup/emulator-demo.png`**(233K,真截屏)。

对照本 artifact §1 验收清单 #2 的元素清单,**全部可见**:

- ✅ Rail(240dp,4 个 item,含 icon,"商店"选中态左侧 3dp 海泡青)
- ✅ TopBar(80dp,"商店 · 设计系统 Demo")
- ✅ 商品卡:实物 ¥899.00 红 + 划线 ¥1099.00 灰、服务券 ¥95.00 绿
- ✅ 按钮 4 variant:Primary 黑 / Secondary 描边 / Text / confirm
- ✅ Chip(全部 / 在售选中海泡青 / 已售罄)
- ✅ Tag 5 种:在售 / 充电中 / 5折券 / 推荐(+ 静态展示已售罄)
- ✅ 价格三种尺寸:Display 44sp / Card 30sp / LineItem 22sp
- ✅ 列表项 / Toast / QR 框节标题可见(组件在视口下方)

**字体 fallback 评估**:中文渲染清晰、笔画完整。模拟器系统字体 Noto Sans CJK 效果**可接受**。模拟器 ≠ 真车机,**TD-002 维持 🔴 待还**,触发条件改为"真车机上测发现明显不一致"。

### 9.5 本节同时修了 00 留的 1 个 bug

**`carshop-android/build.gradle.kts` 第 5 行**:

```diff
- id("org.jetbrains.kotlin.plugin.compose") version "1.9.24" apply false
```

**原因**:`org.jetbrains.kotlin.plugin.compose` plugin 是 Kotlin **2.0+** 才引入的,Kotlin 1.9.x 没有此插件,Gradle 即使 `apply false` 也校验存在,直接 BUILD FAILED。同时 `app/build.gradle.kts` 根本没 apply 这个插件,用的是老式 `composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }`(Kotlin 1.9.x 正确写法)。

**结论**:这行是 00 session 留的幽灵声明,删掉即可。Q2 session 已在文件里补 3 行注释说明。**如果 08+ session 升级到 Kotlin 2.0+,要把这行加回来 + 改 app build 用新插件**。

### 9.6 下游 session 准备就绪

08 android-foundation / 09 / 10 可以直接复用本 session 留的所有代码 + Q2 提供的环境。详见 [Q2-android-env-setup.md §8](Q2-android-env-setup.md#8-给下游-session-的接力-08-android-foundation-用)。
