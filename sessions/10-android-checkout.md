# Session 10 · Android Checkout(车机端 · 下单 + 支付 + 订单中心)

> 实现"下单 → 模拟支付 → 我的订单"完整链路。Demo 的灵魂时刻。

---

## 1. 你要做什么

### 1.1 四个页面

#### A. 订单确认页(`Routes.OrderConfirm/{productId}`)

布局:
- Top Bar:返回 + "确认订单"
- 主区域(可滚):
  - **商品卡**(完整信息:图、标题、规格、单价、数量=1)
  - **收货信息卡**(只读,显示 SPEC §5.1 的默认值,标注"⚠️ Demo 演示·收货信息固定,后续会支持自定义")
  - **金额汇总卡**(商品小计 + 总计,大字号显示)
- 底部固定 CTA(80dp 高):
  - 左:总价(大字号)
  - 右:**"提交订单"** Primary Button

行为:
- 进入时若 productId 无效 → 错误页 + 返回按钮
- 点"提交订单":
  - 调 `POST /api/v1/orders` 创建订单
  - 成功 → 跳到 **模拟支付弹层**(覆盖在当前页之上,不跳 route)
  - 失败:
    - code 1001(商品不存在)→ Toast "商品已失效"
    - code 3000(商品已下架)→ Toast "商品已下架"
    - 网络错 → Toast "网络异常,请重试"

#### B. 模拟支付弹层(`PayDialog`,不是独立 route)

布局(Modal,radius 16dp,480dp 宽,内容居中):
- 顶部小字:**"扫码支付(Demo·点击任意位置完成支付)"**
- 中部:**480×480 二维码图**(用 ZXing 或 QRGenerator 库,内容随便填 "carshop://order/{orderId}")
- 二维码下:**倒计时**(price-mono 字体,起始 02:00 倒数)
- 底部:订单号 + 总金额

行为:
- 点击 Modal 任意位置:
  - 1 秒延迟(模拟扫码流程,给用户视觉反馈,**不算 motion 违规**——必要的状态反馈)
  - 调 `POST /api/v1/orders/{id}/mock_pay`
  - 成功 → 关闭弹层 → 跳到 `Routes.OrderDetail/{orderId}`,显示订单详情
  - 失败 → Toast + 关闭弹层
- 倒计时到 0:关闭弹层 + Toast "支付超时"

#### C. 订单详情(`Routes.OrderDetail/{orderId}`)

布局:
- Top Bar:返回 + "订单详情"
- 主区域(可滚):
  - **大状态徽章**:已支付 ✓(海泡青)/ 待支付(灰)
  - **订单号 + 时间**(plate-mono 字体)
  - **商品行**(从 `product_snapshot` 渲染:图 + 标题 + 规格 + 数量 + 单价)
  - **收货信息卡**
  - **金额汇总**

#### D. 我的订单(`Routes.OrderList`)

布局:
- Top Bar:返回 + "我的订单"
- Tab:全部 / 已支付 / 待支付(只用 status query)
- 主区域:订单卡列表(垂直)
  - 每张订单卡:订单号 + 商品标题 + 总价 + 状态 + 时间
  - 点卡片跳详情
- 空态:CarshopEmpty,文案"还没有订单,逛逛商品吧"

### 1.2 数据层

- `OrderRepository`:create / list / get / mockPay 四个方法
- `OrderConfirmViewModel`、`PayViewModel`、`OrderDetailViewModel`、`OrderListViewModel`

---

## 2. 你不要做什么

- ❌ 接真支付
- ❌ 实现收货地址编辑(地址写死,M-Address 是独立后续模块)
- ❌ 实现券核销(MVP 不做)
- ❌ 实现 banner / 浏览相关页(09 的事)
- ❌ 自己改订单字段(SPEC §5)
- ❌ 实现订单退款 / 取消
- ❌ 实现真扫码(假二维码即可,内容只是个 URL)

---

## 3. 输入

- `CLAUDE.md`、`SPEC.md`(尤其 §5.1 默认地址、§6.1 订单接口)
- `carshop-android/`(08、09、Q4 产物)
- `artifacts/00-design-system.md`(组件 + CarshopQrCodeBox)
- `artifacts/Q4-visual-alignment-09.md`(知道 09 视觉对齐过哪些点,保持风格一致)
- `artifacts/fixtures/04/`
- **必读视觉原型**(09 没读吃过亏,你不要):
  - `design/安卓商城/阶段4 · 屏7 · 订单确认+扫码支付.html`(订单确认页 + 扫码支付弹层)
  - `design/安卓商城/阶段3 · 组件库.html`(CarshopQrCodeBox 等)
  - `design/安卓商城/components/_components-car.css`(精确数值在 CSS,优先于 tokens.json 语义命名)
- ⚠️ **MVP 砍掉的元素禁止还原**:登录 / 搜索 / 购物车 / 我的券,详见 Q4 spec §1.3

---

## 4. 输出

- `carshop-android/app/src/main/java/com/carshop/android/ui/checkout/`
  - `confirm/` OrderConfirmScreen + ViewModel
  - `pay/` PayDialog + ViewModel
  - `detail/` OrderDetailScreen + ViewModel
  - `list/` OrderListScreen + ViewModel
- `data/repository/OrderRepository.kt`
- 在 `CarshopNavHost` 替换占位为真页面
- `artifacts/10-android-checkout.md`:
  - 4 张截图
  - 状态机说明(订单 status 流转)
  - 已踩坑

---

## 5. 验收

完整跑一遍(真后端模式,**Mock 模式不验收**——本 session 涉及订单的 device_id,要真后端):

| # | 场景 | 期望 |
|---|---|---|
| 1 | 详情页点立即购买 | 跳订单确认 |
| 2 | 订单确认页 | 显示商品 / 默认地址 / 总价(单价×1) |
| 3 | 点提交订单 | 网络调用 → 出现支付弹层 + 二维码 + 倒计时 |
| 4 | 点支付弹层 | 1 秒后关闭弹层,跳订单详情,状态"已支付" |
| 5 | 点 Rail "我的订单" | 列表里看到这单 |
| 6 | 重启 App | "我的订单"里这单还在(device_id 持久关联) |
| 7 | 重复点支付按钮 | 幂等,不重复创建订单 |
| 8 | 等倒计时到 0 | Toast 超时,关闭弹层 |
| 9 | 离线提交订单 | Toast 网络错,弹层不弹 |
| 10 | 切到一个下架商品 | "立即购买"被 09 disable 了,不会进 confirm 页 |

---

## 6. 依赖

- **上游**:08、09
- **下游**:11 final-integration

## 7. Mock 策略

可以用 mock 完成 UI,但**验收用真后端**(订单的 device_id / 状态机得真跑)。

## 8. 已知坑

1. **二维码生成**:用 `com.google.zxing:core` + `zxing.WriterException`,Composable 里转 Bitmap → Image。或者更简单用 `com.lightspark:compose-qr-code`
2. **倒计时**:`LaunchedEffect(orderId)` + `delay(1000)` 循环,组件销毁时 cancel
3. **device_id 自动注入**:08 的 Interceptor 已实现,本 session 不要再手动塞
4. **OrderDetail 用 product_snapshot**:不要再去查商品(商品可能已经被改),用快照
5. **金额格式化**:9500 (分) → "¥95.00"(用 `String.format("¥%.2f", amount / 100.0)`)
6. **PayDialog 用 Modal,不是新 Activity / 新 Composable Screen**:就在 OrderConfirmScreen 上 overlay,这样关闭流畅
7. **支付成功 → 跳详情**:用 `navController.navigate(...) { popUpTo(home) }`,清掉 confirm 这一层,避免返回回到 confirm 页又能再支付
8. **设备 ID 持久性**:Settings.Secure.ANDROID_ID 在同一应用 + 同一用户(MVP 不考虑多用户)是稳定的;重装 App 会变(预期内,Demo 不解决)
