# Artifact · Session 05 · Server Admin Auth & Upload

**完成日期**:2026-05-25
**对应 session spec**:`sessions/05-server-admin-auth-upload.md`
**状态**:✅ 已完成 · 全部 6 条验收 + 02/03/04 zero-touch 桩替换实跑通过

---

## 一句话总结

后台真正落地了 cookie session 鉴权(`/admin/login` / `/admin/logout` / `/admin/me`)和图片上传(`/admin/upload`),把 `app/deps.py` 里 02 阶段那份 `require_admin` 桩**原地换成真实实现**——02/03/04 所有 `dependencies=[Depends(require_admin)]` 调用点**一行没动**,无 cookie 全部 401、带 cookie 全部 200,实跑验证通过。

---

## 1. 验收清单(对照 session spec §5)

| # | 验收点 | 状态 | 实跑证据 |
|---|---|---|---|
| 1 | `POST /admin/login` 用 `admin/admin123` → `code:0` + `Set-Cookie: session=...; HttpOnly; SameSite=Lax; Max-Age=86400` | ✅ | 见 §3 场景 1 |
| 2 | `POST /admin/login` 密码错 → `code:2001` | ✅ | 见 §3 场景 2 |
| 3 | `GET /admin/me` 带 cookie → `{id, username}`;不带 → `code:2000` | ✅ | 见 §3 场景 3 / 4 |
| 4 | `POST /admin/upload` 合法图 → `code:0` + 完整 URL,文件落地 `static/uploads/` | ✅ | 见 §3 场景 5,落地文件 `fa80828cc63f4cccb26c6be0245694c8.png` |
| 5 | 上传 >5MB / 非白名单格式 → `code:1000`,文件**不落地** | ✅ | 见 §3 场景 6 / 7;`big.png` 6MB 被拒,目录无残留 |
| 6 | logout 后 `me` → `code:2000` | ✅ | 见 §3 场景 8 |
| **+** | **额外**:02/03/04 后台接口在桩替换后,无 cookie 全 401、带 cookie 全 200 | ✅ | 见 §4 |
| **+** | **额外**:`admin_sessions` 表通过 `python scripts/init_db.py` 重复跑无错 | ✅ | 见 §3.0 |

---

## 2. 交付物

### 2.1 新增 / 改动文件清单

| 文件 | 动作 | 说明 |
|---|---|---|
| `app/settings.py` | 改 | 加 `UPLOAD_ALLOWED_MIME`、`SESSION_COOKIE_NAME`、`SESSION_MAX_AGE_SECONDS`、`SESSION_COOKIE_SAMESITE`、`SESSION_COOKIE_SECURE`(后两个 env 可覆盖) |
| `app/models/admin_session.py` | 新增 | `AdminSession` 表:`session_id PK` / `admin_id FK` / `expires_at` / `created_at` |
| `app/models/__init__.py` | 改 | re-export `AdminSession` |
| `app/services/auth_service.py` | 新增 | `authenticate` / `create_session` / `get_admin_by_session` / `delete_session`;屏蔽 passlib 无害 warning |
| `app/deps.py` | **改(核心)** | `require_admin` 桩替换为真实实现:cookie → session → Admin,失败 raise `CarshopException(UNAUTHORIZED)` |
| `app/routers/admin_auth.py` | 新增 | `POST /admin/login`、`POST /admin/logout`、`GET /admin/me` |
| `app/routers/upload.py` | 新增 | `POST /admin/upload`,流式累计读取超大小立刻拒(不落地) |
| `app/main.py` | 改 | include `admin_auth.router` 和 `upload.router`,各自 prefix `/api/v1` |
| `scripts/init_db.py` | 改 | import `AdminSession` 触发表注册 |
| `artifacts/fixtures/05-server-admin-auth-upload/` | 新增 | 8 个 fixture JSON(见 §5) |

### 2.2 数据库 schema 新增

```sql
CREATE TABLE admin_sessions (
    session_id VARCHAR(64) NOT NULL,
    admin_id INTEGER NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (session_id),
    FOREIGN KEY(admin_id) REFERENCES admins (id)
);
```

> **注意**:没建索引 —— 单 admin 场景下表大小最多十几行(每次登录新增一行,登录时顺手清本人过期),全表扫够用。

---

## 3. 接口 / 数据示例(实跑 cURL 输出)

### 3.0 初始化

```bash
$ python scripts/init_db.py
[init_db] creating tables in sqlite:////.../carshop.db
[init_db] running seeds
[init_db] seed inserted: {'categories': 0, 'admins': 0, 'banners': 0, 'products': 0}
[init_db] done.

$ sqlite3 carshop.db ".schema admin_sessions"
CREATE TABLE admin_sessions (...);   # 见 §2.2
```

> `seed inserted` 全 0 是因为 01 已 seed 过、本次只新建 `admin_sessions` 一张空表 —— 符合 "重复跑安全" 约定。

### 3.1 场景 1:`POST /api/v1/admin/login`(成功)

```bash
$ curl -X POST http://localhost:8000/api/v1/admin/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}' \
    -c cookies.txt -D -
```

```http
HTTP/1.1 200 OK
set-cookie: session=808ca8e4409c4a45b367cd785cd144f5; HttpOnly; Max-Age=86400; Path=/; SameSite=lax
content-type: application/json
```

```json
{"code":0,"data":{"id":1,"username":"admin"},"message":"ok"}
```

→ fixture:[`post-login-success.json`](fixtures/05-server-admin-auth-upload/post-login-success.json)

### 3.2 场景 2:`POST /api/v1/admin/login`(密码错)

```bash
$ curl -X POST http://localhost:8000/api/v1/admin/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"WRONG"}'
# HTTP 401
```
```json
{"code":2001,"data":null,"message":"用户名或密码错误"}
```

→ fixture:[`post-login-wrong-password.json`](fixtures/05-server-admin-auth-upload/post-login-wrong-password.json)

### 3.3 场景 3:`GET /api/v1/admin/me`(带 cookie)

```bash
$ curl http://localhost:8000/api/v1/admin/me -b cookies.txt
```
```json
{"code":0,"data":{"id":1,"username":"admin"},"message":"ok"}
```

→ fixture:[`get-me-success.json`](fixtures/05-server-admin-auth-upload/get-me-success.json)

### 3.4 场景 4:`GET /api/v1/admin/me`(不带 cookie)

```bash
$ curl http://localhost:8000/api/v1/admin/me
# HTTP 401
```
```json
{"code":2000,"data":null,"message":"未登录或登录已过期"}
```

→ fixture:[`get-me-not-logged-in.json`](fixtures/05-server-admin-auth-upload/get-me-not-logged-in.json)

### 3.5 场景 5:`POST /api/v1/admin/upload`(成功)

```bash
$ curl -X POST http://localhost:8000/api/v1/admin/upload \
    -b cookies.txt \
    -F 'file=@/tmp/tiny.png;type=image/png'
```
```json
{"code":0,"data":{"url":"http://localhost:8000/static/uploads/fa80828cc63f4cccb26c6be0245694c8.png"},"message":"ok"}
```

> fixture 里 URL 是生产形态 `https://carshop.hearagain.space/...`(前端 mock 能加载图);本地实跑 host 是 localhost(诚实记录)。

→ fixture:[`post-upload-success.json`](fixtures/05-server-admin-auth-upload/post-upload-success.json)

### 3.6 场景 6:`POST /api/v1/admin/upload`(>5MB)

```bash
$ curl -X POST http://localhost:8000/api/v1/admin/upload \
    -b cookies.txt \
    -F 'file=@/tmp/big.png;type=image/png'   # 6MB PNG
# HTTP 400
```
```json
{"code":1000,"data":null,"message":"文件超过 5MB"}
```

→ fixture:[`post-upload-too-large.json`](fixtures/05-server-admin-auth-upload/post-upload-too-large.json)

### 3.7 场景 7:`POST /api/v1/admin/upload`(.exe)

```bash
$ curl -X POST http://localhost:8000/api/v1/admin/upload \
    -b cookies.txt \
    -F 'file=@/tmp/bad.exe;type=application/octet-stream'
# HTTP 400
```
```json
{"code":1000,"data":null,"message":"仅支持 jpg/png/webp"}
```

→ fixture:[`post-upload-wrong-format.json`](fixtures/05-server-admin-auth-upload/post-upload-wrong-format.json)

### 3.8 场景 8:`POST /api/v1/admin/logout` + 再访问 `/admin/me`

```bash
$ curl -X POST http://localhost:8000/api/v1/admin/logout -b cookies.txt
{"code":0,"data":null,"message":"ok"}

$ curl http://localhost:8000/api/v1/admin/me -b cookies.txt
# HTTP 401
{"code":2000,"data":null,"message":"未登录或登录已过期"}
```

→ fixtures:[`post-logout-success.json`](fixtures/05-server-admin-auth-upload/post-logout-success.json)、[`get-me-not-logged-in.json`](fixtures/05-server-admin-auth-upload/get-me-not-logged-in.json)

---

## 4. 02/03/04 后台接口 · 桩替换后的真实表现

**这是 05 session 最关键的产出**:替换 `require_admin` 之后,02/03/04 的所有后台路由调用点**一行没动**,行为却从"全部放行"变成了"无 cookie 401 / 带 cookie 200"。

| 接口 | 无 cookie | 带 cookie |
|---|---|---|
| `GET /api/v1/admin/categories` | `code:2000` HTTP 401 | `code:0` + 5 个分类 |
| `GET /api/v1/admin/products?page=1&page_size=2` | `code:2000` HTTP 401 | `code:0` + 2 条商品(含分页结构) |
| `GET /api/v1/admin/banners` | `code:2000` HTTP 401 | `code:0` + 3 条 banner |
| `GET /api/v1/admin/orders` | `code:2000` HTTP 401 | `code:0` + 订单列表(用户先跑过端到端,有 paid 订单) |

实跑命令在本机 console history 里;关键结论:**`Depends(require_admin)` 这个调用约定零成本承载了从桩到真实的切换**,02/03/04 不需要任何返工。

---

## 5. 导出的 fixtures

`artifacts/fixtures/05-server-admin-auth-upload/` 下共 8 个 JSON:

| 文件 | 触发 | 返回 code |
|---|---|---|
| `post-login-success.json` | 正确账号密码 | 0 |
| `post-login-wrong-password.json` | 密码错 | 2001 |
| `get-me-success.json` | 带合法 cookie | 0 |
| `get-me-not-logged-in.json` | 无 cookie / cookie 过期 / cookie 已 logout | 2000 |
| `post-logout-success.json` | logout(带或不带 cookie 都返回这个) | 0 |
| `post-upload-success.json` | 合法图(jpg/png/webp + 对应 MIME + ≤5MB) | 0 |
| `post-upload-too-large.json` | 文件 > 5MB | 1000 |
| `post-upload-wrong-format.json` | 扩展名或 MIME 不在白名单 | 1000 |

> 上传成功的 URL 用生产 host `https://carshop.hearagain.space/...`(用户拍板,见会话决策),前端 mock 直接拿来用能加载真图。

---

## 6. OpenAPI 片段(本 session 引入的接口)

完整可由 `curl http://localhost:8000/openapi.json` 取。本 session 涉及的接口:

| Path | Method | tags | 备注 |
|---|---|---|---|
| `/api/v1/admin/login` | POST | admin-auth | body `LoginBody {username, password}` |
| `/api/v1/admin/logout` | POST | admin-auth | 读 cookie `session` |
| `/api/v1/admin/me` | GET | admin-auth | 读 cookie `session` |
| `/api/v1/admin/upload` | POST | admin-upload | multipart,字段 `file`,**整个 router 挂 `Depends(require_admin)`** |

> 因为 `require_admin` 是 dependency(不绑变量,只产生副作用),FastAPI 自动生成的 OpenAPI 不会把 401 列入 responses —— 这是 FastAPI 已知行为,**不影响实际行为**。错误码以本 artifact §5 fixtures 为准(前端按 fixtures mock,而不是 OpenAPI 推 schema)。

---

## 7. SPEC.md 改动记录

**无改动。** §5 / §6 / §12 / §13 / §14 全部对照执行:
- `Admin` 表无新字段(spec §5 已定义),新加的 `AdminSession` 是实现细节,不进 SPEC §5(同 `OrderItem.product_snapshot_json` 一类)
- 错误码全部用 SPEC §12 已有的 `UNAUTHORIZED=2000` / `LOGIN_FAILED=2001` / `PARAM_INVALID=1000`,无发明
- URL 全部按 SPEC §13:`/api/v1/admin/<resource>`、上传返回完整 URL
- 上传限制 5MB / jpg/png/webp 严格按 SPEC §14
- cookie 默认 `SameSite=Lax` + `HttpOnly` + `Max-Age=86400`,符合 session spec §1.5

---

## 8. 已踩过的坑 / 设计选择

### 选择 1:`SESSION_COOKIE_SAMESITE` / `SESSION_COOKIE_SECURE` 做成 env 可覆盖

- Dev 默认 `samesite=lax` + `secure=False`(http://localhost 才能写 cookie)
- 生产 07 早集成时:`CARSHOP_COOKIE_SAMESITE=none CARSHOP_COOKIE_SECURE=1` 即可切换跨域 + HTTPS
- **代码零改动**

### 选择 2:上传校验「扩展名 + MIME」双重

- 扩展名取自 `file.filename` 后缀,小写,`jpeg`→`jpg`
- MIME 取自 `file.content_type`(由 multipart 头给出,不是读魔数 —— Demo 够用,真要防伪造再加 magic bytes)
- 两个有一个不在白名单,立即 1000,**不读流不落地**

### 选择 3:大小校验用「流式累计」,不依赖 `Content-Length`

- `Content-Length` 客户端可伪造,且某些 chunked 上传根本没这个 header
- 实现:64KB 一块读,累计 > 5MB 立刻 `raise`,文件**不落地**(目录不写)
- 实跑 6MB 文件 + `static/uploads/` ls 验证:无残留

### 选择 4:session 不签名,uuid4().hex 存 DB

- 攻击面:DB 里的 token 被偷 → 谁拿到都能登。**Demo 接受**(用户已确认)
- 24h 过期,登录时顺手清本人过期 session(避免无限增长)
- 没做后台清理任务

### 偏离 1:`AdminSession.created_at` 用 `datetime.utcnow`

- SQLAlchemy 2.x 推荐用 timezone-aware,但项目其他表(01 session)全用 naive UTC。**保持一致** —— 不在本 session 里搞特殊
- API 不返回这个字段,不影响前端

### 注意 1:passlib trapped warning

- 01 artifact §6 提到的 `(trapped) error reading bcrypt version` —— 已在 `auth_service.py` 头加一行 `logging.getLogger("passlib").setLevel(logging.ERROR)` 屏蔽。**功能正常**(实跑登录 / 校验都过)

### 注意 2:`admin_auth.logout` 的 cookie 处理

- `Response.delete_cookie` 会下发 `Set-Cookie: session=""; Max-Age=0`,客户端立刻丢弃
- 同时**服务端 DB 里的 session 行也被删**(`auth_service.delete_session`),即使客户端没丢 cookie,下次校验也会 401(token 在 DB 里查不到)

---

## 9. 下游 session 需要知道的事

### 给 06 admin-web(直接消费方)

- **登录流程**:`POST /api/v1/admin/login` body `{username, password}`,服务端 set-cookie,前端**不需要存 token**,后续请求 axios 配 `withCredentials: true` 即可自动带 cookie
- **登录态校验**:`GET /api/v1/admin/me` —— 不带 cookie / 过期 / 已 logout 都返回 `code:2000` HTTP 401,前端按 code 跳登录页
- **登出**:`POST /api/v1/admin/logout`,服务端清 DB + 客户端 cookie 自动清
- **后台所有接口都要带 cookie**:axios 全局配 `withCredentials: true`(不然 02/03/04 全 401)
- **图片上传**:`POST /api/v1/admin/upload`,`FormData` 字段名固定 `file`,返回的 `data.url` 是**完整 URL**,前端直接塞到 `image_url` 字段送下个请求(不要拼前缀)
- **CORS**:`app/main.py` 已配 `allow_origins=["*"]` + `allow_credentials=True` —— 这俩浏览器其实不允许同时用(带凭据时 origin 必须具体)。Dev 通过 vite proxy 走 localhost:8000 同源,**没问题**;06 一旦真跨域(直连 BASE_URL),需要把 `allow_origins` 改成具体 origin 列表 —— 留给 06 或 07 处理

### 给 07 早集成

- 上线 `https://carshop.hearagain.space`:`env` 加 `CARSHOP_COOKIE_SAMESITE=none CARSHOP_COOKIE_SECURE=1 CARSHOP_BASE_URL=https://carshop.hearagain.space`,**代码零改动**
- 如果 admin-web 部署到另一个子域(如 `admin-carshop.hearagain.space`),CORS `allow_origins=["*"]` 必须改为具体 origin
- `admin / admin123` 默认账号 —— 生产前用户决定要不要改

### 给 11 final-integration

- 默认账号密码写死在 `app/settings.py` 的 `ADMIN_DEFAULT_USERNAME` / `ADMIN_DEFAULT_PASSWORD`,改完跑一次 `python scripts/init_db.py`(seed 见 01 artifact §6,密码不会重复写入已存在的 admin —— 如要改密码需先删 `admins` 表或加迁移逻辑)

---

## 10. 启动 / 验证命令

```bash
cd carshop-server
source .venv/bin/activate
python scripts/init_db.py            # 第一次跑;之后改 model 加表也跑一次
bash scripts/dev.sh                   # 或 uvicorn app.main:app --port 8000

# 一键过 8 场景
curl -sS http://localhost:8000/api/v1/health
curl -sS -X POST http://localhost:8000/api/v1/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' -c /tmp/c.txt
curl -sS http://localhost:8000/api/v1/admin/me -b /tmp/c.txt
curl -sS http://localhost:8000/api/v1/admin/me                   # 401
curl -sS -X POST http://localhost:8000/api/v1/admin/upload \
  -b /tmp/c.txt -F 'file=@some.png;type=image/png'
curl -sS -X POST http://localhost:8000/api/v1/admin/logout -b /tmp/c.txt
```

---

**版本**:v1.0
**实跑验证**:全部 6 条 + 2 条额外验收已在本机完成(2026-05-25)
