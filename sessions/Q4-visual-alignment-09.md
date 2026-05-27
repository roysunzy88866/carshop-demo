# Session Q4 · 视觉对齐 09(辅助 session)

> **Q 系列辅助 session**,不在主 12 个 session 序列里。
>
> 09 的逻辑全 ✅,但视觉没贴 `design/安卓商城/` 的 HTML 原型。Q4 的工作就是**把 09 的 UI 视觉对齐到设计原型,不动任何业务逻辑**。

---

## 1. 背景

### 1.1 为什么有 Q4

09 spec §3 输入清单**没有显式列设计 HTML 文件**,只让 09 读了 tokens.json 和 00 设计系统组件。结果 09 按 tokens + M3 Automotive 通用规范"合理发挥"了布局,**没贴原型**。

Q4 不是修 bug,是补一次"视觉对齐",让 Demo 演示效果接近设计师交付的原型。

### 1.2 原型 vs 当前实现的已知差距(你需要自己对照证实)

我**怀疑**(你要自己对照确认):
- 首页 hero 卡(`card--hero`)的位置 / 装饰 / 信息密度
- banner 区上的 badge / 装饰条 / 角标元素
- 分类入口 tile 的视觉(原型可能用 icon-tile 带特定装饰,不是普通 outlined icon)
- 推荐区域的标题排版("为你推荐" vs 原型可能叫别的)
- 商品列表卡片的内部 layout
- 详情页的左右分屏比例 / 信息层级

### 1.3 原型里有的、但 MVP 已砍掉的元素 —— **禁止还原**

设计 HTML 是早于 SPEC 砍需求做的,以下元素**禁止**加回:

- ❌ `appbar__search`(顶栏搜索框)→ ADR-003 砍搜索
- ❌ `avatar` / `who` / `plate`(头像 / 用户名 / 车牌信息)→ ADR-002 无登录
- ❌ 屏 6 购物车 → MVP 砍
- ❌ 屏 8 我的券 / 核销码 → MVP 砍
- ❌ `appbar__badge` 通知红点 / `alert-dot` → MVP 砍
- ❌ "充电中"等状态标签的具体业务逻辑(可保留视觉风格但不实现充电态)

---

## 2. 你要做什么

### 2.1 步骤(严格按此顺序)

#### Step 1 · 把设计 HTML 当作真理源,逐屏对照

**必读 4 个屏**(浏览器或 cat 文本都行,但浏览器看 layout 最准):

1. `design/安卓商城/阶段4 · 屏2 · 首页.html`
2. `design/安卓商城/阶段4 · 屏3 · 商品列表.html`
3. `design/安卓商城/阶段4 · 屏4 · 商品详情-实物.html`
4. `design/安卓商城/阶段4 · 屏5 · 商品详情-服务券.html`

**配套读**:
- `design/安卓商城/阶段3 · 组件库.html`(组件原型)
- `design/安卓商城/阶段3 · 组件-tokens 对照表.md`(组件 ↔ token 对照)
- `design/安卓商城/components/_components-car.css` + `_tokens.css`(看真实的 padding / radius / shadow 值)

#### Step 2 · 逐屏对照差距清单

对每屏,生成一份"差距清单",写到 artifact:

```markdown
### 屏 2 · 首页

| 元素 | 原型(HTML)| 当前(09 实装)| 差距 | 怎么修 |
|---|---|---|---|---|
| Hero 卡布局 | ... | ... | ... | ... |
| Banner 装饰条 | ... | ... | ... | ... |
| 分类 tile | icon + 文案 + 角标? | Material outlined icon + 文案 | ... | ... |
| 推荐区标题 | "..." | "为你推荐" | ... | ... |
```

#### Step 3 · 改 Compose UI,**只动视觉,不动逻辑**

**允许改**(只动 UI composable):
- `ui/browse/home/HomeScreen.kt`(布局、间距、装饰元素)
- `ui/browse/category/CategoryProductsScreen.kt`
- `ui/browse/detail/ProductDetailScreen.kt`
- 00 设计系统组件**有微调空间**:如果发现 CarshopCard / CarshopPrice 的视觉跟原型不符,**可以小幅调整内部 Modifier**,但**不能改对外 API**

**禁止动**(架构 / 逻辑层):
- ❌ ViewModel(`HomeViewModel.kt` 等):状态流、加载逻辑、错误处理
- ❌ Repository(`CategoryRepository.kt` 等)
- ❌ 网络层(`data/` 全部)
- ❌ Routes / NavHost 结构
- ❌ DTO 数据类
- ❌ `assets/mocks/` fixture 文件
- ❌ 用户裁决过的"轻松口吻"空态文案

**改动原则**:
- "看上去像设计稿" > "代码漂亮"
- 不引入新依赖(除非原型有需要的某个具体效果,且能写一段 TD 说明)
- 微交互保持 09 用户拍板的决策(no-motion 翻页、剩 3 行预加载、Material icon fallback)

#### Step 4 · 实跑对照 + 截图

跑 mockDebug APK 到 `carshop-tablet` 模拟器,对每屏:

- 用 `adb shell screencap -p` 截当前实装
- 在 artifact 里**并排放**:原型 HTML 截图(浏览器开,F12 调到 1920×1080 截屏)+ 当前 Android 截图
- 标注哪些已对齐、哪些主动偏离(写理由)

#### Step 5 · 跑 09 已通过的所有 User Stories 再验证一次

视觉改完后,**US-01 / US-02 / US-03 / US-24 全部重跑一遍**,确保改 UI 没破坏任何行为。

### 2.2 用户裁决的 8 个微交互 · 不变

09 用户拍板的全部保留,**不要因为视觉对齐推翻**:

1. Banner 用 chevron 手动翻页 + 1/3 页码(no auto-rotate)
2. 三段独立 skeleton
3. 不做下拉刷新,TopBar 右上加刷新 button
4. 图片占位灰底 + 失败 Material icon fallback
5. TopBar 软返回 + 系统键双兜底
6. **空态文案"轻松口吻"**(用户原文,不要改):"这里还没东西 / 去别的分类看看" / "推荐准备中" / "哎呀网络走丢了"
7. Toast 2 秒
8. 单图大图详情

### 2.3 用户已拍板的 3 个产品决策 · 不变

1. Banner 只显第 1 张 + 手动翻
2. 空态轻松口吻
3. "推荐"= GET /products 第一页前 10

---

## 3. 你不要做什么

- ❌ 不要加回 MVP 砍的功能(见 §1.3 清单)
- ❌ 不要动业务逻辑 / ViewModel / Repository / 网络层 / Navigation
- ❌ 不要改 00 设计系统组件的对外 API(可以微调内部 Modifier)
- ❌ 不要新建 Composable 文件除非真的必要(原则上只改现有文件)
- ❌ 不要改 SPEC.md / USER_STORIES.md / CHANGE_MAP.md(本 session 不改契约)
- ❌ 不要"为对齐而对齐"——如果发现某个原型设计跟车机 Automotive 规范冲突(比如触控目标 <76dp),保持 Automotive 规范,在 artifact 写"主动偏离 + 理由"
- ❌ 不要打补丁 09 的 USER_STORIES 验收(美化不算 US 通过)

---

## 4. 输入

- `CLAUDE.md` / `SPEC.md` / `STATUS.md`
- `USER_STORIES.md`(US-01/02/03/24 重跑用)
- `TECH_DEBT.md`
- **`design/安卓商城/阶段4 · 屏2-屏5` 四个 HTML**(必读)
- `design/安卓商城/阶段3 · 组件库.html` + `阶段3 · 组件-tokens 对照表.md`
- `design/安卓商城/components/_components-car.css` + `_tokens.css`
- `design/安卓商城/tokens.json`(已经用过的)
- `artifacts/00-design-system.md`(组件 API,不能破坏)
- `artifacts/09-android-browse.md`(知道当前实装 + 微交互裁决)
- `sessions/09-android-browse.md`(知道 09 的 spec 边界)
- **`carshop-android/app/src/main/java/com/carshop/android/ui/browse/`** 所有当前实装的 Composable

---

## 5. 输出 / 交付物

- 修改过的 Composable 文件(只动 ui/browse/ 下的视觉层)
- `artifacts/Q4-visual-alignment-09.md`:
  - **逐屏差距清单 + 修复方案对照**(原型 ↔ 当前 ↔ 修完)
  - 并排截图(每屏 2 张:原型 / 修后)
  - 主动偏离的清单 + 理由(为什么这一点没对齐)
  - US-01/02/03/24 重跑结果(确认行为没破)
  - 已踩坑
- `artifacts/Q4-visual-alignment-09/` 目录,存截图(`home-before.png` / `home-after.png` / `home-prototype.png` 三类)
- 更新 `STATUS.md`:加 Q4 完成
- 更新 `ORCHESTRATOR_LOG.md`:Q4 完成
- 如果发现 design HTML 用了 token 但 tokens.json 没体现(比如 hero 卡的某个特殊阴影值),登记 TD

---

## 6. 验收标准

1. ✅ 4 个屏都做了差距清单(屏 2 / 3 / 4 / 5)
2. ✅ 关键视觉元素至少对齐(Hero 区域结构 / Banner 装饰 / 分类 tile 风格 / 详情左右比例)
3. ✅ 主动偏离 ≤ 3 处,每处都有书面理由
4. ✅ US-01 / US-02 / US-03 / US-24 重跑全 ✅(行为没破)
5. ✅ 微交互 8 决策保持不变(空态文案、chevron 翻页、不做下拉刷新等)
6. ✅ MVP 砍掉的元素**没有**被加回来(搜索框、车牌、购物车等)
7. ✅ artifact 完成 + STATUS + LOG 更新
8. ✅ mockDebug APK 装到模拟器跑得通

---

## 7. 依赖

- **上游**:09 android-browse(改的是它的产物)、00 设计系统(可以微调)、design/ 设计资产
- **下游**:10 android-checkout 时,Q4 的视觉风格作为参考(10 spec 也要补设计 HTML 输入)

## 8. Mock 策略

mockDebug 模式跑 UI 验收,真模式 spot check 一次确认没影响真后端调用。

## 9. 已知坑 / 注意

1. **设计 HTML 是 web 实现,Compose 不能 1:1 复制**——网页 flex / grid 在 Compose 里要换成 Row / Column / LazyColumn / LazyGrid。**像素级 100% 一致不可能**,要的是"看上去像同一个设计语言"
2. **`components/_components-car.css` 里的精确数值**(padding / radius / shadow / 字号)是 token 的真实落地,**优先于 tokens.json 的语义命名**——遇到冲突以 CSS 为准
3. **Hero / banner 装饰元素**可能用了 CSS 渐变 / 阴影 / 装饰线条,Compose 实现可能用 `Modifier.background(Brush.linearGradient(...))` + `Modifier.drawBehind { ... }` 配合
4. **icon-tile 装饰**:如果原型用了带角标 / 装饰圈的 icon-tile,Material outlined icon 视觉差距大,需要自定义 Composable 或者用图层叠加
5. **触控目标不能为视觉牺牲**:即使原型设计的某个按钮看着 60dp 高,Automotive 规范要求 ≥ 76dp。**触控目标 wins**,视觉偏离可接受
6. **截图比对工具**:Mac 自带 ImageMagick `compare` 或 `magick montage` 可以做并排合成图(`brew install imagemagick`)
7. **不要陷入"完美主义"**:对齐度 80% 已经够 Demo 演示,剩 20% 是边缘装饰,留 TD 就行

---

## 10. 跟用户交互的点

| 阶段 | 用户做啥 | Claude Code 做啥 |
|---|---|---|
| Step 1-2 读 HTML + 列差距清单 | 等 | 浏览 HTML + cat CSS + 写差距表 |
| Step 3 改 Compose | 等 | 改文件 |
| Step 4 截图对照 | ⚠️ **你可能要帮启动浏览器**渲染设计 HTML(可选,Claude Code 也能 curl + 自己 headless 截屏) | adb screencap + 拼接对照图 |
| Step 5 跑 US | 等 | 装 APK + 操作模拟器 + 截屏 |
| 收尾 | ⚠️ **你看对照图判断对齐够不够**(主观判断) | 写 artifact |

**预计总时间**:1.5~2.5 小时
