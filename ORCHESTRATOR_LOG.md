# Orchestrator Log · 项目总管日志

> **这是 orchestrator(项目总管 Claude)的工作记忆**。`STATUS.md` 记的是"项目状态",这里记的是**"我作为总管脑子里的临时状态 / 软决策 / 当前思考"**。
>
> 如果 orchestrator 被重启 / 上下文被压缩,读这个文件可以快速回到工作状态,不依赖对话历史。

---

## 工作节奏

- **里程碑事件**(波次完成、session 完成、关键决策)立刻在这里加一笔
- **每条不超过 5 行**,关键信息为主
- **倒序排列**(最新在上)

---

## 日志(倒序)

### 2026-05-27 · v0.1 收山 · 准备首次 commit

**用户决定 v0.1 到此为止**,**人工测试**:
- ✅ 前端流程(模拟器跑通端到端)
- ✅ 后端 CRUD 跟前端对应逻辑

**人测发现 4 个 bug,登记 TD-026~029**(用户明确说"记录,不用改"):
- TD-026:商品下架时下单失败提示不友好(Toast 一闪而过)
- TD-027:订单 Tab 进入缺少自动刷新(刚下完单看不到)
- TD-028:列表页缺下拉刷新(跟 09 微交互 #3 用户改主意冲突)
- TD-029:UI 视觉跟设计稿差距大(Q5 翻车遗留 + 用户认"操作错误导致")

**今天 orchestrator 直接修了 8 处视觉(用户清单 9 处的 8 处)**:
- AppShell.kt:Rail 从 3 项简化为 2 项【首页 / 订单】
- CarshopRail.kt:选中态改石墨蓝底 + 白文字 + 海泡青竖条 + 12dp 圆角胶囊
- HomeScreen.kt:Banner chevron 换 ChevronLeft/Right + 撤全屏分隔线
- CategoryProductsScreen.kt:撤分隔线 + 撤筛选按钮 + 标题 headlineMedium
- CarshopTopBar.kt:加 titleStyle 参数
- ProductDetailScreen.kt:撤分隔线 + 加假数据(已售/7天无理由/24h)
- OrderConfirmScreen.kt / OrderDetailScreen.kt:撤分隔线
- 剩余 Rail 宽度 / 分类瓦片 SVG 图标未动 → TD-029

**Q5 翻车反思**(给未来 orchestrator):
- mini-session spec 写得不够紧 → worker 自由发挥(把"主页对齐"做成"全屏对齐")
- worker 自创原型没有的元素(全屏 TopBar 分隔线 / 品牌区 / 筛选按钮)
- orchestrator 没在 worker 列差距清单时把关,直到用户报"很丑"才发现
- 教训:**视觉对齐 session spec 必须写"逐元素照搬用户清单,禁止加未列出的元素"硬约束**;Q4 模式正确,Q5 模式错误

**Commit 前 checklist**(2026-05-27):
- [x] root .gitignore 建好(子项目已有自己的,root 兜底 + 防御性)
- [x] TD-026~029 登记
- [x] ORCHESTRATOR_LOG 记 v0.1 收山(本条)
- [x] STATUS.md v0.1 收山节
- [ ] 用户拍板:APK release 是否纳入仓库(orchestrator 推荐:不,*.apk 已 .gitignore)
- [ ] 用户拍板:git init 在 root 单仓 / 还是分子仓(orchestrator 推荐:root 单仓)
- [ ] 用户执行:git init && git add && git commit

**v0.1 最终状态**:
- 12/12 主 sessions + Q2/Q3/Q4/Q5 完成
- 公网:carshop.hearagain.space(API + 下载站)/ carshop-admin.hearagain.space(后台)
- APK release 12 MB,debug keystore 签名,装 AVD 跑通
- 25 个 User Story · 35 个场景全跑(US-16 暴力破解预期失败记 TD-024)
- 29 条 TECH_DEBT 显式登记 + 路线图明确(TD-029 视觉遗留是最大未尽 bug)

---

### 2026-05-26 · 🎉 11 final-integration 收官 · **MVP 已交付**

**Orchestrator 独立验证**(不依赖 11 自报):
- ✅ `curl https://carshop.hearagain.space/download/` 真 200,HTML 下载页渲染正常
- ✅ APK 直链 `carshop-release.apk` 12,132,365 bytes / `content-type=application/vnd.android.package-archive` / MD5 `066878ad533554d415d8498b6a7f88c5`(本地 build 产物对得上)
- ✅ artifacts/11-final-integration.md 14KB / 10 节完整(公网入口 / 测试数据 / 1.1A 10 场景 / US-12~25 全证据 / APK 信息 / 下载站部署 / 已知问题 / 后续模块 / 工时拆分)
- ✅ STATUS.md 顶部 "🎉 MVP 已交付" / session 表格 11 ✅
- ✅ TD-024(暴力破解防护) + TD-025(价格边界)新增登记
- ✅ TD-021 解决(SPEC §5 product.title 加 max_length=128 说明)

**11 关键交付**:
- 业务流程 1.1A 10 场景全跑(curl + AVD 截图 20 张)
- USER_STORIES §六 12 个加强场景 curl 实跑 + Then 段证据
  - US-15 path traversal:UUID 重命名 + 磁盘验证无残留 ✅
  - US-16 暴力破解:错 10 次无限流 ❌ 预期失败记 TD-024
  - US-17 并发下单 5 个唯一订单号 ✅
  - US-18 并发支付 5 个 paid_at 差 < 1ms < 5ms 放宽阈值 ✅
  - US-19 删商品 + pending 订单:product_id=None + snapshot 完整 ✅
  - US-20 改价并发:total_amount 锁定下单时快照 ✅
  - US-21 quantity 边界:0/-1 → 1000 ✅
  - US-22 price 边界:price=0/INT32_MAX 接受记 TD-025
  - US-23 大分页:page=99999 返空不崩 / page_size=10000 拒 ✅
  - US-24 超长 title:1000 字符 → 1000(TD-021 对齐后)✅
  - US-25 banner SSRF:只存字符串 + lsof 验无外联 ✅
- 下载站部署:`app.mount("/download", StaticFiles(...))` + Mac mini downloads/ 目录 + launchd env `CARSHOP_DOWNLOAD_DIR` + cloudflared 走原有 `carshop.hearagain.space` 子域
- Release APK 12MB(debug keystore · USE_MOCK=false · 公网 BASE_URL · 横屏 1920×1080 · API 24+)
- APK 装 AVD 冷启动验证首页 + 真后端拉数据 ✅

**用户决策不录 demo.mp4**:adb screenrecord 在 AVD 上反复卡住,用户拍板跳过。20 张截图 + APK 公网可下 + 端到端实跑足够 demo 交付。Orchestrator 一开始误读 artifact §7.3 "用户决策跳过" 为 worker 撒谎,**经用户澄清后撤回指控** — worker 这次诚实报告,是 orchestrator 武断。

**11 踩坑 · 前任 worker 窗口被截图爆 vision context 死掉**:某张截图超 2000px,Claude Code 拒收,窗口 dead。**新窗口读文件接力**,所有进度从文件恢复 → 这正反向验证了项目"文件化记忆"设计成立(陌生 session 凭 CLAUDE.md + SPEC + STATUS + sessions/ + artifacts 就能续跑)。

**项目最终状态**:
- 12/12 主 sessions + Q2/Q3/Q4 全 ✅
- 公网:`carshop.hearagain.space/api/v1`(API)/ `carshop-admin.hearagain.space`(后台)/ `carshop.hearagain.space/download/`(APK 下载)
- 真数据:6 分类 / 14 商品 / 3 banner / 56 订单 / 1 admin
- 25 条 TECH_DEBT 显式登记,7 条按优先级排序待还
- 后续模块路线图:M-Address / 购物车 / 券核销 / 真支付 / 用户登录 / 图片处理 / DB 备份

**给未来 Orchestrator 的提示**:
- "撒谎" 这种重指控,务必先核对历史窗口/对话,**user 跟 worker 直接对话的内容 orchestrator 看不到**,不能凭 artifact 单方说法定罪
- 文件化记忆策略成立:Demo 5 hr × 12 session 跑下来,任意窗口死掉都能接力,这是 Claude Code 项目可扩展的关键

---

### 2026-05-26 · 10 android-checkout 完工 · 车机端结算模块端到端打通

**Orchestrator 独立验证**(不依赖 10 自报):
- ✅ `curl /health` 200(后端真在跑)
- ✅ US-04 真后端实跑:订单号 `O202605261703353284` 匹配 `^O[0-9]{18}$` / `status=pending` / `total=7700` / `shipping=上海市浦东新区世纪大道 100 号`
- ✅ US-04.2 跨设备查 → `code=1001 "订单不存在"`(不泄露存在性)
- ✅ US-06 幂等支付:同订单 3 次 mock_pay,paid_at 完全一致 `2026-05-26T17:04:12.628842+08:00`
- ✅ 10 张截图全在(home → category → detail → confirm → pay-dialog → paying → order-detail → list-all → list-pending-empty → restart-persist)
- ✅ TD-023 真登记 TECH_DEBT.md L305(US-18 并发 paid_at race ~1ms 抖动)
- ✅ artifact 10-android-checkout.md 完整(9 节 · 含 6 个踩坑诚实记录 · 70 分钟全程时长拆分)

**10 关键交付**(车机端结算 4 屏 + Repository):
- `OrderRepository`(沿用 09 callApi 模式)+ `ui/checkout/{confirm,detail,list}/` 三组 ViewModel+Screen
- **OrderConfirm**:2 列(收货卡 + 商品卡 + 价格明细)+ 96dp 底部 CTA + PayDialog overlay
- **PayDialog**:M3 Dialog 480dp · qrose `rememberQrCodePainter` 320dp QR · 02:00 mono 倒计时 · 点 Dialog 任意位置 1 秒后 mock_pay · `popUpTo(Home)` 清 confirm 跳详情
- **OrderDetail**:海泡青 ✓ "已支付" 徽章 + 订单号 mono + product_snapshot 渲染 + 应付/实付分行
- **OrderList**:M3 PrimaryTabRow `contentColor=tertiary` 让默认 indicator 海泡青 · 3 Tab(全部/已支付/待支付)+ 76dp Automotive 触控规范

**Orchestrator 微交互拍板 3 条**(对齐 spec):
1. PayDialog 用 M3 Dialog(不是全屏 overlay,对齐 spec §1.1.B "Modal 480dp")
2. QR 用 `io.github.alexzhirkevich:qrose:1.0.1`(KMP 一行 Composable,不用 ZXing 手转 Bitmap)
3. OrderList 用 M3 PrimaryTabRow(不是 Chip 行;Tab 适合互斥状态切换)

**踩坑 · "用户跳过 AskUserQuestion = 按推荐执行" 误判**:worker 第一轮我看到 dismiss 误以为授权,我立刻纠偏。worker 全部改回。**未来 session 提示**:`AskUserQuestion` 被 dismiss ≠ 授权按推荐走,要等明确指令或 orchestrator 拍板。

**11 final-integration 兜底清单**(由 10 + 之前累积):
1. TD-021(真后端 title 128 限制 vs SPEC 不设限 漂移)
2. TD-023(真后端并发 paid_at race ~1ms 抖动)
3. V8(倒计时归零超时实跑 · 代码完备)
4. V9(离线提交实跑 · 代码完备)
5. US-14~25 加强场景(XSS / path-traversal / 暴力破解 / 商品删除时订单完整性 / 改价瞬间下单 / quantity 边界 / 价格边界 / 分页边界 / SSRF · 全 🔴 必跑)
6. 公网 admin 跑一遍 US-08~11(Q3 留)
7. verify-stories.mjs US-11 regex 修(TD-015)

**进度**:11/12 主 sessions 完成 + Q2/Q3/Q4 全完。**剩 11 final-integration 一个**,Demo 收尾。

---

### 2026-05-26 · 新 orchestrator 接手 · Q4 验证 + STATUS 补全

**前任 orchestrator 上下文满,我接手**。读完 11 个交接文件 + sessions/ artifacts/ 全貌。

**独立验证 Q4**(用户说"完成"后核对):
- ✅ `Q4-visual-alignment-09.md` artifact 完整(5 处对齐 + 3 处主动偏离都有书面说明)
- ✅ TD-022 真登记在 TECH_DEBT.md L305
- ✅ Q4 自己加了 STATUS session 表格行 + LOG 条目(都写了)
- ⚠️ Q4 漏了 STATUS 顶部"项目当前阶段"摘要 —— 我补了

**下一步**:启动 **10 android-checkout**(订单确认 + 模拟支付弹层 + 订单详情 + 我的订单 4 个 Composable + OrderRepository)
- 10 spec §3 明确要读 design HTML(阶段4 屏7 · 订单确认+扫码支付),吸取 09 教训
- 10 启动时会触发"前端微交互"决策(支付弹层 / 倒计时 / 订单详情布局 / 列表 tab 切换),按 Q4/09 风格答(no-motion / 轻松口吻 / Material icon fallback / 触控目标 ≥76dp)
- 验收**用真后端**(订单 device_id / 状态机得真跑),不 mock

---

### 2026-05-26 · Q4 视觉对齐 09 完工

**改动范围(3 个文件,零业务逻辑)**:
- `HomeScreen.kt`:分类图标容器 56dp·accent/default 双色 + "为你推荐" headlineSmall
- `BrowseCommon.kt`:ProductCard 换原生 Card·图片全出血·22dp body padding(CarshopCard 内置 24dp 全边距是根因,换掉)
- `ProductDetailScreen.kt`:左图 weight(1f)+aspectRatio 响应式 + 新增 ServiceVoucherCard(石墨蓝背景+seafoam光晕+关键词映射图标)

**US 重跑结果**:US-01 / US-02(在售+下架+service_voucher 三变体) / US-03("没有更多了") / US-24(1000字符 5行省略不崩版) 全 PASS

**主动偏离登记**:filter chip 行(P-01)/ ServiceVoucherCard 商户 logo(P-02)/ 详情图片缩略图条(P-03) 均主动跳过 → TD-022 新登记

**下游影响**:零。10 android-checkout 可直接开始,不依赖 Q4 任何新接口或状态。

---

### 2026-05-26 · 08 android-foundation 完工 · 车机端真连公网

**Orchestrator 独立验证**:
- ✅ 2 个 APK 产出(mockDebug 17MB + realDebug 17MB)
- ✅ V5 实证:`X-Device-Id: 1505568b08432197` 从 ANDROID_ID 真注入,响应 cf-ray header 证明真过 cloudflare
- ✅ Mock 模式 count=5 / 真模式 count=6(因为后台真加了 1 分类),证明双链路区分对
- ✅ DesignSystemDemo 保留为运行时不可达 + @Preview(方案 A 落地)
- ✅ NavHost 注册全 6 routes + Coming Soon 占位(方案 A 落地)

**项目里程碑**:
- 车机端 → API 客户端 → 公网 → cloudflared → Mac mini → SQLite,**端到端打通**
- 进度 9/12,**最大风险已过**(后端架构、跨域、安卓环境、首次端到端 全部验证)
- 剩下 09/10 是写业务页(基于已经稳定的骨架),11 是收尾

**下一步**:09 android-browse(首页 + 分类列表 + 商品详情)
- 真业务页第一次出现,会触发"前端微交互"决策(loading 时长 / 空态文案 / 图片占位 / 按下反馈 等)
- 用户之前问过这个,准备 09 启动时一起拍板



### 2026-05-26 · Q3 admin-web 公网部署

**Q3 session 把 carshop-admin/ build 后部署到 Mac mini,公网 `https://carshop-admin.hearagain.space`(端口 18772)**:
- 服务端 plist `CARSHOP_CORS_ALLOW_ORIGINS` 追加新 origin → `launchctl unload && load` 重读 env → preflight 返 `access-control-allow-origin: https://carshop-admin.hearagain.space`
- `VITE_API_BASE_URL=https://carshop.hearagain.space` 烤进 dist/(1.3MB JS chunk) → rsync 到 Mac mini → 跑 add_static_site.sh 起 launchd 静态站 + 自动加 Cloudflare CNAME + cloudflared ingress
- 用户在 Mac mini 浏览器跑通 **US-08 / US-09 / US-10 / US-11**,截图齐全(Network 面板验证真跨域 + cookie 工作)

**踩坑 / 加债**:
- 用户 Air 本机科学上网 `nayout` 代理对刚加的子域 TLS 不稳,Chrome 报 `chrome-error://chromewebdata/`,命令行报 `LibreSSL SSL_ERROR_SYSCALL`。换 Mac mini 直接进可解。属于本机代理分流规则没匹配新子域,不入 TD。
- DNS 传播窗口:add_static_site.sh 收尾的"公网测试"在 DNS 传完前探,首测必 FAIL。等 30 秒~1 分钟再测就 OK。
- **新增 TD-020**:`python -m http.server` 不做 SPA fallback,直接访问 `/login` / `/products` 子路径都 404。Demo 阶段从 `/` 进可绕,正式还债换 caddy/nginx 或升级 add_static_site.sh 加 `--spa-fallback` 参数。

**下游 session 现状**:
- 11 final-integration 可以用公网 admin 演示 Demo 全流程,不依赖本地 npm run dev。建议把"公网 admin 跑一遍 US-08~11"加进 11 spec §6 必跑清单做兜底。
- 08~10 Android 不受影响(原生 HTTP 客户端不走浏览器 CORS 规则)。

**主线下一步仍是 08 android-foundation**。Q 系列辅助 session 全部清空(Q1/Q2/Q3)。

---

### 2026-05-26 · Q2 安卓装机 + 00 build 债还清

**Q2 session 在用户本机 M4 装完整安卓开发链路 + 跑通 00 design-system demo**:
- 全套版本:Temurin JDK 17.0.19 / Android Studio 2025.3.4.7 / Gradle wrapper 8.7 / AGP 8.5 / Kotlin 1.9.24 / Compose BOM 2024.06
- AVD:`carshop-tablet` Pixel Tablet 2560×1600 API 34 arm64-v8a Google APIs Landscape
- APK 15MB,模拟器装上能开,demo 页 8/9 核心元素全可见(截图存 `artifacts/Q2-android-env-setup/emulator-demo.png`)
- 总耗时 ~50 分钟(spec 估 1.5-2.5h,用户网速好 + 没遇到无法绕的坑)

**修了 00 留的 1 个 build bug**:
- 顶层 `build.gradle.kts` 第 5 行 `org.jetbrains.kotlin.plugin.compose` version "1.9.24" → **这个插件 Kotlin 2.0+ 才有,1.9 没有**,Gradle 直接报 plugin not found。app build 用的是老式 `composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }`,根本没 apply 这个插件。
- 删那一行 + 补 3 行注释。Q2 spec §1.2 明确允许这种"补 00 漏的 build 实跑"跨 session 修改。
- 用户拍板 A 方案(最小动作),不升 Kotlin 2.0。

**踩坑 / 加债**:
- 坑 1:Temurin cask 装要 sudo,后台 shell 弹不了密码 → 用户手动跑一次 brew
- 坑 2:Setup Wizard 默认装 SDK Platform 36.1,**不装 34**,要在 SDK Manager 手动勾
- 坑 3:`./gradlew` 后台执行找不到 ANDROID_HOME(zsh 快照里没) → 建 `local.properties`
- 坑 4:Studio 2025.3 Setup Wizard 不装 cmdline-tools,本地 GUI 够用,CI 不行
- TD-002 思源黑体复盘:模拟器渲染可接受,维持 🔴(等真车机)
- 新增 TD-017(local.properties per-developer)/ TD-018(cmdline-tools 缺,CI 挡)/ TD-019(老式 compose 配置,Kotlin 2.0 时还)

**下游 session 现状**:
- 08 android-foundation **环境就绪**,可以直接开干。Q2 artifact §8 给了"先做 3 件确认 + 别踩 3 个坑"的接力清单。
- 08 启动前 `cd carshop-android && ./gradlew assembleDebug` 应该 30 秒内出 APK(缓存全在)

**给未来 orchestrator**:
- 用户本机 = **M4 Apple Silicon** Mac mini-2(跟 07 部署的 Mac mini "mac" 不是同一台,那台是 **Intel x86_64**)
- 用户网速好(6GB Studio 装 ~2 分钟)
- 装机相关任何 sudo 操作要让用户手动跑(Claude Code 后台 shell 没法弹密码框)

### 2026-05-26 · 07 早集成完工 · 后端全套上公网

**Orchestrator 独立验证**(curl 公网,不依赖 07 自报):
- ✅ `https://carshop.hearagain.space/api/v1/health` 200
- ✅ 5 个分类的 icon_url 都是公网 URL(01 hardcoded localhost 债修了)
- ✅ 跨设备下单 + X-Device-Id 穿透 cloudflared 工作
- ✅ 登录 Set-Cookie 含 `SameSite=none; Secure`(TD-004 真还了)

**07 关键收获**:
- 用户的 CLAUDE.md 说 `/opt/homebrew/python3.12`,实际 Mac mini 是 **Intel x86_64**(brew prefix=/usr/local),没装 3.12。用 `/usr/local/bin/python3.11` 跑通。**未来 LOG / HANDOFF 记录:Mac mini 不是 Apple Silicon**
- 端口顺延 18771(18765-18770 全占了:panqian / aishare / saysay 等)
- 反对 07 自己推荐的 A 方案(vite proxy 伪同源),改用 B(真跨域 VITE_API_BASE_URL),抓到 4 处 drift 全修
- 新增 3 条债:TD-014(SVG 缺)、TD-015(verify-stories regex)、TD-016(logout 缺 Secure)

**下一步**:
- 08 android-foundation 起 Kotlin 项目
- 取决于 Q2 安卓环境装机进度(用户应该并行开了)

### 2026-05-26 · 规范升级影响审计 + 11 session 兜底登记

**用户提出**:今天加了 8 个新规范,旧 sessions(01-06)是否需要回溯配合?

**审计结果**:
- GLOSSARY 一致性 grep 扫描:**零不一致**(品类 / 购物车 / 搜索 / base64 / 用户登录 / ANDROID_ID / 元 / snapshot / device_id 全部检查,旧代码完全干净)
- USER_STORIES US-14~25(新加 12 个边界 / 安全场景):旧 sessions 没跑过,但都在 §六索引表里跟未来 session 关联,加上 11 必跑,**有 3 层兜底**

**落地动作**:
- `sessions/11-final-integration.md` §1.1 拆 A/B/C 三节,**B 段显式列 US-14~25 必跑**
- `CLAUDE.md` 加铁律 9:规范升级默认 Forward-only,不回溯旧 session

**给未来 orchestrator 的提示**:任何"规范升级"按这套套路:
1. 加 ADR 在 DECISIONS
2. GLOSSARY 一致性 grep 一次
3. 影响兜底进 11 session spec
4. 评估是否要回溯(默认不回溯,除非安全 / 合规)

### 2026-05-26 · 文档规范全集 + 交接文件落盘

**用户反思 + 主动决策**:
- 问"AI 项目该有的规范文档是啥,你为啥不主动列",指出我有"讨好"成分
- 用户认知:接受了"基础三件套"必要性,但拒绝过度工程

**今天落盘**:
- `README.md`(项目入口,给人类)
- `GLOSSARY.md`(35+ 术语统一)
- `DECISIONS.md`(12 个 ADR,把 STATUS 决策表升级成可读 ADR 格式)
- `sessions/Q1-docs-standards-audit.md`(辅助 session spec,用户会开新窗口跑)
- `ORCHESTRATOR_HANDOFF.md`(继任者读这个 5 分钟接手,不依赖对话)
- `CLAUDE.md` 启动协议拆"必读"和"按需查阅"

**用户接下来要做**:
1. 开新 Claude Code 窗口跑 Q1 session(补 ARCHITECTURE / CHANGELOG / SECURITY 等剩余文档)
2. 或者直接跳到 07 早集成

**给继任 orchestrator 的提示**:
- 任何"我为啥不主动提"的觉察,记下来,**下次主动**
- AI 助手默认倾向响应,要刻意逆向(给用户出标准菜单)

### 2026-05-26 · 质量基线升级(用户选 A 方案)

**用户提出 3 个问题**:
1. 测试用例太少 → 加 TEST_MATRIX.md + USER_STORIES 加 12 个边界/安全场景(US-14~25)
2. 改东西要读全文档 → 加 CHANGE_MAP.md(实体→引用文件地图)
3. 技术债务清单 → 加 TECH_DEBT.md(已含 13 条债务)

**今天落盘**:
- `TECH_DEBT.md`(13 条债务)
- `CHANGE_MAP.md`(11 大实体 + 7 跨切关注 + 改动场景速查)
- `TEST_MATRIX.md`(150 格覆盖矩阵 + 12 优先补的场景)
- `USER_STORIES.md` 加节六(US-14~25 共 12 个故事)
- `CLAUDE.md` 启动协议扩到 10 步,加铁律 8(技术债登记)

**影响**:
- 07 之后每个 session 启动多读 2~3 个新文件,但有 CHANGE_MAP 后**改东西更快**
- 测试场景翻一倍,session 多花 30% 时间但质量硬
- 任何"妥协"必须立刻进 TECH_DEBT,不能藏

**下一步不变**:07 早集成可以启动了。

### 2026-05-25 20:55 · 06 admin-web 完工 · 准备 07 早集成

**06 交付质量极高**:
- Playwright 自动化:Mock 11/11 + 真后端 11/11 全过
- 我独立验证 DB:产品表 13 条(12 seed + 1 真模式新建)、uploads 3 张真图
- 4 个踩坑诚实记录:Service Worker 状态不跨刷新、vite 多进程占端口、AntD InputNumber 选择器、Card bordered 弃用
- **关键发现**:06 自己在跑 Playwright 时误把 mock 当 real,**通过 sqlite3 物证发现并修复**——这是真"测试为真"的体现,正是我们引入 USER_STORIES 想要的效果

**当前状态**:
- 后端完工(02-05) + 后台 Web 完工(06),本地端到端打通
- DB:5 分类 / 13 商品(含 06 测试单) / 3 banner / 1 admin / 3 orders / 3 上传图
- carshop-admin/ Mock 模式 + 真模式都能跑(`npm run dev:mock` / `dev:real`)

**下一步:07 早集成**(检查站,串行,独占):
- 部署 carshop-server 到 Mac mini,公网 `carshop.hearagain.space`
- 沿用用户 CLAUDE.md 的 add_static_site.sh / launchd / cloudflared 模式
- 端口选 18767(顺延 panqian 18765 / aishare 18766)
- 在公网真域名下重跑 US-08~11 + 跑端到端 US-12 / US-13
- **暴露所有契约漂移** → 修 SPEC + 更新 fixtures

### 2026-05-25 19:35 · 06 admin-web 启动

**用户开了 06 窗口**,回放严谨,5 个问题我全答了:
- 真后端优先,MSW 完整保留
- App.tsx 整体替换
- createBrowserRouter(按 spec)
- 截图用 Playwright(自动化,11 可复用)
- 不部署,07 处理

**预计**:2~3 小时

**等 06 完成后我要做**:
- 自己跑一遍 US-08/09/10/11 真后端验证(独立于 06 自报)
- 检查 admin-web 跟真后端打通(尤其 cookie 跨域)
- 看 Playwright 截图质量

### 2026-05-25 19:25 · 波 2.5 完工 · 后端全部就绪 · 准备 06 admin-web

**05 表现**:用户在我贴新规则(User Stories)之前 05 就跑完了。我自己事后跑了 US-08(3 场景)+ US-09(3 场景)全部通过。05 artifact 6 条验收 + 2 条额外验收已自记录。

**当前状态**:
- 整个后端竣工:5 张表 + 30+ 条路由 + 真鉴权 + 图片上传 + 12 商品 seed
- 02/03/04 zero-touch 替换为真鉴权:无 cookie 401、带 cookie 200,全部通过
- DB 现状:5 分类 / 12 商品 / 3 banner / 1 admin / 3 测试 orders / 2 admin_sessions
- `app/main.py` 路由全挂、`app/deps.py` require_admin 是真实现(从桩升级)

**给下游 session 的提示**:
- 06 admin-web 可以直接连真后端开干(`http://localhost:8000/api/v1`)
- 也可以走 fixtures(`artifacts/fixtures/02~05/` 共 30+ JSON)做 MSW mock
- 推荐两种模式都支持:`VITE_USE_MOCK=true` 走 mock,默认走真后端

**下一步**:
- 用户开 06 admin-web window,贴启动模板。该 session 涉及 US-08/09/10/11 四个故事
- 06 完工后,启动 07 早集成(部署到 Mac mini carshop.hearagain.space)

**Orchestrator 自检**(我刚做了的事):
- ✅ 跑了 US-04 完整下单流程
- ✅ 跑了 US-08 三个场景
- ✅ 跑了 US-09 三个场景
- ✅ 确认 require_admin 是真实现,02/03/04 后台接口需要 cookie
- ✅ DB 数据健全

### 2026-05-25 19:10 · 波 2 完工 · 准备启动 05

**当前状态**:
- 波 2(02 catalog / 03 banner / 04 order-pay)全部完成
- 我用 curl 跑过 8 个端到端场景全过(见上方对话/STATUS)
- `app/main.py` 30 条路由全挂、`app/deps.py` 只有 1 份 `require_admin` 桩
- 05 admin-auth-upload 窗口在等"开始"

**下一步**:
- 用户选了"档位 B":写 USER_STORIES.md 严肃化测试
- 落完档位 B 后,05 启动,跑完后要按相关 User Story 验证(而不是只 curl)

**软决策**(对话里说过、但 SPEC/STATUS 没记的):
- 后台 admin-web 在 Demo 阶段**只在本地跑**,不上 Mac mini 公网(07 早集成时可改)
- cookie 配 `SameSite=Lax` + 非 Secure(本地 dev),settings.py 提供 env 切换
- session 用 `uuid4().hex` 存 DB,不签名不加密

**等用户的待办**:
- 05 启动后跑通其 User Story,然后波 3(06 admin-web)

### 2026-05-25 18:30 · 引入 USER_STORIES + 严肃测试

**为什么**:用户问"测试是真测试吗",我承认现在是 curl 冒烟,不是真业务验证。引入 USER_STORIES.md 让每个 session 完成前必须跑通自己的 story,不只是 curl 通。

### 2026-05-25 18:00 · 波 2 启动

**用户开了 3 个 Claude Code 窗口同时跑 02/03/04**(原本设计是 4 个含 05,后修正:05 不能并行,因为它的工作是替换 02/03/04 的桩)。

**用户拍板的关键决策**(转记到 STATUS.md):
- 子域名 `carshop.hearagain.space`
- Python ≥3.11(实跑 3.12)
- `/api/v1/health` 进 SPEC §6.1
- 订单号 4 位用随机数,不用序号

### 2026-05-25 17:00 · 波 1 完工

- 00 design-system:车机端 Compose + 后台 AntD,用户实跑 `npm run dev` 验证主题对了
- 01 server-foundation:FastAPI + SQLite,seed 5 分类 + 1 admin + 3 banner + 12 商品

### 2026-05-25 16:00 · 框架搭完

写完 12 个 session spec + CLAUDE.md + SPEC.md + STATUS.md。用户选了"D 方案":Mock 走真契约 + 早集成 + 契约校验。

---

## Orchestrator 自己的 SOP

### 收到 session 完成的信号时
1. **不要**只看 session 自己说"完成了" → **去读项目文件**
2. 读对应 artifact、跑 curl 验证、看 STATUS
3. 跑相关 User Story 才算"真完成"(见 USER_STORIES.md)
4. 完成后立刻在本文件加一笔

### 多 session 并行时
- 主动盯 `app/main.py` / `app/deps.py` 这些共享文件的合并
- 任何冲突 / 漏挂 / 重复定义都是 orchestrator 的活
- 不让用户处理代码层冲突

### 上下文压缩前的兜底
- 重要的临时决策、当前阶段、下一步都进本文件
- STATUS.md 是项目状态、ORCHESTRATOR_LOG.md 是 orchestrator 自己的"备忘录"

---

## 项目当前阶段速查(写给被重启的我)

- **波**:波 2 已完成,波 2.5(05)即将启动
- **后端**:5 张表 + 30 条路由(02 分类+商品 / 03 banner / 04 订单+支付)全部就绪,无登录鉴权
- **05 任务**:把 `app/deps.py` 的 `require_admin` 从桩换成真 cookie session 校验,加 `POST /api/v1/admin/login` 和 `/upload`
- **下一波**:06 admin-web(React + AntD,消费 05 真鉴权)
- **再下一波**:07 早集成,部署到 Mac mini `carshop.hearagain.space`

---

**最后更新**:2026-05-26(Q2 安卓装机完工)
