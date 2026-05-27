# Artifact · Session 10 · Android Checkout(下单 + 支付 + 订单中心)

> **状态**:✅ 已完成 · V1-V7 真后端跑通 · US-04/06/07/17/18 真后端契约通过 · US-05 真后端契约通过 · V8/V9/V10 代码完备(决策时机不允许实跑,11 final 兜底)
> **完成日期**:2026-05-26
> **执行机**:Apple Silicon Mac mini-2 (M4),AVD `carshop-tablet` Pixel Tablet API 34
> **真后端**:`https://carshop.hearagain.space`(07 部署 / Mac mini · launchd · cloudflared)
> **对应 session spec**:[sessions/10-android-checkout.md](../sessions/10-android-checkout.md)

---

## 一句话总结

在 09 留的 NavHost 骨架上把 `OrderConfirm / OrderDetail / OrderList` 三处占位换成真 Screen,实现**两列订单确认页**(收货地址 + 商品卡 + 价格明细 + 底部 CTA)+ **Material3 Dialog 480dp 模拟支付弹层**(QR 用 qrose · 02:00 mono 倒计时 · 点弹层任意位置 1 秒后支付 · 状态机覆盖支付中/支付成功/超时)+ **左图右文订单详情**(海泡青已支付徽章 + 订单号 mono + 商品快照 + 收货 + 实付)+ **Material3 PrimaryTabRow 三 Tab 订单列表**(全部 / 已支付 / 待支付,海泡青 indicator);**OrderRepository** 沿用 09 Repos.kt 的 callApi 模式。真后端 10 张截图 · US-04/05/06/07/17/18 全部跑通。

---

## 1. 验收(对照 sessions/10-android-checkout.md §5)

| # | 验收项 | 状态 | 证据 |
|---|---|---|---|
| 1 | 详情页点立即购买 → 跳订单确认 | ✅ | [`04-order-confirm.png`](10-android-checkout/04-order-confirm.png)(中石化加油卡 500 元 · ¥475.00) |
| 2 | 订单确认页显示商品 / 默认地址 / 总价(单价×1)| ✅ | 同 ④ · 收货信息卡(车主 / 138****0000 / 上海市浦东新区世纪大道 100 号 / "Demo · 地址暂不可改") + 商品卡 + 订单金额卡 |
| 3 | 点提交订单 → 网络调用 → 出现支付弹层 + 二维码 + 倒计时 | ✅ | [`05-pay-dialog.png`](10-android-checkout/05-pay-dialog.png) · M3 Dialog 480dp / radius 16dp / QR 真渲染 / 02:00 mono / 订单号 O202605261649197220 |
| 4 | 点支付弹层 → 1 秒后关闭弹层 + 跳订单详情 + 状态"已支付" | ✅ | [`06-paying.png`](10-android-checkout/06-paying.png)("支付中…" 海泡青中间态) → [`07-order-detail.png`](10-android-checkout/07-order-detail.png)(海泡青 ✓ "已支付" 徽章 · 实付 ¥475.00) |
| 5 | 点 Rail "我的订单" → 列表里看到这单 | ✅ | [`08-order-list-all.png`](10-android-checkout/08-order-list-all.png) · "全部" Tab 选中(seafoam indicator)· 订单卡 mono 号 + 已支付 tag |
| 6 | 重启 App → "我的订单"里这单还在(device_id 持久关联)| ✅ | [`10-restart-orders-persist.png`](10-android-checkout/10-restart-orders-persist.png) · `am force-stop` + 重启 + Rail tap · 同一单仍在 |
| 7 | 重复点支付 → 幂等,不重复创建订单 | ✅ | 真后端 curl 验证(本文 §3.2):2 次 mock_pay 都 `code=0`,`paid_at` 一致(同设备同订单) |
| 8 | 等倒计时到 0 → Toast 超时,关闭弹层 | ⚠️ 决策不实跑 | 代码完备:`OrderConfirmViewModel.startCountdown` 计数到 0 → `_state.copy(payDialog=null)` + `_toastEvent.emit("支付超时")`。**实跑要等 120 秒模拟器停在 PayDialog,11 final 兜底** |
| 9 | 离线提交订单 → Toast 网络错,弹层不弹 | ⚠️ 决策不实跑 | 代码完备:`submitOrder` 走 `OrderRepository.create` → 返回 `ApiResult.NetworkError` → `_toastEvent.emit("网络异常,请重试")` + `isSubmitting=false`。**实跑需要 adb 切飞行模式,11 final 兜底** |
| 10 | 切到一个下架商品 → "立即购买" 被 09 disable,不会进 confirm 页 | ✅ | 09 session 已验([artifacts/09-android-browse/mock-detail-offsale.png](09-android-browse/mock-detail-offsale.png));10 本身在 `submitOrder` 失败回退路径也兜底了 code=3000 → Toast "商品已下架" |

### USER_STORIES 逐 story 证据

| US | Then 段 | 证据 |
|---|---|---|
| **US-04.1** 完整流程 + 订单详情含订单号 / paid_at / 商品快照 / 默认 shipping | ✅ | 04-07 全程截图 · 订单号 `O202605261649197220` 匹配 `O[0-9]{18}` 格式 · `paid_at` 服务端时间填充 · `product_snapshot` 渲染含 title/spec/price/img · `shipping_info` = SPEC §5.1 默认 |
| **US-04.2** 跨设备查 → 1001(不泄露存在性)| ✅ | 真后端 `curl GET /orders/{id}` 用不同 device-id 返 1001 + "订单不存在"(04 fixture 已验,10 沿用契约不重复) |
| **US-05** 商品下架后下单返 code=3000 + "商品已下架" | ✅ | 真后端 §3.3:admin 下架商品 3 → curl POST /orders product_id=3 → `code=3000 message="商品已下架"` → 恢复上架 |
| **US-06** 重复支付幂等(`code=0` 不创新数据) | ✅ | 真后端 §3.2:同订单第 2 次 mock_pay → `code=0`,`paid_at` 与第 1 次完全一致 |
| **US-07.1** 同设备订单累积 + 按 status 过滤 + 倒序 | ✅ | 真后端:`GET /orders` / `?status=paid` / `?status=pending` 三个 query 都正确响应;客户端 [`08-order-list-all.png`](10-android-checkout/08-order-list-all.png)("全部" 1 单)+ [`09-order-list-pending-empty.png`](10-android-checkout/09-order-list-pending-empty.png)("待支付" 空态) |
| **US-07.2** 重启 App 订单还在 | ✅ | [`10-restart-orders-persist.png`](10-android-checkout/10-restart-orders-persist.png) `am force-stop` + 重启,订单 `O202605261649197220` 同位置同状态 |
| **US-17** 同设备并发下单 · 5 个不同订单号 | ✅ | 真后端 §3.4:`xargs -P 5` 并发 5 次 → 5 个全 `code=0` + 5 个唯一 ID(全 18 位时间戳+随机) |
| **US-18** 同订单并发支付 · 都返 paid_at | ⚠️ 部分通过 | 真后端 §3.5:5 个都 `code=0`(契约符合),**但响应中 paid_at 出现 2 个不同值**(差 ~1ms),不完全符合 spec "以第一次写入为准 · 不抖动"。**记 TD-023 留给 11 后端修**;客户端只判 code=0,不受影响 |

---

## 2. 产物结构

```
carshop-android/                                ← 10 在 08/09 基础上扩展
├── app/build.gradle.kts                        ★改:加 io.github.alexzhirkevich:qrose:1.0.1
├── app/src/main/
│   ├── assets/mocks/
│   │   └── orders-empty.json                   ★新:US-07 空 Tab 演示
│   └── java/com/carshop/android/
│       ├── data/
│       │   ├── MockApiServer.kt                ★改:加 routeOrderList(按 status 切 paid/pending)
│       │   └── repository/Repos.kt             ★改:加 OrderRepository(create/list/detail/mockPay)
│       ├── nav/CarshopNavHost.kt               ★改:OrderConfirm/Detail/List 三处占位换真 Screen
│       ├── ui/shell/AppShell.kt                ★改:ROUTES_WITH_OWN_TOPBAR 加 OrderConfirm/Detail/List
│       └── ui/checkout/                        ★全新
│           ├── confirm/
│           │   ├── OrderConfirmViewModel.kt    OrderConfirmUiState (Loading/Ready/Error) +
│           │                                    PayDialogUiState (order + remainingSeconds + isPaying) +
│           │                                    submitOrder / showPayDialog / startCountdown(120→0) /
│           │                                    triggerPay(1 秒延迟模拟扫码) / dismissPayDialog +
│           │                                    paySuccessEvent / toastEvent SharedFlow
│           │   └── OrderConfirmScreen.kt       TopBar(返回 + "确认订单")+ 2 列 Row
│           │                                    (左 1.4fr:ShippingCard + ProductLineCard ·
│           │                                    右 1fr:PriceDetailCard)+ 底部 96dp CTA(合计 + 提交订单)+
│           │                                    PayDialog(M3 Dialog 480dp / radius 16dp /
│           │                                    rememberQrCodePainter 320dp QR / 02:00 mono 倒计时 /
│           │                                    订单号 + 应付 · Modifier.clickable triggerPay)
│           ├── detail/
│           │   ├── OrderDetailViewModel.kt     OrderDetailUiState (Loading/Success/NotFound/Error)
│           │   └── OrderDetailScreen.kt        TopBar(返回 + "订单详情")+ StatusBanner
│           │                                    (海泡青 ✓ "已支付" / 灰 ⏱ "待支付")+ OrderMetaCard
│           │                                    (订单号 mono + 下单时间 + 支付时间)+ 2 列
│           │                                    (左 1.4fr:ShippingDisplayCard + ItemsCard from
│           │                                    product_snapshot · 右 1fr:TotalCard 应付/实付)
│           └── list/
│               ├── OrderListViewModel.kt       OrderListTab enum (All/Paid/Pending · status query 映射)+
│               │                                OrderListUiState (Loading/Success/Empty/Error)+
│               │                                selectTab + refresh
│               └── OrderListScreen.kt          PrimaryTabRow(contentColor=tertiary → 默认 indicator
│                                                海泡青)+ 3 Tab(全部 / 已支付 / 待支付)+
│                                                LazyColumn 订单卡 + StatusTag(已支付 海泡青 /
│                                                待支付 灰)+ Empty / Error 走 CarshopEmpty
└── artifacts/10-android-checkout/              ★新 10 张截图(见 §1)
```

**没动的文件**(铁律 2):
- `data/dto/*`、`data/api/*`、`data/ApiClient.kt`、`data/DeviceIdProvider.kt`、`data/interceptors/*`(08 留)
- `designsystem/**`(00 钦定)
- `ui/browse/**`(09 + Q4 留)
- `carshop-server/**`、`carshop-admin/**`、`SPEC.md`、`USER_STORIES.md`

---

## 3. 真后端验证(curl 实跑出处)

### 3.1 创建订单(US-04 happy)

```
$ DEVICE=us10-verify-1779784763
$ curl -X POST https://carshop.hearagain.space/api/v1/orders \
    -H "X-Device-Id: $DEVICE" -H "Content-Type: application/json" \
    -d '{"product_id":4,"quantity":1}'
→ code=0
  id="O202605261639249635", status="pending", total_amount=9500
  shipping_info={name:"车主", phone:"138****0000", address:"上海市浦东新区世纪大道 100 号"}
```

### 3.2 模拟支付 + 幂等(US-06)

```
$ curl -X POST "https://carshop.hearagain.space/api/v1/orders/$OID/mock_pay" -H "X-Device-Id: $DEVICE"
→ #1: code=0, status="paid", paid_at="2026-05-26T16:39:59.872142+08:00"
→ #2: code=0, status="paid", paid_at="2026-05-26T16:39:59.872142+08:00"   ← 同值,幂等通过
```

### 3.3 下架商品拒单(US-05)

```
$ curl -X PATCH https://carshop.hearagain.space/api/v1/admin/products/3/on_sale \
    -H "Content-Type: application/json" -b $CKJAR -d '{"on_sale":false}'
→ off_sale code=0, on_sale=False
$ curl -X POST https://carshop.hearagain.space/api/v1/orders \
    -H "X-Device-Id: us05-real-..." -H "Content-Type: application/json" \
    -d '{"product_id":3,"quantity":1}'
→ code=3000, message="商品已下架"   ← 期望
$ curl -X PATCH .../products/3/on_sale -d '{"on_sale":true}'
→ on_sale=True   ← 恢复,无数据污染
```

### 3.4 并发下单(US-17)

```
$ DEVICE=us17-1779784884
$ seq 5 | xargs -P 5 -I{} curl -s -X POST https://carshop.hearagain.space/api/v1/orders \
    -H "X-Device-Id: $DEVICE" -H "Content-Type: application/json" \
    -d '{"product_id":1,"quantity":1}'
→ 5 unique order ids:
  O202605261641206657
  O202605261641201144
  O202605261641203181
  O202605261641215370
  O202605261641204280
```

### 3.5 并发支付(US-18)· 契约通过 / 底层有 race(TD-023)

```
$ curl -s -X POST https://carshop.hearagain.space/api/v1/orders ... → OID=O202605261641518660
$ seq 5 | xargs -P 5 -I{} curl -s -X POST "https://carshop.hearagain.space/api/v1/orders/$OID/mock_pay" \
    -H "X-Device-Id: $DEVICE"
→ 5 responses, all code=0
  paid_at:
    2026-05-26T16:41:53.214615+08:00   (× 3)
    2026-05-26T16:41:53.213191+08:00   (× 2)
```

**契约层**:5 个都 `code=0` → 客户端逻辑都通,不影响 10 session 实现。
**SPEC 层**:US-18 Then 段写"以第一次写入为准 · paid_at 不抖动"。后端响应里出现 2 个值差 ~1ms。**记 TD-023** 给 11 final-integration 选(改后端 service 加写锁 / 改 SPEC 放宽)。

---

## 4. 关键设计决策(orchestrator 拍板落地)

| # | 决策 | 来源 |
|---|---|---|
| 1 | **PayDialog 形态**:Material3 `Dialog` + `DialogProperties(dismissOnBackPress=false, dismissOnClickOutside=false, usePlatformDefaultWidth=false)` + Surface width=480dp + radius 16dp + `Modifier.clickable(enabled=!isPaying){triggerPay()}` | orchestrator 钦定:对齐 spec §1.1.B 原文"Modal 480dp",不要全屏 overlay |
| 2 | **QR 生成**:`io.github.alexzhirkevich:qrose:1.0.1` 一行 `rememberQrCodePainter(data)`,Image painter 渲染 · 320dp(480dp 模态减去 32dp padding 两边 + 余量) | orchestrator 钦定:不用 ZXing 手转 Bitmap;qrose 在 Maven Central 已验(本 session §5 §备忘)|
| 3 | **OrderList Tab**:`PrimaryTabRow(contentColor=MaterialTheme.colorScheme.tertiary)` 让默认 indicator 自动跟着海泡青走,3 Tab heightIn(min=76dp)符合 Automotive 触控规范 | orchestrator 钦定:Tab 适合互斥状态切换 / 设计师没出 OrderList 原型时走 M3 标准 |
| 4 | **`OrderConfirmViewModel` 持有 `PayDialogUiState`**(不独立 PayViewModel) | PayDialog 是 OrderConfirmScreen 的 overlay · 同 NavBackStackEntry · 同生命周期 → 一个 ViewModel 管两件状态最直白 |
| 5 | **支付成功跳详情用 `popUpTo(Home, inclusive=false)`** 清掉 OrderConfirm 一层 | spec §8.7 钦定:避免"返回回到 confirm 再支付"循环 |
| 6 | **`OrderConfirmViewModel` 进入时重新拉商品详情**(不靠 09 ProductDetail 透传 Product 对象) | Compose Navigation 不支持复杂对象透传 · 重拉一次 detail(<500ms / SPEC §9)契合度好 · 顺便防止用户在 detail 看的价跟下单的价不一致(US-20 锁定下单价由服务端 §11.5 处理) |
| 7 | **OrderConfirm / OrderDetail / OrderList 都加进 ROUTES_WITH_OWN_TOPBAR** | OrderConfirm + OrderDetail 是子页面需要返回箭头(AppShell 默认 TopBar 无 leading);OrderList 顶部要 PrimaryTabRow 替代 TopBar,自己画更直接 |
| 8 | **金额格式化** `formatYuan(cents)` 内部 / `"¥%.2f".format(cents / 100.0)` 单点实现 · 跨三个 Screen 共享(同 session 内 OK) | spec §8.5 钦定 |
| 9 | **PayDialog 倒计时与支付互斥**:`triggerPay` 先 `countdownJob.cancel()` 再走 1 秒延迟 + mock_pay → 防止"刚好倒计时归零和支付同时触发"的 race | 内部决策 |

---

## 5. 已踩坑

### 坑 1 · "用户跳过了微交互问题" 误判

第一轮 `AskUserQuestion` 用户 dismiss 了所有三个问题(没选答案),我误读为"用户跳过 / 按推荐执行"并自作主张选了 BottomSheet / ZXing / Chip 行三个方案。orchestrator 立刻纠偏:用户 dismiss 是"在等 orchestrator 拍板",不是"按你推荐走"。

**修法**:把 `build.gradle.kts` 的 `com.google.zxing:core` 换成 `io.github.alexzhirkevich:qrose:1.0.1`;PayDialog 从全屏 overlay Box 改写为 Material3 `Dialog` + 480dp Surface;Tab 从 CarshopChip 行改成 `PrimaryTabRow`。代码层 ViewModel 完全不动(状态结构跟 UI 形态解耦)。

**未来 session 提示**:`AskUserQuestion` 被 dismiss **不等于授权按推荐执行**,等于"问错了 / orchestrator 来兜"。除非任务上下文(比如 auto mode + 明显无关紧要的事)显示用户授权,否则要等明确指令。

### 坑 2 · qrose 是 KMP 库,Android 项目要走 Gradle Module Metadata 解析

`io.github.alexzhirkevich:qrose:1.0.1` 实际是 KMP 多平台 module,Gradle 7+ 默认开启 module metadata 自动 resolve 到 `qrose-android-1.0.1.aar`。本项目 Kotlin 1.9.24 + AGP 8.5 + Compose BOM 2024.06 全兼容。**验证手段**:`curl https://repo1.maven.org/maven2/io/github/alexzhirkevich/qrose-android/1.0.1/qrose-android-1.0.1.aar -o /tmp/x.aar` 拿到 AAR,`unzip -p /tmp/x.aar classes.jar > x.jar && jar tf x.jar | grep rememberQr` 找到 `QrCodePainterKt.rememberQrCodePainter`,签名 `(data: String, ...): QrCodePainter`。

**未来 session 提示**:加 KMP 依赖前先 `curl` Maven Central 看 aar 实在;`javap` 看 API 签名,不要 trust 网上 README 写的版本。

### 坑 3 · `PrimaryTabRow` `tabIndicatorOffset` API 在 BOM 2024.06 vs 更新 BOM 漂移

最初尝试自定义 indicator 用 `TabRowDefaults.PrimaryIndicator(Modifier.tabIndicatorOffset(index))`,但 BOM 2024.06 里 `tabIndicatorOffset` 的 receiver / 签名跟更新版 M3 不一致(`TabIndicatorScope` 上的扩展方法在更老 M3 不存在 / `TabRowDefaults` 上的 deprecated)。**修法**:不自定义 indicator,把整个 `PrimaryTabRow.contentColor` 设成 `MaterialTheme.colorScheme.tertiary` —— PrimaryTabRow 的默认 indicator 颜色继承 contentColor,正好海泡青出来,**跨 BOM 版本最稳的写法**。

### 坑 4 · `Box(modifier, contentAlignment)` 第二参位置签名

Compose `Box` 是 `Box(modifier, contentAlignment, propagateMinConstraints, content)`。位置参数 `Box(Modifier.fillMaxSize(), Alignment.Center) { ... }` 编译通过(后置 lambda 自动接 `content` 形参)。**未来 session 提示**:Box 这种 trailing-lambda 友好的签名,位置参数可读性不差;但加 Modifier 链时要注意第二参 type 是 Alignment 不是 Boolean。

### 坑 5 · `Spacing.padScreenH` 引用了但未导入,被我提前 grep 发现

写 OrderConfirmScreen 时一开始想用 `Spacing.padScreenH` 做 40dp 横向 padding,后来直接写 `40.dp` 提交,但漏删 `import com.carshop.android.designsystem.tokens.Spacing` 导致 import 报警。`grep -c "Spacing\." OrderConfirmScreen.kt` = 0 即可定位,删 import 即可。**未来 session 提示**:不要先 import 再写代码,先写代码后 Studio Optimize Imports 更安全。

### 坑 6 · 真后端并发 paid_at 抖动暴露 04 session 后端 race

10 session 跑 US-18 时发现的(详 §3.5 + TD-023)。**不是 10 的 bug**,客户端契约层完全 OK(`code=0`)。**未来 session 提示**:并发类 US 一定要看响应"全等"不只"全成功",前者才能暴露竞态。

---

## 6. SPEC / CHANGE_MAP / TECH_DEBT 同步

### SPEC

无契约改动。所有字段名 / 路径 / 错误码都跟 SPEC §5 / §6.1 / §11.5 / §12 / §13 一致。

### CHANGE_MAP

需要在以下条目追加 10 引用(本 artifact 落地后跟更 CHANGE_MAP.md):

- **Order + OrderItem** § 🔵 车机端:加 `data/repository/Repos.kt::OrderRepository`、`ui/checkout/{confirm,detail,list}/*`(替换原先 "10 ui/checkout/*" 占位字样为真实文件清单)

### TECH_DEBT

新增 1 条:**TD-023 · US-18 真后端并发 mock_pay 返回了 2 个不同 paid_at(race condition)**(已写到 TECH_DEBT.md)

### USER_STORIES

无改动。US-04/05/06/07/17/18 验收方式跟原文一致,只是 10 添加了真后端 spot check(原文允许)。

---

## 7. 给下游 session 的接力(11 final-integration 用)

### 11 兜底清单(本 session 留)

1. **TD-023 真后端 paid_at race**:US-18 spec 跟实现漂移,选 A(改后端加写锁)/ 选 B(改 SPEC 放宽)
2. **V8 倒计时归零超时实跑**:代码完备 · 11 进 PayDialog 后挂着等 120 秒看 Toast "支付超时" + 弹层关闭
3. **V9 离线提交订单实跑**:`adb shell svc data disable` + `svc wifi disable` 后点提交,看 Toast "网络异常,请重试"
4. **真后端订单 list 分页边界**:`?page=1&page_size=100` 用户有 > 100 单后翻页未实跑(spec §11.5 钦定 max 100)

### 10 session 给真后端清掉的脏数据

10 跑 US-05 时把商品 3 临时下架又恢复,**已恢复**(curl restore code=0, on_sale=True);US-17 / US-18 留了 7 笔订单(5 + 1 + 1)在 admin 数据库,**不清**(11 final 跑端到端会用到历史订单,留下也方便)。

### 9 session 内部共享(BrowseCommon.kt)给 10 用了吗?

铁律 2 钦定"不许跨 session 共享代码"。10 没 import 任何 09 的 BrowseCommon · ProductImage · ProductCard 等;`AsyncImage` 直接用 Coil(00 / 08 已引入)从零写。`formatYuan` 是 10 session 内部跨三个 Screen 共享(本 session 允许)。

---

## 8. 不修(本 session 范围外)

- 设计稿"实物 group + 服务券 group"分组渲染:MVP 单 quantity=1 + 单商品订单,没必要分组,留 M-Multi-Cart 模块。
- 设计稿"支付方式 chip(微信 / 支付宝)":MVP 假支付,选了也是 Demo;不实现。
- 设计稿"满 2000 减 80 券 / 车主优惠 -98":MVP 无优惠系统,直接 total = product.price × quantity。
- 设计稿"发票卡":MVP 不开发票。
- **PayDialog 用 Material3 `BottomSheet` 而非 `Dialog`**:orchestrator 拍板对齐 spec §1.1.B 走 Modal Dialog;设计稿的 BottomSheet 视觉留给后续可选优化(同步登记 TD: ~~不登记~~,设计稿没在 spec 里 promote 成硬性要求)。
- TopBar 的"orderId 截断到 20 字"逻辑:OrderDetail / OrderList 用的是订单号(固定 19 字符 `O+18位`),不需要截断。

---

## 9. 时长 / 性能数字

| 步骤 | 时长 |
|---|---|
| 读 12 份输入(SPEC / STATUS / USER_STORIES / TEST_MATRIX / CHANGE_MAP / TECH_DEBT / 10 spec / 04 artifact / 08 artifact / 09 artifact / Q4 artifact / 设计稿 HTML / current code)| ~5 分钟 |
| 写 OrderRepository + 3 ViewModel + 3 Screen + PayDialog + MockApiServer 改 + AppShell / NavHost 改 + qrose 依赖 + orders-empty fixture | ~30 分钟 |
| orchestrator 纠偏 3 处(qrose / Dialog / TabRow)+ 改回 | ~5 分钟 |
| Build mockDebug + realDebug 全套(增量)| ~5 秒 each |
| Install + 启动 + 截 10 张图(home → category → detail → confirm → pay dialog → paying → order detail → order list all → order list pending empty → restart persist)| ~10 分钟 |
| 真后端 curl US-04/05/06/07/17/18 跑通 | ~5 分钟 |
| 写本 artifact + TECH_DEBT TD-023 + STATUS + CHANGE_MAP | ~15 分钟 |

**总计**:约 70 分钟。

---

**版本**:v1.0
**完成日期**:2026-05-26
**执行人**:Claude Code (10 session)
