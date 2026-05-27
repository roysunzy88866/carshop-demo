# Artifact · Session 06 · Admin Web(运营后台)

**完成日期**:2026-05-25
**对应 session spec**:`sessions/06-admin-web.md`
**状态**:✅ 已完成 · **Mock 模式 11/11 + 真后端模式 11/11 实跑通过**(US-08/09/10/11 + 错误场景)

---

## 一句话总结

在 `carshop-admin/` 上把 00 的主题骨架做成完整运营后台:React 18 + Vite + AntD 5 + react-router v6 + axios + MSW。**两种模式都跑通**——Mock 用 MSW + 内存状态机(初始数据从 `artifacts/fixtures/02~05` 真实响应导入,LocalStorage 持久化 loggedIn),真后端走 vite proxy 到 `localhost:8000`。Playwright 自动化跑 US-08~11 全部场景,**两种模式各 11 项行为验收全过**,截图 + DB 物证齐全。

---

## 1. 验收清单(对照 session spec §5 + USER_STORIES §六)

### 1.1 启动 / 通用

| # | 验收点 | 状态 | 证据 |
|---|---|---|---|
| 共享约定 1 | 金额单位全用「分」,只在前端边界转换 | ✅ | `PriceInput`/`PriceDisplay`,DB 实测 ¥12.34 → price=1234 |
| 共享约定 2 | 图片走 URL · 上传走 `/admin/upload` 拿 URL | ✅ | `ImageUpload.customRequest` 显式调,见 §4 |
| 共享约定 4 | 错误码用 SPEC §12 码表,前端按 code 判断 | ✅ | `client.ts` 全局 envelope 解包 + `ApiError(code)` |
| 共享约定 5 | URL 前缀按 SPEC §13(公开/后台) | ✅ | axios `baseURL='/api/v1'`,所有 admin 接口在 `/admin/*` |
| 共享约定 6 | 图片返回完整 URL,前端直接 `<img src>` | ✅ | 见 `ImageUpload` / `PriceDisplay` |
| 共享约定 7 | 分页 `page` 从 1 起,`page_size` 默认 20 | ✅ | `ProductList` / `OrderList` 默认 20 |
| 共享约定 8 | 时间 ISO 8601 字符串,不要 timestamp | ✅ | `dayjs(order.created_at).format(...)` 直接消费 |
| 共享约定 10 | 前端 mock 取自后端 fixtures,不允许自捏 | ✅ | `src/mocks/fixtures/{catalog,banner,order,auth}/` 直接 cp 自 `artifacts/fixtures/02~05`,**未改一字** |

### 1.2 USER_STORIES · 行为验收(US-08/09/10/11)

| Story | 场景 | Mock 模式 | 真后端模式 |
|---|---|:-:|:-:|
| **US-08** 运营登录 | 1. admin/admin123 → 跳 /products | ✅ | ✅ |
| | 2. 密码错 → 文案 "用户名或密码错误" | ✅ | ✅ |
| **US-09** 新建商品 | 1. 上传 png + 填表 + 提交 + 列表里看到 + 价格边界正确 | ✅ | ✅ (DB id=13,price=1234,on_sale=1) |
| | 2. 上传 > 5MB → 文案 "文件不能超过 5MB",**不落地** | ✅ | ✅ (client-side 拦,beforeUpload return LIST_IGNORE) |
| | 3. 上传 .exe → 文案 "文件格式不支持" | ✅ | ✅ |
| **US-10** 删分类 | 1. 删有商品分类 → 文案 "该分类下还有 N 个商品" (code=1002) | ✅ | ✅ |
| | 2. 新建空分类 → 删 → 成功 | ✅ | ✅ |
| **US-11** 看订单 | 1. 列表带 `device_id_short`(后 6 位)+ 总金额 + 状态 tab | ✅ | ✅ |
| | 2. 点单行打开 Drawer · 商品快照 + shipping_info 全显示 | ✅ | ✅ |

**汇总**:`mock 模式验证:11/11 通过` · `real 模式验证:11/11 通过`(脚本输出存在 `06-admin-web-screenshots/<mode>/stories/results.json`)。

---

## 2. 交付物清单

### 2.1 新增 / 改动文件

```
carshop-admin/
├── package.json                        # +deps: axios react-router-dom dayjs msw @playwright/test @ant-design/icons
├── vite.config.ts                       # +proxy /api → localhost:8000
├── public/
│   └── mockServiceWorker.js             # MSW 安装的 SW 脚本 (npm run msw:init)
├── scripts/
│   ├── screenshot.mjs                   # Playwright 自动截图(每个页面一张)
│   └── verify-stories.mjs               # Playwright US-08~11 行为验证
└── src/
    ├── main.tsx                         # +VITE_USE_MOCK=true 时启 MSW
    ├── App.tsx                          # ← 替换 00 demo 为 RouterProvider+AuthProvider+AntApp
    ├── router.tsx                       # createBrowserRouter
    ├── vite-env.d.ts                    # ImportMetaEnv 类型
    ├── auth/AuthContext.tsx             # me/login/logout + 401 拦截 forceLogout
    ├── api/
    │   ├── client.ts                    # axios + envelope 解包 + ApiError + Page<T>
    │   ├── auth.ts category.ts product.ts banner.ts order.ts upload.ts
    ├── pages/
    │   ├── Login.tsx                    # 登录页
    │   ├── Layout.tsx                   # 登录后骨架(Sider 4 菜单 + Header 用户/登出)
    │   ├── CategoryList.tsx             # 列表 + 新建/编辑 Modal + 删除 (US-10)
    │   ├── ProductList.tsx              # 列表 + 分类筛/上下架筛 + 分页 + on_sale Switch
    │   ├── ProductEdit.tsx              # 新建/编辑表单 (US-09)
    │   ├── BannerList.tsx               # 列表 + 新建/编辑 Modal · link_type 联动
    │   └── OrderList.tsx                # 只读列表 + 详情 Drawer (US-11)
    ├── components/
    │   ├── PriceInput.tsx               # 元↔分边界一次性转换
    │   ├── PriceDisplay.tsx             # 分→¥XX.XX
    │   └── ImageUpload.tsx              # AntD Upload + customRequest 调 /admin/upload
    └── mocks/
        ├── browser.ts                   # MSW setupWorker
        ├── handlers.ts                  # 全套 handler(15 个接口) + 链接联动校验
        ├── state.ts                     # 内存状态机 + localStorage 持久化
        └── fixtures/                    # 直接 cp 自 artifacts/fixtures/02~05(共 41 个 JSON)
            ├── catalog/  (17 个)
            ├── banner/   (5 个)
            ├── order/    (12 个)
            └── auth/     (8 个)
```

### 2.2 截图(Playwright 自动跑出来,本机实跑实证)

`artifacts/06-admin-web-screenshots/mock/` 和 `/real/` 两套各 6 张:

- `01-login.png` 登录页
- `02-categories.png` 分类管理(5 条 seed + 新建按钮 + 删除二次确认)
- `03-products.png` 商品管理(real 模式 13 条 — 含自动化测试创建的 id=13)
- `04-product-new.png` 新建商品表单
- `05-banners.png` Banner 管理(3 条 seed + 跳转类型 tag)
- `06-orders.png` 订单查看(tab + device_id_short + ¥ 总金额)

外加每个 mode 子目录 `stories/` 下 11 张 US 行为证据 PNG。

**复现命令**:`npm run screenshot`(mock) / `MODE=real npm run screenshot`(real)
**前置**:dev server 起在 `http://127.0.0.1:5173`,模式由 `VITE_USE_MOCK` 决定。

---

## 3. 路由 / 页面 / 接口对照

### 3.1 路由表(react-router-dom v6 · `createBrowserRouter`)

| Path | 组件 | 守卫 | 说明 |
|---|---|---|---|
| `/login` | `Login` | 无 | 登录页(未登录默认进这里) |
| `/` | `Layout` | `user || → /login` | 登录后骨架,index → /products |
| `/categories` | `CategoryList` | ✓ | US-10 |
| `/products` | `ProductList` | ✓ | US-09 列表 |
| `/products/new` | `ProductEdit` | ✓ | US-09 新建 |
| `/products/:id/edit` | `ProductEdit` | ✓ | 编辑 |
| `/banners` | `BannerList` | ✓ | Banner 管理 |
| `/orders` | `OrderList` | ✓ | US-11 |
| `*` | → `/` | — | 404 fallback |

### 3.2 axios client 关键配置

```ts
baseURL: '/api/v1'        // 走 vite proxy 到 localhost:8000(或 MSW 拦截)
withCredentials: true     // ★ 必开,否则 cookie 不带 — 05 cookie 不工作
timeout: 15000
// 全局拦截:envelope.code !== 0 → throw ApiError(code, message, httpStatus)
// 401 / code=2000(除 /admin/me)→ 触发 forceLogout 跳 /login
```

### 3.3 后台接口消费清单(对照 02/03/04/05 artifact)

| 接口 | 调用方 | 来源 fixture |
|---|---|---|
| `POST /admin/login` | `Login.tsx` | `auth/post-login-{success,wrong-password}.json` |
| `POST /admin/logout` | `Layout.tsx`(Dropdown) | `auth/post-logout-success.json` |
| `GET /admin/me` | `AuthContext.refresh` | `auth/get-me-{success,not-logged-in}.json` |
| `POST /admin/upload` | `ImageUpload.customRequest` | `auth/post-upload-{success,too-large,wrong-format}.json` |
| `GET/POST/PUT/DELETE /admin/categories` | `CategoryList` | `catalog/get-categories.json` 等 |
| `GET/POST/PUT/PATCH/DELETE /admin/products` | `ProductList` / `ProductEdit` | `catalog/get-admin-products*.json` 等 |
| `GET/POST/PUT/DELETE /admin/banners` | `BannerList` | `banner/*.json` |
| `GET /admin/orders` | `OrderList` | `order/admin-get-orders-list.json` |
| `GET /admin/orders/:id` | `OrderList`(Drawer)| `order/admin-get-order-detail.json` |

字段名、嵌套结构、`device_id_short`、`product_snapshot`、ISO 8601 时间、分页 envelope `{list,total,page,page_size}` 全部 100% 跟 fixtures 对齐,**没改一个字段**。

---

## 4. MSW 实现要点(完整模拟,不糊弄)

### 4.1 内存状态机(`src/mocks/state.ts`)

启动时从 4 个 fixture JSON 初始化:`categories`(5)、`products`(12)、`banners`(3)、`orders`(1),还有 `loggedIn` 标志。

**持久化**:整个 `state` 序列化到 `localStorage['carshop-msw-state']`,每次 mutate 后写一次。**这样 SPA 刷新不会丢登录态**,真实 dev UX 接近真后端。

**重置**:浏览器 DevTools `localStorage.removeItem('carshop-msw-state')` + 刷新,或 `import.resetState()`。

### 4.2 Handler 行为对齐 SPEC

完整覆盖 15 个后台接口 + 业务校验:

- **登录**:用户名/密码硬编码 `admin/admin123`,匹配返回 200 + `Set-Cookie`(浏览器实际不存,因为 SW 不能设真 cookie——靠 `loggedIn` 标志模拟);不匹配返回 `code=2001` HTTP 401
- **`require_admin` 等效**:每个 admin 路由开头检查 `state.loggedIn`,无登录返回 `code=2000` HTTP 401(整套 02/03/04/05 后台路由的鉴权行为完全等效)
- **上传**:校验扩展名(jpg/png/webp)+ 大小(5MB),通过则返回 picsum 占位 URL(前端能加载到真图);失败返 `code=1000`
- **分类删除**:统计 `products.filter(p => p.category_id === id).length`,>0 时返 `code=1002` + 文案 "该分类下还有 N 个商品,请先移除或删除商品"(完全复刻 02 artifact §3.3)
- **Banner link 联动校验**:`link_type=none` 但 `link_target` 非空 → 1000;`link_type=product/category` 但目标不存在 → 1000(复刻 03 artifact 决策 1 的严格策略)
- **商品分页**:`category_id` / `on_sale` 筛选 + slice 分页,返回 `{list,total,page,page_size}`
- **订单列表**:`status` 筛选 + `created_at` 倒序 + 分页

### 4.3 启动方式

`src/main.tsx`:

```ts
if (import.meta.env.VITE_USE_MOCK === 'true') {
  const { worker } = await import('./mocks/browser');
  await worker.start({ onUnhandledRequest: 'bypass' });
}
```

`onUnhandledRequest: 'bypass'` 让 MSW 不拦截非 `/api/*` 的请求(如 vite HMR、AntD 静态图)。

---

## 5. 已踩过的坑 / 决策记录

### 坑 1:Service Worker 状态不跨页刷新 → MSW 登录态丢失

**症状**:Playwright `page.goto('/categories')` 触发全页面 reload,导致 MSW state 模块级变量重新初始化(loggedIn=false),用户立刻被踢回 /login。所以测试只能看到登录页 6 次。

**修法**:
1. **MSW state 持久化到 `localStorage`**(`state.ts:loadFromStorage/persist`),刷新存活
2. **Playwright 改用 SPA 内部导航**:点 Sider 菜单 item 而不是 `page.goto`

两套都做了,任何一处坏了另一套兜底。

### 坑 2:vite dev server 端口冲突静默失败 → 跑了 mock 当 real

**症状**:第一次跑 `kill <bg-id>` 没把 vite 进程链(`npm exec` → `node vite`)杀干净,第二个 `vite --port 5173` 不报错就退出,但旧的 mock vite 还占着端口。Playwright 连过去拿到的是 MSW 模式,验证全过但 DB 没动。

**发现方式**:`ps aux | grep vite` 看到两个 vite 进程都活着;`sqlite3 carshop.db "SELECT count(*) FROM products;"` 还是 seed 的 12,但 UI 显示 13。

**修法**:`kill <pid> <pid> <pid>` 把 `node vite` / `npm exec vite` / `zsh -c eval` 三个进程一起杀;之后跑真模式 sqlite 立刻有 id=13,确认双链路打通。

**留给下游**:同时维护 mock / real 两个 dev server 时,**端口要错开**(mock 用 5173,real 用 5174 之类),不要复用。本 session demo 阶段不重要(切模式重启即可),但 07 集成期可能要并存。

### 坑 3:AntD InputNumber 内部 `<input>` 不响应 `input[id="price"]`

**症状**:Playwright `page.fill('input[id="price"]', '12.34')` 等不到元素。

**原因**:AntD InputNumber 把 Form.Item 注入的 id 给到 wrapper,内层 input 走 `.ant-input-number-input`。

**修法**:用复合选择器 `input#price, .ant-input-number-input` 兜底。

### 坑 4:Card `bordered` 已废弃(warning,不影响功能)

AntD 5 后续版本把 `<Card bordered>` 改成 `<Card variant>`。本 session 没改,留 warning 给后续视情况批量替换。

### 决策 1:axios `withCredentials: true` 必开

否则 05 的 cookie 不会随后续请求带回去,所有 admin 接口都 2000。**这是 06 第一个要确认的关键**。

### 决策 2:`<App>` 用 `Ant App` 包一层

为了用 `useApp().message` 而不是直接 `import { message } from 'antd'`——后者在 AntD 5+ 不读 ConfigProvider 主题(static method 限制)。所有页面都通过 `const { message } = App.useApp();` 拿。

### 决策 3:ProductEdit 编辑模式分两个请求

PUT `/admin/products/:id` 不含 `on_sale`(02 artifact §6 "PUT 不能改 on_sale"),所以编辑保存时如果用户改了上架开关,要再发一次 `PATCH /admin/products/:id/on_sale`。前端把这事拆成 `update + setOnSale` 两次 await,用户感知不到。

### 决策 4:Order DELETE 没有 Demo 入口(SPEC 决策)

后台订单列表只读,**不暴露删除/状态修改入口**(SPEC §6.2 后台订单只 GET)。

---

## 6. SPEC.md / USER_STORIES.md 改动记录

**无改动。** 06 严格落地 SPEC §6.2 后台接口契约、§12 错误码、§13 URL 规范,字段名、错误码、分页结构没动。USER_STORIES.md §六索引表里 06 的 4 个 stories 全部按原文跑通。

---

## 7. 启动 / 复跑命令

### Mock 模式(MSW · 不依赖后端)

```bash
cd carshop-admin
npm install                      # 第一次跑
npm run msw:init                 # 如果 public/mockServiceWorker.js 不存在
npm run dev:mock                 # VITE_USE_MOCK=true npx vite,默认 5173
```

打开 `http://localhost:5173`,直接用 `admin/admin123` 登录(Login 表单已预填)。

### 真后端模式(走 vite proxy → localhost:8000)

```bash
# 终端 A:起后端
cd carshop-server
source .venv/bin/activate
uvicorn app.main:app --host 127.0.0.1 --port 8000

# 终端 B:起前端
cd carshop-admin
npm run dev:real                 # VITE_USE_MOCK=false
```

打开 `http://localhost:5173`,用 admin/admin123 登录(真后端的)。

### 自动化验证

```bash
# 截图(每页一张,落到 artifacts/06-admin-web-screenshots/<mode>/)
npm run screenshot               # mock
MODE=real npm run screenshot

# US-08/09/10/11 行为验证(11 项,落 PNG 证据 + results.json)
node scripts/verify-stories.mjs               # mock
MODE=real node scripts/verify-stories.mjs
```

---

## 8. 给下游 session 的接力说明

### 给 07 early-integration(直接消费方)

- **前端代码就绪**,真后端模式已实跑通过,07 把整套部署到 Mac mini(`carshop.hearagain.space`)时:
  - 改 `vite.config.ts` proxy target 到生产域(或干脆删 proxy,改用绝对 URL via `VITE_API_BASE`)
  - 后端 `app/main.py` CORS `allow_origins=["*"]` + `allow_credentials=true` 同用,**浏览器会拒**(05 artifact §9 已提醒),改成具体 origin
  - 后端 cookie 切 `SameSite=None; Secure`(05 已支持 env 切换)
- **MSW fixtures 是契约金标准**:07 集成时如果遇到字段不对齐,要么改 SPEC 改后端、要么改前端,**不允许偷偷"修正" fixtures**
- 后台已就绪的 CRUD 入口可以直接给用户演示

### 给 08~10 android sessions(参考价值)

- `src/mocks/fixtures/` 整套 mock 数据已经过 06 实测对齐,**Android `MockWebServer` 可以直接消费这些 JSON**(SPEC §15.2)
- 价格边界转换、ISO 时间格式、分页结构、`device_id_short` 字段名都在这里落地一遍,Android 端按同样格式即可
- 后台已支持 admin 真实工作流(分类/商品/banner/订单 全套 CRUD),Android 调试期间可以从后台造数据

### 给 11 final-integration

- 全套 13 个 US 中,06 这边已经实跑 US-08/09/10/11(后台 4 个),11 阶段重点跑 US-12/13(后台改 → 车机端看到 / 车机端下单 → 后台看到)的双向联动
- 测试脚手架 `scripts/verify-stories.mjs` 可以扩展加 US-12/13 case,复用 Playwright 基础设施

---

## 9. 已知遗留(不阻塞 session 完成)

1. **分类图标显示破图**:seed 里 `/static/icons/car-parts.svg` 等图标文件实际不在 server 上(01 session seed 时只写了 URL,没放文件)。需要 07 阶段补 5 个 SVG 到 `carshop-server/static/icons/` 或重写 seed 改成 picsum 占位。**不影响 admin 功能**。
2. **AntD Card `bordered` deprecation warning**:5+ 的 API 变更,replace 一下即可,不影响功能。
3. **CORS `allow_origins=["*"]` 在生产带 cookie 会失败**:05 artifact §9 已记录,留给 07。
4. **测试产生的脏数据**:`carshop-server` DB 里有 1 条 `id=13 "US-09 自动化测试商品"` 是 verify-stories 真后端跑出来的真物证。可以 sqlite 删一下或 `python scripts/init_db.py` 重置(会重新 seed)。

---

**版本**:v1.0
**实跑验证**:Mock 模式 11/11 + 真后端模式 11/11 行为验收通过,截图齐全,DB 物证齐全(2026-05-25)。
