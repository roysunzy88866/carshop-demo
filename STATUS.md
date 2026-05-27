# 车机商店项目 · 进度板

> 每个 session 结束前**必须**更新本文件。

## 项目当前阶段

# 🎉 v0.1 收山 · MVP 已交付(2026-05-27)

**人工测试通过**:前端流程 + 后端 CRUD↔前端对应。  
**已知 bug(决策不修留 v0.2)**:TD-026 下架提示 / TD-027 订单 Tab 自动刷新 / TD-028 下拉刷新 / TD-029 UI 视觉对齐遗留。

**🎬 在线 Demo**:https://appetize.io/app/b_ygi7ew6n3ugxh2ulyjnpojjlly(浏览器云端模拟器直接跑,免安装)

---

# 🎉 MVP 已交付(2026-05-26)

**所有 12 个 session 全部 ✅**。Demo 已可对外:
- 公网 API:https://carshop.hearagain.space/api/v1
- 公网后台:https://carshop-admin.hearagain.space(admin/admin123)
- APK 下载:https://carshop.hearagain.space/download/(12 MB · MD5 `066878ad...`)

**端到端跑通**:1.1.A 10 个业务场景 + US-12~25 全部 USER_STORIES(15 个)有 curl/截图/磁盘验证证据。**已知偏离**全部登记 TECH_DEBT.md 并标"上生产前必修"。详见 [artifacts/11-final-integration.md](artifacts/11-final-integration.md)。

---

**00~07 全部完成** —— 设计系统 + 服务端全套 + 运营后台 admin-web 落地 + 公网真集成。
- 00:车机端 Compose + 后台 AntD 主题骨架。
- 01:`carshop-server/` 骨架 + 5 表 + seed。
- 02 / 03 / 04:catalog / banner / order&pay 接口,8 场景端到端通过。
- 05:cookie-session 鉴权 + 图片上传,02/03/04 桩替换 zero-touch 通过。
- 06:React + AntD 后台,**Mock(MSW)+ 真后端双模式各 11/11 实跑通过**(US-08/09/10/11 + 错误场景),Playwright 自动化脚本 + 截图证据齐全。
- **07:carshop-server 上 Mac mini(`carshop.hearagain.space` · launchd 18771 · cloudflared)**,admin-web 本地真跨域调公网,1.3 表 9 场景 + US-12 / US-13 / US-17 / US-18 全 PASS。CORS / cookie env 化、VITE_API_BASE_URL 加上、01 seed 硬编码 localhost icon URL 修了。

**00~08 全部完成** —— 车机端骨架就位:网络层(Retrofit + OkHttp + Moshi + DeviceIdInterceptor + ApiResult)、8 个公开接口 Retrofit 声明、Compose Navigation + Rail-AppShell + 6 个 Coming Soon 占位、MockWebServer 模式切换两个 buildType(mockDebug 走 fixtures / realDebug 直连公网 carshop.hearagain.space),V1~V5 全 PASS。

**00~09 全部完成** —— 车机端浏览模块完成:3 段独立 skeleton 首页(banner 手动 chevron + 5 分类 Material icon fallback + 推荐 LazyRow)+ LazyVerticalGrid 3 列分类列表(剩 3 行预加载分页 · 25 条 3 页验证)+ 左图右文商品详情(下架 disabled + 文案);Mock 模式 V1-V10 + US-01/02/03/24 全过 · Real 模式真后端 happy 路径 + US-24 121 字符 spot check 通过。

**Q4 视觉对齐完成**(辅助 session)—— 改 3 个 Compose 文件(HomeScreen / BrowseCommon / ProductDetailScreen),5 处对齐:分类瓦片 56dp 底盒(加油充电 seafoam accent)/ "为你推荐" 升 headlineSmall / ProductCard 换原生 Material3 Card 全出血图片 / 详情左图 50/50 响应式分屏 / ServiceVoucherCard 装饰卡(石墨蓝背景 + seafoam 光晕 + 按 title 关键词映射图标)。ViewModel / Repository / NavHost / fixtures 全部未动;US-01/02/03/24 重跑全 ✅;主动偏离 3 处(filter chip 行 / 商户 logo / 图片轮播)记 TD-022。

**00~10 全部完成** —— 车机端结算模块落地:**OrderRepository**(沿用 09 callApi 模式)+ **OrderConfirm**(2 列:收货卡 + 商品卡 + 价格明细 + 96dp 底部 CTA)+ **PayDialog**(M3 Dialog 480dp · qrose 320dp QR · 02:00 mono 倒计时 · 点 modal 任意位置 1 秒后 mock_pay · `popUpTo(Home)` 清 confirm 层跳详情)+ **OrderDetail**(海泡青 ✓ 已支付徽章 · 订单号 mono · product_snapshot 渲染 · 应付/实付)+ **OrderList**(M3 PrimaryTabRow contentColor=tertiary 海泡青 indicator · 3 Tab 全部/已支付/待支付)。真后端 V1-V7 / US-04 / 05 / 06 / 07 / 17 / 18 全跑通,V8/V9 代码完备决策不实跑(11 final 兜底),US-18 暴露后端 paid_at race(TD-023)。

**下一步**:11 final-integration —— 兜底清单 4 条:TD-021(title 128 漂移)/ TD-023(paid_at race)/ V8 倒计时超时实跑 / V9 离线提交实跑。

## Session 状态总览(共 12 个)

| ID | 名称 | 状态 | 开始日期 | 完成日期 | 产物 |
|---|---|---|---|---|---|
| 00 | design-system | ✅ 已完成(Android build + demo 页 Q2 session 实跑通过) | 2026-05-25 | 2026-05-25 | [00-design-system.md](artifacts/00-design-system.md) |
| 01 | server-foundation | ✅ 已完成 | 2026-05-25 | 2026-05-25 | [01-server-foundation.md](artifacts/01-server-foundation.md) |
| 02 | server-catalog-api | ✅ 已完成 | 2026-05-25 | 2026-05-25 | [02-server-catalog-api.md](artifacts/02-server-catalog-api.md) |
| 03 | server-banner-api | ✅ 已完成 | 2026-05-25 | 2026-05-25 | [03-server-banner-api.md](artifacts/03-server-banner-api.md) |
| 04 | server-order-pay-api | ✅ 已完成 | 2026-05-25 | 2026-05-25 | [04-server-order-pay.md](artifacts/04-server-order-pay.md) |
| 05 | server-admin-auth-upload | ✅ 已完成 | 2026-05-25 | 2026-05-25 | [05-server-admin-auth-upload.md](artifacts/05-server-admin-auth-upload.md) |
| 06 | admin-web | ✅ 已完成(Mock + 真后端双模式 11/11 实跑通过) | 2026-05-25 | 2026-05-25 | [06-admin-web.md](artifacts/06-admin-web.md) |
| 07 | early-integration ⭐ | ✅ 已完成(公网部署 + 真跨域 + US-12/13/17/18 全 PASS) | 2026-05-26 | 2026-05-26 | [07-early-integration.md](artifacts/07-early-integration.md) |
| 08 | android-foundation | ✅ 已完成(两个 buildType + V1~V5 全 PASS) | 2026-05-26 | 2026-05-26 | [08-android-foundation.md](artifacts/08-android-foundation.md) |
| 09 | android-browse | ✅ 已完成(Mock V1-V10 + Real happy + US-24 真后端 spot check) | 2026-05-26 | 2026-05-26 | [09-android-browse.md](artifacts/09-android-browse.md) |
| 10 | android-checkout | ✅ 已完成(V1-V7 + US-04/05/06/07/17/18 真后端实跑 · V8/V9 代码完备) | 2026-05-26 | 2026-05-26 | [10-android-checkout.md](artifacts/10-android-checkout.md) |
| 11 | final-integration | ✅ 已完成(US-15~25 全跑 · 下载站上线 · APK 公网可下) | 2026-05-26 | 2026-05-26 | [11-final-integration.md](artifacts/11-final-integration.md) |
| Q2 | android-env-setup ⚙️ 辅助 | ✅ 已完成(本机 M4 装机 + demo 实跑 + 还 00 build 债) | 2026-05-26 | 2026-05-26 | [Q2-android-env-setup.md](artifacts/Q2-android-env-setup.md) |
| Q3 | admin-web-public-deploy ⚙️ 辅助 | ✅ 已完成(公网 admin 上线 + US-08/09/10/11 全 PASS) | 2026-05-26 | 2026-05-26 | [Q3-admin-web-public-deploy.md](artifacts/Q3-admin-web-public-deploy.md) |
| Q4 | visual-alignment-09 ⚙️ 辅助 | ✅ 已完成(分类图标容器 + ProductCard 全出血 + ServiceVoucherCard + US-01/02/03/24 重跑全 PASS) | 2026-05-26 | 2026-05-26 | [Q4-visual-alignment-09.md](artifacts/Q4-visual-alignment-09.md) |
| Q5 | full-ui-visual-alignment ⚙️ 辅助 | ✅ 已完成(CarshopRail 品牌+字号+颜色 / 全屏 TopBar 分隔线 / CategoryProducts 筛选图标 / ProductCard 服务券角标 / Detail CTA 高度修正) | 2026-05-27 | 2026-05-27 | [Q5-full-ui-visual-alignment.md](artifacts/Q5-full-ui-visual-alignment.md) |

**状态值**:⏳ 待开始 / 🚧 进行中 / ✅ 已完成 / ⚠️ 受阻 / 🔄 需返工

## 关键决策记录

只记跟开发相关的、影响接口/数据/技术选型的决策。

| 日期 | 决策 | 理由 |
|---|---|---|
| 2026-05-25 | 用户登录砍掉 | Demo 不需要,用设备 ID 关联订单 |
| 2026-05-25 | 商品搜索砍掉 | 用户决定,分类导航足够 |
| 2026-05-25 | 收货地址写死默认值 | 简化下单流程,后续作为独立模块 M-Address 追加 |
| 2026-05-25 | 服务端选 FastAPI + SQLite | Demo 数据量小,易部署,沿用 Mac mini 架构 |
| 2026-05-25 | 后台前端选 React + Ant Design | CRUD 后台 AntD 现成,省时间 |
| 2026-05-25 | 车机端选原生 Android (Kotlin) | 兼容性最好,避免 Flutter/RN 在车机的兼容坑 |
| 2026-05-25 | 车机端用 Jetpack Compose + Material 3 | 设计稿即 M3 风格,Compose 表达力最强;最低 API 24 |
| 2026-05-25 | 屏幕基准改为 1920×1080 横屏(不再多分辨率自适应) | 设计稿明确该尺寸,12.3" 中控主流 |
| 2026-05-25 | 导航形态:左侧 Rail 240dp + 主内容 1680dp | 设计稿要求,车机交互习惯 |
| 2026-05-25 | 采用设计稿的 5 个分类(汽车用品/加油充电/洗车保养/周边餐饮/旅行服务) | 设计稿已经按这套做,改起来无成本 |
| 2026-05-25 | 商品增加 product_type 字段:physical / service_voucher | 设计稿要求实物红价、服务券绿价 |
| 2026-05-25 | 设计系统采用品牌方向 C · 海泡青电车感(Light only,no motion) | tokens.json v1.0.0 已定 |
| 2026-05-25 | 后台跟车机端不共享代码,各自从 tokens.json 翻译 | 跨技术栈、避免耦合 |
| 2026-05-25 | 服务端公网子域名定为 `carshop.hearagain.space` | 用户拍板,直白好记,跟其他项目隔离 |
| 2026-05-25 | SPEC §8 Python 版本改为「≥ 3.11(实际跑 3.12)」 | 用户确认,3.12 完全兼容 |
| 2026-05-25 | **波次安排修正**:05 不能跟 02/03/04 并行,改为波 2.5 独占执行 | 05 spec §1.3 本质是「替换 02/03/04 的桩」,并行会冲突。修订 sessions/README.md |
| 2026-05-25 | `bcrypt` pin 到 4.2.0 | passlib 1.7.4 跟 bcrypt 5.0+ 不兼容,触发误导性错误 |
| 2026-05-25 | `GET /api/v1/health` 加入 SPEC §6.1 公开接口表 | 用户确认,探活 / 部署用,非业务接口 |
| 2026-05-26 | **质量基线升级**:加 CHANGE_MAP.md + TEST_MATRIX.md + TECH_DEBT.md + USER_STORIES US-14~25(12 个边界/安全场景)| 用户选 A,提前还测试和可维护性的债 |
| 2026-05-25 | 06 admin-web 采用真后端优先 + MSW 完整保留双模式 | MSW 兜 07 之前的开发期、给 09/10 Android `MockWebServer` 提供契约金标准;真后端模式同时跑通验证联调 |
| 2026-05-25 | 06 用 react-router v6 `createBrowserRouter`,axios `withCredentials:true`,AntD 5 `<App>` 包一层 | spec 钦定 + cookie 必须带回去 + AntD 5 message static 不读 ConfigProvider |
| 2026-05-26 | **07 服务端口定 18771**(不是 spec 写的 18767) | 18767 已被 saysay.hearagain.space 占,18765~18770 全占,顺延 |
| 2026-05-26 | **07 用 /usr/local/bin/python3.11**(不是 /opt/homebrew/python3.12) | Mac mini 是 Intel x86_64(brew prefix=/usr/local),没装 3.12;SPEC §8 ≥3.11 满足,passlib + bcrypt 4.2.0 跑得通(harmless trapped warning) |
| 2026-05-26 | **07 admin-web 走 B 方案**:加 VITE_API_BASE_URL env,本地 admin-web 真跨域调公网服务端(不走 vite proxy)| 用户反对 A 方案——A 用 vite proxy 等于"伪同源",CORS / SameSite=None Secure / cookie 跨域问题全被吃掉,07 检查站就失去意义。B 暴露真痛点,实际抓到 4 处 drift 全修(见 07 artifact) |
| 2026-05-26 | **07 把 01 seed `icon_url` 从 `http://localhost:8000/...` 改成 `/static/icons/...`** | 01 留的债:URL 硬编码,部署到公网时 icon 会指错。改成相对路径让 absolutize_url 跟 CARSHOP_BASE_URL 走。CLAUDE.md 例外条款:跨 session 改 seed 算"产物维护"。SVG 文件仍缺,记 TD-014 |
| 2026-05-26 | **07 CORS allow_origins 改 env 驱动**(`CARSHOP_CORS_ALLOW_ORIGINS`),默认 `localhost:5173` | 05 留的 `allow_origins=["*"]` + `allow_credentials=True` 在浏览器侧会被拒(规范禁止),必须显式列。生产 plist 注入 `localhost:5173,127.0.0.1:5173,https://carshop.hearagain.space` |
| 2026-05-26 | **07 上线 cookie 切 `SameSite=None; Secure`(走 env)**——TD-004 部分还清 | HTTPS 跨域必须;launchd plist 注入 `CARSHOP_COOKIE_SAMESITE=none + CARSHOP_COOKIE_SECURE=1` |
| 2026-05-26 | **Q3 admin-web 公网部署 `carshop-admin.hearagain.space`(端口 18772)** | 用户拍板子域名;沿用 panqian-tunnel + add_static_site.sh 标准模式,服务端 plist `CARSHOP_CORS_ALLOW_ORIGINS` 追加新 origin;US-08/09/10/11 全 PASS;SPA fallback 已知偏离记 TD-020 |

## 已知问题 / TODO

- [x] ~~设计文件待抓取~~ → 设计文件已在 `design/安卓商城/` 本地齐全,已读 tokens.json + 组件参考
- [ ] M-Address(收货地址管理)作为独立模块,Demo 完成后再规划
- [ ] 设计稿里有"购物车""我的券+核销码"页,**MVP 不做**,但需要用户确认是否后续追加为独立模块
- [x] ~~sessions 02~11 详细 spec~~ → 已全部写完(框架阶段完成)
- [x] ~~**Session 00 车机端 build 实跑**~~:Q2 session 2026-05-26 已完成(`./gradlew assembleDebug` 通过 · APK 15M · demo 页模拟器实跑 · 见 [00 artifact §9](artifacts/00-design-system.md#9-build-实跑验证q2-session-2026-05-26-补))
- [x] ~~SPEC §8 Python 版本~~ → 已改为「≥ 3.11(实际跑 3.12)」
- [x] ~~`GET /api/v1/health` 进 SPEC~~ → 已加入 SPEC §6.1

## 下一步

**11 final-integration**:端到端跑通全部 US-01~13 + 高优先级边界场景;兜底 10 留的 V8/V9 实跑、TD-021 / TD-023 决策。详见上方 11 兜底清单。

11 final-integration 兜底清单(累积):
- TD-021(09 发现):真后端 title 128 限制 vs SPEC US-24 "不设限" 漂移
- TD-023(10 发现):真后端 US-18 并发 mock_pay 返了 2 个不同 paid_at(差 ~1ms · 契约 OK · SPEC 漂移)
- V10(09)网络断开整页错误态实跑(代码完备)
- V5(09)空分类实跑(09 没造空 fixture)
- V8(10)PayDialog 倒计时归零超时实跑(代码完备 · 等 120 秒)
- V9(10)离线提交订单 Toast 实跑(代码完备 · 切飞行模式)

| Decision 日期 | 内容 |
|---|---|
| 2026-05-26 | **08 android-foundation 用两个 buildType `mockDebug` / `realDebug`** —— spec §1.5(USE_MOCK debug 默认 true / release 默认 false)跟 V4/V5(Mock 跑通 + 真后端跑通)矛盾,只有拆 buildType 才能两个验收都过;applicationIdSuffix `.mock` / `.real` 让两个 APK 可同时装。大总管 A 方案确认 |
| 2026-05-26 | **08 Rail 3 item:首页 / 我的订单 / 关于**(spec §1.4 原文)。"关于" 占位文案:`车机商店 Demo · v0.1 · 2026 · powered by carshop.hearagain.space`。大总管 A 方案 |
| 2026-05-26 | **08 NavHost 注册全 6 条 route + Coming Soon 占位**,09/10 进来"打开文件替换 composable 内容"即可,不动 NavHost 结构,降低跨 session 冲突面 |
| 2026-05-26 | **08 DesignSystemDemo 保留为运行时不可达 + @Preview**,使命达成不污染 App,Studio Preview 给 09/10 当"活的设计参考" |
| 2026-05-26 | **09 AppShell 加 ROUTES_WITH_OWN_TOPBAR 集合**:Home/Category/Detail 跳过默认 TopBar 让 Screen 自己画带 leading/actions 的 TopBar(spec §1.1 A.1 / B / C 三处都要求页面级 TopBar 跟 08 "Shell 统一 TopBar" 不可调和,这是唯一解) |
| 2026-05-26 | **09 banner 三种 link_type 都验证**:mock banner 1 改 link=product/1(跳详情)、banner 2 维持 link=none(不动)、banner 3 改 link=category/2(跳分类)|
| 2026-05-26 | **09 categoryId/productId 走 String → toIntOrNull**:Route 路径用 String 占位,ViewModel SavedStateHandle 转 Int,失败 throw → Navigation 回退(spec §8 坑 6 钦定) |
| 2026-05-26 | **09 发现实现 vs SPEC 漂移**:真后端 admin POST title max_length=128,但 SPEC §11.3 / US-24 / TD-015 都说"MVP 不设限" → 记 TD-021 留给 11 选(改 SPEC 或改后端 schema) |
| 2026-05-26 | **10 PayDialog 用 M3 Dialog 480dp,不用 BottomSheet**(orchestrator 拍板) | 对齐 spec §1.1.B 原文"Modal,radius 16dp,480dp 宽,内容居中";`DialogProperties(dismissOnBackPress=false, dismissOnClickOutside=false, usePlatformDefaultWidth=false)` + 整个 Surface `Modifier.clickable triggerPay`。BottomSheet 是设计稿可选优化,spec 没 promote 成硬性要求 |
| 2026-05-26 | **10 QR 用 qrose(io.github.alexzhirkevich:qrose:1.0.1)** —— 一行 `rememberQrCodePainter` 一行 `Image`,不用 ZXing 手转 Bitmap 30 行 | orchestrator 拍板;Maven Central 已验拉到,KMP 模块 Gradle Module Metadata 自动 resolve `qrose-android` AAR |
| 2026-05-26 | **10 OrderList Tab 用 M3 PrimaryTabRow(contentColor=tertiary),不用 CarshopChip 行** | orchestrator 拍板:Tab 互斥状态切换 / Chip 多选 filter;设计稿没出"我的订单"原型走 M3 标准;PrimaryTabRow 默认 indicator 颜色继承 contentColor,跨 BOM 最稳 |
| 2026-05-26 | **10 OrderConfirm/OrderDetail/OrderList 加进 ROUTES_WITH_OWN_TOPBAR** | Confirm + Detail 子页面要返回箭头(AppShell 默认 TopBar 无 leading);OrderList 顶部要 PrimaryTabRow 替代 TopBar |
| 2026-05-26 | **10 真后端 US-18 发现 paid_at race**:5 个并发 mock_pay 都 code=0(契约 OK),响应里 paid_at 出现 2 个值差 ~1ms → 记 TD-023 留给 11 修后端 service 加写锁 |

---

**最后更新**:2026-05-26(11 final 收官 · **MVP 已交付** · US-15~25 全跑 · 下载站 carshop.hearagain.space/download 上线 · APK 12MB 公网可下 · 新增 TD-024 暴力破解防护 + TD-025 价格边界 · 解决 TD-021 title 128 字符 spec 对齐)
