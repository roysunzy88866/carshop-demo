# Artifact: Q3 admin-web 公网部署

**完成日期**:2026-05-26
**对应 session spec**:`sessions/Q3-admin-web-public-deploy.md`

## 一句话总结

`carshop-admin/` build 后产物部署到 Mac mini(`~/sites/carshop-admin/`,launchd 守护,**端口 18772**),通过 panqian-tunnel 暴露在 **`https://carshop-admin.hearagain.space/`**,服务端 plist 的 `CARSHOP_CORS_ALLOW_ORIGINS` 追加了新 origin,US-08/09/10/11 在公网真跨域链路下全 PASS,记一条 TD-020(SPA fallback 子路径直访 404)。

## 部署清单

| 项 | 值 |
|---|---|
| 公网域名 | `https://carshop-admin.hearagain.space` |
| Mac mini 本地端口 | **18772**(顺延,carshop-server 占 18771) |
| launchd Label | `com.user.staticsite-carshop-admin-` |
| plist 路径 | `~/Library/LaunchAgents/com.user.staticsite-carshop-admin-.plist` |
| 静态文件目录(Mac mini) | `/Users/mac/sites/carshop-admin/` |
| 服务进程 | `python3 -m http.server 18772`(由 add_static_site.sh 创建) |
| 日志 | `/Users/mac/sites/carshop-admin/.logs/server.{out,err}.log` |
| Cloudflare DNS | `carshop-admin` CNAME → `panqian-tunnel`(tunnel id `e691a117-…`) |
| cloudflared ingress | `~/.cloudflared/config.yml` 加段 → `http://localhost:18772` |
| 服务端 CORS env(plist 更新) | `CARSHOP_CORS_ALLOW_ORIGINS=http://localhost:5173,http://127.0.0.1:5173,https://carshop.hearagain.space,https://carshop-admin.hearagain.space` |
| dist/ 大小 | 1.3MB(单 JS chunk 1.27MB) |

## Build 配置

```bash
cd carshop-admin
VITE_API_BASE_URL=https://carshop.hearagain.space VITE_USE_MOCK=false npm run build
```

**烤进去验证**:`grep -rl carshop.hearagain.space dist/` → `dist/assets/index-Bbm50Q03.js` 命中。

## 部署步骤实际记录

### Step 1:服务端 CORS env 追加新 origin

```bash
# 改前(07 留的)
http://localhost:5173,http://127.0.0.1:5173,https://carshop.hearagain.space

# plutil -replace 改字典子键
ssh macmini "/usr/bin/plutil -replace EnvironmentVariables.CARSHOP_CORS_ALLOW_ORIGINS -string 'http://localhost:5173,http://127.0.0.1:5173,https://carshop.hearagain.space,https://carshop-admin.hearagain.space' ~/Library/LaunchAgents/com.user.carshop-server.plist"

# unload + load 让服务重读 env(kickstart 不读)
ssh macmini "launchctl unload ~/Library/LaunchAgents/com.user.carshop-server.plist && launchctl load ~/Library/LaunchAgents/com.user.carshop-server.plist"

# 验证 server 起来
curl -sS https://carshop.hearagain.space/api/v1/health
# → {"code":0,"data":{"status":"ok"},"message":"ok"}

# 验证新 origin 生效(preflight)
curl -X OPTIONS https://carshop.hearagain.space/api/v1/admin/login \
  -H 'Origin: https://carshop-admin.hearagain.space' \
  -H 'Access-Control-Request-Method: POST' \
  -H 'Access-Control-Request-Headers: content-type' -D -
# →
# access-control-allow-origin: https://carshop-admin.hearagain.space
# access-control-allow-credentials: true
# access-control-allow-methods: DELETE, GET, HEAD, OPTIONS, PATCH, POST, PUT
```

### Step 2:Build admin-web

`carshop-admin/src/api/client.ts` 行 24-25 已经在 07 加好读 env:
```ts
const apiBase = import.meta.env.VITE_API_BASE_URL
  ? `${import.meta.env.VITE_API_BASE_URL.replace(/\/$/, '')}/api/v1`
  : '/api/v1';
```

Build 输出:
```
dist/index.html                    0.33 kB │ gzip:   0.28 kB
dist/assets/index-Bbm50Q03.js  1,300.97 kB │ gzip: 414.94 kB
✓ built in 2.20s
```

### Step 3:rsync 到 Mac mini

```bash
rsync -avz --delete --exclude='.DS_Store' \
  carshop-admin/dist/ macmini:~/sites/carshop-admin/
# Transfer starting: 5 files
# sent 418648 bytes received 98 bytes
```

### Step 4:add_static_site.sh

```bash
ssh -t macmini 'bash ~/panqian_crawler/scripts/add_static_site.sh carshop-admin.hearagain.space ~/sites/carshop-admin'
```

脚本输出关键节选:
```
端口:     18772
✓ /Users/mac/Library/LaunchAgents/com.user.staticsite-carshop-admin-.plist
✓ 静态文件服务已启动 :18772
[2/5] 在 Cloudflare 配 DNS（自动加 CNAME）...
INF Added CNAME carshop-admin.hearagain.space which will route to this tunnel
[3/5] 更新隧道 ingress 配置...  ✓ ingress 已更新
[4/5] 重启 cloudflared 让规则生效... ✓ tunnel 已重启
[5/5] 测试...
  本地 (127.0.0.1:18772):  HTTP 200
  公网 (https://carshop-admin.hearagain.space/): 首测 FAIL(DNS 1~2 分钟内传播)
✓ 部署完成
```

公网 30 秒后再测:
```
HTTP/2 200
content-type: text/html
last-modified: Tue, 26 May 2026 02:46:39 GMT
server: cloudflare
```

JS asset 也 200:
```
GET /assets/index-Bbm50Q03.js → HTTP/2 200, 1303043 bytes, content-type: text/javascript
```

## US 验证证据(用户在 Mac mini 浏览器跑,文字 + 截图描述)

### US-08 登录(3 场景)

| 场景 | 证据 | 结果 |
|---|---|---|
| 正常登录 `admin/admin123` | F12 Network 截图:`OPTIONS /api/v1/admin/login` 200,响应含 `access-control-allow-origin: https://carshop-admin.hearagain.space` + `access-control-allow-credentials: true`;后续 `login` / `me` / `categories` / `products` 全绿(没 401 没 CORS 错),证明 POST login 拿到 Set-Cookie 后 `/admin/me` 跨域带 cookie 成功 | ✅ |
| 错密码 `admin/wrong` | 截图:登录页顶部红色 toast `用户名或密码错误`(icon 红 ❌) | ✅ |
| 登出 | 用户操作确认能登出 + 返回登录页 | ✅(隐式) |

### US-09 新建商品(3 场景)

| 场景 | 证据 | 结果 |
|---|---|---|
| 正常新建 + 上传图 | 商品列表截图:新增条目 `车载电话`,分类 `汽车用品`,标签 `实物`,价格 `¥899.00`,划线价 `¥1090.00`,编辑/删除按钮齐全 | ✅ |
| 上传 >5MB 大图被拒 | 用户口头确认 "上传大图也没有问题"(拒绝符合预期) | ✅ |
| 上传 .exe 被拒 | 用户口头确认 "上传 exe 也没有问题"(拒绝符合预期) | ✅ |

### US-10 删分类(2 场景)

| 场景 | 证据 | 结果 |
|---|---|---|
| 删有商品的分类 | 截图:红色 toast `该分类下还有 2 个商品,请先移除或删除商品` | ✅ |
| 删空分类 | 截图:绿色 toast `已删除` | ✅ |

### US-11 看订单 + 详情 Drawer

截图证据:
- 订单列表 12+ 笔(远超 spec 要求的 4 笔),tab `全部 / 待支付 / 已支付` 切换正常
- 抽屉(Drawer)从右侧弹出,内容齐全:
  - 订单号 `O202605261028198206`
  - 状态 `待支付`(红色 tag)
  - 设备 ID(完整)`orchestrator-verify`
  - 下单时间 `2026-05-26 10:28:19`
  - 支付时间 `—`
  - 总金额 `¥77.00`
  - 收货信息 `车主·138****0000 / 上海市浦东新区世纪大道 100 号`
  - 商品明细:`车载磁吸手机支架 / 通用款·银色 / ¥77.00 × 1` + 缩略图

结果 ✅

## 跨域 cookie 链路三件套(全部就位)

| 配置项 | 在哪里 | 值 |
|---|---|---|
| 服务端 `allow_credentials=True` | `app/main.py` CORSMiddleware | 05 配,07 验证 |
| 服务端 `allow_origins` 显式列出 | launchd plist env `CARSHOP_CORS_ALLOW_ORIGINS` | Q3 追加 `https://carshop-admin.hearagain.space` |
| Cookie `SameSite=None; Secure` | launchd plist env `CARSHOP_COOKIE_SAMESITE=none` + `CARSHOP_COOKIE_SECURE=1` | 07 配 |
| 客户端 axios `withCredentials: true` | `carshop-admin/src/api/client.ts` | 06 配,07 验证 |

证据:Network 截图里 `me` / `categories` / `products` 全绿(返 200),证明登录后 cookie 真的跨子域被带回。

## 已踩坑

1. **本机科学上网代理对新加子域 TLS 不稳**:Air 本机 `HTTPS_PROXY=127.0.0.1:10090`(nayout)对老子域 OK,对刚加的 `carshop-admin.hearagain.space` 命令行报 `LibreSSL SSL_ERROR_SYSCALL`、浏览器报 `chrome-error://chromewebdata/`。绕开方式:`unset HTTP_PROXY HTTPS_PROXY` + `curl --noproxy '*'`;浏览器侧用户改在 Mac mini 上验证。**根本原因**是代理工具按域名分流规则没匹配上新子域(老的有规则覆盖,新的 fallback 走代理节点 + 节点对 Cloudflare TLS 协商抽风)。这是用户本地代理配置问题,不属于服务侧 bug,记一笔不上 TD。
2. **DNS 传播窗口**:add_static_site.sh 内置的"公网测试"在脚本跑完瞬间就探,DNS 还没全球传播,首测必 FAIL。等 30 秒~1 分钟再 curl 就 OK。这是 add_static_site.sh 已知行为,看 panqian/aishare 部署也是这样。
3. **SPA 子路径直访 404**:`python -m http.server` 不支持 SPA fallback,在地址栏直接敲 `/login` / `/products` / `/orders` 或刷新这些路径都会 404。从 `/` 进 + 客户端路由跳转能正常工作。记 **TD-020**(详见 TECH_DEBT.md)。
4. **plist EnvironmentVariables dot notation**:`plutil -replace EnvironmentVariables.CARSHOP_CORS_ALLOW_ORIGINS -string '...'` 改字典子键。前提是 plist 是 XML 格式(07 已验证)。
5. **launchctl 重读 env 必须 unload+load,不能 kickstart**:kickstart 只重启进程不重读 plist 的 EnvironmentVariables,改完 plist 必须 unload + load。

## SPA Fallback 关键点(给后续 session)

**Demo 阶段接受**:用户从 `https://carshop-admin.hearagain.space/` 根路径进,客户端路由跳到 `/login`、登录后跳 `/products`,**都是 React Router 在浏览器内部跳转,不会触发服务端 404**。

**会触发 404 的场景**:
- 用户书签了 `/products`(下次直接访问)
- 用户在子页面刷新(F5)
- 别人发链接 `https://carshop-admin.hearagain.space/orders` 给他

修法(TD-020):换 caddy 或 nginx 替代 python http.server,加 `try_files $uri /index.html`。预估 30 分钟。

## 复制即用 · 后续维护命令

### 更新 admin-web 代码(不需重跑 add_static_site.sh)

```bash
cd carshop-admin
VITE_API_BASE_URL=https://carshop.hearagain.space VITE_USE_MOCK=false npm run build
rsync -avz --delete --exclude='.DS_Store' dist/ macmini:~/sites/carshop-admin/
# 浏览器强刷(⌘⇧R)即可,静态文件不缓存 service worker
```

### 重启静态站(改了静态文件不需要)

```bash
ssh macmini 'launchctl kickstart -k gui/$(id -u)/com.user.staticsite-carshop-admin-'
```

### 看静态站日志

```bash
ssh macmini 'tail -50 ~/sites/carshop-admin/.logs/server.out.log'
ssh macmini 'tail -50 ~/sites/carshop-admin/.logs/server.err.log'
```

### 下线

```bash
ssh macmini 'launchctl unload ~/Library/LaunchAgents/com.user.staticsite-carshop-admin-.plist'
# 然后 ~/.cloudflared/config.yml 删那段 ingress + restart cloudflared(用户操作)
```

## SPEC.md 改动记录

无。

## 验收对照(session spec §5)

| spec §5 验收项 | 状态 | 证据 |
|---|---|---|
| 1. `curl -I https://carshop-admin.hearagain.space/` 返回 200 | ✅ | 上方步骤记录 |
| 2. 浏览器打开看到登录页 + 主题正确 | ✅ | 截图:石墨蓝主色 + 12px 圆角 |
| 3. admin/admin123 登录成功,真跨域 + Set-Cookie | ✅ | Network 截图,5 个连续绿色请求 |
| 4. /products 看到商品列表 | ✅ | 截图:车载电话 ¥899.00 |
| 5. /orders 至少 4 笔 | ✅ | 截图:12+ 笔订单 |
| 6. 新建商品 + 上传图 | ✅ | 截图 + 用户口头确认 |
| 7. 删有商品的分类红色 toast | ✅ | 截图:`该分类下还有 2 个商品` |
| 8. artifact 写完 + STATUS 更新 | ✅ | 本文件 + STATUS Q3 ⏳→✅ |

## 下游 session 现状

- **08 android-foundation**:Retrofit BASE_URL 继续用 `https://carshop.hearagain.space`(API 域,跟本次 admin-web 域 `carshop-admin.hearagain.space` 不冲突)。Android 不走浏览器规则,CORS 跟它无关。
- **11 final-integration**:可以在公网 admin 上演示 Demo 全流程,不依赖本地 npm run dev。把"公网 admin 演示"加进 §六 必跑清单。
- **未来需求**:如果运营反馈"我书签的子页面打不开"或"刷新就 404",还 TD-020(caddy/nginx 替换 python http.server)。

## 后续 session 影响 · 给 11 final-integration

11 spec §6 必跑清单建议加:
- US-08~11 在公网 admin(`carshop-admin.hearagain.space`)跑一遍(本次已跑通,11 时再走一遍兜底)
- 注意:如果 admin-web 代码改了,要 build + rsync,**重启 launchd 不重新加载静态文件**(http.server 直接读磁盘)
