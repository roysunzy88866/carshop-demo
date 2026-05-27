# Session 05 · Server Admin Auth & Upload(后台认证 + 图片上传)

> 把 02/03/04 里 `require_admin` 的桩**替换为真实实现**,并提供图片上传接口。

---

## 1. 你要做什么

### 1.1 登录相关接口

| Method | Path | 说明 |
|---|---|---|
| POST | `/api/v1/admin/login` | body: `{username, password}`,成功 set-cookie + 返回 admin 信息 |
| POST | `/api/v1/admin/logout` | 清 cookie |
| GET | `/api/v1/admin/me` | 用 cookie 校验,返回当前管理员;未登录 → code 2000 |

### 1.2 图片上传

| Method | Path | 说明 |
|---|---|---|
| POST | `/api/v1/admin/upload` | multipart/form-data,字段名 `file`,返回 `{url}` 完整 URL |

### 1.3 替换 02/03/04 的桩

把之前的 `require_admin` 桩(永远返回 None / True)**替换**为真实实现:
- 从 cookie 取 session_id
- 查 session(自定义表 `admin_sessions`,或者用简单的 in-memory dict + 过期清理)
- 找到对应 admin → 返回 admin 对象
- 没找到 → 抛 `UnauthorizedError`(code 2000)

### 1.4 上传规则(严格)

- 字段名固定 `file`
- 大小限制 **5 MB**
- 格式限制 **jpg / png / webp**(按 MIME 和扩展名双重校验)
- 文件名:用 UUID + 原扩展名(`uuid4().hex + ext`),避免冲突和路径注入
- 保存路径:`static/uploads/{uuid}.{ext}`
- 返回:`{"url": "https://carshop.hearagain.space/static/uploads/abc.png"}`(完整 URL,见 SPEC §13)

### 1.5 会话管理

简化方案(Demo 够用):
- 登录成功 → 生成 `session_id = uuid4().hex`
- 存入数据库表 `admin_sessions(session_id PK, admin_id, expires_at)`
- 写 cookie:`Set-Cookie: session=<id>; HttpOnly; SameSite=Lax; Max-Age=86400`
- 过期 24 小时
- 登出 → 删该 session

> **不要用 JWT**,Demo 不需要,会增加复杂度。

---

## 2. 你不要做什么

- ❌ 多用户 / 角色 / 权限
- ❌ 验证码 / 二步验证
- ❌ 修改密码接口
- ❌ 图片处理(裁剪 / 压缩 / 水印)
- ❌ OSS / 云存储

---

## 3. 输入

- `CLAUDE.md`、`SPEC.md`(尤其 §14 配置项 admin 默认账号)
- `carshop-server/`、`artifacts/01-04 所有 server artifacts`

---

## 4. 输出

- `carshop-server/app/routers/admin_auth.py`
- `carshop-server/app/routers/upload.py`
- `carshop-server/app/services/auth_service.py`(login / logout / session 管理)
- `carshop-server/app/models/admin_session.py`(新增表)
- 数据库 migration(简单脚本:`scripts/init_db.py` 加上新表创建)
- **修改** `app/deps.py` 的 `require_admin` 为真实实现
- `artifacts/05-server-admin-auth-upload.md` + `artifacts/fixtures/05/`:
  - `post-login-success.json`
  - `post-login-wrong-password.json`(code 2001)
  - `get-me-success.json`
  - `get-me-not-logged-in.json`(code 2000)
  - `post-upload-success.json`
  - `post-upload-too-large.json`(code 1000)
  - `post-upload-wrong-format.json`(code 1000)

---

## 5. 验收

```bash
# 登录
curl -X POST http://localhost:8000/api/v1/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  -c cookies.txt -i
# 看到 Set-Cookie: session=...

# 校验登录态
curl http://localhost:8000/api/v1/admin/me -b cookies.txt
# {code:0, data:{username:"admin"...}}

# 不带 cookie
curl http://localhost:8000/api/v1/admin/me
# {code:2000, ...}

# 现在调后台接口,没 cookie 被拒
curl http://localhost:8000/api/v1/admin/products
# {code:2000, ...}

# 带 cookie 调成功
curl http://localhost:8000/api/v1/admin/products -b cookies.txt
# {code:0, data:{list:...}}

# 上传图
curl -X POST http://localhost:8000/api/v1/admin/upload \
  -b cookies.txt \
  -F 'file=@/path/to/some.png'
# {code:0, data:{url:"https://.../static/uploads/abc.png"}}

# 上传超过 5MB
curl -X POST http://localhost:8000/api/v1/admin/upload \
  -b cookies.txt \
  -F 'file=@/path/to/large.png'
# {code:1000, ...}

# 上传错误格式(.exe)
curl -X POST http://localhost:8000/api/v1/admin/upload \
  -b cookies.txt \
  -F 'file=@/path/to/x.exe'
# {code:1000, ...}

# 退出
curl -X POST http://localhost:8000/api/v1/admin/logout -b cookies.txt
curl http://localhost:8000/api/v1/admin/me -b cookies.txt
# {code:2000, ...}
```

---

## 6. 依赖

- **上游**:01、02、03、04(替换它们的桩)
- **下游**:06 admin-web(真用)、07 早集成

---

## 7. Mock 策略

不需要。

---

## 8. 已知坑

1. **改 02/03/04 的代码**:**这是允许的例外**(本 session 的工作就是这个),其他 session 之间不许互改
2. **FastAPI cookie**:用 `Response.set_cookie(...)` 而不是 header
3. **multipart 接收**:`UploadFile = File(...)`,装 `python-multipart`
4. **大小校验在读流前**:用 `Content-Length` header 预判,不要先读完再判断
5. **MIME 校验不靠扩展名**:用 `mimetypes` 或读魔数前几字节
6. **session 过期清理**:不做后台任务,登录时顺便删本人过期 session 即可(简化)
7. **CORS + cookie**:确保 `app.main` 的 CORS middleware `allow_credentials=True`,且 cookie 跨域要 `SameSite=None; Secure`(经 HTTPS)
