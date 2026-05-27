# Session Q3 · Admin-Web 公网部署(辅助 session)

> **Q 系列辅助 session**,不在主 12 个 session 序列里。
>
> 把 `carshop-admin/` build 后部署到 Mac mini,公网 `https://carshop-admin.hearagain.space`,让任何浏览器都能进后台,不依赖本地 npm run dev。

---

## 1. 你要做什么

### 1.1 子域名(已定)

`carshop-admin.hearagain.space` —— 用户 2026-05-26 拍板。

### 1.2 步骤(严格按此顺序)

#### Step 1:服务端先加 CORS 允许新 origin

新 admin 站要跨域调 `carshop.hearagain.space` 的 API,服务端必须放它进 allowlist。

```bash
ssh macmini 'cat ~/Library/LaunchAgents/com.user.carshop-server.plist | grep -A1 CARSHOP_CORS_ALLOW_ORIGINS'
```

看到当前值类似:
```
http://localhost:5173,http://127.0.0.1:5173,https://carshop.hearagain.space
```

需要追加 `,https://carshop-admin.hearagain.space`。两种改法:

**A(推荐)**:改 plist 文件,然后 `launchctl unload && load` 让服务重读 env
**B**:让用户在 Mac mini 上手动 nano plist + launchctl 重启

用 A(全自动):
```bash
ssh macmini "/usr/bin/plutil -replace EnvironmentVariables.CARSHOP_CORS_ALLOW_ORIGINS -string 'http://localhost:5173,http://127.0.0.1:5173,https://carshop.hearagain.space,https://carshop-admin.hearagain.space' ~/Library/LaunchAgents/com.user.carshop-server.plist"
ssh macmini "launchctl unload ~/Library/LaunchAgents/com.user.carshop-server.plist && launchctl load ~/Library/LaunchAgents/com.user.carshop-server.plist"
```

验证:
```bash
sleep 3
curl -sS https://carshop.hearagain.space/api/v1/health
# 应该仍 200,代表服务起来了
```

#### Step 2:本地 build admin-web 指向公网 API

```bash
cd /Users/Admin/Documents/Projects/车机商店需求/carshop-admin
VITE_API_BASE_URL=https://carshop.hearagain.space VITE_USE_MOCK=false npm run build
```

产物在 `carshop-admin/dist/`,看大小 < 5MB 为佳。

**验证 BASE_URL 真的烤进去了**:
```bash
grep -r "carshop.hearagain.space" dist/ | head -3
```

应该能看到引用。如果没看到,build 错了,要检查 `vite.config.ts` 是否在 build 时读 env。

> ⚠️ **关键**:`carshop-admin/src/api/client.ts` 必须真的用 `import.meta.env.VITE_API_BASE_URL`(07 加进去的)。如果没读,baseURL fallback 到 `/api/v1`,部署后会调不到接口。**build 前先 grep 确认**。

#### Step 3:rsync 到 Mac mini

```bash
ssh macmini 'mkdir -p ~/sites/carshop-admin'
rsync -avz --delete \
  --exclude='.DS_Store' \
  /Users/Admin/Documents/Projects/车机商店需求/carshop-admin/dist/ \
  macmini:~/sites/carshop-admin/
```

#### Step 4:跑 add_static_site.sh

```bash
ssh -t macmini 'bash ~/panqian_crawler/scripts/add_static_site.sh carshop-admin.hearagain.space ~/sites/carshop-admin'
```

这个脚本(用户 CLAUDE.md 文档化的)会自动:
1. 起一个 `python3 -m http.server` 在新端口(顺延找空闲,目前 18765-18771 都占,会用 18772)
2. Cloudflare DNS 加 CNAME(指向 panqian-tunnel)
3. 写 launchd plist + 加载
4. 改 `~/.cloudflared/config.yml` 加 ingress
5. 重启 cloudflared
6. curl 本地 + 公网测试

**等脚本输出 "OK" + 公网测试通过**。

#### Step 5:Verify 端到端

```bash
# 静态资源加载
curl -sSI https://carshop-admin.hearagain.space/ | head -5
# 应该 200 OK + text/html

# 一个 JS asset
curl -sSI "$(curl -sS https://carshop-admin.hearagain.space/ | grep -oE 'assets/[^\"]+\.js' | head -1 | sed 's|^|https://carshop-admin.hearagain.space/|')" | head -3
# 应该 200 OK

# 跨域 cookie 测试(浏览器手动):
# 1. 打开 https://carshop-admin.hearagain.space/
# 2. F12 → Network,登录 admin/admin123
# 3. 看 POST /api/v1/admin/login:
#    - Status 200
#    - Response 含 Set-Cookie: session=...; SameSite=None; Secure
#    - 下次刷新 GET /api/v1/admin/me 自动带 Cookie 头
# 4. 操作分类 / 商品 / banner / 订单各一次,无 401 / CORS 错误
```

### 1.3 跑通的 User Stories

这是公网 admin 第一次 demo,跑通这些(都是 06 跑过的 stories,在新环境下重新验证):

- **US-08** 登录(3 个场景)
- **US-09** 新建商品(成功 + 上传 5MB 拒 + 上传 .exe 拒)
- **US-10** 删分类(冲突 + 成功)
- **US-11** 看订单 + 详情 Drawer

每个故事在 artifact 里贴 Network 面板截图(展示真跨域 + cookie 工作)。

---

## 2. 你不要做什么

- ❌ **不要**部署 admin-web 到任何"开发模式"(`npm run dev`),必须 build 后静态托管
- ❌ **不要**改 `carshop-admin/` 业务代码(本 session 只做部署)
- ❌ **不要**自己改服务端代码,只改 launchd plist env 变量
- ❌ **不要**新建 cloudflared tunnel(沿用现有 panqian-tunnel,跟 07 一样)
- ❌ **不要**自己拍板子域名(已经定 `carshop-admin.hearagain.space`)
- ❌ **不要**改任何其他 session 的 artifact / 代码

---

## 3. 输入

- `CLAUDE.md`、`README.md`
- `sessions/Q3-admin-web-public-deploy.md`(本文件)
- `artifacts/07-early-integration.md`(看 07 部署模式,CORS env 名、cookie env 名都跟它对齐)
- `/Users/Admin/.claude/CLAUDE.md`(用户的 Mac mini 部署架构 / add_static_site.sh)
- `carshop-admin/`(06 产物)

---

## 4. 输出 / 交付物

- 公网 `https://carshop-admin.hearagain.space/` 可访问 + 可登录 + 可操作
- Mac mini 上:
  - `~/sites/carshop-admin/` 静态文件
  - `~/Library/LaunchAgents/com.user.staticsite-carshop-admin.plist`(由 add_static_site.sh 创建)
  - cloudflared config 多一段 ingress
  - 服务端 plist 的 `CARSHOP_CORS_ALLOW_ORIGINS` 多了 `https://carshop-admin.hearagain.space`
- `artifacts/Q3-admin-web-public-deploy.md`:
  - 步骤记录(每个 ssh / 脚本调用 + 输出)
  - 端口分配(应该是 18772 或更大)
  - 4 个 US 截图(US-08/09/10/11 在新公网下跑通)
  - 已踩坑
- 更新 `STATUS.md`:加 Q3 完成、加部署清单条目
- 更新 `README.md`:"已部署项目清单"(在用户全局 CLAUDE.md 里);本项目 README "快速上手"加公网地址
- 更新 `ORCHESTRATOR_LOG.md`:Q3 完成

---

## 5. 验收标准

1. ✅ `curl -I https://carshop-admin.hearagain.space/` 返回 200
2. ✅ 浏览器打开 https://carshop-admin.hearagain.space/,看到登录页(石墨蓝主色,12px 圆角)
3. ✅ admin/admin123 登录成功,Network 面板看到:
   - 调的是 `https://carshop.hearagain.space/api/v1/admin/login`(不是 localhost)
   - 响应 `Set-Cookie: session=...; SameSite=None; Secure`
4. ✅ 进入 /products,看到 13 条商品(12 seed + 06 测试单 + 07 数据)
5. ✅ 进入 /orders,看到至少 4 笔订单(3 seed + 我刚才 orchestrator 那笔)
6. ✅ 新建一个商品 + 上传一张图(US-09),刷新后看到,公网 image_url 正确
7. ✅ 删一个有商品的分类 → 红色 toast "该分类下还有 N 个商品"(US-10)
8. ✅ artifact 写完 + STATUS 更新

---

## 6. 依赖

- **上游**:06(admin-web 代码)、07(服务端公网部署 + CORS env 化)
- **下游**:11 final-integration(可以用公网 admin 演示)

## 7. Mock 策略

无,纯部署 session。

## 8. 已知坑

1. **端口冲突**:`add_static_site.sh` 自动找空闲,目前 18765~18771 占用,会用 18772+
2. **DNS 传播**:Cloudflare DNS 加 CNAME 后,**世界另一端可能要 1~5 分钟传播**,但本机 + Mac mini 之间通常秒级。如果 `curl https://carshop-admin.hearagain.space/` 拿到 cloudflared "1033 Error",等 1 分钟重试
3. **SPA 路由(react-router-dom)在静态托管下的 fallback**:`python -m http.server` **没有**自动 SPA fallback,直接访问 `https://carshop-admin.hearagain.space/products` 会 404,只有从 `/` 进去客户端路由才能跳过去。
   - 如果命中,记 TD:add_static_site.sh 升级支持 SPA fallback,或者用 caddy/nginx 替代 python http.server
   - Demo 阶段可以接受用户从首页进入
4. **CORS preflight cache**:浏览器会缓存 5 分钟,改完 plist 重启 server 后如果某些请求仍报 CORS,关 tab 重开
5. **plutil -replace 改 EnvironmentVariables**:这是字典子键,要用 dot notation;确认 plist 是 XML 格式(不是 binary)
6. **build 产物路径**:vite 默认 `dist/`,确认 `carshop-admin/vite.config.ts` 没改 outDir
7. **跨域 cookie**:07 已配 `SameSite=None; Secure`,这次部署到 HTTPS 子域,cookie 应自动工作

---

## 9. 跟用户交互的点

| 阶段 | 用户做啥 | Claude Code 做啥 |
|---|---|---|
| Step 1 改 plist | 等 | ssh 改 |
| Step 2 build | 等 | 跑 npm run build |
| Step 3 rsync | 等 | 跑 rsync |
| Step 4 add_static_site.sh | 等(可能 1~2 分钟)| 跑脚本,看输出 |
| Step 5 Verify 浏览器 | ⚠️ 用户打开浏览器登录,操作 4 个 US,截图 / 描述给 Claude Code | Claude Code 协助看 Network 面板 |
| 收尾 | 等 | 写 artifact + 更 STATUS |

**预计总时间**:15~30 分钟(主要看 build + cloudflared 传播)
