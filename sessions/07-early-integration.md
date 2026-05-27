# Session 07 · Early Integration(早集成 · 检查站)⭐

> **关键检查站**。把后端 + 后台 Web 第一次真集成,部署到 Mac mini,**暴露所有契约漂移**,在车机端开工前修完。
>
> **本 session 必须串行,不允许并发任何其他 session**。

---

## 1. 你要做什么

### 1.1 部署后端到 Mac mini

**子域名已确定**:`carshop.hearagain.space`(用户 2026-05-25 拍板,见 STATUS.md 决策记录)。

按用户 CLAUDE.md 描述的标准流程:

1. **本地准备**:
   - 确认 `carshop-server/` 跑得通(本地 `uvicorn` 起得来)
   - 写一份 `requirements.txt`(从 pyproject 导出)
2. **同步代码到 Mac mini**:
   ```bash
   ssh macmini 'mkdir -p ~/carshop-server'
   rsync -avz --delete --exclude='carshop.db' --exclude='__pycache__' \
     ~/Documents/Projects/车机商店需求/carshop-server/ macmini:~/carshop-server/
   ```
3. **Mac mini 上装依赖 + 初始化 DB**:
   ```bash
   ssh macmini 'cd ~/carshop-server && python3.12 -m venv venv && source venv/bin/activate && pip install -r requirements.txt && python scripts/init_db.py'
   ```
4. **写 launchd plist**(参考 `~/panqian_crawler/scripts/add_static_site.sh` 的做法),起 uvicorn 守护进程,端口顺延(18767+):
   - 文件:`~/Library/LaunchAgents/com.user.carshop-server.plist`
   - `ProgramArguments`:`uvicorn app.main:app --host 127.0.0.1 --port 18767`
   - `WorkingDirectory`:`/Users/mac/carshop-server`
   - `KeepAlive`:true
   - `StandardOutPath` / `StandardErrorPath`:`~/carshop-server/.logs/server.{out,err}.log`
5. **接 cloudflared**:
   - DNS:Cloudflare 加 CNAME `<subdomain>.hearagain.space` 指向 panqian-tunnel
   - `~/.cloudflared/config.yml` 加 ingress 规则:
     ```yaml
     - hostname: <subdomain>.hearagain.space
       service: http://localhost:18767
     ```
   - 重启 cloudflared
6. **验证公网可达**:
   ```bash
   curl https://<subdomain>.hearagain.space/api/v1/health
   curl https://<subdomain>.hearagain.space/api/v1/categories
   ```

> 子域名已经定了(`carshop.hearagain.space`),不要再问用户。

### 1.2 配置 `BASE_URL`

服务端的 `settings.BASE_URL` 改为 `https://<subdomain>.hearagain.space`,这样图片 URL 等返回的是公网完整 URL。

### 1.3 跑通真集成场景(Admin Web 连真后端)

把 `carshop-admin/` 改为连真后端(改 vite proxy 或环境变量 `VITE_API_BASE_URL`),在浏览器跑完整流程:

| 场景 | 步骤 |
|---|---|
| 登录 | admin / admin123 → 成功跳到 /products |
| 分类 | 新建一个 "测试分类" → 改名 → 删除(预期成功) |
| 分类冲突 | 删除"汽车用品"(seed,下面有商品) → 应该失败,显示提示 |
| 图片上传 | 在新建商品页传一张本地 png → 拿到完整 URL → 浏览器直接打开能看到图 |
| 商品 | 新建一个完整商品(实物类型) → 上架 → 列表能看到 → 下架 → 列表筛"全部"能看到、筛"已上架"看不到 → 删除 |
| Banner | 新建 banner(link_type=product,指向刚创建的商品) → 服务端校验通过 |
| Banner 失败 | 新建 banner(link_type=product,指向 99999) → 失败,提示"商品不存在" |
| 订单查看 | 用 `curl -X POST https://<sub>.hearagain.space/api/v1/orders -H "X-Device-Id: test-001" -H "Content-Type: application/json" -d '{"product_id":1,"quantity":1}'` 造一笔订单 → 后台 /orders 能看到 |
| 模拟支付 | `curl -X POST /api/v1/orders/<id>/mock_pay -H "X-Device-Id: test-001"` → 后台 status 变 paid |
| 退出 | 登出 → 跳回 /login;直接访问 /products 跳 /login |

### 1.4 **找出并修复契约漂移**

每跑一个场景,逐项对照 `artifacts/fixtures/`,如果发现:
- 字段名实际跟 fixture 不一致 →
- 嵌套结构不一致 →
- 错误码触发点不一致 →

**必须**:
1. 在 `artifacts/07-early-integration.md` 记录漂移详情
2. 决定**修哪边**:改 SPEC.md + 改服务端 / 还是改 admin-web / 还是改 fixture
3. 修完重新跑一遍验证
4. 更新对应 fixtures(让后续车机 session 用对的)

### 1.5 后台部署(可选 Demo)

把 admin-web build 后部署到 Mac mini(子域名比如 `carshop-admin.hearagain.space`),用 add_static_site.sh 脚本。**如果用户觉得后台本地跑就够了**,可以跳过这步,在 artifact 里记原因。

---

## 2. 你不要做什么

- ❌ 改车机端代码(还没做)
- ❌ 把发现的 SPEC 漂移**只改一边**(只改服务端不改 fixture)
- ❌ 跳过任何验收场景就声称完成
- ❌ 自己定子域名(必须问用户)
- ❌ 跑生产数据库迁移(SQLite 直接初始化即可)
- ❌ 调整业务范围 / 加新功能(本 session 只做集成,不写新功能)

---

## 3. 输入

- `CLAUDE.md`、`SPEC.md`
- `carshop-server/`、`carshop-admin/`(波 1~3 的产物)
- `artifacts/01~06.md` 和 `artifacts/fixtures/`
- 用户的 CLAUDE.md 里 Mac mini 部署架构、cloudflared、launchd 那部分

---

## 4. 输出

- **公网可访问**:
  - `https://<subdomain>.hearagain.space/api/v1/health` 返回 OK
  - 所有公开接口可访问
  - admin-web(本地或部署)可登录、可操作
- **修过的 SPEC / fixtures**:契约漂移修完
- **新 launchd plist**:`com.user.carshop-server.plist`
- **新 cloudflared config 段**:加到 `~/.cloudflared/config.yml`
- **更新 STATUS.md**:把决策(子域名、端口、漂移修复)写进决策记录
- `artifacts/07-early-integration.md`:
  - 子域名 / 端口 / 部署清单
  - 跑过的场景清单(逐项打勾)
  - **契约漂移清单**(每一条:发现的问题、怎么修、修在哪边)
  - 已踩坑

---

## 5. 验收标准

1. ✅ 公网 curl `https://<sub>.hearagain.space/api/v1/health` 返回 OK
2. ✅ 1.3 表里 9 个场景全部跑过,每个都符合预期
3. ✅ 所有契约漂移已修复并验证(漂移清单 → 修复 → 重跑场景 → 通过)
4. ✅ `artifacts/fixtures/02~05` 已更新到跟真服务端一致
5. ✅ `artifacts/07-early-integration.md` 完成
6. ✅ `ssh macmini 'launchctl list | grep carshop'` 显示服务在跑
7. ✅ Mac mini 重启后服务能自动起(可以 `launchctl unload/load` 验证)

---

## 6. 依赖

- **上游**:01、02、03、04、05、06(全要完成)
- **下游**:08 android-foundation(用本 session 修过的契约)、09、10、11

## 7. Mock 策略

不需要,这就是要把 mock 切到真后端。

## 8. 已知坑

1. **子域名是用户决策**:CLAUDE.md 明确"新选子域名永远问用户"
2. **端口冲突**:从 18767 往后找空闲(panqian 18765,aishare 18766)
3. **cloudflared 重启**:`launchctl kickstart -k gui/$(id -u)/com.cloudflare.cloudflared` 或 `ssh macmini 'tail -20 ~/.cloudflared/cloudflared.err.log'` 排查
4. **launchd 跑 python 的坑**:用 Homebrew python(`/opt/homebrew/bin/python3.12`),不要用系统 python(无 FDA 权限)
5. **`com.apple.provenance` xattr**:如果 launchd python 读不了文件,`xattr -c -r ~/carshop-server`
6. **CORS**:确保 admin-web 跨域能带 cookie(`allow_credentials=True`,`SameSite=None; Secure` 经 HTTPS)
7. **vite proxy vs 真域名**:开发期用 vite proxy 转发,真集成把 `VITE_API_BASE_URL=https://<sub>.hearagain.space` 跑一遍
8. **不要把生产 DB 删了**:`scripts/init_db.py` 应当判断 DB 已存在就跳过,或者重命名为 `init_db.py` + `reset_db.py`
9. **图片在 Mac mini 上的路径**:`/Users/mac/carshop-server/static/uploads/`,确保 launchd 启动的 uvicorn 有写权限
10. **HTTPS 图片**:cloudflared 自动 HTTPS,但 BASE_URL 要写 `https://`,不要写 `http://`
