# Artifact · Session 01 · Server Foundation

**完成日期**:2026-05-25
**对应 session spec**:`sessions/01-server-foundation.md`
**状态**:✅ 已完成 · 全部 7 条验收 + 用户加的"DB 至少 5 行商品"全部实跑通过

---

## 一句话总结

`carshop-server/` 项目骨架就位:FastAPI + SQLAlchemy + SQLite,5 张表 + 5 分类 + 1 admin + 3 banner + 12 商品全部 seed 完成,`/api/v1/health` 实跑返回 200。**地基已稳,02~05 session 可以并行开工。**

---

## 1. 验收清单(对照 session spec §5)

| # | 项 | 状态 | 实跑证据 |
|---|---|---|---|
| 1 | `python scripts/init_db.py` 无报错 | ✅ | 输出 `seed inserted: {'categories': 5, 'admins': 1, 'banners': 3, 'products': 12}` |
| 2 | uvicorn 启动无报错 | ✅ | `uvicorn app.main:app --reload --host 0.0.0.0 --port 8000` 起来,端口 8000 listening |
| 3 | `curl /api/v1/health` 返回正确 JSON | ✅ | 见 §3.1 |
| 4 | 5 个分类按 sort 顺序 | ✅ | 汽车用品 / 加油充电 / 洗车保养 / 周边餐饮 / 旅行服务 |
| 5 | admin 账号存在 | ✅ | `username=admin`,`password_hash=$2b$12$xWa6v...`(bcrypt) |
| 6 | `/static/uploads/` 不可列目录 | ✅ | HTTP 404(StaticFiles `html=False`,符合预期) |
| 7 | 本 artifact 写完 | ✅ | 本文件 |
| **+** | **用户补加**:`SELECT title, product_type, price FROM products LIMIT 5` 至少 5 行 | ✅ | 见 §3.4,共 12 行(4 physical + 8 service_voucher) |

---

## 2. 交付物

### 2.1 目录结构(实际产物)

```
carshop-server/
├── README.md                      启动指南
├── requirements.txt               fastapi + sqlalchemy + passlib + bcrypt(pin 4.2)+ uvicorn 等
├── .gitignore                     .venv / *.db / static/uploads/* 等
├── carshop.db                     (gitignored)SQLite 文件,init_db 生成
├── static/
│   ├── uploads/.gitkeep           上传图目录占位
│   └── icons/.gitkeep             分类图标目录占位
├── app/
│   ├── __init__.py
│   ├── main.py                    FastAPI app + CORS + 全局异常 + /static 挂载
│   ├── settings.py                BASE_URL / DB_PATH / UPLOAD_DIR / Admin 默认账号
│   ├── db.py                      engine / SessionLocal / Base
│   ├── deps.py                    get_db / get_device_id(X-Device-Id header)
│   ├── response.py                ok() / err() 统一响应
│   ├── errors.py                  ErrorCode 枚举 + CarshopException + http_status_for
│   ├── models/
│   │   ├── __init__.py            re-export
│   │   ├── category.py            Category
│   │   ├── product.py             Product + ProductType enum
│   │   ├── banner.py              Banner
│   │   ├── order.py               Order + OrderItem(含 JSON property)
│   │   └── admin.py               Admin
│   ├── schemas/__init__.py        (空,02~05 填)
│   ├── routers/
│   │   ├── __init__.py
│   │   └── health.py              GET /api/v1/health
│   └── seeds/
│       ├── __init__.py
│       └── initial.py             5 分类 + 1 admin + 3 banner + 12 商品
└── scripts/
    ├── init_db.py                 建表 + 跑 seed(重复跑安全)
    └── dev.sh                     启动开发服务器
```

### 2.2 数据库 schema(实际表)

```sql
CREATE TABLE admins (
    id INTEGER NOT NULL,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(256) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (username)
);

CREATE TABLE banners (
    id INTEGER NOT NULL,
    image_url VARCHAR(512) NOT NULL,
    link_type VARCHAR(32) NOT NULL,      -- "none" | "product" | "category"
    link_target INTEGER,
    sort INTEGER NOT NULL,
    on_show BOOLEAN NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE categories (
    id INTEGER NOT NULL,
    name VARCHAR(64) NOT NULL,
    icon_url VARCHAR(512) NOT NULL,
    sort INTEGER NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE orders (
    id VARCHAR(32) NOT NULL,             -- 订单号 O20260525120030001
    device_id VARCHAR(128) NOT NULL,
    status VARCHAR(16) NOT NULL,         -- "pending" | "paid"
    total_amount INTEGER NOT NULL,       -- 分
    shipping_info TEXT NOT NULL,         -- JSON 字符串
    created_at DATETIME NOT NULL,
    paid_at DATETIME,
    PRIMARY KEY (id)
);
CREATE INDEX ix_orders_device_id ON orders (device_id);

CREATE TABLE order_items (
    id INTEGER NOT NULL,
    order_id VARCHAR(32) NOT NULL,
    product_id INTEGER,                  -- nullable:商品被删后保留快照
    product_snapshot_json TEXT NOT NULL, -- JSON
    quantity INTEGER NOT NULL,           -- MVP 固定 1
    price INTEGER NOT NULL,              -- 分
    PRIMARY KEY (id),
    FOREIGN KEY(order_id) REFERENCES orders (id) ON DELETE CASCADE
);
CREATE INDEX ix_order_items_order_id ON order_items (order_id);

CREATE TABLE products (
    id INTEGER NOT NULL,
    category_id INTEGER NOT NULL,
    title VARCHAR(128) NOT NULL,
    product_type VARCHAR(32) NOT NULL,   -- "physical" | "service_voucher"
    price INTEGER NOT NULL,              -- 分
    original_price INTEGER,
    spec VARCHAR(128),
    main_image_url VARCHAR(512) NOT NULL,
    description TEXT NOT NULL,
    on_sale BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY(category_id) REFERENCES categories (id)
);
CREATE INDEX ix_products_category_id ON products (category_id);
```

---

## 3. 接口 / 数据示例

### 3.1 `GET /api/v1/health`

```bash
$ curl http://localhost:8000/api/v1/health
```

```json
{"code":0,"data":{"status":"ok"},"message":"ok"}
```

保存为 fixture:`artifacts/fixtures/01-server-foundation/health.json`

### 3.2 分类(seed 后)

```sql
sqlite> SELECT id, name, sort, icon_url FROM categories ORDER BY sort;
```

| id | name | sort | icon_url |
|---:|---|---:|---|
| 1 | 汽车用品 | 1 | http://localhost:8000/static/icons/car-parts.svg |
| 2 | 加油充电 | 2 | http://localhost:8000/static/icons/fuel-charge.svg |
| 3 | 洗车保养 | 3 | http://localhost:8000/static/icons/wash-maintain.svg |
| 4 | 周边餐饮 | 4 | http://localhost:8000/static/icons/food-nearby.svg |
| 5 | 旅行服务 | 5 | http://localhost:8000/static/icons/travel-service.svg |

> ⚠️ icon SVG 文件本身没在本 session 提供(00-design-system artifact 的 `/static/icons/` 目录留给后续填,见 spec §1.1)。`icon_url` 字段已经按 SPEC §13 约定填完整 URL(带 BASE_URL host)。访问会 404,**不影响 02 session 开发**(那时只用到字段值),07 早集成时如果需要可由用户补 SVG。

### 3.3 admin(seed 后)

```
id | username | password_hash
 1 | admin    | $2b$12$xWa6v...(bcrypt,密码 admin123)
```

### 3.4 商品(seed 后 · 共 12 个)

类型分布:**physical 4 个 / service_voucher 8 个**,覆盖 SPEC §5 两种 product_type。

| id | category | title | type | price(分) | original |
|---:|---|---|---|---:|---:|
| 1 | 汽车用品 | 车载磁吸手机支架 | physical | 8900 | 12900 |
| 2 | 汽车用品 | 便携式车载吸尘器 120W | physical | 19900 | 29900 |
| 3 | 汽车用品 | 车窗遮阳挡(前挡) | physical | 3900 | NULL |
| 4 | 加油充电 | 中石化加油卡 100 元 | service_voucher | 9500 | 10000 |
| 5 | 加油充电 | 中石化加油卡 500 元 | service_voucher | 47500 | 50000 |
| 6 | 加油充电 | 特来电充电券 30 度 | service_voucher | 3600 | NULL |
| 7 | 洗车保养 | 标准洗车单次券 | service_voucher | 2900 | 3900 |
| 8 | 洗车保养 | 小保养套餐(机油+机滤) | service_voucher | 19900 | 29900 |
| 9 | 周边餐饮 | 星巴克中杯券 | service_voucher | 2800 | 3500 |
| 10 | 周边餐饮 | 肯德基早餐套餐券 | service_voucher | 1800 | 2200 |
| 11 | 旅行服务 | 车载应急工具包 | physical | 12900 | 15900 |
| 12 | 旅行服务 | 高速公路通行券 50 元 | service_voucher | 4800 | 5000 |

主图全部 `https://picsum.photos/seed/<slug>/600/400`(seed 确定性,跨 session 截图一致)。

### 3.5 Banner(seed 后)

| id | sort | link_type | image_url |
|---:|---:|---|---|
| 1 | 1 | none | https://picsum.photos/seed/banner-sale/1680/480 |
| 2 | 2 | none | https://picsum.photos/seed/banner-charge/1680/480 |
| 3 | 3 | none | https://picsum.photos/seed/banner-newuser/1680/480 |

---

## 4. 接口对照(SPEC §6)

本 session 只实现 health,业务接口全部由 02~05 session 实现。

| Method | Path | 状态 | 说明 |
|---|---|---|---|
| GET | `/api/v1/health` | ✅ 已实现(本 session 新加,不在 SPEC §6) | 用于探活 / CI / 部署检测 |
| GET | `/api/v1/categories` | ⏳ 02 session | model 就绪 |
| GET | `/api/v1/products(/:id)` | ⏳ 02 session | model 就绪,seed 有 12 条 |
| GET | `/api/v1/banners` | ⏳ 03 session | model 就绪,seed 有 3 条 |
| POST | `/api/v1/orders` 等 | ⏳ 04 session | model 就绪(订单号字符串 PK + JSON 字段已经设计完) |
| `/api/v1/admin/*` | ⏳ 05 session | admin 表已就绪,密码 bcrypt 哈希 |

---

## 5. SPEC.md 改动记录

**无改动。** 数据模型 100% 对照 SPEC §5(包括 product_type enum、订单号字符串 PK、shipping_info JSON 字段、金额全 int 分)。

**建议改 SPEC §8 的两处**(下方 §6 详述偏离),由用户决定是否落:
1. Python 版本:`3.11` → `≥ 3.11`(本机用 3.12 跑通,完全兼容)
2. 依赖 pin:把 `bcrypt` 锁到 `4.x`(passlib 1.7.4 与 bcrypt 5.0+ 不兼容,见 §6)

---

## 6. 已踩过的坑 / 偏离

### 坑 1:`passlib 1.7.4` ↔ `bcrypt 5.0` 不兼容(已修)

**现象**:`pip install passlib[bcrypt]` 默认装最新 bcrypt 5.0,但 passlib 1.7.4 用已废弃的 `bcrypt.__about__.__version__` 探测版本,运行时报 `AttributeError: module 'bcrypt' has no attribute '__about__'`,并触发一个内部 `detect_wrap_bug` 走错路径,最终抛 `ValueError: password cannot be longer than 72 bytes`(误导性错误,实际 password 只有 8 字节)。

**修法**:`requirements.txt` 拆成两行 + pin bcrypt 4.x:
```
passlib==1.7.4
bcrypt==4.2.0
```

**残留**:启动 / hash 时会打一行无害的 `(trapped) error reading bcrypt version`,passlib 自己 trap 住了,**功能正常**(实跑哈希 `$2b$12$xWa6v...` 已验证)。passlib 上游近三年没出新版,这个 warning 短期无解。05 session 实现登录时如果觉得吵,可在 `app/main.py` 加一行 `logging.getLogger("passlib").setLevel(logging.ERROR)` 屏蔽。

### 偏离 1:Python 版本 3.11 → 3.12

**原因**:本机只有 `python3.12`,没有 `python3.11`。3.12 跟 3.11 在本项目使用的所有库上完全兼容(FastAPI / SQLAlchemy 2.x / passlib / pydantic 2.x / uvicorn 都明确支持 3.12),实跑全部通过。

**建议**:把 SPEC §8 的 `Python 3.11` 改为 `Python ≥ 3.11`(或 `3.11+`),涵盖 3.12/3.13。这是个无成本的拓宽。

**风险**:无。02~05 session 拿来即用,无需任何改动。

### 偏离 2:`/api/v1/health` 不在 SPEC §6 接口表里

**原因**:session spec §1.1 要求 health 端点,但 SPEC.md §6 没列。功能上是探活 / CI / 部署用,与业务无关,**不视为契约破坏**。如果你想列入 SPEC §6.1 作为「公开接口」一行,我可以补;否则保持现状。

### 注意 1:`shipping_info` 字段实现成 TEXT(JSON 字符串)

SPEC §5 写的是 "JSON",SQLite 没原生 JSON 类型,我用 `Text` + Python property `shipping_info_dict` 自动 dump/load。**API 层返回时仍是 JSON object**,跟 SPEC 一致。`OrderItem.product_snapshot_json` 同样处理。

### 注意 2:`Order.id` 是字符串 PK,**没有自增整数 ID**

按 SPEC §13 严格执行。生成订单号格式 `O{YYYYMMDDHHMMSS}{4位序号}` 留给 04 session 实现(本 session 不写订单业务逻辑)。spec §8 已有建议(`O{strftime}{random.randint(1000,9999)}`)。

---

## 7. 下游 session 需要知道的事

### 给 02 session(catalog-api)

- Product / Category model 字段名 / 类型已确定,**不要再加字段**(要加先改 SPEC §5)
- seed 已有 12 商品,启动后 `GET /api/v1/products` 实现完立刻能返回数据
- 金额单位「分」,**返回时不要除 100**(SPEC §11)
- 分页返回结构 `{list, total, page, page_size}`(SPEC §13)
- 图片 URL 已经是完整 URL(`https://picsum.photos/...`),前端拿来就能 `<img src>`

### 给 03 session(banner-api)

- Banner model 已就绪,3 条 seed 数据 `link_type=none` 可直接返回
- 注意 `on_show` 过滤(SPEC §6.1)

### 给 04 session(order-pay-api)

- `Order.id` 字符串 PK,**自己生成订单号**(SPEC §13 + spec §8.3 已有建议算法)
- `device_id` 必从 `X-Device-Id` header 取(`app.deps.get_device_id` 已写好,直接用)
- `shipping_info` 默认值见 SPEC §5.1(用户没传时服务端写死)
- `product_snapshot` 字段是 JSON,Order model 提供了 property 自动序列化

### 给 05 session(admin-auth-upload)

- Admin 表已 seed(`admin` / `admin123`,bcrypt 哈希)
- 验证密码用 `passlib.context.CryptContext(schemes=["bcrypt"]).verify(plain, hash)`
- 上传配置已在 `app.settings`:`UPLOAD_MAX_BYTES=5MB`,`UPLOAD_ALLOWED_EXT={jpg,jpeg,png,webp}`,`UPLOAD_DIR` 已建好
- 上传后返回 URL 拼接:`f"{BASE_URL}/static/uploads/{filename}"`,不要返回相对路径(SPEC §13)

### 通用

- 加路由:在 `app/routers/` 下新建文件,在 `app/main.py` 末尾 `app.include_router(...)` 挂上
- 抛业务错误:`raise CarshopException(ErrorCode.NOT_FOUND, "商品不存在")`,全局 handler 会自动转 JSON
- 不要直接 `return {"code": ...}`,用 `app.response.ok(data)` / `app.response.err(code, msg)`

---

## 8. 启动命令汇总

```bash
cd carshop-server
python3.12 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python scripts/init_db.py    # 第一次跑 / 想重置数据时
bash scripts/dev.sh           # 起服务,默认 http://localhost:8000
```

---

**版本**:v1.0
**实跑验证**:全部 7 条验收 + 用户加项已在本机完成
