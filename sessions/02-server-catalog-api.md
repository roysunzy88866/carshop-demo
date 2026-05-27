# Session 02 · Server Catalog API(分类 + 商品接口)

> 提供分类和商品的公开接口(车机用)+ 后台管理接口。**最核心的领域接口**。

---

## 1. 你要做什么

在 `carshop-server/app/` 下添加分类和商品的路由实现,**严格对照 SPEC.md §6.1 / §6.2**:

### 1.1 公开接口(车机用,不要登录)

| Method | Path | 说明 |
|---|---|---|
| GET | `/api/v1/categories` | 分类列表,按 sort 升序 |
| GET | `/api/v1/products` | 商品列表,query: `category_id`(可选)、`page`(默认1)、`page_size`(默认20,最大100);只返回 `on_sale=true` |
| GET | `/api/v1/products/:id` | 商品详情,**下架商品也能返回**(前端按 on_sale 字段决定怎么显示) |

### 1.2 后台接口(必须登录,加 `Depends(require_admin)`)

| Method | Path | 说明 |
|---|---|---|
| GET | `/api/v1/admin/categories` | 全部分类(不过滤) |
| POST | `/api/v1/admin/categories` | 新建分类。body: `{name, icon_url, sort}` |
| PUT | `/api/v1/admin/categories/:id` | 改分类 |
| DELETE | `/api/v1/admin/categories/:id` | 删分类。**若该分类下还有商品 → 返回 code 1002 资源冲突** |
| GET | `/api/v1/admin/products` | 商品列表(支持 `category_id`、`on_sale`、`page`、`page_size`) |
| POST | `/api/v1/admin/products` | 新建商品 |
| GET | `/api/v1/admin/products/:id` | 商品详情 |
| PUT | `/api/v1/admin/products/:id` | 改商品 |
| DELETE | `/api/v1/admin/products/:id` | 删商品(软删 vs 硬删:**硬删**,Demo 简单为先) |
| PATCH | `/api/v1/admin/products/:id/on_sale` | 切上下架。body: `{on_sale: bool}` |

### 1.3 响应字段(精确定义,前端按这个写 type)

#### Category(分类)
```json
{
  "id": 1,
  "name": "汽车用品",
  "icon_url": "https://carshop.hearagain.space/static/icons/car-parts.svg",
  "sort": 1,
  "created_at": "2026-05-25T12:30:00+08:00"
}
```

#### Product(商品)
```json
{
  "id": 1,
  "category_id": 2,
  "category_name": "加油充电",
  "title": "中石化加油卡 100 元",
  "product_type": "service_voucher",
  "price": 9500,
  "original_price": 10000,
  "spec": "100 元面值,全国通用",
  "main_image_url": "https://carshop.hearagain.space/static/uploads/abc.png",
  "description": "中石化全国 30000+ 加油站通用...",
  "on_sale": true,
  "created_at": "2026-05-25T12:30:00+08:00",
  "updated_at": "2026-05-25T12:30:00+08:00"
}
```

**注意**:列表接口返回的 Product 也带全字段(不裁剪),分页 envelope 见 SPEC §13。

### 1.4 错误情况(严格按 SPEC §12)

- 商品/分类不存在 → `code: 1001`(HTTP 404)
- 入参缺字段 / 类型错 → `code: 1000`(HTTP 400)
- 删分类但下面有商品 → `code: 1002`(HTTP 409),message `"该分类下还有 N 个商品,请先移除或删除商品"`
- 后台接口未登录 → `code: 2000`(HTTP 401)

> `require_admin` 这个 dependency 在本 session **暂时用桩**(假装永远登录成功),由 05 替换为真实实现。在代码里用 `# TODO(05): replace stub` 标注。

---

## 2. 你不要做什么

- ❌ 实现真的 admin 登录(05 的事,这里用桩)
- ❌ 实现图片上传(05 的事)
- ❌ 实现 banner / order 接口(03 / 04 的事)
- ❌ 改 DB schema(01 已定,有问题先改 SPEC.md)
- ❌ 写前端代码
- ❌ 实现"软删除""审核流"这种 SPEC 没写的功能

---

## 3. 输入

- `CLAUDE.md`、`SPEC.md`
- `carshop-server/`(01 session 的产物)
- `artifacts/01-server-foundation.md`(看 01 实际做了什么)

---

## 4. 输出 / 交付物

- `carshop-server/app/routers/category.py`(公开 + 后台)
- `carshop-server/app/routers/product.py`(公开 + 后台)
- `carshop-server/app/schemas/category.py`、`product.py`(Pydantic models)
- 在 `app/main.py` 挂载新路由
- `artifacts/02-server-catalog.md`:
  - OpenAPI 片段(从 `/openapi.json` 复制)
  - 每个接口的 cURL 调用例 + 真实响应 JSON(成功 + 失败两种)
  - 已知偏离 SPEC 的地方
- `artifacts/fixtures/02/` 目录,内含:
  - `get-categories.json`
  - `get-products.json`(列表)
  - `get-product-1.json`(详情)
  - `post-product-success.json`
  - `delete-category-conflict.json`(错误样例)
  - …每个接口至少一份

---

## 5. 验收标准(必须实际跑过)

跑下面这串命令,每一步都必须成功:

```bash
# 公开接口
curl http://localhost:8000/api/v1/categories                    # 5 个分类
curl http://localhost:8000/api/v1/products                       # 商品列表
curl http://localhost:8000/api/v1/products?category_id=2         # 加油充电下的商品
curl http://localhost:8000/api/v1/products/1                     # 详情
curl http://localhost:8000/api/v1/products/99999                 # code=1001

# 后台接口(stub,先假定能调通)
curl -X POST http://localhost:8000/api/v1/admin/products \
  -H "Content-Type: application/json" \
  -d '{"category_id":1,"title":"测试","product_type":"physical","price":1000,"main_image_url":"https://example.com/a.png","description":"x","on_sale":true}'
curl -X PATCH http://localhost:8000/api/v1/admin/products/1/on_sale \
  -H "Content-Type: application/json" -d '{"on_sale":false}'
curl http://localhost:8000/api/v1/products/1                     # 还能查到,但 on_sale=false
curl http://localhost:8000/api/v1/products                       # 列表不含这个商品
```

所有响应符合 SPEC.md §6 / §12 / §13。

---

## 6. 依赖

- **上游**:01 server-foundation(必须先完成)
- **下游**:06 admin-web(消费后台接口)、08~10 android 端(消费公开接口)、07 早集成

---

## 7. Mock 策略

本 session 不需要 mock(本身是 mock 的源头)。

---

## 8. 已知坑 / 注意

1. **`require_admin` 桩**:在 02 阶段写成 `def require_admin(): return None`,**所有后台接口在 02 阶段都不校验**。05 替换。**必须在路由里就用 `Depends(require_admin)`,不要等 05 再加**,否则 05 改不动了
2. **图片 URL 返回完整 URL**:DB 里存的是 `/static/uploads/abc.png`,**响应时拼上 `BASE_URL`**(`settings.BASE_URL + path`)。这事在 Pydantic schema 的 validator 里做
3. **分页 envelope**:统一格式 `{list, total, page, page_size}`,放在 `response.data` 里
4. **product 列表带 category_name**:连表 join,不要让前端再查
5. **删分类时检查商品数**:`SELECT COUNT(*) FROM products WHERE category_id = ?`,>0 就抛 1002
6. **PATCH on_sale 用专门接口**:不要让 PUT 改 on_sale,语义清晰
7. **PATCH on_sale 的桩**:即使 require_admin 是桩,这个接口还是要写完整逻辑
