# Artifact · Session 02 · Server Catalog API(分类 + 商品接口)

**完成日期**:2026-05-25
**对应 session spec**:`sessions/02-server-catalog-api.md`
**状态**:✅ 已完成 · 20 条验收 + 4 项契约统一全部实跑通过

---

## 一句话总结

在 `carshop-server/` 加上**分类**和**商品**的公开 + 后台接口,严格对照 SPEC §6.1 / §6.2 / §12 / §13;`require_admin` 走 02 桩(永远放行,等 05 替换);新建工具 `app/utils/url.py` 统一处理图片 URL 拼接,`app/utils/datetime.py` 统一带时区的 ISO 8601 序列化。全部 fixtures 已落到 `artifacts/fixtures/02/`。

---

## 1. 实际交付物清单

### 新增文件

| 文件 | 用途 |
|---|---|
| `carshop-server/app/utils/__init__.py` | utils 包占位 |
| `carshop-server/app/utils/url.py` | `absolutize_url(value)`:把 DB 里的相对路径拼成完整 URL,完整 URL 原样返回 |
| `carshop-server/app/utils/datetime.py` | `to_iso(dt)`:naive datetime 当 UTC,统一序列化成 ISO 8601 带时区(SQLite 不保留 tz,这里补上) |
| `carshop-server/app/schemas/common.py` | `Page[T]` 泛型分页 envelope(虽然本 session 实际用 dict 直接拼,留给后续 session 复用) |
| `carshop-server/app/schemas/category.py` | `CategoryOut` / `CategoryCreate` / `CategoryUpdate` |
| `carshop-server/app/schemas/product.py` | `ProductOut` / `ProductCreate` / `ProductUpdate` / `ProductOnSalePatch` |
| `carshop-server/app/routers/category.py` | 分类路由:`public_router` + `admin_router` |
| `carshop-server/app/routers/product.py` | 商品路由:`public_router` + `admin_router` |

### 修改文件

| 文件 | 改动 |
|---|---|
| `carshop-server/app/deps.py` | 新增 `require_admin()` 桩(永远放行,带 `# TODO(05): replace stub`) |
| `carshop-server/app/main.py` | `include_router` 挂载本 session 的 4 个 router(public/admin × category/product) |

### Fixtures(导出到 `artifacts/fixtures/02/`)

| 文件 | 对应接口 |
|---|---|
| `openapi-02.json` | 从 `/openapi.json` 抽出本 session 涉及的 8 条 paths |
| `get-categories.json` | `GET /api/v1/categories` 成功 |
| `get-products.json` | `GET /api/v1/products` 默认参数 |
| `get-products-by-category.json` | `GET /api/v1/products?category_id=2` |
| `get-products-paged.json` | `GET /api/v1/products?page=1&page_size=5` |
| `get-product-1.json` | `GET /api/v1/products/1` 详情 |
| `get-product-404.json` | `GET /api/v1/products/99999` → code=1001 |
| `get-admin-products.json` | `GET /api/v1/admin/products` 默认参数(含下架商品) |
| `get-admin-products-offsale.json` | `GET /api/v1/admin/products?on_sale=false` |
| `post-category-success.json` | `POST /api/v1/admin/categories` 成功 |
| `put-category-success.json` | `PUT /api/v1/admin/categories/:id` 成功 |
| `delete-category-conflict.json` | `DELETE /api/v1/admin/categories/1` → code=1002 |
| `post-product-success.json` | `POST /api/v1/admin/products` 成功 |
| `put-product-success.json` | `PUT /api/v1/admin/products/:id` 成功 |
| `patch-on-sale.json` | `PATCH /api/v1/admin/products/:id/on_sale` |
| `delete-product-success.json` | `DELETE /api/v1/admin/products/:id` 成功 |
| `post-product-1000.json` | `POST /api/v1/admin/products` 缺字段 → code=1000 |

---

## 2. 接口对照(SPEC §6.1 / §6.2)

### 2.1 公开接口

| Method | Path | 状态 | 备注 |
|---|---|---|---|
| GET | `/api/v1/categories` | ✅ 已实现 | 按 `sort` 升序,5 个 seed 分类全部返回,`icon_url` 已 absolutize |
| GET | `/api/v1/products` | ✅ 已实现 | 只返回 `on_sale=true`,分页 envelope `{list,total,page,page_size}`,每条带 `category_name` |
| GET | `/api/v1/products/:id` | ✅ 已实现 | 下架商品也能返回(前端按 `on_sale` 字段处理) |

### 2.2 后台接口(全部挂 `Depends(require_admin)`,02 桩永远放行)

| Method | Path | 状态 | 备注 |
|---|---|---|---|
| GET | `/api/v1/admin/categories` | ✅ 已实现 | 全部分类不过滤,按 `sort` 升序 |
| POST | `/api/v1/admin/categories` | ✅ 已实现 | `{name, icon_url, sort}` |
| PUT | `/api/v1/admin/categories/:id` | ✅ 已实现 | 字段全可选 |
| DELETE | `/api/v1/admin/categories/:id` | ✅ 已实现 | 下属商品 > 0 → code=1002(HTTP 409) |
| GET | `/api/v1/admin/products` | ✅ 已实现 | query: `category_id` / `on_sale` / `page` / `page_size`,不过滤上架状态 |
| POST | `/api/v1/admin/products` | ✅ 已实现 | 必填:`category_id, title, product_type, price`;非法分类 → code=1001 |
| GET | `/api/v1/admin/products/:id` | ✅ 已实现 | 不存在 → code=1001 |
| PUT | `/api/v1/admin/products/:id` | ✅ 已实现 | 字段全可选,**不含 on_sale**(走专门 PATCH) |
| DELETE | `/api/v1/admin/products/:id` | ✅ 已实现 | **硬删**;先把 `order_items.product_id` 置 NULL 保留下单快照(SPEC §5 设计) |
| PATCH | `/api/v1/admin/products/:id/on_sale` | ✅ 已实现 | body: `{on_sale: bool}` |

### 2.3 字段示例(精确格式)

**Category**:
```json
{
  "id": 1,
  "name": "汽车用品",
  "icon_url": "http://localhost:8000/static/icons/car-parts.svg",
  "sort": 1,
  "created_at": "2026-05-25T10:28:43.500633+00:00"
}
```

**Product**:
```json
{
  "id": 1,
  "category_id": 1,
  "category_name": "汽车用品",
  "title": "车载磁吸手机支架",
  "product_type": "physical",
  "price": 8900,
  "original_price": 12900,
  "spec": "通用款 · 银色",
  "main_image_url": "https://picsum.photos/seed/carmount/600/400",
  "description": "...",
  "on_sale": true,
  "created_at": "2026-05-25T10:28:47.053214+00:00",
  "updated_at": "2026-05-25T10:28:47.053218+00:00"
}
```

**分页 envelope**(放在 `data` 里):
```json
{
  "code": 0,
  "data": {
    "list": [...],
    "total": 12,
    "page": 1,
    "page_size": 20
  },
  "message": "ok"
}
```

---

## 3. 实际 cURL 调用例

### 3.1 公开接口

```bash
# 分类列表
$ curl http://localhost:8000/api/v1/categories
{"code":0,"data":[5 categories],"message":"ok"}

# 商品列表(默认)
$ curl http://localhost:8000/api/v1/products
→ total=12, page=1, page_size=20, list 含 12 条(全部 on_sale=true)

# 按分类筛
$ curl 'http://localhost:8000/api/v1/products?category_id=2'
→ total=3, list 是 [id=4,5,6] 加油充电下的商品

# 分页
$ curl 'http://localhost:8000/api/v1/products?page=1&page_size=5'
→ total=12, list_len=5

# 详情
$ curl http://localhost:8000/api/v1/products/1
→ 含 category_name="汽车用品"

# 不存在
$ curl http://localhost:8000/api/v1/products/99999
HTTP 404 · {"code":1001,"data":null,"message":"商品不存在"}
```

### 3.2 后台接口(02 桩免登录)

```bash
# 新建分类
$ curl -X POST http://localhost:8000/api/v1/admin/categories \
    -H 'Content-Type: application/json' \
    -d '{"name":"临时分类","icon_url":"/static/icons/temp.svg","sort":99}'
→ 返回新 id=6,icon_url 自动 absolutize 为 "http://localhost:8000/static/icons/temp.svg"

# 删有商品的分类 → 1002
$ curl -X DELETE http://localhost:8000/api/v1/admin/categories/1
HTTP 409 · {"code":1002,"data":null,"message":"该分类下还有 3 个商品,请先移除或删除商品"}

# 新建商品(用相对路径图片地址)
$ curl -X POST http://localhost:8000/api/v1/admin/products \
    -H 'Content-Type: application/json' \
    -d '{"category_id":1,"title":"测试","product_type":"physical","price":1000,
         "main_image_url":"/static/uploads/test.png","description":"x","on_sale":true}'
→ 响应里 main_image_url = "http://localhost:8000/static/uploads/test.png"(absolutize 验证)

# PATCH on_sale → 详情仍可见,但公开列表不含
$ curl -X PATCH http://localhost:8000/api/v1/admin/products/3/on_sale \
    -H 'Content-Type: application/json' -d '{"on_sale":false}'
$ curl http://localhost:8000/api/v1/products/3  # 仍能查到,字段 on_sale=false
$ curl http://localhost:8000/api/v1/products     # 列表里没有 id=3

# 后台查下架商品
$ curl 'http://localhost:8000/api/v1/admin/products?on_sale=false'
→ total=1, list=[{id:3,...}]

# 缺字段 → 1000
$ curl -X POST http://localhost:8000/api/v1/admin/products \
    -H 'Content-Type: application/json' -d '{"title":"漏字段"}'
HTTP 400 · {"code":1000,"data":null,"message":"Field required"}
```

---

## 4. 错误码覆盖(SPEC §12)

| code | 触发场景 | 验证 |
|---|---|---|
| 0 | 所有正常路径 | ✅ |
| 1000 | POST/PUT/PATCH 入参缺字段 / 类型错(由 RequestValidationError 全局处理转 1000) | ✅ HTTP 400 |
| 1001 | 商品/分类 ID 不存在;POST/PUT product 时 `category_id` 不存在 | ✅ HTTP 404 |
| 1002 | DELETE 有商品的分类 | ✅ HTTP 409 |

`2000` / `3000` 不在本 session 范围(分别由 05 / 04 触发)。

---

## 5. SPEC.md 改动记录

**无改动。** 本 session 完全按 SPEC §5 / §6 / §12 / §13 实现,字段名、HTTP 码、URL 前缀、分页 envelope 全部对齐。

---

## 6. 已踩过的坑 / 临时方案

### 坑 1:SQLite 不保留 timezone,默认序列化丢 tz

**现象**:数据库列定义是 `DateTime(timezone=True)`,seed 写入用 `datetime.now(timezone.utc)`(aware),但 SQLite 用 ISO 字符串存,读回来变 naive datetime。Pydantic v2 默认对 naive datetime 序列化成 `2026-05-25T10:28:43.500633`(无 tz 偏移),违反 SPEC §13。

**修法**:新建 `app/utils/datetime.py`,`to_iso(dt)`:naive 当 UTC,统一返回 `2026-05-25T10:28:43.500633+00:00`。每个 schema 的 datetime 字段加 `@field_serializer("created_at"...)` 走它。

**影响**:03 / 04 session 在自己的 schema 里要做同样处理,**复用 `app.utils.datetime.to_iso`** 即可,不要重复实现。

### 坑 2:`require_admin` 桩是空函数,但路由必须挂上 `Depends(require_admin)`

**现状**:`def require_admin(): return None`,挂在 `admin_router = APIRouter(dependencies=[Depends(require_admin)])` 上。02 阶段任何后台接口都直接放行。

**05 session 改的时候**:重写 `require_admin` 函数体(从 cookie 读 session,校验失败 `raise CarshopException(ErrorCode.UNAUTHORIZED, "未登录")`),所有路由的 `Depends` 调用点完全不动。已用 `# TODO(05): replace stub` 标注位置。

### 注意 1:`PUT /admin/products/:id` **不含 on_sale 字段**

故意的,语义清晰:改商品基础信息 vs. 改上下架状态,职责分离。前端 admin-web(06)若要做"编辑商品"表单包含上架开关,提交时分两个请求,或后端再做一个组合接口(SPEC 没要求,先不做)。

### 注意 2:删商品时 OrderItem 处理

```sql
UPDATE order_items SET product_id = NULL WHERE product_id = ?;
DELETE FROM products WHERE id = ?;
```

`product_snapshot_json` 保留下单时的商品快照(SPEC §5 设计),订单详情仍可完整展示。04 session 写订单详情接口时直接读 snapshot,不要回查 product 表。

### 注意 3:`category_name` 是 join 出来的虚拟字段,**不在 Product DB 里**

实现走的是路由层手动批量查 `Category` 表(`_serialize_with_categories`),不是 SQLAlchemy relationship。原因:简单、明确、不会触发 N+1。

### 注意 4:`absolutize_url` 的语义

- 值含 `://` 或以 `//` 开头 → 完整 URL,原样返回(seed 的 `https://picsum.photos/...` 走这条)
- 值以 `/` 开头 → 拼 `BASE_URL`(05 上传后存的 `/static/uploads/xxx.png` 走这条)
- 其它(裸文件名) → 当 uploads 文件名补全(兜底,正常不会触发)

---

## 7. 下游 session 需要知道的事

### 给 06 admin-web

- **8 个后台接口全部可调**,02 桩免登录 → 05 接入后会强制登录(届时 cookie 自动带上即可)
- **分页 envelope** 是 `data.list / data.total / data.page / data.page_size`,不是 AntD 默认结构,前端要写 adapter
- **图片字段**(`icon_url` / `main_image_url`)返回的是**完整 URL**,前端直接 `<img src>`,不要拼前缀
- **金额是分**(int),前端展示要除 100,提交时乘 100
- **删分类**遇 1002 时,把 `message` 直接 Toast 给用户即可(已经是人话)
- **`PUT /admin/products` 不能改 `on_sale`**,要切上下架走 `PATCH /admin/products/:id/on_sale`,body `{on_sale:true|false}`

### 给 08~10 android

- 商品列表 `data.list` 是 `Product[]`,每条带 `category_name`,详情页可以直接显示分类名不用回查
- 详情接口下架商品也能拿到,字段 `on_sale=false`,UI 要按这个改"立即购买"按钮状态
- 图片字段是完整 URL,Coil / Glide 直接加载
- 价格 `price` / `original_price` 是分,显示时除 100

### 给 03(banner-api)/ 04(order-pay-api)

- **复用 `app.utils.datetime.to_iso`** 处理 datetime 序列化,不要重新实现
- **复用 `app.utils.url.absolutize_url`** 处理 image_url 字段(banner 的 `image_url` 也要走这个)
- **复用 `app.deps.require_admin`** 挂后台路由,不要自己写桩
- `app/schemas/common.py` 里有 `Page[T]` 泛型,可用可不用(本 session 路由用的是手写 dict)

### 给 07 早集成

- 验证联调时用 fixtures 对比:`artifacts/fixtures/02/*.json`
- BASE_URL 切到生产域名(`https://carshop.hearagain.space`)时,所有 `icon_url` / `main_image_url` 会自动用生产 host,因为走 `absolutize_url`,**前端不用改任何东西**
- 注意 seed 里 `icon_url` 是 `http://localhost:8000/...`(01 session seed 时存的完整 URL),如果切到生产部署,这部分**不会**被 absolutize 自动修正(已是完整 URL,走原样返回)。**需要在 07 集成时把 seed 改成相对路径**(`/static/icons/car-parts.svg`)或重新跑一次 init_db 把 BASE_URL 改成生产域。给 01 / 07 session 留意。

---

## 8. 启动命令(02 session 完成后)

```bash
cd carshop-server
source .venv/bin/activate
python scripts/init_db.py   # 已 seed 过可跳
bash scripts/dev.sh         # http://localhost:8000
```

可用接口(本 session 范围):
- `GET  /api/v1/categories`
- `GET  /api/v1/products[?category_id=&page=&page_size=]`
- `GET  /api/v1/products/:id`
- `GET  /api/v1/admin/categories`
- `POST /api/v1/admin/categories`
- `PUT  /api/v1/admin/categories/:id`
- `DELETE /api/v1/admin/categories/:id`
- `GET  /api/v1/admin/products[?category_id=&on_sale=&page=&page_size=]`
- `GET  /api/v1/admin/products/:id`
- `POST /api/v1/admin/products`
- `PUT  /api/v1/admin/products/:id`
- `PATCH /api/v1/admin/products/:id/on_sale`
- `DELETE /api/v1/admin/products/:id`

---

**版本**:v1.0
**实跑验证**:20 条验收 + 4 项契约统一全部在本机完成,fixtures 已落盘
