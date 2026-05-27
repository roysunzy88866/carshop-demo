# Tech Debt · 技术债务清单

> **每条债务必须有**:类别 / 描述 / 影响等级 / 为啥这么干 / 触发还债条件 / 工作量预估 / 状态
>
> **状态**:🟢 已解决 / 🟡 计划中 / 🔴 待还 / ⚪ 接受不还(决策不还)

---

## 维护规则

- 任何 session 发现"为了赶进度做妥协",**立刻**在这里加一条
- artifact 里"已知偏离 / 暂时绕过"那种内容,**同步抄一份**进来
- 每次发版前 review 这份清单,看是否有高影响等级的债务必须还
- ⚪ 标记的是"决策不还"(比如 Demo 永远不上生产),要写清理由

---

## 影响等级

- **高**:不还会影响安全 / 数据完整性 / 关键路径可用
- **中**:不还会影响用户体验 / 维护成本 / 扩展性
- **低**:不还也不影响主要功能,纯洁癖

---

## 债务清单

### TD-001 · bcrypt 版本锁死 4.2.0

- **类别**:依赖管理
- **状态**:🔴 待还
- **影响**:低
- **描述**:`requirements.txt` 把 `bcrypt` pin 到 4.2.0,因为 passlib 1.7.4 跟 bcrypt 5.0+ 不兼容,触发误导性错误
- **为啥这么干**:01 session 踩坑,临时 pin 解决
- **触发还债**:passlib 2.x 出 + 兼容 bcrypt 5+,或者迁移到原生 bcrypt 包不用 passlib
- **预估**:1 小时
- **登记日期**:2026-05-25(01 session)

---

### TD-002 · 思源黑体未打包进 APK · 用 system fallback

- **类别**:UX / 视觉一致性
- **状态**:🔴 待还
- **影响**:中(可能影响视觉一致性,尤其老车机系统字体差异大)
- **描述**:车机端 `Typography.kt` 用 `FontFamily.SansSerif`,没下载思源黑体打进 `app/src/main/assets/fonts/`
- **为啥这么干**:00 session 决策为 APK 体积优先,国内 Android 基本预装 Noto Sans SC
- **触发还债**:**真车机**上测,如果文字渲染明显不一致(笔画太瘦 / 字号不准),立刻还
- **预估**:30 分钟(下载 + 改 1 个 import)
- **登记日期**:2026-05-25(00 session)
- **Q2 复盘(2026-05-26)**:Pixel Tablet API 34 模拟器实测 Noto Sans CJK 渲染清晰、笔画完整、字号一致,**模拟器层面可接受**。但模拟器 ≠ 真车机,**维持 🔴 待还**,触发条件改为"真车机上测发现明显不一致时"。

---

### TD-003 · Session ID 不签名不加密

- **类别**:安全
- **状态**:⚪ 接受不还(Demo)
- **影响**:中(生产环境是高,Demo 接受)
- **描述**:`uuid4().hex` 直接存 DB,cookie 里也是原文,没 HMAC 签名也没加密
- **为啥这么干**:Demo,简化实现。攻击面是"DB 被偷 = 谁拿到都能登"
- **触发还债**:**任何上生产前**必须改。用 itsdangerous 或 fastapi-users
- **预估**:2 小时
- **登记日期**:2026-05-25(05 session)

---

### TD-004 · Cookie 跨域 / HTTPS 配置硬编码

- **类别**:可移植性
- **状态**:🔴 待还(07 session 部分还)
- **影响**:中
- **描述**:`SameSite=Lax` + `Secure=False` 硬编码,生产 HTTPS 跨域需要 `SameSite=None; Secure`
- **为啥这么干**:Demo 本地 dev 简化
- **触发还债**:07 早集成时 settings.py 加 env 切换;部署到生产时改 env
- **预估**:30 分钟
- **登记日期**:2026-05-25(05 session)

---

### TD-005 · 上传不去重,同文件多次上传 = 多个副本

- **类别**:存储
- **状态**:🔴 待还
- **影响**:低
- **描述**:`/admin/upload` 用 `uuid4().hex.ext` 作文件名,同一张图传 N 次 = N 个文件占空间
- **为啥这么干**:Demo,实现简单
- **触发还债**:上线后磁盘占用看趋势,或者运营反馈"同一张图我又传了"
- **预估**:2 小时(SHA-256 文件指纹 + 去重表)
- **登记日期**:2026-05-25(05 session)

---

### TD-006 · 上传无防 path-traversal · 无防恶意文件名

- **类别**:安全
- **状态**:🔴 待还
- **影响**:中
- **描述**:虽然用 UUID 重命名了所以风险低,但**没显式校验**用户上传文件名里 `../` 之类的;也没扫病毒
- **为啥这么干**:Demo,uuid 重命名间接挡了大部分坑
- **触发还债**:任何上生产前
- **预估**:30 分钟(加 sanitize 校验)+ 病毒扫描 4 小时(可选)
- **登记日期**:2026-05-25(05 session)

---

### TD-007 · 订单号生成靠随机,真高并发下会冲突

- **类别**:数据完整性
- **状态**:🔴 待还
- **影响**:低(Demo)/ 高(生产)
- **描述**:`O{14位时间戳}{4位随机}` 随机重试 3 次。同秒内 > 3 个订单 + 随机重复 = 报错
- **为啥这么干**:Demo 量小,简单
- **触发还债**:订单量上 100/秒 之前
- **预估**:1 小时(用全局 counter / Snowflake-like id)
- **登记日期**:2026-05-25(04 session)

---

### TD-008 · shipping_info 写死(M-Address 模块未做)

- **类别**:功能完整性
- **状态**:🔴 待还(已纳入路线图)
- **影响**:中(用户没法填真地址)
- **描述**:订单的 `shipping_info` 永远是 SPEC §5.1 那个默认值
- **为啥这么干**:用户明确说"先 Demo,M-Address 作为独立模块"
- **触发还债**:Demo 通关后开 M-Address 模块
- **预估**:0.5 ~ 1 天(新增表 + UI + API)
- **登记日期**:2026-05-25(SPEC 初版决策)

---

### TD-009 · 没有日志聚合 / 监控

- **类别**:运维
- **状态**:🔴 待还
- **影响**:中(出 bug 难排查)
- **描述**:服务端只有 uvicorn 默认 log,没有结构化日志,没监控,没告警
- **为啥这么干**:Demo
- **触发还债**:上生产前,起码加结构化 logging + Sentry/类似
- **预估**:2~4 小时
- **登记日期**:2026-05-25

---

### TD-010 · 无 DB 备份策略

- **类别**:运维
- **状态**:🔴 待还
- **影响**:高(数据丢了找不回)
- **描述**:`carshop.db` SQLite 单文件,挂掉就没了
- **为啥这么干**:Demo
- **触发还债**:**有真实用户数据进入后立刻**做。最简单:cron 每天 rsync 一份到另一个目录或 iCloud
- **预估**:30 分钟
- **登记日期**:2026-05-25

---

### TD-011 · AntD Card `bordered` 属性已废弃

- **类别**:代码维护
- **状态**:🔴 待还
- **影响**:低
- **描述**:06 用了 AntD 5 老语法 `<Card bordered>`,新版本是 `<Card variant>`,跑会 warning 不会崩
- **为啥这么干**:06 session 没顺手改,留 warning
- **触发还债**:AntD 6 出之前批量替换
- **预估**:5 分钟(全局 search-replace)
- **登记日期**:2026-05-25(06 session)

---

### TD-012 · 测试覆盖率薄弱(USER_STORIES 缺边界/特殊字符/并发场景)

- **类别**:测试质量
- **状态**:🟡 计划中
- **影响**:中
- **描述**:当前 USER_STORIES.md 35 场景以 happy path 为主,缺边界值、特殊字符、并发、安全测试
- **为啥这么干**:Demo MVP 优先功能落地
- **触发还债**:用户 2026-05-25 提出。看用户对 `TEST_MATRIX.md` 提案的回复决定
- **预估**:2~3 小时补 20 个边界场景 + 写 TEST_MATRIX.md
- **登记日期**:2026-05-25(用户 review)

---

### TD-013 · 没有 CHANGE_MAP / 变更地图,改东西要全 grep

- **类别**:可维护性
- **状态**:🟡 计划中
- **影响**:中(项目越大越痛)
- **描述**:当前模型,改一个字段要扫所有 md / 所有代码文件
- **为啥这么干**:项目初期模块少没问题
- **触发还债**:加到 12+ session 之后开始痛。用户已经预感到。
- **预估**:30 分钟初版 + 每次改动维护
- **登记日期**:2026-05-25(用户 review)

---

### TD-014 · 分类图标 SVG 文件缺失(/static/icons/*.svg 404)

- **类别**:UI / 资产
- **状态**:🔴 待还
- **影响**:低
- **描述**:5 个分类的 icon_url 现在指向 `https://carshop.hearagain.space/static/icons/{car-parts,fuel-charge,wash-maintain,food-nearby,travel-service}.svg`,但 `~/carshop-server/static/icons/` 只有 `.gitkeep`,实际文件缺失,任何 UI 渲染 `<img src={icon_url}>` 会 404。
- **为啥这么干**:00 design-system session 没产出 SVG。07 顺手把 01 seed 的硬编码 `http://localhost:8000/...` 改成相对路径 `/static/icons/...`,让 absolutize_url 跟环境走,**但没补文件**,文件来源属于设计师产出物,等运营 / 设计师补图。
- **触发还债**:08+ 车机端 / 后台真渲染 icon 时,用户感觉到图裂。
- **预估**:1 小时(下载/绘制 5 个 24×24 单色 SVG + scp 到 `~/carshop-server/static/icons/`,无需重启服务)
- **登记日期**:2026-05-26(07 session)

---

### TD-015 · verify-stories.mjs 的 US-11 regex 针对 MSW 假数据写死

- **类别**:测试质量
- **状态**:🔴 待还
- **影响**:低
- **描述**:`scripts/verify-stories.mjs` 里 US-11 用 `getByText(/ce-abc|…[a-z]{2}-[a-z]{3}/)` 匹配 device_id_short,只能命中 MSW mock 数据 `…ce-abc`。真后端 seed 数据 `seed-device-abc123` 后 6 位是 `abc123` 含数字,不匹配,假阳性失败。
- **为啥这么干**:06 写 Playwright 时 MSW 数据是参照,没想到 07 真后端 device_id 形态会不同。
- **触发还债**:11 final-integration 还要跑 verify-stories.mjs,得修这个 regex 让两个模式都通过。改成 `getByText(/^…[0-9a-zA-Z-]{6}$/)` 或检查表格 row 数即可。
- **预估**:5 分钟
- **登记日期**:2026-05-26(07 session)

---

### TD-017 · carshop-android/local.properties 是 per-developer 文件,需手动建

- **类别**:开发体验 / 入门门槛
- **状态**:🔴 待还
- **影响**:低(挡住 build 但有清晰错误信息)
- **描述**:任何新开发者 / 新机器都要在 `carshop-android/` 手动建 `local.properties`,内容 `sdk.dir=...`。文件在 `.gitignore` 不入库。没有自动化脚本提示。
- **为啥这么干**:Android Gradle 标准做法。Q2 session 装机时踩,加文件就修了。
- **触发还债**:任何新开发者首次 clone 这个仓库 build 会挂。最低成本修法:在 README 里加一节"首次 build 准备",或者 `app/build.gradle.kts` 里加 fallback 用 `$ANDROID_HOME` env 兜底。
- **预估**:15 分钟(README 加一节)
- **登记日期**:2026-05-26(Q2 session)

---

### TD-018 · Android cmdline-tools 未装,无法 CI 自动化 build

- **类别**:CI / 自动化
- **状态**:🔴 待还
- **影响**:中(11 final-integration 想跑 CI 时挡住)
- **描述**:Studio 2025.3 Setup Wizard 默认不装 `cmdline-tools/`,所以 `sdkmanager` / `avdmanager` CLI 不可用。本机 build 没问题(Studio GUI 完整),但任何 headless CI / GitHub Actions / Docker 构建走不通。
- **为啥这么干**:Q2 session 装机时本机 GUI 已经够用,没装。
- **触发还债**:11 final-integration 或任何"想 push 到 CI 跑安卓 build"的时刻。修法:Studio GUI → SDK Manager → SDK Tools → 勾 "Android SDK Command-line Tools (latest)" → Apply。
- **预估**:10 分钟(GUI 勾选 + 下载 + 验证 `sdkmanager --version`)
- **登记日期**:2026-05-26(Q2 session)

---

### TD-019 · 顶层 build.gradle.kts 锁 Kotlin 1.9.24,Compose 编译器走老式插件配置

- **类别**:依赖管理 / 现代化
- **状态**:🔴 待还
- **影响**:低(能跑,但 Kotlin 2.0+ 是大势所趋)
- **描述**:Q2 修了 00 留的幽灵 plugin 后,项目实际配置是 Kotlin 1.9.24 + `composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }`(老式)。Kotlin 2.0 之后正确写法是 `plugins { id("org.jetbrains.kotlin.plugin.compose") }`。
- **为啥这么干**:00 session 钦定 Kotlin 1.9.24 + Compose Compiler Extension 1.5.14,Q2 不改业务代码所以维持。
- **触发还债**:Kotlin / AGP 大版本升级(2026 后基本不可避免),或者要用 K2 编译器 / Compose Multiplatform 时。
- **预估**:1 小时(升 Kotlin → 2.0.21 + 改 app build.gradle.kts + 加回顶层 compose plugin + 删 composeOptions 块 + 跑 assembleDebug 验证)
- **登记日期**:2026-05-26(Q2 session)

---

### TD-020 · admin-web 静态站不支持 SPA fallback,子路径直访 404

- **类别**:运维 / Demo 体验
- **状态**:🔴 待还
- **影响**:低(从首页进 + 不刷新子页面就好;书签 / 直链 / F5 子页面会 404)
- **描述**:`carshop-admin.hearagain.space` 由 `python -m http.server 18772` 提供,**没有 SPA fallback**(不会把未知路径 rewrite 到 `index.html`)。直接在地址栏敲 `/login` / `/products` / `/orders` 或刷新这些路径都会拿到 `404 Not Found`。从 `/` 进入后客户端 React Router 跳转一切正常。
- **为啥这么干**:沿用用户 Mac mini 的 `add_static_site.sh` 标准模式(panqian/aishare 都用 python http.server),Demo 阶段够用。
- **触发还债**:运营 / 用户开始书签子页面,或者 11 final-integration 要 demo 任意直链。
- **修法**:换 caddy 或 nginx 替代 python http.server,加 `try_files $uri /index.html`。或者升级 `add_static_site.sh` 支持 `--spa-fallback` 参数,所有受影响项目共享。后者更通用。
- **预估**:30 分钟(写 Caddyfile + 加 launchd plist)/ 1 小时(升级 add_static_site.sh 通用参数化)
- **登记日期**:2026-05-26(Q3 session)

---

### TD-021 · 真后端 title 长度 128 限制 vs SPEC US-24 "不设限" 漂移

- **类别**:数据校验 / spec 一致性
- **状态**:🟢 已解决(2026-05-26 · 11 final · 选 A 改 SPEC 对齐实现)
- **影响**:低
- **描述**:`POST /api/v1/admin/products` Pydantic schema 把 `title` max_length 限到 128 字符,任何 > 128 字符 title 直接返 `code=1000, message="String should have at most 128 characters"`。但 SPEC §11.3 / USER_STORIES US-24 / TD-015 都说"MVP 当前不设限,运营自律"。**实现已经强制限了 128 字符,SPEC 没写**。
- **为啥这么干**:05 admin schema 写时没意识到跟 SPEC / TD-015 漂移;09 真后端 spot check 121 字符 OK / 1000 字符被拒,才发现。
- **修法(已落地)**:11 final orchestrator 拍板选 A:
  - SPEC §5 Product.title 加一句 "服务端 Pydantic 校验 max_length=128;超长 → code=1000"
  - USER_STORIES US-24 Then 段加 "title > 128 字符 → code=1000(后端 Pydantic max_length=128 校验拒)"
  - 后端代码不动
- **实际工作量**:10 分钟(SPEC + USER_STORIES + 本文档)
- **登记日期**:2026-05-26(09 session 真后端 spot check)/ **解决日期**:2026-05-26(11 final)

---

### TD-016 · logout 响应 Set-Cookie 缺 Secure 属性

- **类别**:安全 / 一致性
- **状态**:🔴 待还
- **影响**:低(只在 SameSite=None 部署下生效;cookie 是要被删,不会真泄露)
- **描述**:`POST /admin/logout` 返回 `set-cookie: session=""; ...; SameSite=none` **不带 Secure**,而 login 返回 `; SameSite=none; Secure`。SameSite=None 必须配 Secure,Chrome / Safari 会拒接收(虽然这是删 cookie,行为上无害,但浏览器 console 会打 warning)。
- **为啥这么干**:05 写 logout 时 `response.delete_cookie(...)` 没透传 secure 参数,只透传 samesite。
- **触发还债**:任何上生产前。改 `app/routers/admin_auth.py` logout 那段,把 secure=SESSION_COOKIE_SECURE 一并传进去。
- **预估**:10 分钟
- **登记日期**:2026-05-26(07 session)

---

### TD-023 · US-18 真后端并发 mock_pay 返回了 2 个不同 paid_at(race condition)

- **类别**:并发 / 数据完整性
- **状态**:🔴 待还(11 final orchestrator 选 B 放宽 SPEC 接受现状 · 但**接真支付前必修**)
- **影响**:低(Demo)/ **高(真支付,见下方警告)**
- **描述**:10 session 真后端实跑 US-18:同设备同订单并发 5 次 `POST /orders/{id}/mock_pay`,**5 个都返 code=0**(契约 OK),但响应里的 `paid_at` 出现两个值,差 ~1ms(`...213191+08:00` × 2 次、`...214615+08:00` × 3 次)。SPEC US-18 Then 段原本写"以第一次写入为准 · DB 里 paid_at 字段只有一个值,不抖动"。后端 `order_service.mock_pay` 检查 `status == "paid"` 直接 return,但没有行级锁;两个事务先后看到 pending → 各自写一次 paid_at → 各自返回自己写入的值 → 实际 DB 最终值由最后 commit 决定。
- **为啥这么干**:04 session 实现时是"幂等 = code=0 不报错",**没考虑写入并发竞争**导致响应不一致。04 artifact §6.6 写的"幂等 · 已 paid 时不再 commit DB"逻辑只防住了"已经 paid 之后的再次调用",防不住"同时两个 mock_pay 都看到 pending"的窗口。
- **客户端影响**:**无**(当前 Demo)。10 session 客户端只判 `code=0` → 显示订单详情。即使两个 client 同时点支付,服务端响应里的 paid_at 微差不影响 UI(用户只看到一次成功 + 一个 paid_at)。这是契约表面通过、底层有 race 的典型场景。
- **11 final 处理**(2026-05-26):orchestrator 选 B → 改 USER_STORIES US-18 Then 段放宽为 "paid_at 差 ≤ 5ms 可接受",**SPEC 跟齐当前实现,后端代码不动**。
- ⚠️ **上生产前必修**:真支付场景下 1ms 抖动 = 可能重复扣款 / 风控异常 / 对账失败。Demo 不接真支付,放宽 SPEC 可接受;**接真支付前必须改后端 `mock_pay` 用 `SELECT ... FOR UPDATE` + 检查 rowcount 加锁**,只有第一个事务能写入 paid_at,后续事务读到已 paid 状态返回同一值。
- **触发还债**:接真支付前(微信 / 支付宝集成时)立即修。
- **预估**:1 小时(后端 service 改 + 加 SQLite 写锁 + 重跑 US-18 验所有 5 个 paid_at 完全一致)
- **登记日期**:2026-05-26(10 session)/ **11 final 决策**:2026-05-26 选 B 放宽 SPEC

---

### TD-024 · 登录接口无暴力破解防护(US-16 实跑确认)

- **类别**:安全
- **状态**:🔴 待还(11 final orchestrator 接受现状 · 但**禁止上生产**)
- **影响**:低(Demo)/ **高(生产)**
- **描述**:11 final session 跑 US-16 实测:同 IP 连错 10 次 `admin/wrong{1-10}` 全部返 `code=2001`,**无任何限流 / 锁定 / 延迟**。第 11 次用正确密码 `admin123` 立即登录成功 `code=0`,证明无锁定。任何攻击者可无限制穷举密码。
- **为啥这么干**:05 admin-auth-upload session 实现时只做 bcrypt 比较 + cookie session,**没加 rate limit / 失败计数 / 锁定**。Demo 阶段决策接受。
- **触发还债**:**任何上生产前必须实现**。建议方案:
  - 简单:用 `slowapi`(基于 Redis 或内存)限同 IP `POST /admin/login` 5次/分钟
  - 完整:加 `admin_login_failures` 表记录 (ip, username, ts),累计失败 5 次后锁定 5 分钟,返 `code=2002` 或 HTTP 429
- **预估**:2~4 小时(slowapi 集成 + 配置 + 测试)
- **登记日期**:2026-05-26(11 final session,US-16 实跑)

---

### TD-025 · 商品 price 边界(price=0 / 无上限)spec 严格 vs 实现宽松

- **类别**:数据校验 / spec 一致性
- **状态**:🔴 待还(11 final orchestrator 接受现状,改 SPEC 放宽)
- **影响**:低
- **描述**:11 final session 跑 US-22 实测:
  - `price=0` → 后端 `Field(..., ge=0)` 校验通过,`code=0` 商品创建成功(SPEC US-22 期望 `code=1000`)
  - `price=2147483647` → 后端无上限,`code=0` 成功(SPEC US-22 期望"应当拒")
  - `price=-100` → 拒(`code=1000`,符合 spec)
  - `original_price < price` → 暂未测,后端也无校验
- **为啥这么干**:05 schema 设计时只校验 `ge=0`(避免负价),没加 `>=1` 上限 / `gt=0` / `original_price >= price` 关联校验。Demo 阶段运营自律。
- **修法(11 final 已落地)**:11 final orchestrator 选 B 改 SPEC 接受:USER_STORIES US-22 Then 段从"应当拒"放宽为"实现接受 ≥0 任意整数,运营自律"。后端代码不动。
- ⚠️ **生产前应加**:`price >= 1`(免费商品在生产是异常)+ `price <= 99999999`(¥999999.99,避免 int overflow)+ `original_price >= price`(关联校验)
- **预估**:30 分钟(schema 改 + 单测)
- **登记日期**:2026-05-26(11 final,US-22 实跑)

---

### TD-022 · 商品列表排序 / 筛选 chip 行未实现

- **类别**:功能完整性 / UI
- **状态**:🔴 待还
- **影响**:低(当前分类导航够用,Demo 可接受)
- **描述**:设计稿分类列表页有一行 filter chip(价格排序、服务类型等),Q4 视觉对齐 session 主动偏离未实现。当前列表只能按服务端默认顺序展示。
- **为啥这么干**:09 spec 未排期此功能;UI 骨架做了 `LazyVerticalGrid`,加 chip 行需要新 ViewModel state + API 查询参数,属于业务逻辑改动,超出 Q4 视觉 only 范围。
- **触发还债**:运营反馈"找不到便宜的 / 找不到加油卡",或 Demo 通关后规划 M-Filter 模块。
- **预估**:4~6 小时(ViewModel 加 sortBy/filterType 状态 + API query 参数 + chip 行 UI)
- **登记日期**:2026-05-26(Q4 session)

---

### TD-026 · 商品下架时下单失败提示不够友好

- **类别**:UX
- **状态**:🔴 待还
- **影响**:低(Demo 罕见路径,但用户人测时遇到)
- **描述**:商品在后台被下架的瞬间,用户碰巧点"立即购买" → 后端正确返 `code=3000 "商品已下架"`,客户端把这串文案显示成 **Toast 一闪而过**。文案虽然友好,但 Toast 出现位置低 + 时间短,用户来不及看清,感觉像"按钮不响 / 系统报错"。
- **为啥这么干**:10 session 所有错误码 → Toast 单点处理;没区分"业务拒绝"和"系统错误"。
- **修法**:`OrderConfirmViewModel` 把 code=3000 分支独立出来,UI 弹 `AlertDialog`(标题"商品刚刚下架了" + 副文案"看看其他类似商品?" + 双按钮"返回首页 / 看看推荐")。
- **触发还债**:Demo 二期 UX 打磨;或运营反馈"用户说点了没反应"。
- **预估**:30 分钟
- **登记日期**:2026-05-27(v0.1 收山 · 用户人测)

---

### TD-027 · 订单 Tab 进入缺少自动刷新

- **类别**:UX / 数据时效
- **状态**:🔴 待还
- **影响**:中(影响"刚下完单看不到"的核心体验)
- **描述**:每次点 Rail "订单" 进入 OrderList,**不会自动拉最新数据**。`OrderListViewModel` 只在第一次 init 时拉一次。导致刚下完单返回订单列表,**新订单不显示**(要手动点 TopBar 刷新或杀进程才出现)。
- **为啥这么干**:10 session 没考虑"进入即刷新"语义,默认 `init { fetchList() }` 一次就完。
- **修法**:`OrderListScreen` 用 `LaunchedEffect` 监听进入事件,每次 resume 调 `viewModel.refresh()`;或用 NavBackStackEntry savedStateHandle 收"新下单完成"事件主动 invalidate。
- **触发还债**:Demo 演示卡过(用户反馈)/ 任何上线前必修。
- **预估**:15 分钟
- **登记日期**:2026-05-27(v0.1 收山 · 用户人测)

---

### TD-028 · 列表页缺下拉刷新手势

- **类别**:UX / 操作丰富度
- **状态**:🔴 待还
- **影响**:低(目前 TopBar 有"刷新"按钮兜底,功能不缺,只是手势不全)
- **描述**:首页 / 分类列表 / 订单列表都**没有下拉刷新手势**。当前只有 TopBar 上的刷新图标按钮。
- **⚠️ 跟 09 微交互 #3 决策冲突**:09 session 用户明确拍板"不做下拉刷新,TopBar 加刷新按钮"(no-motion 风格)。**2026-05-27 用户改主意,要求加下拉刷新**。还债时要更新 09 micro-interaction 决策记录。
- **修法**:用 Material3 `PullToRefreshBox`(BOM 2024.06 已支持)包裹三处 LazyColumn / LazyGrid / LazyRow,状态联动各 ViewModel 的 isRefreshing 字段。
- **预估**:1.5 小时(三个列表 + 状态联动 + 测试)
- **登记日期**:2026-05-27(v0.1 收山 · 用户人测改主意)

---

### TD-029 · 整体 UI 视觉跟设计稿差距仍大(Q5 翻车遗留)

- **类别**:UX / 视觉一致性
- **状态**:🔴 待还(v0.1 决策不还)
- **影响**:中(Demo 演示给外人看显得不精致)
- **描述**:Q5 worker 越界把"主页深度对齐 mini-session"做成了"全屏对齐",但执行时**自由发挥**(自创全屏 TopBar 分隔线 / 筛选按钮等原型没有的元素),反而破坏了 Q4 已经对齐的部分。
- **用户清单 9 处问题** + **2026-05-27 修了 8 处**(Rail items / 选中态 / Banner chevron / 撤全屏分隔线 / 列表标题字号 / 详情假数据 等),**剩余未对齐**:
  - 分类瓦片图标用 Material Icons 近似(`DirectionsCar/LocalGasStation/...`),设计稿是自绘 SVG
  - Rail 宽度感官偏宽(CSS 钦定 240dp,跟代码一致;可能要 200dp)
  - 字体字重精确数值未对齐(Inter 字号/字距 vs Compose 默认)
  - 装饰元素(hero 卡背景渐变 / 阴影 / banner 装饰条 等)
- **用户原话**:"界面UI很丑,没有按照设计稿做。暂时不让AI重写了,这是我的一个操作错误导致的。"
- **为啥这么干**:Q5 spec 写得不够紧;worker 不严格按用户清单逐条对齐,自由发挥;orchestrator 没盯紧。
- **触发还债**:v1.1 视觉打磨;Demo 要展示给外人看的时刻
- **修法**:
  - A. 起一个超严格 visual-alignment session,**逐元素照搬用户清单**,禁止 worker 加未列出的元素
  - B. 人工 Compose 调整(用户/设计师手动)
  - C. SVG 翻 ImageVector 走 valkyrie 等工具
- **预估**:4~8 小时(A/C 路线)/ 不可估(B 视情况)
- **登记日期**:2026-05-27(v0.1 收山 · 用户人测确认)

---

## 还债优先级建议

```
按"影响 × 触发紧迫"排序:
1. TD-010(无 DB 备份) ← 高影响,有真用户立刻
2. TD-003(session 不签名) ← 上生产前必还
3. TD-004(cookie 跨域)  ← 07 session 部分还
4. TD-008(M-Address)    ← Demo 通关后下一步
5. TD-006(上传安全)     ← 上生产前
6. TD-002(思源黑体)     ← 看真车机渲染效果定
7. TD-009(日志/监控)    ← 上生产前
8. 其他低影响 / 决策不还
```

---

**版本**:v1.7
**最后更新**:2026-05-27(v0.1 收山 · 加 TD-026 下架提示 / TD-027 订单 Tab 自动刷新 / TD-028 下拉刷新 / TD-029 视觉对齐遗留)
**最后更新**:2026-05-26(11 final · 加 TD-024 暴力破解无防护 · TD-025 价格边界 spec drift)
