# Decisions · 架构决策记录(ADR)

> 用 [Michael Nygard ADR 格式](https://github.com/joelparkerhenderson/architecture-decision-record)的简化版:每个决策记录"为啥这么定 + 当时考虑了什么 + 什么时候反悔"。
>
> 跟 [STATUS.md](STATUS.md) 决策表的区别:STATUS 是流水账,本文件是**重要决策的完整背景**,可读性优先。
>
> **维护规则**:不可删,只能加(打 ⚠️ 弃用 / 重新决策)。新决策接着编号。

---

## ADR-001 · 服务端选 FastAPI + SQLite

**日期**:2026-05-25
**状态**:✅ 生效

**问题**:服务端要选一套技术栈,要轻、Demo 友好、能跑 Mac mini。

**决策**:Python 3.11+ + FastAPI + SQLite + SQLAlchemy。

**理由**:
- Demo 数据量小,SQLite 单文件 DB 够,免运维
- FastAPI 自带 OpenAPI,fixtures 导出方便
- Mac mini 上已有 Python 生态,沿用部署架构

**反悔条件**:
- 数据量 > 100 万行 / 并发 > 100 QPS → 迁 Postgres
- 多端实时同步 → 加 WebSocket 或 SSE

**关联**:[TD-007 订单号并发碰撞](TECH_DEBT.md)

---

## ADR-002 · 砍掉用户登录(无账号体系)

**日期**:2026-05-25
**状态**:✅ 生效(Demo)

**问题**:车机上登录极不友好(键盘难输手机号 / 二维码登录又增加复杂度)。

**决策**:Demo 阶段**完全无用户账号体系**,用车机的 `Settings.Secure.ANDROID_ID` 作 device_id,**通过 `X-Device-Id` header 传给服务端关联订单**。

**理由**:
- Demo 演示最重要,登录步骤拖累体验
- 设备 ID 在同一车机上稳定,App 重启订单仍可见(US-07.2)

**取舍**:
- 跨设备订单不通(预期内,Demo 不解决)
- 换车机后历史订单丢失

**反悔条件**:
- 真上线时要做手机号扫码登录(M-Account 模块)

**关联**:[SPEC §11.5](SPEC.md)、[GLOSSARY device_id](GLOSSARY.md)

---

## ADR-003 · 砍掉商品搜索

**日期**:2026-05-25
**状态**:✅ 生效(Demo)

**问题**:车机端搜索框输入慢、车主行驶中不该用,价值低。

**决策**:**不做**搜索框 / 模糊匹配 / 语音搜索。靠分类导航 + 推荐位 + Banner 引导浏览。

**反悔条件**:
- 商品超 500 个,导航效率拖低
- 用户反馈"找不到东西"

---

## ADR-004 · 收货地址 Demo 阶段写死

**日期**:2026-05-25
**状态**:✅ 生效(Demo · 待 M-Address 模块)

**问题**:车机上输入收货地址极不友好,但又是订单必备字段。

**决策**:订单 `shipping_info` 字段写死为 SPEC §5.1 默认值:
```json
{ "name": "车主", "phone": "138****0000", "address": "上海市浦东新区世纪大道 100 号" }
```

**取舍**:
- 用户实际收不到货(Demo 不发货,接受)
- 真实场景下绝对不能这样

**反悔条件**:
- 一旦开始真发货 → 立刻做 M-Address 模块

**关联**:[TD-008](TECH_DEBT.md)、[SPEC §5.1](SPEC.md)

---

## ADR-005 · 设计系统采用品牌方向 C(海泡青电车感)

**日期**:2026-05-25
**状态**:✅ 生效

**问题**:设计稿给了 3 个品牌方向(A 复古 / B 极简 / C 海泡青电车感),要选一个落地。

**决策**:**C · 海泡青电车感**:深石墨蓝 `#1A2027` + 海泡青 `#00C2A8`,Light only,**no motion**(车机里减少动效干扰驾驶)。

**理由**:
- 跟车主"出行"心智匹配
- "no motion"减少驾驶分心
- 主色对比度高,车机阳光下可读

**关联**:[design/安卓商城/tokens.json](design/安卓商城/tokens.json)、[TD-002 思源黑体未打包](TECH_DEBT.md)

---

## ADR-006 · 后台跟车机端不共享代码

**日期**:2026-05-25
**状态**:✅ 生效

**问题**:后台(TypeScript)和车机端(Kotlin)有重复的数据结构定义,要不要做共享层?

**决策**:**不共享**。各自从 [tokens.json](design/安卓商城/tokens.json) 和 [SPEC.md](SPEC.md) 翻译,各自维护自己语言的类型。

**理由**:
- 跨技术栈共享会拖累各自演进
- Demo 量小,重复工作小
- 共享层是个"二次真理之源",违反 SPEC.md 唯一性原则

**取舍**:改 schema 要改两边(可接受)。

---

## ADR-007 · 服务端公网子域名 `carshop.hearagain.space`

**日期**:2026-05-25
**状态**:✅ 生效

**问题**:服务端要部署到 Mac mini,接 cloudflared tunnel,需要一个公网子域名。

**决策**:**`carshop.hearagain.space`**(用户拍板)。

**理由**:
- 直白好记
- 跟用户其他项目隔离(`panqian.` / `aishare.`)
- 沿用现有 `panqian-tunnel`,不新建隧道

**关联**:[sessions/07-early-integration.md](sessions/07-early-integration.md)

---

## ADR-008 · 服务端 Python ≥ 3.11(实际 3.12)

**日期**:2026-05-25
**状态**:✅ 生效

**问题**:SPEC 原写 3.11,但本机只有 3.12。

**决策**:写"≥ 3.11(实际 3.12)",允许向上兼容。

**理由**:
- Python 3.12 跟 3.11 完全兼容
- 锁版本对 Demo 没必要

---

## ADR-009 · Demo 不接真实支付(扫码 UI + 假成功)

**日期**:2026-05-25
**状态**:✅ 生效

**问题**:Demo 要不要接微信 / 支付宝?

**决策**:**不接**。车机显示一个二维码弹层(内容可以是 `carshop://order/{id}` 假协议),用户点击弹层任意位置即触发 `/orders/:id/mock_pay`,1 秒后状态推到 paid。

**理由**:
- 真支付要 ICP / 备案 / 营业执照,Demo 不值得
- 视觉上的"扫码 → 支付成功"流程已足够演示
- 后续真上线要接,留 mock_pay 接口作切换点(改实现即可)

**关联**:[SPEC §6.1](SPEC.md) `POST /orders/:id/mock_pay`

---

## ADR-010 · 引入 USER_STORIES 作"完成"硬门槛

**日期**:2026-05-25(用户提出)
**状态**:✅ 生效(从 06 起执行)

**问题**:之前 session 完成的标准是"curl 通过 + artifact 写完",但**curl 通过 ≠ 业务完整**。

**决策**:加 [USER_STORIES.md](USER_STORIES.md),每个 session 必须跑通它涉及的 stories,**逐条留 Then 段验证证据**。CLAUDE.md 铁律 7 强制。

**理由**:
- 防"假测试"(curl 200 但实际数据不对)
- 防业务流程断裂(只测接口不测端到端)
- BDD 是 20 年成熟实践,AI 时代复兴

**触发**:用户问"测试是真测试吗"

---

## ADR-011 · 引入 CHANGE_MAP / TEST_MATRIX / TECH_DEBT(质量基线升级)

**日期**:2026-05-26
**状态**:✅ 生效

**问题**:
1. 项目变大后改东西要全 grep
2. 测试维度不够(happy path 为主)
3. 技术债务隐性,没有显式登记

**决策**:加三份文档:
- [CHANGE_MAP.md](CHANGE_MAP.md):实体 → 引用文件地图
- [TEST_MATRIX.md](TEST_MATRIX.md):150 格覆盖矩阵
- [TECH_DEBT.md](TECH_DEBT.md):13 条已知债务

**CLAUDE.md** 启动协议扩到 10 步,加铁律 8(技术债登记)。

**理由**:
- 项目过 6 个 session 时,启动协议读 4 个文件已经接近上下文上限
- 用户主动提出对应风险("我如果想改一个东西的话")
- 项目规模可预见会扩到 20+ sessions(M-Address / M-Cart / M-Coupon...)

---

## ADR-012 · 补三个基础规范:README + GLOSSARY + DECISIONS

**日期**:2026-05-26
**状态**:✅ 生效

**问题**:用户问"行业标准 AI 项目应该有哪些文档,你为啥不主动提"。Orchestrator 反思:确实漏了"基础三件套"。

**决策**:补:
- [README.md](README.md):项目入口(给人类)
- [GLOSSARY.md](GLOSSARY.md):术语统一
- [DECISIONS.md](DECISIONS.md):本文件,ADR 格式

**理由**:
- README 让新人 / 6 个月后的自己能进入项目
- GLOSSARY 防"device_id / 设备 ID / X-Device-Id" 这种多种叫法
- DECISIONS 把 STATUS 里散落的决策提炼成可读的 ADR

**Orchestrator 自我反思**:
- 倾向于"响应"而不是"引导",这是 AI 助手的局限
- 未来接新项目时,**第一件事就摆出标准文档菜单让用户拍板**,不再凭自己挑

---

## ADR 模板(下次新增决策用这个)

```markdown
## ADR-XXX · 一句话标题

**日期**:YYYY-MM-DD
**状态**:✅ 生效 / ⚠️ 弃用(被 ADR-YYY 取代)

**问题**:[当时面对的问题 / 选型困境]

**决策**:[选了什么,具体落到哪个文件 / 哪段代码]

**理由**:
- [为什么这样做]
- [拒绝的其他选项]

**取舍 / 已知代价**:
- [接受了什么风险或缺陷]

**反悔条件**:
- [什么时候应该回来重新决策]

**关联**:[相关 SPEC / TD / 其他 ADR]
```

---

**版本**:v1.0
**最后更新**:2026-05-26
