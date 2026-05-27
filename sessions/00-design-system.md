# Session 00 · Design System(设计系统)

> **状态**:🚧 准备就绪(设计文件已到位)
>
> **品牌方向**:C · 海泡青电车感(冷峻科技) · Light mode only · no motion
> **平台**:Kotlin + Jetpack Compose + Material 3
> **目标设备**:1920×1080 横屏 / 12.3" 中控 / 观看距离 ~70cm

---

## 1. 你要做什么

把 `design/安卓商城/` 下的设计资产沉淀成**可被多个 session 复用的设计系统**:

### 1.1 车机端(carshop-android/)

创建 Android 项目骨架,只搭框架 + 设计系统,**不实现任何业务页面**:

```
carshop-android/
├── app/
│   ├── build.gradle.kts            (Compose + Material 3 依赖)
│   └── src/main/
│       ├── AndroidManifest.xml     (横屏锁定、主题)
│       └── java/com/carshop/android/
│           ├── MainActivity.kt     (空载体,只演示主题)
│           └── designsystem/
│               ├── CarshopTheme.kt        (主题入口,MaterialTheme 包装)
│               ├── tokens/
│               │   ├── Colors.kt          (从 tokens.json 翻译,语义化命名)
│               │   ├── Typography.kt      (M3 type scale + 业务字号)
│               │   ├── Spacing.kt         (object Spacing { val s0=0.dp ... })
│               │   ├── Radius.kt          (object Radius { val md=12.dp ... })
│               │   ├── Sizing.kt          (touch-target=80dp,rail=240dp 等)
│               │   └── Elevation.kt
│               └── components/
│                   ├── CarshopButton.kt        (Primary/Secondary/Text/Icon)
│                   ├── CarshopCard.kt          (主卡片容器,无阴影 + 1dp 描边)
│                   ├── CarshopListItem.kt      (80dp 高列表项)
│                   ├── CarshopChip.kt          (胶囊 chip)
│                   ├── CarshopTag.kt           (32dp 高静态 tag)
│                   ├── CarshopTopBar.kt        (80dp 高顶栏)
│                   ├── CarshopRail.kt          (240dp 宽左侧导航)
│                   ├── CarshopEmpty.kt         (空状态)
│                   ├── CarshopToast.kt
│                   ├── CarshopLoading.kt
│                   ├── CarshopDialog.kt        (radius 16dp)
│                   ├── CarshopBottomSheet.kt   (顶部 radius 20dp)
│                   ├── CarshopPrice.kt         (¥ 缩小 0.5em,自动按 productType 上色)
│                   └── CarshopQrCodeBox.kt     (480×480 二维码框,带倒计时)
└── settings.gradle.kts
```

### 1.2 后台(carshop-admin/)

只搭主题配置,不搭页面:

```
carshop-admin/
├── package.json                (React 18 + AntD 5 + Vite)
├── vite.config.ts
└── src/
    ├── main.tsx                (空骨架,ConfigProvider 包装)
    ├── App.tsx                 (一个最小演示页面,展示主题已生效)
    └── theme/
        ├── tokens.ts           (从 tokens.json 翻译过来的核心值)
        └── antdTheme.ts        (Ant Design ConfigProvider 的 token 配置)
```

> **关键决策**:后台跟车机端**不共享代码**。各自从 `tokens.json` 翻译一份。AntD 只用其默认风格 + 主色覆盖,**不做精细化定制**(后台是 Demo 内部用,不需要)。

### 1.3 文档化产物(artifacts/00-design-system.md)

必须留下:
- 完整 token 表(颜色/字号/间距/圆角/阴影 + 含 hex 值和 dp/sp 值)
- 每个组件的 API(参数、用法示例)
- 一张测试截图(车机端主题已应用、后台主题已应用)
- 已知偏离设计稿的地方(如有)

---

## 2. 你不要做什么

- ❌ 实现业务页面(首页、商品列表、详情、订单等 → 08/09 session 干)
- ❌ 接服务端 API
- ❌ 写购物车 / 优惠券 / 我的券(MVP 不做,设计稿里的这些 HTML 忽略)
- ❌ 做动画 / motion(品牌方向明确 no-motion)
- ❌ 做 Dark mode(品牌方向明确 Light only)
- ❌ 在车机端和后台之间共享代码

---

## 3. 输入(读哪些)

| 文件 | 用途 |
|---|---|
| `/Users/Admin/Documents/Projects/车机商店需求/CLAUDE.md` | session 行为规范 |
| `/Users/Admin/Documents/Projects/车机商店需求/SPEC.md` | 项目总 spec(尤其 §8 技术约束) |
| `design/安卓商城/tokens.json` | **设计 token 真理之源** |
| `design/安卓商城/components/_components-car.css` | 车机端组件视觉参考 |
| `design/安卓商城/阶段3 · 组件库.html` | 组件视觉效果(参考,不要照搬旧 v0.1 的对照表.md) |
| `design/安卓商城/阶段4 · 屏总览.html` | 屏的样子(只看视觉风格,不做页面) |

> **⚠️ 注意**:`design/安卓商城/阶段3 · 组件-tokens 对照表.md` 是 v0.1.0 旧文档(方向 A 极简黑白),**不要用**。一切以 `tokens.json` 为准。

---

## 4. 核心 token 速查(从 tokens.json 提取的关键值)

### 4.1 颜色(语义)

| Token | Hex | 用途 |
|---|---|---|
| primary | `#1A2027` | 主 CTA 底色(石墨蓝) |
| on-primary | `#FFFFFF` | 主 CTA 文字 |
| secondary | `#5D7B8A` | 次要文字 / icon(港口灰) |
| tertiary | `#00A892` | 服务券价格 / 充电语义(海泡青加深) |
| error / text-price | `#E63946` | 实物商品价格 + 错误(信号红) |
| background | `#F5F7F9` | 屏幕底色(瓷白) |
| surface | `#FFFFFF` | 卡片底色 |
| surface-variant | `#E8EDF1` | icon tile / 输入框底 |
| outline | `#D4DCE4` | 1dp 默认描边 |
| text-primary | `#1A2027` | 主文字 |
| text-secondary | `#5D7B8A` | 次文字 |
| text-tertiary | `#8FA4B1` | 占位文字 |
| text-disabled / strike | `#B8C5CF` | 禁用 / 划线原价 |
| brand-seafoam | `#00C2A8` | 品牌锚色(在售/选中/hero glow) |
| border-accent | `#00C2A8` | 选中态左侧 3dp 强调条 |

### 4.2 字号(sp)

| Token | sp | weight | 用途 |
|---|---|---|---|
| display-small | 48 | 700 | 空状态主文案 |
| headline-small | 32 | 700 | 楼层标题 |
| title-large | 28 | 700 | 页面 / Top Bar 标题 |
| title-medium | 24 | 600 | 卡片主标题 / Dialog |
| title-small | 22 | 600 | 商品名 / 列表项 |
| body-large | 20 | 400 | **主力正文** |
| body-medium | 18 | 400 | 卡片描述 |
| body-small | 16 | 400 | 辅助 / 时间戳 |
| label-large | 22 | 500 | 主按钮文字 |
| label-small | 16 | 500 | tag / 角标 |
| price-display | 44 | 700 | 详情页主价 |
| price-card | 30 | 700 | **卡片现价** |
| price-line-item | 22 | 700 | 订单行价 |
| price-strike | 16 | 400 | 划线原价 |

字体族:`Source Han Sans SC, Noto Sans SC, system-ui, sans-serif`(默认)+ `JetBrains Mono`(订单号/倒计时/车牌)

### 4.3 间距(dp)

| Token | dp | 用途 |
|---|---|---|
| s0 | 0 | 贴边 |
| s2 | 8 | 网格单元 / 最小间距 |
| s3 | 16 | **卡片间 gap** |
| s4 | 24 | **卡片内 padding / section 间距** |
| s5 | 32 | 列表 vertical |
| s6 | 40 | **页面左右边距 pad-screen-h** |
| s7 | 48 | 区块大间距 |
| s9 | 80 | 主触控目标 |

### 4.4 圆角(dp)

| Token | dp | 用途 |
|---|---|---|
| sm | 8 | 商品图 / icon tile |
| md | 12 | **主圆角(卡片 / 按钮 / 输入框)** |
| lg | 16 | Hero / Dialog |
| xl | 20 | BottomSheet(顶部) |
| full | 9999 | Chip / 头像 |

### 4.5 尺寸关键值(dp)

| Token | dp | 用途 |
|---|---|---|
| touch-target | 80 | **主触控目标(按钮/Top Bar/列表项)** |
| touch-target-compact | 64 | 紧凑触控目标 |
| height-chip | 48 | Chip |
| height-tag | 32 | 静态 Tag |
| width-rail | 240 | **左侧 Rail 导航** |
| width-content | 1680 | 主内容区(1920-240) |
| qr-code | 480 | 扫码弹窗二维码 |

### 4.6 描边 / Elevation

| 描边 token | dp |
|---|---|
| default | 1 |
| thick (focus) | 2 |
| accent (选中条) | 3 |

| Elevation | dp | 用途 |
|---|---|---|
| 0 | 0 | **卡片首选**(无阴影 + 1dp 描边) |
| 2 | 3 | Top Bar 滚动后 |
| 3 | 6 | Dialog / BottomSheet |
| 4 | 8 | 菜单 / 浮动按钮 |
| 5 | 12 | Toast / Snackbar |

---

## 5. Compose 主题骨架(给你的起点)

```kotlin
@Composable
fun CarshopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF1A2027),
            onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFF5D7B8A),
            tertiary = Color(0xFF00A892),
            error = Color(0xFFE63946),
            background = Color(0xFFF5F7F9),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFE8EDF1),
            outline = Color(0xFFD4DCE4),
            onSurface = Color(0xFF1A2027),
            onSurfaceVariant = Color(0xFF5D7B8A),
            // ... 完整映射见 tokens.json semantic 节
        ),
        typography = CarshopTypography,   // 自己写
        shapes = Shapes(
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(16.dp)
        ),
        content = content
    )
}
```

---

## 6. AntD 主题骨架(后台用)

```typescript
// src/theme/antdTheme.ts
import type { ThemeConfig } from 'antd';

export const carshopAntdTheme: ThemeConfig = {
  token: {
    colorPrimary: '#1A2027',
    colorInfo:    '#00A892',
    colorSuccess: '#00A892',
    colorError:   '#E63946',
    colorWarning: '#E63946',
    colorBgBase:  '#F5F7F9',
    colorTextBase:'#1A2027',
    borderRadius: 12,
    fontFamily:   '"Source Han Sans SC","Noto Sans SC",system-ui,sans-serif',
  },
};
```

---

## 7. 验收标准

完成这个 session 的硬性指标(必须**实际跑过**,不是"应该能跑"):

1. ✅ `carshop-android/` 能 build 成 APK
2. ✅ 装到一台横屏安卓平板/模拟器上,启动后看到一个 demo 页面,展示:
   - 一个左侧 Rail(240dp 宽,3 个示例 item,选中态有 3dp 海泡青条)
   - 一个 Top Bar(80dp 高,显示"商店"标题)
   - 主内容区有:1 张商品卡(标题 + 价格 红 / 划线 / 销量)+ 1 张服务券卡(价格绿)+ 1 个 Primary 按钮 + 1 个 Secondary 按钮 + 1 个 Chip + 1 个 Tag
   - 字号、颜色、间距、圆角全部走 token
3. ✅ `carshop-admin/` 能 `npm run dev` 跑起来,打开 localhost 看到 AntD 默认页面 + 主题色已变成石墨蓝
4. ✅ artifact 文档(`artifacts/00-design-system.md`)已完成,含 token 表 + 组件 API + 截图
5. ✅ STATUS.md 已更新状态为 ✅ 已完成

---

## 8. 依赖

- **上游**:无(地基 session)
- **下游**:06 admin-web、07 android-foundation、08 android-browse、09 android-checkout 都会引用本 session 产物

## 9. Mock 策略

本 session 不依赖任何运行时数据,纯静态产物。Demo 页面里的内容用硬编码假数据即可。

---

## 10. 已知坑 / 注意

1. **不要照搬 `阶段3 · 组件-tokens 对照表.md`**——它是 v0.1.0 老版本,跟 v1.0.0 的 tokens.json 不一致。一切以 tokens.json 为准。
2. **思源黑体不要打进 APK**——字体文件大。Noto Sans SC 作为 system-ui 在国内安卓基本都有,先用 system fallback。如果实际效果差,再考虑下载字体(在 artifact 里记录决策)。
3. **横屏锁定**:在 AndroidManifest 里 `android:screenOrientation="landscape"`。
4. **不要写 Preview Composable 滥用**:每个核心组件给一个 Preview 即可,验收用真机 / 模拟器。
5. **Rail 不要用 Material 3 的 NavigationRail**——它默认 80dp 宽不够,我们要 240dp。自己写一个 LazyColumn-based Rail。

---

## 11. 时间预估

- Android 部分:1.5~2 小时(项目骨架 + 主题 + 10 个组件)
- 后台部分:30 分钟(主题配置 + 演示页面)
- 文档:30 分钟

**总计**:2~3 小时一次 session 内完成。
