# Session 04 · Server Order & Mock Pay API(订单 + 模拟支付)

> 订单创建、查询、模拟支付。**Demo 阶段不接真支付**,但订单状态机要做对。

---

## 1. 你要做什么

### 1.1 公开接口(车机用,需要 `X-Device-Id` header)

| Method | Path | 说明 |
|---|---|---|
| POST | `/api/v1/orders` | 创建订单。body: `{product_id, quantity}`(MVP quantity 固定 1,但参数还是接收) |
| GET | `/api/v1/orders?status=&page=&page_size=` | 当前设备的订单列表(按 `X-Device-Id` 过滤),status 可选:`pending` / `paid` |
| GET | `/api/v1/orders/:id` | 订单详情(只能查当前设备的,跨设备查 → code 1001) |
| POST | `/api/v1/orders/:id/mock_pay` | 模拟支付,把状态推到 `paid`,写 `paid_at` |

### 1.2 后台接口(`Depends(require_admin)`)

| Method | Path | 说明 |
|---|---|---|
| GET | `/api/v1/admin/orders?status=&page=&page_size=` | 所有订单(不按设备过滤) |
| GET | `/api/v1/admin/orders/:id` | 订单详情 |

### 1.3 业务规则

#### 创建订单
1. 入参校验:`product_id` 必填,`quantity` 默认 1
2. 取出 X-Device-Id(从 header,见 SPEC §11.5)。**没传 → code 1000**
3. 查 product:不存在 → code 1001;`on_sale=false` → code 3000("商品已下架")
4. 生成订单号(见 SPEC §13:`O{YYYYMMDDHHMMSS}{4位随机}`)
5. 写 Order:`status="pending"`、`total_amount = product.price * quantity`、`shipping_info = SPEC.md §5.1 默认值`、`device_id = X-Device-Id`
6. 写 OrderItem:`product_snapshot_json = {id, title, product_type, price, spec, main_image_url}`(下单时商品快照)
7. 返回完整订单(含 items)

#### 模拟支付
1. 取订单(只能取当前 X-Device-Id 的)
2. 已 paid → 幂等返回成功(`code: 0`,数据照常返回)
3. status 不是 pending → code 3000("订单状态不支持支付")
4. 设 `status=paid`、`paid_at=now`
5. 返回订单

### 1.4 响应字段

#### Order(订单)
```json
{
  "id": "O20260525123030001",
  "device_id": "abc123def456",
  "status": "paid",
  "total_amount": 9500,
  "shipping_info": {
    "name": "车主",
    "phone": "138****0000",
    "address": "上海市浦东新区世纪大道 100 号"
  },
  "items": [
    {
      "id": 1,
      "product_id": 5,
      "product_snapshot": {
        "id": 5,
        "title": "中石化加油卡 100 元",
        "product_type": "service_voucher",
        "price": 9500,
        "spec": "100 元面值",
        "main_image_url": "https://carshop.hearagain.space/static/uploads/abc.png"
      },
      "quantity": 1,
      "price": 9500
    }
  ],
  "created_at": "2026-05-25T12:30:00+08:00",
  "paid_at": "2026-05-25T12:31:05+08:00"
}
```

后台接口的订单详情**多一个字段** `device_id_short`(取后 6 位,前端展示用):

```json
{ ..., "device_id_short": "f456" }
```

---

## 2. 你不要做什么

- ❌ 接真支付(微信/支付宝)
- ❌ 写履约 / 发卡密 / 发货逻辑
- ❌ 自动扣库存
- ❌ 退款
- ❌ 改 DB schema

---

## 3. 输入

- `CLAUDE.md`、`SPEC.md`
- `carshop-server/`、`artifacts/01-server-foundation.md`
- `artifacts/02-server-catalog.md`(查商品要用 product 模型)

---

## 4. 输出

- `carshop-server/app/routers/order.py`(公开)
- `carshop-server/app/routers/admin_order.py`(后台)
- `carshop-server/app/schemas/order.py`
- `carshop-server/app/services/order_service.py`(订单号生成、快照逻辑)
- 路由挂载
- `artifacts/04-server-order-pay.md`:接口文档 + cURL + fixtures
- `artifacts/fixtures/04/`:
  - `post-order-success.json`
  - `post-order-product-not-found.json`(code 1001)
  - `post-order-no-device-id.json`(code 1000)
  - `get-orders-list.json`
  - `get-order-detail.json`
  - `post-mock-pay-success.json`
  - `post-mock-pay-already-paid.json`(幂等成功)

---

## 5. 验收

```bash
# 创建订单
curl -X POST http://localhost:8000/api/v1/orders \
  -H "X-Device-Id: test-device-abc" \
  -H "Content-Type: application/json" \
  -d '{"product_id":1,"quantity":1}'
# 返回订单,状态 pending

ORDER_ID="<上面返回的 id>"

# 模拟支付
curl -X POST http://localhost:8000/api/v1/orders/$ORDER_ID/mock_pay \
  -H "X-Device-Id: test-device-abc"
# 返回订单,状态 paid

# 列表
curl "http://localhost:8000/api/v1/orders?status=paid" \
  -H "X-Device-Id: test-device-abc"
# 包含上面这单

# 跨设备查不到
curl http://localhost:8000/api/v1/orders/$ORDER_ID \
  -H "X-Device-Id: other-device"
# code=1001

# 缺 X-Device-Id
curl -X POST http://localhost:8000/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"product_id":1,"quantity":1}'
# code=1000
```

---

## 6. 依赖

- **上游**:01(DB)、02(查商品要 Product 表存在 + ORM)
- **下游**:06 admin、09/10 android、07 早集成

---

## 7. Mock 策略

不需要。

---

## 8. 已知坑

1. **订单号生成可能并发碰撞**:Demo 量小不会触发,但代码里加一层 `try/retry`(最多 3 次,每次重新生成)
2. **快照字段**:product_snapshot_json 存的是**下单当时**的商品状态,后续即使商品改名/改价/删除,订单显示仍是当时的。`OrderItem.price` 字段也是当时单价
3. **跨设备查不到**:不是 403 而是 404(code 1001),不要泄露"订单存在但不属于你"
4. **mock_pay 必须幂等**:重复调用不报错,直接返回当前状态
5. **device_id 取 X-Device-Id**(SPEC §11.5),不要从 body 读
6. **shipping_info 默认值**:在 service 层硬编码 SPEC §5.1 的值,不要 hardcode 散落各处
