# Artifact · Session 04 · Server Order & Mock Pay API

**完成日期**:2026-05-25
**对应 session spec**:`sessions/04-server-order-pay-api.md`
**状态**:✅ 已完成 · 11 条实跑(覆盖 session spec §5 验收 + 4 个错误码触发 + 2 个后台接口)全部通过

---

## 一句话总结

订单 4 个公开接口 + 2 个后台只读接口落地。订单号自己生成、商品快照写进 OrderItem、模拟支付状态机做对、跨设备隔离用 1001(不泄露存在)、mock_pay 幂等。**fixtures 已导出 11 个 JSON**,前端可直接拿去 mock。

---

## 1. 验收清单(对照 session spec §5)

| # | 项 | 状态 | 实跑证据(curl 出处) |
|---|---|---|---|
| 1 | `POST /api/v1/orders` 成功,返回 pending 订单 | ✅ | [post-order-success.json](fixtures/04/post-order-success.json) |
| 2 | 缺 X-Device-Id → code=1000 | ✅ | [post-order-no-device-id.json](fixtures/04/post-order-no-device-id.json) |
| 3 | 商品不存在 → code=1001 | ✅ | [post-order-product-not-found.json](fixtures/04/post-order-product-not-found.json) |
| 4 | 商品已下架 → code=3000 | ✅ | [post-order-product-off-sale.json](fixtures/04/post-order-product-off-sale.json) |
| 5 | `POST /orders/:id/mock_pay` 成功 → status=paid + paid_at | ✅ | [post-mock-pay-success.json](fixtures/04/post-mock-pay-success.json) |
| 6 | 重复 mock_pay 幂等(code=0,paid_at 不变) | ✅ | [post-mock-pay-already-paid.json](fixtures/04/post-mock-pay-already-paid.json)(paid_at 与 #5 相同) |
| 7 | `GET /orders?status=paid` 列表,分页结构正确 | ✅ | [get-orders-list.json](fixtures/04/get-orders-list.json) |
| 8 | `GET /orders/:id` 详情 | ✅ | [get-order-detail.json](fixtures/04/get-order-detail.json) |
| 9 | 跨设备查 → code=1001("订单不存在",不暴露存在) | ✅ | [get-order-detail-cross-device.json](fixtures/04/get-order-detail-cross-device.json) |
| 10 | `GET /admin/orders` 后台列表,带 device_id_short | ✅ | [admin-get-orders-list.json](fixtures/04/admin-get-orders-list.json) |
| 11 | `GET /admin/orders/:id` 后台详情 | ✅ | [admin-get-order-detail.json](fixtures/04/admin-get-order-detail.json) |

---

## 2. 交付物

| 文件 | 行数 | 作用 |
|---|--:|---|
| [carshop-server/app/schemas/order.py](../carshop-server/app/schemas/order.py) | 19 | `OrderCreateIn` 入参 + `OrderStatusLiteral` |
| [carshop-server/app/services/order_service.py](../carshop-server/app/services/order_service.py) | 160 | 订单号生成 / 默认 shipping / 快照 / 序列化 / `create_order` / `mock_pay` |
| [carshop-server/app/services/__init__.py](../carshop-server/app/services/__init__.py) | 0 | 包初始化 |
| [carshop-server/app/routers/order.py](../carshop-server/app/routers/order.py) | 102 | 4 个公开接口 |
| [carshop-server/app/routers/admin_order.py](../carshop-server/app/routers/admin_order.py) | 54 | 2 个后台接口(挂 `Depends(require_admin)`) |
| `carshop-server/app/main.py` | +3 | import + 2 行 `include_router` |
| `artifacts/fixtures/04/*.json` | 11 个 | 全部 curl 真实响应 |

> ⚠️ 没碰 02/03/05 的文件。`app/deps.py` 里的 `require_admin` 桩**已经存在**(02 写的),我直接 `import` 复用,**没改任何一行**。

---

## 3. 接口对照(SPEC §6.1 + §6.2)

| Method | Path | 状态 | 备注 |
|---|---|---|---|
| POST | `/api/v1/orders` | ✅ 100% 对齐 SPEC | 入参 `{product_id, quantity}`,quantity 默认 1 |
| GET | `/api/v1/orders` | ✅ 100% 对齐 SPEC | 参数 `status?` + `page` + `page_size` |
| GET | `/api/v1/orders/:id` | ✅ 100% 对齐 SPEC | 跨设备 → 1001 |
| POST | `/api/v1/orders/:id/mock_pay` | ✅ 100% 对齐 SPEC | 幂等 |
| GET | `/api/v1/admin/orders` | ✅ 100% 对齐 SPEC | 不按设备过滤,响应多 `device_id_short` |
| GET | `/api/v1/admin/orders/:id` | ✅ 100% 对齐 SPEC | 同上 |

### 3.1 cURL 调用例

```bash
# 1. 创建订单
curl -X POST http://localhost:8000/api/v1/orders \
  -H "X-Device-Id: test-device-abc" \
  -H "Content-Type: application/json" \
  -d '{"product_id":4,"quantity":1}'

# 2. 模拟支付(用上面返回的 id)
curl -X POST http://localhost:8000/api/v1/orders/O202605251857146279/mock_pay \
  -H "X-Device-Id: test-device-abc"

# 3. 列表
curl "http://localhost:8000/api/v1/orders?status=paid&page=1&page_size=20" \
  -H "X-Device-Id: test-device-abc"

# 4. 详情(跨设备查 → 1001)
curl http://localhost:8000/api/v1/orders/O202605251857146279 \
  -H "X-Device-Id: other-device"

# 5. 后台列表
curl http://localhost:8000/api/v1/admin/orders

# 6. 后台详情
curl http://localhost:8000/api/v1/admin/orders/O202605251857146279
```

### 3.2 响应字段(创建订单实跑 · 见 `post-order-success.json`)

```json
{
  "code": 0,
  "data": {
    "id": "O202605251857146279",
    "device_id": "test-device-abc",
    "status": "pending",
    "total_amount": 9500,
    "shipping_info": {
      "name": "车主",
      "phone": "138****0000",
      "address": "上海市浦东新区世纪大道 100 号"
    },
    "items": [
      {
        "id": 1,
        "product_id": 4,
        "product_snapshot": {
          "id": 4,
          "title": "中石化加油卡 100 元",
          "product_type": "service_voucher",
          "price": 9500,
          "spec": "100 元面值",
          "main_image_url": "https://picsum.photos/seed/fuel100/600/400"
        },
        "quantity": 1,
        "price": 9500
      }
    ],
    "created_at": "2026-05-25T18:57:14.033006+08:00",
    "paid_at": null
  },
  "message": "ok"
}
```

后台接口的响应多一个 `device_id_short`(取 device_id 后 6 位):

```json
{ "data": { ..., "device_id_short": "ce-abc" } }
```

### 3.3 错误码触发清单

| code | 触发条件 | fixture |
|---:|---|---|
| 1000 | `POST /orders` 时未传 `X-Device-Id` header | [post-order-no-device-id.json](fixtures/04/post-order-no-device-id.json) |
| 1001 | `POST /orders` 时 product_id 不存在 | [post-order-product-not-found.json](fixtures/04/post-order-product-not-found.json) |
| 1001 | `GET /orders/:id` 跨设备查(订单存在但不属于当前 device_id) | [get-order-detail-cross-device.json](fixtures/04/get-order-detail-cross-device.json) |
| 1001 | `GET /admin/orders/:id` 查不存在的 order_id | — 同上模式 |
| 3000 | 商品 `on_sale=false` 还想下单 | [post-order-product-off-sale.json](fixtures/04/post-order-product-off-sale.json) |

> mock_pay 没有 3000 触发示例,因为 Demo 阶段订单状态只有 pending / paid 两种,创建后立刻就是 pending,paid 之后再调走幂等返回 0。要触发 "订单状态不支持支付" 需要数据库里手动改个非法 status。

---

## 4. OpenAPI 片段(由 FastAPI `/openapi.json` 摘取)

完整片段(本 session 涉及的 6 个 path + `OrderCreateIn`/`HTTPValidationError`/`ValidationError` 三个 schema)放在 [fixtures/04/openapi-fragment.json](fixtures/04/openapi-fragment.json)。

关键 schema:

```json
"OrderCreateIn": {
  "properties": {
    "product_id": {"type": "integer", "minimum": 1, "title": "Product Id"},
    "quantity":   {"type": "integer", "minimum": 1, "title": "Quantity", "default": 1}
  },
  "type": "object",
  "required": ["product_id"]
}
```

---

## 5. SPEC.md 改动记录

**无改动。** 数据模型、API 契约、错误码、URL 规范、字段名 100% 走 SPEC.md,没引入任何新字段、新错误码、新路径。

> 用户已在对话里说要顺手把 SPEC §5 那个示例订单号 `O20260525120030001` 换掉(那个 `001` 看起来像序号,实际算法是 4 位随机),由用户自己改,我不动。

---

## 6. 已踩过的坑 / 实现注意

### 6.1 `require_admin` 复用 02 的桩(不重复加)

session spec 让本 session 在没有 05 的情况下也能跑后台接口,我原计划是自己在 `app/deps.py` 加一个临时占位。**结果发现 02 已经写好了**(见 `app/deps.py:23-30`,注释里就是 `TODO(05): replace stub` 那行)。直接 `from app.deps import require_admin` 用,**一行都没动**。02/03/04 三个并行 session 因此不会在 `deps.py` 上出文件冲突。

### 6.2 `app/services/` 目录是本 session 新建

02/03 没建 services 层,我按 session spec §4 要求新建。带 `__init__.py`,以 `from app.services import order_service` 形式 import。05 / 06 后续如果也要加 service 层,继续往这个目录加文件即可,**不会跟我冲突**。

### 6.3 订单号生成 · 4 位随机 + retry 3

```python
def _generate_order_id() -> str:
    ts = datetime.now(_CST).strftime("%Y%m%d%H%M%S")
    return f"O{ts}{random.randint(1000, 9999)}"
```

实跑生成:`O202605251857146279`(18:57:14 时刻 + 6279 随机)。Demo 量小不会碰撞,代码里有 retry 3 次 + 兜底再多 4 位的保险。

### 6.4 跨设备查不到 → 统一 1001("订单不存在")

session spec §8.3 + SPEC §11.5 要求**不泄露存在**。实现方式:`_get_order_for_device(db, order_id, device_id)` 在 SQL 上直接 `WHERE device_id=...`,查不到一律 `NOT_FOUND`,**不区分**"订单不存在"和"订单存在但不属于你"。前端收到的 message 一律是 "订单不存在",没法反推。

### 6.5 时间格式 · 统一 +08:00

SPEC §13 要求 ISO 8601 字符串。`shipping_info_dict` 和 `paid_at` 输出统一转 `+08:00`(中国标准时区)。DB 里存 UTC,序列化时 `astimezone(_CST)`。如果某条记录 `tzinfo=None`(理论上不该有,但 SQLite + SQLAlchemy 在某些场景会丢 tz),先按 UTC 当作底料再转。

### 6.6 mock_pay 幂等 · 已 paid 直接返回(不改 paid_at)

实跑:第一次支付 `paid_at = 2026-05-25T18:57:37.065841+08:00`,第二次调同接口返回的 `paid_at` 一字不差。这意味着**已 paid 时不再 commit DB**(`order.status == "paid"` → 直接 `return order`)。

### 6.7 product_snapshot 里的图片 URL · 序列化时再兜底拼一次

存入 DB 时已经是完整 URL(`https://picsum.photos/...`),但万一 05 上传后存进商品的是相对路径(`/static/uploads/xxx.png`),那个相对路径会被原样塞进快照。`_item_to_dict` 在序列化时再走一次 `absolutize_url`,跨场景都能保证返回完整 URL(SPEC §13)。

### 6.8 `app/main.py` 是并发改动的热点

01 之后 02 / 03 / 04 都要在 `main.py` 末尾加 `include_router`。我本次合并到的版本里 02 (category + product) 已经入主了。挂法跟 banner 一致:

```python
from app.routers import admin_order, banner, category, health, order, product
...
app.include_router(order.router, prefix="/api/v1")
app.include_router(admin_order.router, prefix="/api/v1")
```

> 注意:02 用 `category.public_router` + `category.admin_router` 两个 router 拆开挂,我跟 banner 一样**一个文件一个 router**(`order.router` / `admin_order.router`),通过完整路径区分 `/orders` vs `/admin/orders`。两种风格在本仓库并存,功能等价。

---

## 7. 下游 session 需要知道的事

### 给 06 admin-web(后台前端)

- 后台只有两个**只读**接口:`GET /api/v1/admin/orders` 和 `GET /api/v1/admin/orders/:id`,**没有**创建 / 编辑 / 删除订单的接口(MVP 范围)
- 列表返回 `{list, total, page, page_size}`,每条订单含 `device_id_short`(后 6 位),前端列表表头展示这个字段即可
- `total_amount` 单位是「分」,展示时除 100
- `status` 只有 `pending` 和 `paid` 两个值,可以做 tab 切换
- 当前后台接口**鉴权是桩**(永远放行),05 接入真鉴权后前端要带 cookie(05 session 会处理)

### 给 09 android-browse / 10 android-checkout(车机端)

- **必须**带 `X-Device-Id` header,不带直接 1000。车机端用 `Settings.Secure.ANDROID_ID`
- 创建订单只需要 `{product_id, quantity}`,**不要**传 device_id 到 body(SPEC §11.5)
- `shipping_info` 是服务端写死的,**前端不要传**(传了也不生效,以服务端为准)
- 下单成功后立刻拿到完整订单(含订单号),后续支付直接 `POST /orders/{id}/mock_pay`
- mock_pay **必须**也带 `X-Device-Id`,跨设备的订单返回 1001
- 列表分页:`page=1` 起,`page_size` 默认 20,最大 100。`status` 不传 → 返回 pending + paid 全部
- fixtures 已经在 `artifacts/fixtures/04/` 全套就绪,Mock Web Server 直接读这些 JSON 当响应

### 给 07 early-integration(早集成)

- 服务端启动:`carshop-server/scripts/dev.sh`,默认 `http://localhost:8000`
- 部署到 Mac mini 后,`BASE_URL` 切到 `https://carshop.hearagain.space`,所有快照里的 `main_image_url` 也会自动跟着换(seed 里是 picsum 的完整 URL,不受影响;05 上传的相对路径会跟着 BASE_URL 走)
- 没有跑数据库 migration,直接复用 01 的 schema

### 给 05 admin-auth-upload(等它接手鉴权)

- 我 import 的是 `from app.deps import require_admin`,05 替换函数实现即可,**我的 router 不需要改**
- 后台两个接口用 `dependencies=[Depends(require_admin)]` 挂在路由层(参考 `admin_order.py:22 / 49`)
- 当前桩永远返回 None,05 改成校验失败时 `raise CarshopException(ErrorCode.UNAUTHORIZED)` 即可

---

## 8. 数据库变化

**无 schema 变化**。完全用 01 已建好的 `orders` + `order_items` 表,字段一个没加。

11 次实跑后数据库里新增 1 条订单(ID `O202605251857146279`,状态 paid)。下次跑 `python scripts/init_db.py` 会重置数据库(01 artifact §3 说 init_db 重复跑安全 = drop & recreate)。

---

## 9. 启动命令(实跑)

```bash
cd carshop-server
source .venv/bin/activate
uvicorn app.main:app --host 127.0.0.1 --port 8000
# 起来后 30 个 route 挂上
# 8 个本 session 的:POST /orders, GET /orders, GET /orders/:id,
# POST /orders/:id/mock_pay, GET /admin/orders, GET /admin/orders/:id
# (FastAPI 把 OPTIONS 自动算进去)
```

---

**版本**:v1.0
**实跑验证**:11 条 curl 在 `http://127.0.0.1:8000` 上全部通过(见 `fixtures/04/*.json`)
