# Session 06 · Admin Web(后台管理 Web)

> React + Vite + Ant Design 5,运营用的后台。完成商品 / 分类 / Banner / 订单的所有管理页面。

---

## 1. 你要做什么

### 1.1 目录结构

```
carshop-admin/
├── package.json
├── vite.config.ts                 (proxy /api → http://localhost:8000)
├── tsconfig.json
├── index.html
└── src/
    ├── main.tsx                   (ConfigProvider 包 antdTheme + AntdApp + RouterProvider)
    ├── App.tsx                    (路由配置)
    ├── theme/
    │   └── antdTheme.ts           (从 00 session 已有)
    ├── api/
    │   ├── client.ts              (axios 实例,withCredentials: true,401 拦截跳登录)
    │   ├── category.ts            (类型 + 方法)
    │   ├── product.ts
    │   ├── banner.ts
    │   ├── order.ts
    │   └── upload.ts
    ├── pages/
    │   ├── Login.tsx              (登录页)
    │   ├── Layout.tsx             (登录后的布局:Sider 菜单 + Header + Content)
    │   ├── CategoryList.tsx
    │   ├── ProductList.tsx
    │   ├── ProductEdit.tsx        (新增 / 编辑)
    │   ├── BannerList.tsx
    │   ├── BannerEdit.tsx
    │   └── OrderList.tsx
    ├── components/
    │   ├── ImageUpload.tsx        (封装 AntD Upload,调 /api/v1/admin/upload)
    │   ├── PriceInput.tsx         (输入元,内部转分;显示分,内部转元)
    │   └── PriceDisplay.tsx       (分 → ¥XX.XX)
    └── mocks/                     (07 早集成之前的开发用)
        ├── browser.ts             (MSW setup)
        └── handlers.ts            (从 artifacts/fixtures/02~05 加载)
```

### 1.2 页面清单

| 页面 | 路径 | 功能 |
|---|---|---|
| 登录 | `/login` | 用户名密码登录 |
| 分类管理 | `/categories` | 列表 + 新建 Modal + 编辑 Modal + 删除确认 + 排序 |
| 商品管理 | `/products` | 列表(可按分类/上下架筛选)+ 上下架 Switch + 删除 |
| 商品新建/编辑 | `/products/new` `/products/:id/edit` | 完整表单(用 PriceInput、ImageUpload、规格、描述) |
| Banner 管理 | `/banners` | 列表 + 新建 Modal + 编辑 Modal + on_show Switch |
| 订单查看 | `/orders` | 只读列表(可按 status 筛),点查看详情 Drawer |

### 1.3 关键交互细节

- **未登录**:任何 page(除 /login)都跳 /login
- **401 拦截**:axios interceptor 捕获 401(code 2000)→ 清本地状态 → 跳 /login
- **价格输入**:用 `<PriceInput />` 组件,显示 "¥99.99",内部值是 9999(分)
- **图片上传**:AntD `<Upload>` 组件 + 自定义 `customRequest`,**显式调** `/api/v1/admin/upload`,拿到 URL 后填入表单字段
- **删除分类失败(有商品)**:服务端返回 code 1002 → 显示 antd `<message.error>` 提示"该分类下还有商品"
- **新增商品的 product_type**:Radio Group(实物 / 服务券),决定后续价格颜色样式(后台不需要,但是字段要传对)
- **MSW(开发用)**:`import.meta.env.DEV` 时启用 MSW 拦截。**fixtures 必须从 artifacts/fixtures/ 复制过来,不允许自己写数据**

### 1.4 表单字段(商品)

参照 SPEC.md §5 Product 模型:
- 标题(必填,1~100 字符)
- 分类(必选,下拉选)
- 类型(必选 Radio:实物 / 服务券)
- 现价(必填,PriceInput)
- 原价(可选)
- 规格(可选,单行文本)
- 主图(必选,ImageUpload)
- 描述(可选,Textarea)
- 是否上架(Switch)

---

## 2. 你不要做什么

- ❌ 自己实现 Modal / Form / Table(用 AntD)
- ❌ 美化超出 AntD 默认 + 主题色(Demo 后台,功能优先)
- ❌ 实现 SPEC 没写的功能(评价管理 / 用户管理 / 数据看板 / 导出 Excel)
- ❌ 国际化
- ❌ 移动端适配
- ❌ 权限分级
- ❌ 自己写图片上传库
- ❌ 自己写 mock 数据(必须从 artifacts/fixtures/ 拿)

---

## 3. 输入

- `CLAUDE.md`、`SPEC.md`(尤其 §5、§6.2、§12、§13)
- `carshop-admin/src/theme/`(00 session 已建)
- **`artifacts/02~05` 的所有 server artifacts**(尤其 fixtures/)
- 设计稿:**本 session 不强制按设计稿**,AntD 默认风格 + 主题色即可(后台是 Demo 内部用)

---

## 4. 输出

- `carshop-admin/` 完整 React 项目
- `artifacts/06-admin-web.md`:
  - 路由清单 + 截图(每个页面一张)
  - MSW handlers 列表(对照 fixtures)
  - 已踩坑

---

## 5. 验收(必须实际跑过)

**两种模式都要跑**:

### 5.1 Mock 模式(本 session 主要用这个)

```bash
cd carshop-admin
npm run dev    # vite dev server,MSW 启用,真后端没起也能跑
```

打开 localhost:5173:
1. ✅ 登录页能输入 admin/admin123,提交后跳到 /products
2. ✅ /categories 看到 5 个分类(seed),能新建、改、删(MSW 模拟返回成功)
3. ✅ /products 看到商品列表,能筛选,能上下架,能删
4. ✅ /products/new 能完整填表(含图片上传,MSW 拦截返回假 URL)
5. ✅ /banners 同上
6. ✅ /orders 看到订单列表(MSW 假数据)

### 5.2 真后端模式(把 MSW 关掉,真连 localhost:8000)

`VITE_USE_MOCK=false npm run dev`:
1. ✅ 登录:用真服务端的 admin/admin123
2. ✅ 操作分类:能调通真接口,DB 真的有变化
3. ✅ 上传图片:能传到 `carshop-server/static/uploads/`,返回完整 URL
4. ✅ 新增商品,然后开浏览器另一个标签调 `curl /api/v1/products`,能看到

### 5.3 出错场景

- ✅ 输错密码 → 显示 "用户名或密码错误"
- ✅ 删有商品的分类 → 显示 "该分类下还有 N 个商品"
- ✅ 上传 .exe → 显示 "文件格式不支持"
- ✅ 上传 > 5MB → 显示 "文件不能超过 5MB"

---

## 6. 依赖

- **上游**:00 design-system、02、03、04、05(后台接口全要)
- **下游**:07 早集成(直接用本 session 的产物跑联调)

---

## 7. Mock 策略

- **开发期**:用 MSW,fixtures **必须从 `artifacts/fixtures/02~05` 复制**到 `src/mocks/fixtures/`
- **任何字段名不一致** → 停下来,要么改 SPEC,要么找用户对齐,**不允许偷偷"修正"**

---

## 8. 已知坑

1. **价格元/分转换**:坚决在边界一次性转,内部全用分。Form 的 `initialValues` 也是分(显示前才除 100)
2. **AntD Upload 组件**:`customRequest` 写法绕开它默认行为,自己 `axios.post` 调 `/api/v1/admin/upload`
3. **图片预览**:上传成功后,要在表单里显示已上传的图(`<Image src={url} />`)
4. **MSW 启用方式**:`if (import.meta.env.DEV && import.meta.env.VITE_USE_MOCK !== 'false') { import('./mocks/browser').then(({worker}) => worker.start()) }`
5. **axios baseURL**:`/api/v1`(走 vite proxy 到 localhost:8000)
6. **`withCredentials: true`** 必须开,否则 cookie 不带
7. **路由用 react-router-dom v6 的 createBrowserRouter**,不要用旧的 BrowserRouter
8. **HMR + MSW**:开发体验顺,出问题先 hard refresh
