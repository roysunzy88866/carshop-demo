# 车机商店 Demo · 项目入口

> 给**人类**看的项目说明。AI Claude 进项目要读 `CLAUDE.md`,不是这份。

---

## 这是什么

装在安卓车机上的车主用品商店 **Demo**:
- 用户在车机大屏上浏览商品(加油卡 / 洗车卡 / 保养 / 车装 / 应急用品)
- 选中 → 提交订单 → 扫码"模拟支付"完成购买
- 运营在 Web 后台维护商品 / 分类 / Banner / 看订单
- **Demo 阶段**:不接真实支付、不接真实发货、不接真实账号体系

详细需求看 [SPEC.md](SPEC.md)。

---

## 当前状态(v0.1 MVP 已交付)

### 🎬 在线试用(浏览器直接跑 APK)

👉 **[https://appetize.io/app/b_ygi7ew6n3ugxh2ulyjnpojjlly](https://appetize.io/app/b_ygi7ew6n3ugxh2ulyjnpojjlly)**

Appetize.io 在浏览器里启动一台云端安卓模拟器,把车机端 APK 跑起来,不用装任何东西。点开就能看 Demo 走完整下单流程。

### 公网入口

| 资源 | URL | 备注 |
|---|---|---|
| 📱 **在线 Demo** | https://appetize.io/app/b_ygi7ew6n3ugxh2ulyjnpojjlly | 浏览器即开即玩 |
| API | https://carshop.hearagain.space/api/v1 | FastAPI + SQLite · Mac mini |
| 后台 | https://carshop-admin.hearagain.space | React + AntD · `admin / admin123` · 从 `/` 进 |
| APK 下载 | https://carshop.hearagain.space/download/ | v0.1.0 · 12 MB |

- **进度**:12 / 12 主 session + Q2/Q3/Q4/Q5 全完(详见 [STATUS.md](STATUS.md))
- **后端**:本地 `localhost:8000` / 公网 `carshop.hearagain.space`(07 部署)
- **后台**:本地 `localhost:5173` / 公网 `carshop-admin.hearagain.space`(Q3 部署)
- **车机端**:Android Compose · Material 3 · 1920×1080 横屏 · 全套业务页(08-10)

---

## 快速上手(5 分钟跑起来)

### 起后端

```bash
cd carshop-server
source .venv/bin/activate          # 第一次:python3.12 -m venv .venv && pip install -r requirements.txt
python scripts/init_db.py          # 第一次:初始化 DB + seed
uvicorn app.main:app --reload --port 8000
```

验证:`curl http://localhost:8000/api/v1/health` → `{"code":0,"data":{"status":"ok"},"message":"ok"}`

### 起后台 Web

```bash
cd carshop-admin
npm install                        # 第一次
npm run dev:real                   # 连真后端
# 或 npm run dev:mock              # MSW mock,不依赖后端
```

打开 `http://localhost:5173/`,用 `admin / admin123` 登录。

**或者直接用公网部署**:浏览器进 `https://carshop-admin.hearagain.space/`(Q3 已部署),不用本地起 dev。**注意:必须从根路径 `/` 进,不要直访 `/login` / `/products` 等子路径(SPA fallback 未做,TD-020 待还)。**

### 跑车机端

待 08-10 session 完成。届时 `cd carshop-android && ./gradlew assembleDebug` 装 APK。

---

## 项目文件指南(每个 md 是干什么的)

### 🔴 必读(进项目第一周)

| 文件 | 看完能知道 |
|---|---|
| [README.md](README.md) | 这份文件,项目入口 |
| [SPEC.md](SPEC.md) | 做什么、数据模型、API 契约 · **真理之源** |
| [STATUS.md](STATUS.md) | 当前在哪一步、下一步是啥 |
| [CLAUDE.md](CLAUDE.md) | AI 协作的硬规矩(给 Claude 看,但人也该懂规则)|

### 🟡 改东西前看

| 文件 | 看完能知道 |
|---|---|
| [CHANGE_MAP.md](CHANGE_MAP.md) | 改一个字段 / 接口 / 错误码,要扫哪些文件 |
| [USER_STORIES.md](USER_STORIES.md) | 25 个用户故事 + Given/When/Then 验收场景 |
| [TEST_MATRIX.md](TEST_MATRIX.md) | 测试覆盖率矩阵 + 缺哪些场景 |
| [GLOSSARY.md](GLOSSARY.md) | 术语统一,防"同一概念多种叫法" |
| [DECISIONS.md](DECISIONS.md) | 关键架构决策 + 为啥这么定 |
| [TECH_DEBT.md](TECH_DEBT.md) | 已知技术债 + 还债条件 |

### ⚫ Orchestrator 专用

| 文件 | 用途 |
|---|---|
| [ORCHESTRATOR_LOG.md](ORCHESTRATOR_LOG.md) | Orchestrator 自己的工作记忆(防上下文丢)|
| [ORCHESTRATOR_HANDOFF.md](ORCHESTRATOR_HANDOFF.md) | 上下文满了,**新 orchestrator 接手时读这个** |

### Session 专用

| 目录 | 内容 |
|---|---|
| [sessions/](sessions/) | 12 个 session 的独立 spec,每个 Claude Code session 启动时读自己那份 |
| [artifacts/](artifacts/) | session 完成后留下的产物(实际接口、fixtures、截图)|

---

## 怎么协作 · Vibe Coding 模式

**用户**(项目所有者):
- 看不懂代码,但懂产品 / 业务 / 体验
- 做决策:体验、文案、视觉、定价、合规、上线时间、命名
- 不操心:代码、技术冲突、文件管理

**Orchestrator Claude**:
- 总管所有 sessions,翻译需求 → session spec
- 验证质量、修代码冲突、维护文档真理之源
- 决策"灰色地带"技术问题(语言 / 框架 / 库)

**Worker Sessions**(Claude Code):
- 每个独立窗口跑一个 session,严格按 spec 执行
- 完成后留 artifact + 更新 STATUS + 加 tech debt
- 不擅自跨界

---

## 协作铁律(细则在 CLAUDE.md)

1. SPEC.md 是真理之源,改契约必须先改 SPEC
2. 各 session 物理隔离,只动自己 spec 允许的文件
3. 必须留 artifact + 更新 STATUS
4. Mock 必须取自后端 fixtures,不许自捏
5. 金额一律分、图片一律 URL、设备 ID 走 header
6. User Story 跑通才算完成,curl 通过 ≠ 完成
7. 发现技术债立刻登记 TECH_DEBT.md

---

## 想加新模块怎么办

参考 [CHANGE_MAP.md §五"常见改动场景速查"](CHANGE_MAP.md#五常见改动场景速查),里面给了"加新字段 / 新错误码 / 新页面"三种标准流程。

简版:
1. 改 SPEC.md(契约层)
2. 新 session spec 在 `sessions/XX-name.md`
3. 加 STATUS + 加 USER_STORIES + 加 CHANGE_MAP 条目
4. 开 Claude Code 跑 session

---

## 联系 / 反馈

项目所有者:roy(roysunzy@gmail.com)
Mac mini 部署:`carshop.hearagain.space`(API,07 session)+ `carshop-admin.hearagain.space`(后台,Q3 session)

---

**版本**:v1.1
**最后更新**:2026-05-26(Q3 加公网 admin 地址)
