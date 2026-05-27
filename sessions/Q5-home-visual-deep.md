# Session Q5 · 主页视觉深度对齐(辅助 mini-session)

> **Q 系列辅助 session**,不在主 12 个 session 序列里。
>
> Q4 视觉对齐已经改过主页 2 处(分类瓦片 + "为你推荐" 字号),但用户实际跑下来**主页仍然觉得丑**。Q5 的工作就是**对主页一屏做深度视觉对齐**,把 Q4 没动到的部分全部贴齐设计原型。

---

## 1. 背景 · 这是什么

### 1.1 用户原话

> "OK,C(超聚焦 mini-session),主页最丑"

意思:用户跑了 release APK 看了主页,觉得视觉跟设计稿差距大。Q4 没对齐够。

### 1.2 工作边界(铁律)

- ✅ **只动一屏一个文件**:`carshop-android/app/src/main/java/com/carshop/android/ui/browse/home/HomeScreen.kt`
- ✅ 可以微调 `ui/browse/BrowseCommon.kt` 里的 ProductCard(如果推荐 LazyRow 用了它)
- ✅ 可以微调 00 设计系统组件**内部 Modifier**(不改对外 API)
- ❌ **不动**:`HomeViewModel.kt`、`Repos.kt`、NavHost、Routes、DTO、任何业务逻辑
- ❌ **不动**:其他屏(Category / Detail / Confirm / Pay / OrderDetail / OrderList)
- ❌ **不还原** MVP 砍掉的元素(搜索 / 登录 / 头像 / 购物车 / 通知红点 等,见 Q4 spec §1.3)
- ❌ **不回滚** Q4 已对齐的两处(分类瓦片 56dp 底盒 / "为你推荐" headlineSmall)

### 1.3 Q4 已对齐 vs Q5 该深化

| 元素 | Q4 状态 | Q5 是否动 |
|---|---|---|
| 分类瓦片图标容器(56dp 底盒 + accent) | ✅ Q4 已对 | ❌ 保持 |
| "为你推荐" 字号(headlineSmall) | ✅ Q4 已对 | ❌ 保持 |
| Banner 区(图片 / 装饰条 / 翻页 chevron / 页码) | ❓ 未深化 | **✅ 必看** |
| Hero 卡 / Banner 旁边的装饰元素 | ❓ 未深化 | **✅ 必看** |
| 分类瓦片整体布局(横排间距 / 容器 padding / 整体节奏) | ❓ 未深化 | **✅ 必看** |
| 分类瓦片下方文字(字号 / 颜色 / 间距) | ❓ 未深化 | **✅ 必看** |
| 推荐 LazyRow 卡片(尺寸 / 间距 / 整体节奏 / 阴影) | ❓ 未深化 | **✅ 必看** |
| 顶部 AppBar / 标题栏 | ❓ 未深化 | **✅ 必看**(注意:搜索框 / 头像 / 通知红点禁止还原) |
| 整页背景 / 大区块间距 / 分割线 | ❓ 未深化 | **✅ 必看** |

---

## 2. 你要做什么

### 2.1 步骤(严格按此顺序)

#### Step 1 · 当真理源,逐元素比对

**必读输入**(全部):
1. `design/安卓商城/阶段4 · 屏2 · 首页.html`(浏览器打开 + cat 文本,**两者都要**)
2. `design/安卓商城/components/_components-car.css`(精确数值,优先于 tokens.json 语义)
3. `design/安卓商城/components/_tokens.css`(token 值)
4. `design/安卓商城/tokens.json`(语义命名)
5. `design/安卓商城/阶段3 · 组件库.html`(组件原型,看 banner / category-tile / product-card 的精确视觉)
6. `design/安卓商城/阶段3 · 组件-tokens 对照表.md`(组件↔token 映射)
7. **当前 Compose**:`carshop-android/app/src/main/java/com/carshop/android/ui/browse/home/HomeScreen.kt`(完整读)
8. **依赖组件**:`ui/browse/BrowseCommon.kt`(ProductCard 等)
9. **设计系统组件**:`designsystem/components/*.kt`(CarshopCard / CarshopTopBar 等,看现有 API)
10. **Q4 artifact**:`artifacts/Q4-visual-alignment-09.md`(知道 Q4 改了什么,不要回滚)

#### Step 2 · 列差距清单(写到 artifact)

按"原型 vs 当前 vs 差距 vs 修法"四列表格,逐元素列。

```markdown
| 元素 | 原型(HTML)| 当前(Compose) | 差距 | 修法 |
|---|---|---|---|---|
| AppBar 标题 | ... | ... | ... | ... |
| Banner 容器圆角 / 阴影 | ... | ... | ... | ... |
| Banner 图片尺寸 / aspect ratio | ... | ... | ... | ... |
| Banner chevron 大小 / 位置 | ... | ... | ... | ... |
| Banner 页码 1/3 样式 | ... | ... | ... | ... |
| 分类区上下间距 | ... | ... | ... | ... |
| 分类瓦片下方文字 | ... | ... | ... | ... |
| "为你推荐" 区域 padding | ... | ... | ... | ... |
| 推荐卡片宽度 / 高度 | ... | ... | ... | ... |
| 推荐卡片之间间距 | ... | ... | ... | ... |
| 整页背景色 | ... | ... | ... | ... |
| 大区块之间间距(banner / 分类 / 推荐) | ... | ... | ... | ... |
| ... | ... | ... | ... | ... |
```

**没看到原型有的元素 / 看到 MVP 砍掉的元素**:在差距清单里写 "❌ MVP 砍 / 不还原"(防止误加回搜索框等)。

#### Step 3 · 改 HomeScreen.kt

按差距清单逐条改。原则:

- **优先级**:整体布局节奏 / 容器尺寸 / 间距 > 颜色微调 > 装饰元素细节
- **修不到的细节**:写到 artifact §"主动偏离" + 写理由(比如"原型有 SVG 渐变背景,Compose 实现需引入新依赖,留 TD 不做")
- **不引入新依赖**(除非真的必要,引入要写 TD)
- **触控目标 ≥ 76dp**:即使原型按钮看起来 60dp,Automotive 规范优先,视觉偏离可接受

#### Step 4 · 实跑对照 + 截图

```bash
cd carshop-android
./gradlew assembleMockDebug          # 用 mockDebug 跑,不依赖真后端
adb install -r app/build/outputs/apk/mockDebug/app-mockDebug.apk
adb shell am start -n com.carshop.android.mock/com.carshop.android.MainActivity
adb shell screencap -p /sdcard/home_after.png
adb pull /sdcard/home_after.png artifacts/Q5-home-visual-deep/
```

**截图陷阱**(11 worker 死因):截图必须 ≤ 2000px。AVD 是 1920×1080 安全。如果用 macOS 自带 screencapture(Retina × 2 = 3840px)→ 必须 `sips -Z 1920 input.png --out output.png` 缩。

对应的"原型截图":Mac 上用浏览器打开 `阶段4 · 屏2 · 首页.html`,F12 设 viewport 到 1920×1080,然后浏览器自带截图工具或 macOS `Cmd+Shift+5` 截窗口 → `sips -Z 1920` 缩。

**至少 3 张截图**:
- `home-before.png`(改之前,Q4 状态)
- `home-after.png`(Q5 改完)
- `home-prototype.png`(浏览器渲染的原型)

#### Step 5 · 跑 US-01 回归

视觉改完后,跑 US-01 一遍(首页正常显示 + 5 分类 + 推荐 ≥10 个 + banner 3 张),确认行为没破。简单的 mockDebug 跑一遍就好,不需要 real 模式。

### 2.2 不变量(继承 09/Q4)

以下决策不要因为视觉对齐推翻:

1. Banner chevron 手动翻页 + 1/3 页码(no auto-rotate)
2. 三段独立 skeleton
3. 不做下拉刷新(TopBar 右上是刷新按钮)
4. 图片占位灰底 + 失败 Material icon fallback
5. 空态文案轻松口吻("这里还没东西" / "推荐准备中")
6. **no motion**(车机减少分心)

---

## 3. 输入

- `CLAUDE.md` / `SPEC.md` / `STATUS.md`
- `USER_STORIES.md`(US-01 回归用)
- `TECH_DEBT.md`
- `design/安卓商城/阶段4 · 屏2 · 首页.html`(必读)
- `design/安卓商城/阶段3 · 组件库.html` + `components/_components-car.css` + `_tokens.css` + `tokens.json`
- `artifacts/00-design-system.md`(组件 API)
- `artifacts/Q4-visual-alignment-09.md`(知道 Q4 改了什么)
- `carshop-android/app/src/main/java/com/carshop/android/ui/browse/home/HomeScreen.kt`
- `carshop-android/app/src/main/java/com/carshop/android/ui/browse/BrowseCommon.kt`

---

## 4. 输出

- 改过的 `HomeScreen.kt`(可能加微调 `BrowseCommon.kt`)
- `artifacts/Q5-home-visual-deep.md`:
  - 差距清单表
  - 改完的截图 + 原型截图 + Q4 状态截图(三张并列)
  - 主动偏离清单 + 理由
  - US-01 回归结果
  - 踩坑
- `artifacts/Q5-home-visual-deep/` 目录,存截图
- 更新 `STATUS.md`(加 Q5 行)
- 更新 `ORCHESTRATOR_LOG.md`(Q5 完工)
- 发现 token / 组件不完整登记 TD

---

## 5. 验收

1. ✅ 差距清单覆盖至少 8 个主要元素
2. ✅ Banner 区 / 推荐 LazyRow / 整体节奏三个大件都做了视觉调整(不只动微小细节)
3. ✅ 三张截图齐(before / after / prototype)且 ≤ 2000px
4. ✅ 主动偏离 ≤ 5 处,每处有书面理由
5. ✅ US-01 mockDebug 回归通过
6. ✅ Q4 已对齐的两处(分类瓦片 + "为你推荐" 字号)**没有被回滚**
7. ✅ MVP 砍掉的元素(搜索 / 头像 / 通知红点)**没有被加回来**
8. ✅ artifact + STATUS + LOG 更新

---

## 6. 依赖

- **上游**:00 设计系统 / 09 android-browse / Q4 视觉对齐(改的是它们的产物)
- **下游**:无(本 session 只动一屏,不影响其他)

## 7. Mock 策略

mockDebug 跑 UI 验收(不依赖真后端)。一切验证只看视觉,不需要真后端数据。

## 8. 已知坑

1. **HTML ≠ Compose 1:1**:网页 flex/grid 在 Compose 是 Row/Column/LazyGrid,渐变/阴影/装饰元素要重写。**对齐 80% 已经够**,剩 20% 留 TD
2. **CSS 精确数值 > tokens.json 语义命名**:遇到冲突以 CSS 为准
3. **触控目标 wins**:Automotive ≥ 76dp,视觉偏离可接受
4. **截图 ≤ 2000px**:必要时 `sips -Z 1920` 缩,11 worker 因为这个死过窗口
5. **不要陷入完美主义**:用户说"主页丑",目标是 "看上去没那么丑了",不是"逐像素对齐"
6. **预计耗时**:1~2 小时(单屏,深度对齐)

---

## 9. 跟用户的交互点

| 阶段 | 用户做啥 | worker 做啥 |
|---|---|---|
| Step 1-2 读 HTML + 列差距清单 | 等 | 浏览 HTML + cat CSS + 写差距表 |
| Step 2 列差距清单完 | **看差距清单,确认重点**(可选,可不停) | 等用户拍 / 或自决继续 |
| Step 3 改 Compose | 等 | 改 HomeScreen.kt |
| Step 4 截图对照 | **看对照图判断够不够**(主观) | adb screencap + 拼对照图 |
| Step 5 跑 US-01 | 等 | mockDebug 装 APK + 看首页正常 |
| 收尾 | **决定接受 or 继续迭代** | 写 artifact |

**预计总时间**:1.5~2 小时

---

**版本**:v1.0
**创建日期**:2026-05-26
