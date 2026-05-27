# Artifact · Session 03 · Server Banner API

**完成日期**:2026-05-25
**对应 session spec**:`sessions/03-server-banner-api.md`
**状态**:✅ 已完成 · 全部 7 条验收 + 跨表存在性校验 + 严格 link_type 联动校验全部实跑通过

---

## 一句话总结

Banner 公开接口(`GET /api/v1/banners`)+ 后台 4 个 CRUD 接口全部实现。**关键约束**:`link_type` 与 `link_target` 联动严格校验(none → 必须 null;product/category → 必须指向真实存在的资源,否则 code=1000)。Admin 鉴权用 `require_admin` 桩(05 session 替换),桩函数已在 `app/deps.py`(02 session 同源添加,共用)。

---

## 1. 验收清单(对照 session spec §5 + 用户拍板的严格策略)

| # | 项 | 状态 | 实跑证据 |
|---|---|---|---|
| 1 | `GET /banners` 返回 3 条 seed banner(按 sort 升序,仅 on_show=true) | ✅ | 见 §3.1;响应壳 `{code:0, data:[...], message:"ok"}` |
| 1.补 | 公开接口过滤 `on_show=false` | ✅ | 测试创建 `on_show=false` 的 banner 后,`/banners` 仍只返回 3 条 |
| 2 | `GET /admin/banners` 返回**全部**(含 on_show=false) | ✅ | 见 §3.2 |
| 3 | `POST` 合法(link_type=product, target=1) → 200 + 新 banner | ✅ | 见 §3.3 |
| 4 | `POST` 非法(link_type=product, target=99999) → code=1000 | ✅ | message:"link_target 指向的商品不存在(product_id=99999)" |
| 5 | `PUT` 改 sort/on_show 生效;不存在 id → code=1001 | ✅ | 见 §3.5 |
| 6 | `DELETE` 直接删,不校验依赖;不存在 id → code=1001 | ✅ | 见 §3.6 |
| 7 | `link_type=none` + `link_target` 非空 → code=1000(用户拍板:严格) | ✅ | message:"link_type=none 时 link_target 必须为 null" |
| 补 | `link_type=category` + 不存在 target → code=1000 | ✅ | message:"link_target 指向的分类不存在(category_id=99999)" |

---

## 2. 交付物清单

| 文件 | 角色 |
|---|---|
| `carshop-server/app/schemas/banner.py` | `BannerOut` / `BannerCreate` / `BannerUpdate` Pydantic 模型 + 形状层校验 |
| `carshop-server/app/routers/banner.py` | 5 个路由 + `_validate_link_target_exists` + `_absolutize_image_url` |
| `carshop-server/app/main.py`(改 1 行) | `app.include_router(banner.router, prefix="/api/v1")` |
| `carshop-server/app/deps.py`(改 1 处) | 加 `require_admin` 桩(与 02 session 同源,合并后保留一份)|
| `artifacts/fixtures/03/get-banners.json` | 公开 banner 列表真实响应 |
| `artifacts/fixtures/03/get-admin-banners.json` | 后台 banner 列表真实响应 |
| `artifacts/fixtures/03/post-banner-success.json` | 创建成功真实响应 |
| `artifacts/fixtures/03/post-banner-invalid-link.json` | code=1000 错误响应 |
| `artifacts/fixtures/03/delete-banner-not-found.json` | code=1001 错误响应 |

---

## 3. 接口对照(SPEC §6.1 / §6.2)

| Method | Path | 状态 | 备注 |
|---|---|---|---|
| GET | `/api/v1/banners` | ✅ 实现 | 公开,只返回 `on_show=true`,按 sort 升序 |
| GET | `/api/v1/admin/banners` | ✅ 实现 | 后台,返回全部,按 sort 升序 |
| POST | `/api/v1/admin/banners` | ✅ 实现 | 含 link_type/link_target 联动校验 + 跨表存在性校验 |
| PUT | `/api/v1/admin/banners/:id` | ✅ 实现 | partial update;改完后再次跑联动校验 |
| DELETE | `/api/v1/admin/banners/:id` | ✅ 实现 | 直接删,不校验依赖(spec §8.3) |

### 3.1 `GET /api/v1/banners`

```bash
curl http://localhost:8000/api/v1/banners
```

```json
{
  "code": 0,
  "data": [
    {"id": 1, "image_url": "https://picsum.photos/seed/banner-sale/1680/480",    "link_type": "none", "link_target": null, "sort": 1, "on_show": true},
    {"id": 2, "image_url": "https://picsum.photos/seed/banner-charge/1680/480",  "link_type": "none", "link_target": null, "sort": 2, "on_show": true},
    {"id": 3, "image_url": "https://picsum.photos/seed/banner-newuser/1680/480", "link_type": "none", "link_target": null, "sort": 3, "on_show": true}
  ],
  "message": "ok"
}
```

→ `artifacts/fixtures/03/get-banners.json`

### 3.2 `GET /api/v1/admin/banners`

```bash
curl http://localhost:8000/api/v1/admin/banners
```

返回结构跟 §3.1 一致,但**不过滤 on_show**。Seed 状态下 3 条 banner 都是 on_show=true,所以肉眼跟公开接口一样。
→ `artifacts/fixtures/03/get-admin-banners.json`

### 3.3 `POST /api/v1/admin/banners`(成功)

```bash
curl -X POST http://localhost:8000/api/v1/admin/banners \
  -H "Content-Type: application/json" \
  -d '{"image_url":"https://picsum.photos/seed/banner-test/1680/480","link_type":"product","link_target":1,"sort":10,"on_show":true}'
```

```json
{
  "code": 0,
  "data": {
    "id": 4,
    "image_url": "https://picsum.photos/seed/banner-test/1680/480",
    "link_type": "product",
    "link_target": 1,
    "sort": 10,
    "on_show": true
  },
  "message": "ok"
}
```

→ `artifacts/fixtures/03/post-banner-success.json`

### 3.4 `POST /api/v1/admin/banners`(失败 · 跨表校验)

```bash
curl -X POST http://localhost:8000/api/v1/admin/banners \
  -H "Content-Type: application/json" \
  -d '{"image_url":"https://x.png","link_type":"product","link_target":99999,"sort":10,"on_show":true}'
```

```json
{"code": 1000, "data": null, "message": "link_target 指向的商品不存在(product_id=99999)"}
```

HTTP 400。→ `artifacts/fixtures/03/post-banner-invalid-link.json`

### 3.5 `PUT /api/v1/admin/banners/:id`

```bash
curl -X PUT http://localhost:8000/api/v1/admin/banners/4 \
  -H "Content-Type: application/json" \
  -d '{"sort":99,"on_show":false}'
```

成功:返回更新后的完整 banner。
不存在 id:`{"code":1001,"data":null,"message":"banner 不存在(id=99999)"}` HTTP 404。

### 3.6 `DELETE /api/v1/admin/banners/:id`

```bash
curl -X DELETE http://localhost:8000/api/v1/admin/banners/4
# {"code":0,"data":{"id":4,"deleted":true},"message":"ok"}
```

不存在 id:同 §3.5 → code=1001 → `artifacts/fixtures/03/delete-banner-not-found.json`

---

## 4. 错误码触发清单(本 session 可能返回的 code)

| code | HTTP | 触发条件 | 实跑过 |
|---:|---:|---|---|
| 0 | 200 | 任何成功调用 | ✅ |
| 1000 | 400 | `link_type=none` 但传了非空 `link_target`(形状校验,Pydantic 抛 ValueError) | ✅ |
| 1000 | 400 | `link_type=product/category` 但 `link_target=null`(形状校验) | ✅(没单独 curl,Pydantic 同一处校验) |
| 1000 | 400 | `link_type=product/category` 且 `link_target` 指向的资源不存在(跨表校验,router 抛 CarshopException) | ✅(product 和 category 各跑一遍) |
| 1001 | 404 | PUT / DELETE 不存在的 banner_id | ✅ |

> 注:Pydantic 形状校验失败原本会走 FastAPI 的 `RequestValidationError`,被 `app/main.py` 的全局 handler 转成 `code=1000`,HTTP 400。

---

## 5. OpenAPI 片段(从 `/openapi.json` 真实导出)

只列接口签名(完整 schemas 在 `app/schemas/banner.py`):

- `GET /api/v1/banners` → `Response List Banners Public`
- `GET /api/v1/admin/banners` → `Response List Banners Admin`
- `POST /api/v1/admin/banners` body `BannerCreate` → success
- `PUT /api/v1/admin/banners/{banner_id}` body `BannerUpdate` → success
- `DELETE /api/v1/admin/banners/{banner_id}` → `{id, deleted}`

`BannerCreate` 字段:`image_url`(必填, 1~512 字符)/ `link_type`(enum 默认 "none")/ `link_target`(int|null)/ `sort`(int 默认 0)/ `on_show`(bool 默认 true)

`BannerUpdate` 同上但**全部字段可选**(partial update,只更新传入字段)。

---

## 6. SPEC.md 改动记录

**无改动。** 本 session 严格落地了 SPEC §5 数据模型、§6.1 / §6.2 接口签名、§12 错误码、§13 URL/响应格式约定。

---

## 7. 已踩过的坑 / 决策记录

### 决策 1:link_type=none + link_target 非空 → 严格 1000(用户拍板)

形状校验放在 Pydantic `BannerCreate` 的 `model_validator(mode='after')` 里,失败抛 ValueError,被 FastAPI 全局 handler 转成 `code=1000`。**理由**:契约即合同(SPEC §13),宽松接受脏数据会让 bug 藏到联调才爆。

### 决策 2:image_url 兼容两种来源(seed full URL / 上传相对路径)

`_absolutize_image_url` helper:`/` 开头才拼 BASE_URL,否则原样返回。**当前 seed 全是 `https://picsum.photos/...`**(完整 URL),所以肉眼上看不到 helper 起作用;但 05 session 的上传接口会返回 `/static/uploads/xxx.png` 这种相对路径,这时 helper 才生效。前端不需要关心,拿到的 image_url 永远是完整 URL。

### 决策 3:partial update 时联动校验跑「合并后的快照」

PUT 时,client 可能只传 `link_type` 不传 `link_target`,或反过来。所以我先用 `data.get(field, banner.field)` 算出 update 后的值,再跑联动校验。否则会出现:原 banner 是 `{type:none, target:null}`,只 PUT `{link_type:"product"}`,如果只校验传入字段会漏掉 target 也得改的事实。

### 决策 4:DELETE 返回 `{id, deleted: true}`(spec 没明说)

session spec §1.2 没规定 delete 的响应体。我返回 `{id, deleted: true}` 让前端容易判断。如果其它 session(02 / 05)的 delete 返回格式不一致,06 admin-web 可能要适配——**建议 07 早集成时统一**。

### 注意 1:`require_admin` 跟 02 session 同源共用

按用户开工前嘱咐,我先看 `app/deps.py`,发现 02 已经写好了 `require_admin` 桩,内容跟我准备加的完全一致(永远放行 + `TODO(05)` 注释),所以我直接 `from app.deps import require_admin` 用,**没新建第二个函数**。05 session 替换实现时,02 / 03 的路由都会自动生效。

### 注意 2:跑 server 验证时与 02 的潜在冲突

我在跑验证的 uvicorn 进程启动后,02 session 把 category / product router 也挂上了 main.py(已收到系统 reminder)。我的进程没开 `--reload`,所以 02 新加的路由在我这个进程里看不到——**不影响我的 banner 验证**。结束时把进程 kill 即可,不留后患。

---

## 8. 下一个 session 需要注意的点

### 给 06 admin-web

- Banner 表单字段:`image_url`(必填,接 05 的上传)/ `link_type` 三选一 select / `link_target` 联动可见(none 时隐藏)/ `sort` int / `on_show` switch
- **关键 UX**:`link_type` 切到 `product` / `category` 时,要让用户从已有 product / category 列表里选,**不要让用户手填 id**——否则用户填错就 1000
- 错误展示:1000 的 message 是给开发者看的中文人话(SPEC §12),前端可以直接当 toast 展示
- DELETE 不需要二次确认逻辑(banner 删了 seed 数据也无副作用,直接删就行)
- mock 数据**必须**从 `artifacts/fixtures/03/` 取(SPEC §15.2 / 共享约定 10),5 个 JSON 都有

### 给 08 android-browse(首页)

- 首页轮播只调 `GET /api/v1/banners`,不带 X-Device-Id 也能拿到(SPEC §6.1 只要求设备 ID 的是订单接口)
- 点 banner 跳转逻辑(spec §1.3):
  - `link_type=none` → 不响应点击
  - `link_type=product` → 跳商品详情页(用 `link_target` 当 product_id)
  - `link_type=category` → 跳分类页(用 `link_target` 当 category_id)
- seed 阶段 3 条 banner 全是 `link_type=none`,所以默认无跳转。要测跳转可在后台手动新建一条 product/category 的

### 给 05 admin-auth-upload

- `require_admin` 桩在 `app/deps.py:require_admin`,替换函数体即可,无需改任何 router(02 catalog / 03 banner 都通过 `Depends(require_admin)` 挂载)
- 鉴权失败用 `raise CarshopException(ErrorCode.UNAUTHORIZED)`,全局 handler 自动转 401 + code=2000

### 给 07 early-integration

- banner 没用到上传 host;到 07 把后端部署到 Mac mini(`carshop.hearagain.space`)后,seed 用的 `https://picsum.photos/...` 完整 URL 不受 BASE_URL 切换影响。如果你在 admin 后台新建一条用上传图的 banner,那条的 image_url 才会变成 `https://carshop.hearagain.space/static/uploads/xxx.png`(`_absolutize_image_url` 自动拼)
- 多个 delete 返回格式可能不统一(见决策 4),建议看一眼对齐

---

## 9. 启动 / 复跑命令

```bash
cd carshop-server
source .venv/bin/activate
uvicorn app.main:app --host 0.0.0.0 --port 8000
# 验证(本 session 5 条 curl 都在 §3 列出)
```

---

**版本**:v1.0
**实跑验证**:全部 7 条验收 + 补充 link_type=none 严格校验 + category 跨表校验全部在本机通过
