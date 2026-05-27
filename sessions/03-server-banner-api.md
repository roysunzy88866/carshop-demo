# Session 03 · Server Banner API(首页 Banner 接口)

> 首页 banner 的公开接口 + 后台管理接口。结构简单,**独立完成,不影响其他 session**。

---

## 1. 你要做什么

实现 banner 的所有路由:

### 1.1 公开接口

| Method | Path | 说明 |
|---|---|---|
| GET | `/api/v1/banners` | 首页 banner 列表,按 sort 升序,只返回 `on_show=true` |

### 1.2 后台接口(`Depends(require_admin)`,桩跟 02 一致)

| Method | Path | 说明 |
|---|---|---|
| GET | `/api/v1/admin/banners` | 全部 banner |
| POST | `/api/v1/admin/banners` | 新建 |
| PUT | `/api/v1/admin/banners/:id` | 改 |
| DELETE | `/api/v1/admin/banners/:id` | 删 |

### 1.3 响应字段

#### Banner
```json
{
  "id": 1,
  "image_url": "https://carshop.hearagain.space/static/uploads/banner1.png",
  "link_type": "product",
  "link_target": 5,
  "sort": 1,
  "on_show": true
}
```

- `link_type` enum:`"none"` | `"product"` | `"category"`
- `link_target`:
  - `link_type=none` → `null`
  - `link_type=product` → product id (int)
  - `link_type=category` → category id (int)
- **服务端要校验**:`link_type=product` 时 `link_target` 必须指向存在的 product;`category` 同理。校验失败 → code 1000

---

## 2. 你不要做什么

- ❌ 实现真 admin 认证(05)
- ❌ 实现图片上传(05)
- ❌ 改 DB schema
- ❌ 实现 banner 轮播动画 / 时长(那是前端的事)

---

## 3. 输入

- `CLAUDE.md`、`SPEC.md`
- `carshop-server/`(01)
- `artifacts/01-server-foundation.md`

---

## 4. 输出

- `carshop-server/app/routers/banner.py`
- `carshop-server/app/schemas/banner.py`
- `app/main.py` 挂载路由
- `artifacts/03-server-banner.md`:接口文档 + cURL + fixtures 链接
- `artifacts/fixtures/03/`:
  - `get-banners.json`
  - `post-banner-success.json`
  - `post-banner-invalid-link.json`(link_type=product 但 product 不存在)

---

## 5. 验收

```bash
curl http://localhost:8000/api/v1/banners                       # 3 个 seed banner
curl -X POST http://localhost:8000/api/v1/admin/banners \
  -H "Content-Type: application/json" \
  -d '{"image_url":"https://x.png","link_type":"product","link_target":1,"sort":10,"on_show":true}'
curl -X POST http://localhost:8000/api/v1/admin/banners \
  -H "Content-Type: application/json" \
  -d '{"image_url":"https://x.png","link_type":"product","link_target":99999,"sort":10,"on_show":true}'
# 上面这个返回 code=1000,message 含"指向的商品不存在"
```

---

## 6. 依赖

- **上游**:01
- **下游**:06 admin、08 android browse 首页

---

## 7. Mock 策略

不需要。

---

## 8. 已知坑

1. **link_target 校验跟 link_type 联动**:在 Pydantic validator 里写,失败抛 1000
2. **image_url 同 02**:DB 存 `/static/...`,返回拼 `BASE_URL`
3. **删 banner 不需要校验依赖**:banner 是独立的,直接删
