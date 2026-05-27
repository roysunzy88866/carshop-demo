# Session Q1 · 文档规范审计 + 补全(辅助 session)

> **Q 系列 = Quality / 辅助 session**,不在主 12 个 session 序列里。本 session 跟业务无关,只跑文档审计 + 补全。

---

## 1. 你要做什么

审视项目当前的文档规范,对照"AI 协作开发项目标准文档清单"补齐**有真实价值的**,跳过**为 Demo 阶段过度工程的**。

### 1.1 标准清单 vs 当前状态(你来 audit)

**🔴 第一档(几乎所有项目都该有)**

| 文件 | 当前有吗 | 你的判断 |
|---|---|---|
| SPEC.md | ✅ | - |
| CLAUDE.md | ✅ | - |
| README.md | ✅(刚补) | - |
| STATUS.md | ✅ | - |
| USER_STORIES.md | ✅ | - |
| TECH_DEBT.md | ✅ | - |
| **CHANGELOG.md** | ❌ | □ 加 / □ 跳过(理由记 DECISIONS)|

**🟡 第二档(项目稍大就需要)**

| 文件 | 当前有吗 | 你的判断 |
|---|---|---|
| CHANGE_MAP.md | ✅ | - |
| TEST_MATRIX.md | ✅ | - |
| GLOSSARY.md | ✅(刚补) | - |
| DECISIONS.md(ADR) | ✅(刚补) | - |
| ORCHESTRATOR_LOG.md | ✅ | - |
| **CONTRIBUTING.md** | ❌ | □ 加 / □ 跳过 |

**⚪ 第三档(特定场景需要)**

| 文件 | 当前有吗 | 你的判断 |
|---|---|---|
| **ARCHITECTURE.md** | ❌ | □ 加 / □ 跳过(SPEC 已含?)|
| **API.md** | ❌ | □ 加(从 FastAPI OpenAPI 导出?)/ □ 跳过 |
| **RUNBOOK.md** | ❌ | □ 加(部署 / 运维指南)/ □ 等 07 一起做 |
| **SECURITY.md** | ❌ | □ 加(责任声明 + 上报渠道)/ □ 跳过 |
| **POSTMORTEMS/** dir | ❌ | □ 建目录 / □ 真出事再说 |
| **MIGRATION.md** | ❌ | □ 加(DB schema 变更流程)/ □ 跳过 |

### 1.2 你的输出

对每个 ❌ 的项目,做一次决策:
- **加**:写出来,内容**精炼**不灌水(Demo 阶段优先短小)
- **跳过**:在 DECISIONS.md 新建一个 ADR,**写清楚为啥跳过 + 什么时候反悔再加**

---

## 2. 你不要做什么

- ❌ **不要**为了凑指标硬加文档(空 / 占位 / 复述其他文件的 → 不如不加)
- ❌ **不要**改业务代码(本 session 不动 `carshop-server/`、`carshop-admin/`、`carshop-android/`)
- ❌ **不要**改 SPEC / USER_STORIES / TEST_MATRIX(它们是别 session 的责任)
- ❌ **不要**在 CHANGELOG 里编造历史(真上版本时再写)
- ❌ **不要**给 ARCHITECTURE.md 画 Mermaid 图除非真有信息量(SPEC.md 已经有了文字版)

---

## 3. 决策建议(我作为 orchestrator 给你的提议,你可以反对)

| 项 | 我倾向 | 理由 |
|---|---|---|
| CHANGELOG.md | **加,但留空版本结构** | MVP 还没发版,只准备好骨架(`## [Unreleased]` + 模板),11 final-integration 时填 v0.1.0 |
| CONTRIBUTING.md | **跳过**(写 ADR) | Demo 阶段单人项目,无意义。任何人加入再补 |
| ARCHITECTURE.md | **加,但只画 ASCII** | 三层:车机端 → 服务端 → 后台,带数据流。Mermaid 反而更难维护,文字图够用 |
| API.md | **跳过**(写 ADR) | SPEC §6 已经够详细,FastAPI 自带 `/docs` 实时 OpenAPI,重复劳动 |
| RUNBOOK.md | **延后到 07 一起做** | 07 早集成有部署步骤,合并进 RUNBOOK 更自然 |
| SECURITY.md | **加,极简版** | 列已知风险 + 上报渠道。TD-003 / 006 / 015 引用到这里 |
| POSTMORTEMS/ | **建空目录 + README** | 0 成本,出事就有地方放 |
| MIGRATION.md | **跳过**(写 ADR) | SQLite Demo,init_db 重跑即可,不需要 migration 流程 |

---

## 4. 输入

- `CLAUDE.md`、`SPEC.md`、`STATUS.md`
- `README.md`、`GLOSSARY.md`、`DECISIONS.md`(刚加的三个,可参考风格)
- `TECH_DEBT.md`(SECURITY 引用)
- 你完全可以**反对我的决策建议**,但要在 artifact 里写清"为啥反对"

---

## 5. 输出 / 交付物

- 加的每个文件:精简、可读、能立刻派上用场
- 跳过的每个项目:在 DECISIONS.md 加一条 ADR(ADR-013 起编号)
- `artifacts/Q1-docs-standards.md`:
  - 审计结论清单(每项加 / 跳)
  - 加的文件清单 + 简要内容
  - 跳的 ADR 编号引用

---

## 6. 验收标准

1. ✅ 上面 1.1 三档清单,每个 ❌ 都有明确决策(加 / 跳)
2. ✅ 跳的有 DECISIONS 条目,内容含"什么时候反悔"
3. ✅ 加的不灌水,每个文件**至少 30% 是实际信息密度**(对照 README / GLOSSARY 的密度)
4. ✅ artifact 写完

**预计时长**:1.5~2 小时

---

## 7. 依赖

- **上游**:无(独立审计 session)
- **下游**:07 早集成(若加 RUNBOOK 会用到)、11 final-integration(若加 CHANGELOG 会填首版)

---

## 8. 已知坑 / 注意

1. **不要陷入"文档完备主义"**:看似"为了规范"加了 10 个文件,实际增加维护负担。**有真实读者的才加**
2. **Demo 阶段的文档优先级**:运营手册 > 部署文档 > 架构图 > 贡献指南
3. **ADR 写得好的标准**:6 个月后读它的人能理解"为啥这么决定 + 现在还对不对"
