# Session 01 · Server Foundation(服务端地基)

> 服务端项目骨架 + 数据库 schema + seed 数据。**所有后端 session 的地基,必须最先做完。**

---

## 1. 你要做什么

创建 `carshop-server/` Python 项目,搭好 FastAPI + SQLAlchemy + SQLite 的骨架,定义所有数据模型(对照 SPEC.md §5),写好初始化和 seed,跑通一个最简单的 `/health` 接口。

**不实现任何业务接口(那是 02~05 的事)。**

### 1.1 目录结构

```
carshop-server/
├── pyproject.toml             或 requirements.txt
├── README.md                  开发指南(启动、初始化、跑测试)
├── .gitignore
├── carshop.db                 (gitignore,自动生成)
├── static/
│   ├── uploads/               (gitkeep,空目录)
│   └── icons/                 (gitkeep,空目录,07 session 填内置图标)
├── app/
│   ├── __init__.py
│   ├── main.py                FastAPI app + 路由挂载 + middleware
│   ├── settings.py            BASE_URL / DB_PATH / UPLOAD_DIR / ADMIN 默认账号
│   ├── db.py                  SQLAlchemy engine / Session
│   ├── deps.py                FastAPI dependencies(DB session、device_id 提取)
│   ├── response.py            统一响应包装 {code, data, message}
│   ├── errors.py              错误码枚举 + 自定义异常
│   ├── models/
│   │   ├── __init__.py
│   │   ├── category.py
│   │   ├── product.py
│   │   ├── banner.py
│   │   ├── order.py           Order + OrderItem
│   │   └── admin.py
│   ├── schemas/               Pydantic 入参/响应 model(空文件占位即可,02~05 填)
│   │   └── __init__.py
│   ├── routers/               接口路由(空 router 占位即可)
│   │   ├── __init__.py
│   │   └── health.py          GET /api/v1/health → {code:0, data:{status:"ok"}, message:"ok"}
│   └── seeds/
│       ├── __init__.py
│       └── initial.py         插入 5 个分类 + 1 个 admin 账号 + 3 个 banner 占位
└── scripts/
    ├── init_db.py             创建表 + 跑 seed
    └── dev.sh                 启动开发服务器(uvicorn --reload)
```

### 1.2 数据模型(严格按 SPEC.md §5)

每个 model 字段、类型、可空必须**100% 对照 SPEC.md §5**。如发现 SPEC 不完整、不清楚 → **停下来改 SPEC**,不要自己脑补。

特别注意:
- 所有金额字段 `Integer`(单位:分),不是 `Float`、不是 `Decimal`
- `created_at` / `updated_at` 用 `DateTime` 带时区
- `Order.id` 是字符串(订单号格式 `O20260525120030001`),不是自增整数
- `Product.product_type` 是 enum:`"physical"` | `"service_voucher"`
- `OrderItem.product_snapshot_json` 是 JSON 字段(SQLite 用 TEXT 存 JSON 字符串)

### 1.3 Seed 数据

`app/seeds/initial.py` 执行后:
- **5 个分类**(对照 SPEC.md §5.0):汽车用品 / 加油充电 / 洗车保养 / 周边餐饮 / 旅行服务
- **1 个 admin**:username=`admin` / password=`admin123`(用 `passlib[bcrypt]` 哈希)
- **3 个 banner 占位**(image_url 用占位图,link_type="none")
- **可选**:每个分类塞 2~3 个示例商品,方便后续 session 调试

### 1.4 中间件 / 工具

`app/main.py` 必须配置:
- CORS:允许 `*`(Demo 阶段,生产收紧)
- 全局异常处理器:把抛出的 `CarshopException` 转为统一响应格式
- 静态文件挂载:`/static` → `static/` 目录

`app/deps.py` 提供:
- `get_db()` → DB session
- `get_device_id(x_device_id: str = Header(None))` → 提取 X-Device-Id header

---

## 2. 你不要做什么

- ❌ 写任何业务接口(02~05 的事)
- ❌ 写认证逻辑(05 的事)
- ❌ 自己发明字段(必须对照 SPEC §5)
- ❌ 用 ORM 关系做复杂查询(各路由的事)
- ❌ 部署到 Mac mini(07 的事)

---

## 3. 输入(读哪些)

- `CLAUDE.md`、`SPEC.md`(尤其 §5 数据模型、§6 API 契约、§12 错误码、§13 URL 规范、§14 配置)
- `STATUS.md`、`sessions/README.md`

---

## 4. 输出 / 交付物

- `carshop-server/` 整个目录(见 1.1 结构)
- `artifacts/01-server-foundation.md`:
  - 已建表清单(用 `sqlite3 carshop.db ".schema"` 输出贴一份)
  - `/api/v1/health` 的 cURL + 响应样例
  - seed 后 `categories` 表的内容截图或 SELECT 结果
  - 已知与 SPEC 不一致的地方(如有,理由 + 是否要更新 SPEC)

---

## 5. 验收标准(必须实际跑过)

1. ✅ `cd carshop-server && python scripts/init_db.py` 跑完无报错,`carshop.db` 文件生成
2. ✅ `bash scripts/dev.sh`(或 `uvicorn app.main:app --reload`)启动,无报错
3. ✅ `curl http://localhost:8000/api/v1/health` 返回 `{"code":0,"data":{"status":"ok"},"message":"ok"}`
4. ✅ `sqlite3 carshop.db "SELECT name FROM categories ORDER BY sort"` 输出 5 个分类名,顺序正确
5. ✅ `sqlite3 carshop.db "SELECT username FROM admins"` 返回 `admin`
6. ✅ `curl http://localhost:8000/static/uploads/` 返回 403/404(目录存在但不能列目录,符合预期)
7. ✅ `artifacts/01-server-foundation.md` 写完

---

## 6. 依赖

- **上游**:无(地基)
- **下游**:02、03、04、05(全部后端 session)、07(早集成)

---

## 7. Mock 策略

不需要 mock,本 session 是被别人 mock 的源头。

---

## 8. 已知坑 / 注意

1. **SQLite + 时区**:Python 默认 datetime 无时区,SQLite 不强类型。统一用 `datetime.now(timezone.utc)`,在响应时格式化为 ISO 8601(`isoformat()`)
2. **password 哈希**:用 `passlib[bcrypt]`,别自己写 MD5
3. **订单号生成**:用 `O{YYYYMMDDHHMMSS}{4位序号}`,序号用 redis/全局计数都太重,简单做法:`O{strftime}{random.randint(1000,9999)}`,碰撞极低
4. **`product_snapshot_json` 字段**:SQLAlchemy 用 `Column(Text)` 存 JSON 字符串,model 提供 property 自动序列化/反序列化
5. **不要把 `carshop.db` 提交到 git**:.gitignore 写好
6. **依赖最小化**:`fastapi`、`uvicorn[standard]`、`sqlalchemy`、`passlib[bcrypt]`、`python-multipart`(05 上传用)。不要装 alembic / redis / celery 之类的(Demo 不需要)
