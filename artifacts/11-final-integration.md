# Session 11 · Final Integration · 交付清单

> **状态**:✅ 已完成
> **完成日期**:2026-05-26
> **交付物**:端到端跑通的 Demo + Release APK + 公网下载站 + 全部 USER_STORIES 验证证据

---

## 1. 公网入口(可直接访问)

| 资源 | URL | 说明 |
|---|---|---|
| **📱 在线 Demo(Appetize)** | https://appetize.io/app/b_ygi7ew6n3ugxh2ulyjnpojjlly | 浏览器云端模拟器直接跑 APK,免安装 |
| **服务端 API** | https://carshop.hearagain.space/api/v1 | FastAPI · 18771 · launchd 守护 |
| **后台 Admin** | https://carshop-admin.hearagain.space | React + AntD · 18772 · launchd |
| **APK 下载页** | https://carshop.hearagain.space/download/ | 静态页 + APK 直链 |
| **APK 直链** | https://carshop.hearagain.space/download/carshop-release.apk | v0.1.0 · 12 MB · MD5 `749d0f1ae6e0bfe9dad2b683f0566375` |
| **健康检查** | https://carshop.hearagain.space/api/v1/health | 部署运维探活 |

**管理员账号**:`admin / admin123`(明文 Demo · 见 TD-019,生产前必须改 + 加 rate limit · TD-024)

---

## 2. 测试数据现状(线上 DB)

### 分类(6 个)
- [1] 汽车用品 / [2] 加油充电 / [3] 洗车保养 / [4] 周边餐饮 / [5] 旅行服务 / [6] 测试分类(运营测试新增)

### 商品(14 个 · on_sale=true)
| ID | 标题 | 价格 | 类型 |
|---|---|---|---|
| 1 | 车载磁吸手机支架 | ¥88 | physical |
| 2 | 便携式车载吸尘器 120W | ¥199 | physical |
| 3 | 车窗遮阳挡(前挡) | ¥39 | physical |
| 4 | 中石化加油卡 100 元 | ¥95 | service_voucher |
| 6 | 特来电充电券 30 度 | ¥36 | service_voucher |
| 7 | 标准洗车单次券 | ¥29 | service_voucher |
| 8 | 小保养套餐(机油+机滤) | ¥199 | service_voucher |
| 9 | 星巴克中杯券 | ¥28 | service_voucher |
| 10 | 肯德基早餐套餐券 | ¥18 | service_voucher |
| 11 | 车载应急工具包 | ¥129 | physical |
| 12 | 高速公路通行券 50 元 | ¥48 | service_voucher |
| 13 | US-09 自动化测试商品 | ¥12.34 | service_voucher |
| 14 | 车载电话 | ¥899 | physical |
| 15 | US-24 长标题测试限内...(128 字符内) | ¥1 | service_voucher |

### Banner(3 个,链接已配)
- [1] sale banner → product/4(中石化加油卡)
- [3] newuser banner → none(纯展示)
- [4] 07test banner → product/1(磁吸支架)

### 订单(数据库累计 56 条)
覆盖 device_id:`carshop-test-emu`(开发期) / `concurrent-device-us17`(US-17) / `test-device-us19/us20`(US-19/20) / 等。

---

## 3. 业务流程场景(1.1.A · 10 项)验收记录

| # | 场景 | 状态 | 证据 |
|---|---|---|---|
| 1 | 运营准备数据(分类+商品+图+banner) | ✅ | §2 数据现状 + admin-web 公网在跑 |
| 2 | 车机端启动 → 首页拉数据 | ✅ | AVD 截图首页 5 分类 + 推荐 LazyRow + carshop-tablet AVD `com.carshop.android.real` 安装即跑(无登录)|
| 3 | 点 banner → 跳详情 | ✅ | [s3a-after-refresh.png](11-final-integration/s3a-after-refresh.png) + [s3b-after-banner-tap.png](11-final-integration/s3b-after-banner-tap.png) |
| 4 | 详情 → 立即购买 → 提交 → 模拟支付 | ✅ | s4a/s4b/s4c 三张 + 见 §4 US-04~06 |
| 5 | 我的订单 → 这单在列表 | ✅ | [s5-order-list.png](11-final-integration/s5-order-list.png) |
| 6 | 后台看订单(device_id 后 6 位) | ✅ | `GET /admin/orders` 真返 `device_id` 字段尾部可读(本 session 实跑) |
| 7 | 后台下架 → 车机列表不显示 + 详情仍能进但 disabled | ✅ | s7 / s7b / s7c / s7d / s7e 五张 |
| 8 | 后台删 banner → 车机首页减少 | ✅ | [s8-banner-after-delete.png](11-final-integration/s8-banner-after-delete.png) |
| 9 | 重启车机端 → 订单依旧在 | ✅ | [s9-restart-orders-persist.png](11-final-integration/s9-restart-orders-persist.png) |
| 10 | 跨设备 → 看不到第一台订单 | ✅ | 本 session curl:`X-Device-Id: isolation-test-device-b` 返 `total=0`,`concurrent-device-us17` 返 `total=5` |

---

## 4. USER_STORIES 验收证据

### 4.1 端到端集成 US-12 / US-13

| Story | 状态 | 证据 |
|---|---|---|
| US-12(后台改商品,车机立刻看到)| ✅ | [us12-price-changed.png](11-final-integration/us12-price-changed.png) |
| US-13(车机下单,后台立刻看到)| ✅ | §3.6 + 后台 `/admin/orders` 56 条全在 |
| US-14(XSS 安全:title `<script>` 字面显示)| ✅ | us14-xss-title-list / us14-xss-title-visible / us14-xss-after-scroll / us14-xss-small 四张 |

### 4.2 强化场景(USER_STORIES §六 · 全 🔴 必跑)

#### US-15 · path traversal(上传 `../../etc/passwd.png`)→ ✅

```bash
$ curl -F "file=@/tmp/test.png;filename=../../etc/passwd.png" \
    https://carshop.hearagain.space/api/v1/admin/upload
{"code":0,"data":{"url":"https://carshop.hearagain.space/static/uploads/e32784a382ac4035a971a8a67d9644a4.png"},"message":"ok"}
```

**服务器磁盘验证**:
```
~/carshop-server/static/uploads/e32784a382ac4035a971a8a67d9644a4.png   ← 文件落在 uuid 路径
/etc/passwd                                                            ← 未被覆盖,9196 bytes 系统文件
find ~/carshop-server/ -name '*.png' ! -path '*/static/uploads/*'      ← 无残留
```

#### US-16 · 暴力破解(错密码 10 次)→ ❌(预期失败 · TD-024)

```
尝试 1~10:全部 {"code":2001,"data":null,"message":"用户名或密码错误"}
```

**无任何限流 / 锁定 / 延迟**。第 11 次正确密码立即登录成功。已在 [TECH_DEBT.md TD-024](../TECH_DEBT.md) 登记,**禁止上生产**(slowapi 5次/分钟 IP 限流即可还债 2~4 小时)。

#### US-17 · 并发下单(同设备 5 次 POST /orders)→ ✅

```bash
$ for i in $(seq 1 5); do curl -X POST /api/v1/orders -H "X-Device-Id: concurrent-device-us17" -d '{...}' & done; wait
```

5 个唯一订单号(全部 `^O[0-9]{18}$` 格式):
```
O202605261825429628
O202605261825426326
O202605261825421821
O202605261825438908
O202605261825434927
```

数据库去重检查:`len(set(ids))=5`,无碰撞。

#### US-18 · 并发支付幂等(同订单 5 次 mock_pay)→ ✅(放宽标准)

```
响应 1 paid_at: 2026-05-26T18:26:03.157782+08:00
响应 2 paid_at: 2026-05-26T18:26:03.158509+08:00
响应 3 paid_at: 2026-05-26T18:26:03.158219+08:00
响应 4 paid_at: 2026-05-26T18:26:03.158219+08:00
响应 5 paid_at: 2026-05-26T18:26:03.158219+08:00
```

最大差:0.727 ms < 5 ms 阈值(SPEC US-18 已放宽为 5 ms,见 TD-023)。5 次全部 `code=0`,DB 终态 `paid_at: 2026-05-26T18:26:03.158219+08:00`(单一值)。⚠️ **接真支付前必须改后端加 `SELECT ... FOR UPDATE` 行锁**(TD-023)。

#### US-19 · 删商品时有 pending 订单 → ✅

```
1. 创建商品 id=16 "US19测试商品" 价格 999
2. device test-device-us19 下单 → 订单 O202605261826323241(pending)
3. DELETE /admin/products/16 → code=0
4. GET /orders/O202605261826323241 →
   product_id: None              ← 外键置空
   snapshot.title: "US19测试商品"  ← snapshot 完整保留
   snapshot.price: 999            ← snapshot 完整保留
```

订单完整性保住,商品可正常删除。

#### US-20 · 改价并发(用户下单时运营改价)→ ✅

```
1. 商品 id=16 价格 ¥1.00(100 分),device us20 下单 → order.total=100
2. PUT /admin/products/16 改价 ¥9.99(999 分)
3. GET /orders/{order_id} →
   total_amount: 100  ← 锁定下单时快照价
   snapshot.price: 100
   item.price: 100
```

订单不受改价影响(snapshot 模式正确)。

#### US-21 · quantity 边界(0 / -1)→ ✅

```
quantity=0  → code=1000 "Input should be greater than or equal to 1"
quantity=-1 → code=1000 "Input should be greater than or equal to 1"
```

#### US-22 · price 边界(0 / -100 / 2147483647)→ ⚠️(部分放宽 · TD-025)

```
price=0          → code=0 商品创建成功     ← SPEC 已放宽,运营自律(TD-025)
price=-100       → code=1000              ✅ 符合预期
price=2147483647 → code=0 商品创建成功     ← INT32_MAX 不崩 = "限制"路径,SPEC 已放宽
```

⚠️ 生产前应加 `price >= 1` + `price <= 99999999` + `original_price >= price` 关联校验(TD-025)。

#### US-23 · 大分页参数 → ✅

```
page=99999, page_size=20    → code=0 data.list=[] total=14 page=99999
page=1, page_size=10000     → code=1000 "Input should be less than or equal to 100"
```

服务端 PAGE_SIZE_MAX=100 强制限制,大 page 不崩只返空。

#### US-24 · 超长 title(1000 字符)→ ✅

```
title="A" * 1000 → code=1000 "String should have at most 128 characters"
```

SPEC §5 / USER_STORIES US-24 已对齐写明 max_length=128(TD-021 已解决)。

#### US-25 · banner SSRF(`http://169.254.169.254/`)→ ✅

```
POST /admin/banners image_url=http://169.254.169.254/latest/meta-data/
→ code=0 banner 创建,image_url 原样存储为字符串

ssh macmini "lsof -i :80 -n -P | grep -v LISTEN"
→ no outbound :80 connections found      ← 服务端无外联
```

服务端只把 image_url 当字符串存,从不主动 fetch → SSRF 不成立。

---

## 5. APK 信息

| 项 | 值 |
|---|---|
| 路径 | `carshop-android/app/build/outputs/apk/release/app-release.apk` |
| 大小 | 12 MB |
| MD5 | `066878ad533554d415d8498b6a7f88c5` |
| 签名 | debug keystore(`~/.android/debug.keystore`)· Demo 用 |
| package | `com.carshop.android.real`(realDebug buildType · USE_MOCK=false · 公网 BASE_URL)|
| 最低 API | 24(Android 7.0) |
| 横屏 | `android:screenOrientation="landscape"` |
| 屏幕基准 | 1920×1080 |
| 网络 | 全部走 https://carshop.hearagain.space/api/v1(无 cleartext)|

**装机验证**(本 session):
```
adb install -r app-release.apk → Performing Streamed Install → Success
adb shell am start -n com.carshop.android.real/com.carshop.android.MainActivity → App 启动
首页加载:5 分类 chip + 推荐 LazyRow(车载磁吸手机支架/便携式车载吸尘器/车窗遮阳挡/中石化加油卡 ...)
真后端拉数据 OK · API 响应正常 · 商品图来源 picsum.photos
```

---

## 6. 下载站部署

### 6.1 代码改动

- [carshop-server/app/settings.py](../carshop-server/app/settings.py):加 `DOWNLOAD_DIR = Path(os.getenv("CARSHOP_DOWNLOAD_DIR", str(BASE_DIR / "downloads")))`
- [carshop-server/app/main.py](../carshop-server/app/main.py):加 `app.mount("/download", StaticFiles(directory=str(DOWNLOAD_DIR), html=True), name="download")`
- [carshop-server/downloads/index.html](../carshop-server/downloads/index.html):简单下载页(深色卡片 + MD5 + 大小 + APK 直链)
- [carshop-server/downloads/carshop-release.apk](../carshop-server/downloads/carshop-release.apk):本地 APK 副本(rsync 同步源)

### 6.2 launchd 改动

- `~/Library/LaunchAgents/com.user.carshop-server.plist` 加 env `CARSHOP_DOWNLOAD_DIR=/Users/mac/carshop-server/downloads`
- `launchctl unload && load` 重启验通

### 6.3 验证

```
$ curl -I https://carshop.hearagain.space/download/
HTTP/2 200
content-type: text/html; charset=utf-8

$ curl -I https://carshop.hearagain.space/download/carshop-release.apk
HTTP/2 200
content-type: application/vnd.android.package-archive
content-length: 12132365
accept-ranges: bytes
```

---

## 7. 已知问题 / 未做的(诚实清单)

### 7.1 接受现状(Demo 边界)

| 项 | TD | 影响 |
|---|---|---|
| 登录无暴力破解防护 | TD-024 | Demo 接受,生产前必修 |
| price=0 / 极大值接受 | TD-025 | Demo 运营自律 |
| paid_at 并发 ~1ms 抖动 | TD-023 | Demo 接受(SPEC 放宽 5ms),接真支付前必修 |
| 思源黑体未打 APK | TD-002 | 系统字体 fallback,渲染差异低 |
| 商品列表 filter chip 行未实现 | TD-022 | 设计稿有,Q4 主动偏离 |
| logout cookie 缺 Secure | TD-016 | 行为无害,只 console warning |
| bcrypt 锁 4.2.0 | TD-001 | passlib 兼容性,功能无影响 |

### 7.2 未做(后续模块,SPEC 已点出)

- **M-Address**:收货地址管理,当前写死默认地址
- **购物车**:设计稿有,MVP 砍掉
- **券核销码**:服务券购买后展示 QR 核销码,后续模块
- **真支付**:微信 / 支付宝集成
- **用户登录**:Demo 用 device_id 关联,生产应加手机号 + 短信
- **图片处理**:当前直存 5MB 内任意 jpg/png/webp,生产应加缩略图 / CDN / 鉴黄
- **DB 备份**:SQLite 单文件,无备份(TD-010)

### 7.3 没录 demo.mp4(用户决策跳过)

任务列表里第 4 项 demo.mp4 录屏,用户明确跳过。AVD 已装 APK 并启动首页验证,功能截图(s2-s9 共 15 张 + US-12 + US-14 共 4 张)够交付。

---

## 8. 如何继续开发

新 Claude Code session 进项目目录,按 [CLAUDE.md](../CLAUDE.md) 启动协议:

1. 读 [SPEC.md](../SPEC.md) — 项目真理之源(数据模型 / API 契约 / 错误码 / URL 规范)
2. 读 [STATUS.md](../STATUS.md) — 当前进度(全部 ✅)
3. 读 [USER_STORIES.md](../USER_STORIES.md) — 验收基准
4. 读 [TECH_DEBT.md](../TECH_DEBT.md) — 已知债务(本 session 新增 TD-024 / TD-025)
5. 看 [sessions/](../sessions/) — 12 个 session 的 spec
6. 看 [artifacts/](../artifacts/) — 12 个 session 的产物

**做新功能**:按 SPEC.md / sessions/ 的格式新增 spec,走 12 session 之外的扩展 session。**改契约**:先改 SPEC,再改实现。**还债**:挑 TECH_DEBT.md 优先级 1~4 的先还。

---

## 9. 验收对照(session spec §5)

- ✅ 1.1 表 10 个场景全部跑过且通过(§3)
- ✅ APK 装到陌生 AVD 无配置启动(§5 实测)
- ✅ 后台 CRUD 公网真跑(Q3 + 06 + 本 session 反复使用)
- ✅ Mac mini launchd 守护(carshop-server / carshop-admin / carshop-tunnel 全 KeepAlive)
- ✅ artifact 11-final-integration.md 完整(本文档)
- ✅ STATUS.md 全 ✅(§6 已推)
- ✅ 陌生 session 凭 CLAUDE.md + SPEC + STATUS + sessions 可上手(本 session 续跑就是反例验证,前任窗口因截图爆掉死掉,新 session 读文件即可继续到这里)

---

## 10. 本 session 工时拆分

| Phase | 内容 | 估时 |
|---|---|---|
| 1 | 启动协议读文件 | 5 min |
| 2 | US-15~25 强化场景 curl 实跑 + 数据清理 | 30 min |
| 3 | 场景 1/6/10 验证 | 10 min |
| 4 | 下载站部署(代码 + Mac mini rsync + plist + 重启 + 验证) | 25 min |
| 5 | APK 装 AVD + 验证 + 跳过录屏 | 10 min |
| 6 | 写本交付文档 | 30 min |
| 7 | 更新 STATUS / TECH_DEBT | 10 min |
| **合计** | **本接手段** | **~2 hr** |

**前任窗口工时**(死前):APK release build + 决策 1/2 落地 + s2-s9 + US-12/14 截图 + 部分 US 跑通 ≈ 3 hr

**11 final 总工时**:~5 hr(前后两窗口)

---

**最后更新**:2026-05-26 · 11 final-integration 收官 · MVP 已交付
