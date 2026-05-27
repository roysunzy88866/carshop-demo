# 车机商店 Demo · 总 Spec

> 这是项目的总需求文档。每个 session 开发前都应先读这份 spec,理解项目全貌、数据模型、API 契约和共享约定。
>
> 各个 session 的独立 spec 在 `sessions/` 目录下。

---

## 1. 产品一句话

一个**装在安卓车机上的车主用品商店 Demo**,用户开车后停下来在车机大屏上浏览商品(加油卡、洗车卡、保养、车装、应急用品等),选中后下单、模拟扫码支付,完成购买流程。

**所有商品、分类、banner 由运营人员在 Web 后台维护。不接真实登录、不接真实支付、不真实发货。**

## 2. 用户角色

| 角色 | 设备 | 用什么 |
|---|---|---|
| **车主** | 安卓车机(横屏) | 车机 App |
| **运营** | PC 浏览器 | Web 后台 |

## 3. 产品边界(明确不做什么)

| 不做 | 原因 |
|---|---|
| 用户登录 / 账号体系 | Demo 不需要,用车机设备 ID 区分"谁下的单" |
| 商品搜索 | 分类导航足够 |
| 真实支付(微信/支付宝) | Demo 阶段,扫码 UI + 假成功即可 |
| 真实发货 / 物流 | 虚拟商品 + Demo 流程,订单状态推到"已支付"为止 |
| 售后 / 退款 | 不在 MVP 范围 |
| 评价 / 收藏 / 购物车 | 不在 MVP 范围,详情页直接"立即购买" |
| 收货地址管理 | MVP 用预填默认地址。后续作为独立模块 M-Address 追加 |
| 多端(iOS / 手机) | 只做安卓车机横屏 + Web 后台 |
| 权限 / 多账号后台 | 后台一个固定账号即可 |

## 4. 功能清单

### 4.1 车机端 App(安卓横屏)

| # | 功能 | 关键点 |
|---|---|---|
| F1 | 首页 | 顶部 banner(后台配,可轮播) + 分类入口栏(横滑) + 推荐商品列表 |
| F2 | 分类页 | 点分类后看到该分类下全部上架商品,网格布局 |
| F3 | 商品详情 | 大图、标题、价格、规格(如有)、文字描述、底部"立即购买"按钮 |
| F4 | 订单确认 | 显示商品 + **预填默认收货信息(只读)** + 合计金额 + "提交订单"按钮 |
| F5 | 模拟支付 | 提交订单后弹支付二维码弹层,点击弹层任意位置 1 秒后显示"支付成功" |
| F6 | 订单中心 | 入口在首页右上角,展示**当前设备 ID 下**的全部订单,按状态 tab 切换(全部/已支付) |
| F7 | 订单详情 | 单笔订单的商品、金额、状态、时间 |

### 4.2 Web 后台(运营端)

| # | 功能 | 关键点 |
|---|---|---|
| B1 | 后台登录 | 一个固定账号密码,登录后写 session cookie |
| B2 | 商品管理 | 列表(分页/筛选分类/筛选上下架) + 新建 + 编辑 + 上下架 + 删除 |
| B3 | 商品编辑表单 | 标题、价格、原价(可选)、规格(可选,文本)、主图(上传)、描述(纯文本)、所属分类、上下架开关 |
| B4 | 分类管理 | 分类列表 + 新建 + 编辑(名称、图标、排序) + 删除(有商品时禁止删) |
| B5 | Banner 管理 | 首页 banner 增删改,上传图 + 跳转(指向某商品 / 某分类 / 无跳转) |
| B6 | 订单查看 | 只读:订单列表,可看每笔订单详情,展示是哪台车机(设备 ID 后 6 位)下的 |

## 5. 数据模型

```
Category        分类
  - id              主键
  - name            分类名(如"加油卡")
  - icon_url        分类图标 URL
  - sort            排序(小的在前)
  - created_at

Product         商品
  - id              主键
  - category_id     所属分类
  - title           商品标题(服务端 Pydantic 校验 max_length=128;超长 → code=1000)
  - product_type    类型:"physical"(实物,价格用信号红 #E63946)| "service_voucher"(服务券,价格用海泡青加深 #00A892)
  - price           现价(单位:分)
  - original_price  原价(单位:分,可空)
  - spec            规格描述(纯文本,可空,如"100元面值")
  - main_image_url  主图 URL
  - description     描述(纯文本,支持换行)
  - on_sale         是否上架(boolean)
  - created_at
  - updated_at

Banner          首页 banner
  - id
  - image_url       图片 URL
  - link_type       跳转类型:"none" | "product" | "category"
  - link_target     目标 ID(product_id 或 category_id)
  - sort            排序
  - on_show         是否展示

Order           订单
  - id              主键(订单号)
  - device_id       下单的车机设备 ID(Android ID)
  - status          "pending" | "paid"
  - total_amount    总金额(分)
  - shipping_info   收货信息(JSON,预填默认值)
  - created_at
  - paid_at         支付时间(可空)

OrderItem       订单项
  - id
  - order_id        外键
  - product_id      外键(可空,若商品被删后保留快照)
  - product_snapshot_json  下单时商品快照(标题/价格/规格/图)
  - quantity        数量(MVP 阶段固定为 1)
  - price           下单时单价(分)

Admin           管理员
  - id
  - username        固定 "admin"
  - password_hash
```

### 5.0 推荐的初始分类(seed 数据)

按设计稿定义的车机商城分类:

| name | icon 建议 | sort | accent 色(来自 design tokens) |
|---|---|---|---|
| 汽车用品 | car-parts | 1 | #00A892 |
| 加油充电 | fuel-charge | 2 | #00A892 |
| 洗车保养 | wash-maintain | 3 | #00A892 |
| 周边餐饮 | food-nearby | 4 | #00A892 |
| 旅行服务 | travel-service | 5 | #00A892 |

> 实际 icon 文件由 00-design-system session 准备并放在 `/static/icons/`。

### 5.1 默认收货信息(写死)

订单创建时,如果 client 没传 `shipping_info`,服务端写入以下默认值:

```json
{
  "name": "车主",
  "phone": "138****0000",
  "address": "上海市浦东新区世纪大道 100 号"
}
```

## 6. API 契约(服务端对外提供)

> 所有接口前缀:`/api/v1`
> 返回格式:`{ "code": 0, "data": ..., "message": "ok" }`,`code != 0` 表示错误
> 车机端用接口需带 `X-Device-Id: <android_id>` header

### 6.1 公开接口(车机端用)

| Method | Path | 说明 |
|---|---|---|
| GET | `/api/v1/health` | 探活,返回 `{status:"ok"}`。部署 / 监控用 |
| GET | `/api/v1/categories` | 分类列表(按 sort 升序) |
| GET | `/api/v1/products?category_id=&page=&page_size=` | 商品列表(只返回 on_sale=true,分页) |
| GET | `/api/v1/products/:id` | 商品详情(下架商品也能拿到,前端按需处理) |
| GET | `/api/v1/banners` | 首页 banner(按 sort 升序,只返回 on_show=true) |
| POST | `/api/v1/orders` | 创建订单。body: `{ product_id, quantity }`。返回订单 |
| GET | `/api/v1/orders?status=` | 当前设备的订单列表(按 X-Device-Id 过滤) |
| GET | `/api/v1/orders/:id` | 订单详情(仅限当前设备的订单) |
| POST | `/api/v1/orders/:id/mock_pay` | 模拟支付。把订单状态推到 paid。返回订单 |

### 6.2 后台接口(后台 Web 用)

> 所有 `/api/v1/admin/*` 接口需要先 `POST /api/v1/admin/login` 登录,服务端 set-cookie

| Method | Path | 说明 |
|---|---|---|
| POST | `/api/v1/admin/login` | body: `{ username, password }` |
| POST | `/api/v1/admin/logout` | 清 cookie |
| GET | `/api/v1/admin/me` | 当前登录态校验 |
| GET/POST/PUT/DELETE | `/api/v1/admin/categories[/:id]` | 分类 CRUD |
| GET/POST/PUT/DELETE | `/api/v1/admin/products[/:id]` | 商品 CRUD |
| PATCH | `/api/v1/admin/products/:id/on_sale` | 上下架切换 |
| GET/POST/PUT/DELETE | `/api/v1/admin/banners[/:id]` | banner CRUD |
| GET | `/api/v1/admin/orders` | 所有订单列表(分页) |
| GET | `/api/v1/admin/orders/:id` | 订单详情 |
| POST | `/api/v1/admin/upload` | 图片上传,返回 `{ url }` |

### 6.3 静态资源

| Path | 说明 |
|---|---|
| `/static/uploads/*` | 上传的图片 |
| `/static/icons/*` | 分类图标(初始内置一批) |

## 7. 验收标准(MVP 完成 = 这些全过)

1. ✅ 车机 App 装到一台横屏安卓平板/车机上,横屏显示,不崩
2. ✅ 首页能看到 banner、分类入口、商品列表,数据来自服务端(改后台后车机端能看到)
3. ✅ 点分类进列表,点商品进详情,点购买进确认页(显示预填地址),提交后弹支付二维码
4. ✅ 点击二维码弹层后变"支付成功",订单出现在"我的订单"中且状态为"已支付"
5. ✅ 重启车机 App,"我的订单"里之前的订单还在(同一设备 ID)
6. ✅ 后台能登录,能完成商品/分类/banner 的 CRUD,新增的商品上架后车机能看到
7. ✅ 后台订单列表能看到车机端所有下单记录,展示设备 ID 后 6 位

## 8. 技术约束

| 项 | 约束 |
|---|---|
| 车机端 | **Kotlin + Jetpack Compose + Material 3**,最低 API 24(Android 7.0,Compose 要求) |
| 车机端构建产物 | 普通 Android APK(非 AAOS-only),装到通用安卓车机/平板都能跑 |
| 车机屏幕 | **1920×1080 横屏锁死**(12.3" 中控基准),其他分辨率按比例缩,不做完整自适应 |
| 车机导航形态 | **左侧 Rail Navigation**(宽 240dp)+ 主内容区(1680dp),符合车机交互习惯 |
| 设计系统 | 必须使用 `design/安卓商城/tokens.json` 定义的 token,品牌方向 C · 海泡青电车感(Light mode only,no motion) |
| 设备识别 | `Settings.Secure.ANDROID_ID`,作为 `X-Device-Id` header 传给服务端 |
| 服务端 | Python ≥ 3.11(实际跑 3.12)+ FastAPI + SQLite + SQLAlchemy |
| 后台前端 | React 18 + Vite + Ant Design 5 |
| 图片存储 | 服务端本地文件目录(`/static/uploads/`),通过静态路径访问 |
| 部署 | 服务端跑在 Mac mini 上,接 cloudflared tunnel(沿用已有部署架构) |
| API | RESTful + JSON,HTTPS(经 cloudflared) |
| 金额单位 | 数据库和接口统一用**分**(int),前端显示时除 100 |

## 9. 非功能要求

- 车机端冷启动 < 3 秒
- 列表/详情接口响应 < 500ms
- 后台同时支持 1 个运营登录,不考虑并发
- Demo 数据,不需要做数据加密

## 10. 项目目录结构(规划)

```
车机商店需求/
├── SPEC.md                  本文件
├── sessions/                每个 session 的独立 spec
│   ├── 00-shared-conventions.md
│   ├── 01-server-skeleton.md
│   ├── 02-server-product-api.md
│   ├── ...
│
└── 实际代码会拆三个仓库(每个 session 内创建):
    ├── carshop-server/      Python FastAPI 服务端
    ├── carshop-admin/       React 后台
    └── carshop-android/     Android 车机端
```

## 11. Session 协同的核心约定

1. **数据模型 §5 是真理之源**:任何 session 想加字段、改字段,必须改这份 SPEC.md,再改实现
2. **API 契约 §6 是合同**:服务端和客户端(车机/后台)都按这份契约对接。先写契约,各端独立开发,联调时不需要重新约定
3. **金额一律用「分」**:数据库、接口、内部传递全用 int(单位:分),只在前端显示时除 100。任何 session 都不能例外
4. **图片走 URL,不走 base64**:上传走 `/api/v1/admin/upload` 拿 URL,其他接口字段存 URL
5. **设备 ID 由车机端注入到 header**:服务端从 `X-Device-Id` 取,不从 body 取
6. **Mock 必须走真契约**(见 §15):前端 session 用的 mock 数据必须取自后端 session 在 artifact 里留下的真实响应样例,不允许自己捏数据
7. **错误码统一**:见 §12,任何 session 不能发明自己的 code
8. **URL 规范统一**:见 §13,任何 session 不能发明自己的路径前缀
9. **契约校验导出**:每个后端 session 必须把实际接口的 OpenAPI schema 和请求/响应样例写到 artifact(见 §15)
10. **早集成兜底**:在大部分后端做完(session 06 之后)立即做一次早集成(session 07),不留到最后一刻

## 12. 统一错误码

所有响应统一格式:`{ "code": <int>, "data": <any|null>, "message": <string> }`

| code | 含义 | HTTP 状态码 |
|---:|---|---:|
| 0     | 成功 | 200 |
| 1000  | 参数错误(缺字段、类型不对、值非法) | 400 |
| 1001  | 资源不存在(查询的 ID 不存在 / 已删除) | 404 |
| 1002  | 资源冲突(如分类已有商品不能删) | 409 |
| 2000  | 未登录(后台接口) | 401 |
| 2001  | 登录信息错误(用户名密码错) | 401 |
| 3000  | 业务规则违反(如商品已下架不能下单) | 422 |
| 9000  | 服务端内部错误(兜底) | 500 |

**约定**:
- `message` 是给开发者看的,中文人话即可,不暴露技术细节
- 前端按 `code` 判断业务结果,**不要**只看 HTTP status code
- 没列入表的 code 一律不许用。要加先改本节

## 13. URL 与路径规范

| 类别 | 规范 |
|---|---|
| **API 前缀** | 全部 `/api/v1`,版本号锁死 v1 |
| **公开接口** | `/api/v1/<resource>`(不带 admin 段) |
| **后台接口** | `/api/v1/admin/<resource>`(必登录) |
| **静态资源** | `/static/uploads/<filename>`(上传图)、`/static/icons/<filename>`(内置图标) |
| **图片 URL 在 API 返回中的格式** | **完整 URL**(含 scheme + host),如 `https://carshop.hearagain.space/static/uploads/abc.png`。前端拿来直接 `<img src>`,不拼接前缀 |
| **资源 ID** | 数据库主键用整数自增,接口返回时仍是 int(不要转字符串) |
| **订单号** | 用形如 `O202605251200304721` 的字符串(年月日时分秒+序号),不暴露自增 ID |
| **分页参数** | 一律 `page`(从 1 开始)+ `page_size`(默认 20,最大 100) |
| **分页返回** | `{ list: [...], total: <int>, page: <int>, page_size: <int> }` |
| **时间格式** | API 返回时间一律 ISO 8601 字符串(`2026-05-25T12:30:00+08:00`),不要 timestamp |

## 14. 服务端配置约定

| 配置项 | 默认值 / 规则 | 备注 |
|---|---|---|
| `BASE_URL` | `http://localhost:8000`(开发)/ `https://carshop.hearagain.space`(生产) | 用于拼图片 URL |
| 服务端口 | `8000`(本地)/ 由 cloudflared ingress 决定(生产) | |
| SQLite 文件路径 | `./carshop.db`(项目目录下) | |
| 上传目录 | `./static/uploads/` | |
| Admin 默认账号 | `admin` / `admin123`(数据库初始化时创建) | Demo 可,生产前改 |
| 图片大小限制 | 单文件 5 MB | |
| 图片格式 | jpg / png / webp | |

## 15. 契约导出与 Mock 策略(关键!)

为了让前端 session 不依赖后端就能开发,**且最终联调时不爆雷**,使用「真契约驱动的 Mock」:

### 15.1 后端 session 完成时必须导出

每个后端 session 完成时,在 `artifacts/<session-id>-server-xxx.md` 里**必须**写:

1. **OpenAPI 片段**:把 FastAPI 自动生成的 `/openapi.json` 中本 session 涉及的接口部分,复制粘贴到 artifact
2. **每个接口的 cURL 调用例 + 真实响应 JSON**(完整,不删减,如):
   ```bash
   $ curl -X GET http://localhost:8000/api/v1/products/1 -H "X-Device-Id: abc"
   ```
   ```json
   { "code": 0, "data": { "id": 1, "title": "中石化加油卡 100 元", "product_type": "service_voucher", "price": 9500, ... }, "message": "ok" }
   ```
3. **错误码触发示例**:列出本 session 可能返回的非 0 错误码,每种给一个触发条件
4. **导出的 fixtures**:把上面的响应 JSON 保存为 `artifacts/fixtures/<session-id>/<endpoint>.json`,前端直接读

### 15.2 前端 session 启动时必须做的事

1. 读对应后端 artifact 的 fixtures,**不允许自己写测试数据**
2. 用 mock 库(车机端:`MockWebServer` / 后台:MSW)拦截请求,返回 fixtures 里的数据
3. 任何字段名 / 嵌套结构跟 fixtures 不一致 → **停下来,改 SPEC.md 或者跟用户确认**,不要绕过

### 15.3 早集成 session(07)的角色

session 07 = 后端基本完成后第一次真集成,目标:
- 把后端部署到 Mac mini(子域名待用户定)
- 把 admin-web 跑起来连真后端
- 跑一次完整的"创建分类 → 上传图 → 创建商品 → 查商品列表"流程
- **暴露所有契约不一致的地方** → 改 SPEC.md / 修代码 → 重新导出 fixtures
- 完成后,后续的车机端 sessions(08/09/10)用更新过的 fixtures

---

**版本**:v1.3
**最后更新**:2026-05-26(11 final · §5 Product.title 加 max_length=128 服务端校验说明)
