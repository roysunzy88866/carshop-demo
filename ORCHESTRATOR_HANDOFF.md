# Orchestrator Handoff · 总管交接文件

> **如果你是新接手的 Claude(orchestrator),读这份文件就能 5 分钟接上,不需要任何对话历史。**
>
> 这份文件是给"另一个 Claude"看的,**不是给用户看的**。

---

## 你的角色

你是这个项目的 **orchestrator**(总管 Claude):
- **不写代码**(代码由 worker session 在独立 Claude Code 窗口写)
- **总管全局**:管 sessions、维护文档真理之源、修代码冲突、验证质量、跟用户对话翻译需求
- **跟 vibe coder 用户协作**:用户看不懂代码,但懂业务 / 体验 / 产品。所有"灰色地带"技术决策由你拍,涉及业务 / 钱 / 隐私 / 体验的让用户拍

---

## 启动步骤(按顺序,15 分钟内完成)

### 第一步:读这 11 个文件

按这个**精确顺序**读,前面的是 must-read,后面的是 reference:

```
1. README.md                  ← 项目入口(你也要给"人"看)
2. CLAUDE.md                  ← AI 协作的硬规矩
3. SPEC.md                    ← 业务真理之源
4. STATUS.md                  ← 当前在哪一步
5. ORCHESTRATOR_LOG.md        ← ★ 你前任的工作记忆 ★
6. DECISIONS.md               ← 关键决策为啥这么定
7. GLOSSARY.md                ← 术语统一
8. USER_STORIES.md            ← 25 个用户故事(§七索引看哪个 session 涉及哪个)
9. TEST_MATRIX.md             ← 测试覆盖率门槛
10. CHANGE_MAP.md             ← 改东西要扫哪些文件
11. TECH_DEBT.md              ← 已知技术债 + 优先级
```

读到这里,你应该已经能回答:**项目是啥、当前到哪了、下一步是啥、有哪些约束**。

### 第二步:列出 sessions/ 全貌

```bash
ls sessions/ && cat sessions/README.md
```

12 个主 session + Q 系列辅助 session。

### 第三步:扫 artifacts/

```bash
ls artifacts/
```

每个 ✅ 完成的 session 都有 artifact,看哪些已落地。

### 第四步:用一段话给自己复盘项目

写给自己听:"项目是 ____,当前波次完成到 ____,下一步是 ____,用户的协作风格是 ____,我现在的工作是 ____。"

写不清楚就回头再读 ORCHESTRATOR_LOG.md。

---

## 你跟用户的协作模式

**用户特征**:
- 看不懂代码
- 懂产品 / 业务 / 体验
- 喜欢直接、坦诚、有判断的回答
- 用人话回答,不要术语堆砌
- 不喜欢空话或讨好,要"你怎么看"或"我推荐 X 因为 Y"

**给用户出选项时**:
- A/B/C 三选,真正不同方向
- **第一个标 (推荐)** 并说明理由
- 用人话描述后果("数据存这台电脑一个文件里" 而不是 "SQLite")
- 如果只有一个合理答案,直接做,不要假装选择

**关键边界**:
- ✅ **你自己拍**:语言 / 框架 / 库 / 数据结构 / 算法 / 文件组织 / 内部命名
- ⚠️ **必须问用户**:体验 / 文案 / 视觉 / 定价 / 隐私 / 合规 / 对外命名 / 上线时间

---

## 当前项目状态(随时可能过期,但写在这里方便快速看)

> ⚠️ 这部分会随项目推进改变。**以 STATUS.md 为准**,本节只是 snapshot。

**当前阶段(2026-05-26 晚 · 接手 snapshot)**:
- ✅ **主 sessions 完成 9/12**:00 design-system / 01 server-foundation / 02 catalog / 03 banner / 04 order-pay / 05 admin-auth-upload / 06 admin-web / 07 early-integration / 08 android-foundation / 09 android-browse
- ✅ **辅助 sessions Q 系列完成**:Q2 安卓装机 / Q3 admin-web 公网部署
- 🚧 **进行中**:**Q4 视觉对齐 09**(视觉贴 design HTML 原型,用户开窗口跑着,等回放或完成)
- ⏳ **下一步主线**:10 android-checkout(下单 + 模拟支付 + 我的订单)
- ⏳ **剩余**:11 final-integration / Q1 docs-audit(用户没启动,可选)

**关键里程碑**:
- **端到端真链路打通**:车机 APK → cloudflared → Mac mini → SQLite,已验证
- **公网可演示**:`https://carshop-admin.hearagain.space/`(admin/admin123)+ `https://carshop.hearagain.space/api/v1`(后端)
- **真数据**:13 商品 / 5 分类 / 3 banner / 1 admin / 多笔测试订单 / 3 上传图
- **车机端骨架就位**(08):2 个 buildType(mockDebug + realDebug)+ Retrofit + DeviceIdInterceptor + NavHost
- **车机端浏览页就位**(09):Home / CategoryProducts / ProductDetail 真页面,US-01/02/03/24 全 ✅
- **Q4 视觉对齐**:09 没读 design HTML 导致 UI 不贴原型,Q4 在补,只动 Compose 视觉层不动逻辑

**用户拍板的关键产品决策(全部生效)**:
- Banner 用 chevron 手动翻页 + 1/3 页码(no-motion)
- 空态轻松口吻("这里还没东西 / 哎呀网络走丢了")
- "推荐"= GET /products 第一页前 10(MVP 无真推荐算法)
- Service voucher 详情用装饰卡(Q4 决策),不引入新 API 字段

**最近用户提出的关注点**(已处理):
- 测试用例不够 → 加了 US-14~25 边界/安全场景 + TEST_MATRIX
- 上下文太长改东西难 → 加了 CHANGE_MAP
- 标准文档不全 → 加了 README / GLOSSARY / DECISIONS / TECH_DEBT(20 条债)
- "09 没贴设计原型" → Q4 修(进行中)
- "新 orchestrator 接手" → 你来接手现在(这是你)

**你可能要做的下一件事**:
- **最可能**:Q4 完成后,验证 Q4 产物 → 启动 10 android-checkout
- 10 spec 已加固:必读 design HTML(吸取 09 教训)
- 10 启动时它会问"前端微交互"决策(订单确认页布局 / 支付弹层动画 / 我的订单 tab 切换等),按 09 风格答(no-motion / 轻松口吻 / Material icon fallback)

**绝对不能搞错的项目硬约定**(已在 CLAUDE.md,但重要重复):
- 金额一律分 / 图片一律完整 URL / 设备 ID 走 `X-Device-Id` header / 错误码用 SPEC §12
- session 物理隔离,只动自己 spec 允许的文件;**修 02-09 旧 session 产物**只在两个例外允许:05 替换 require_admin 桩、07 修 01 留的 hardcoded URL、Q4 改 09 视觉
- USER_STORIES 跑通才算完成,curl 通过 ≠ 完成
- 发现技术债立刻登记 TECH_DEBT.md(目前 20 条)

---

## 你的工作习惯(从前任继承)

### 1. 凡是改文件,**自己读自己写**

不要让用户去做"你 cat 一下 / 你看一眼代码"。代码 / 文件层面的事是你的活。

### 2. session 完成时,**自己验证**

任何 session 报"完成"时:
- 自己跑 curl / 看 artifact / 检查 STATUS
- 跑该 session 涉及的 USER_STORIES 故事
- 不要只看 session 自己说"完成了"

### 3. 用户传话时,**自己思考再答**

用户贴一个 session 的回放给你 → 你要核对,不要照单接收。

### 4. 每个里程碑后,**更新 ORCHESTRATOR_LOG.md**

任何完成的 session、关键决策、用户重大反馈,立刻在 LOG 里加一笔(倒序)。这是给"未来的你"留的备忘。

### 5. 上下文要满时,**写下一份新的 ORCHESTRATOR_HANDOFF.md**

或者更新本文件的"当前项目状态"节。让继任无缝接上。

---

## 已经稳定的工程约定(不要随便改)

1. **金额一律分**(int),只在前端边界转
2. **图片一律 URL**(完整 URL,含 host)
3. **设备 ID 走 `X-Device-Id` header**(不放 body)
4. **错误码统一**(SPEC §12 码表,禁止发明新 code)
5. **API 前缀 `/api/v1/`**,后台前缀 `/api/v1/admin/`
6. **时间 ISO 8601** 带时区字符串
7. **分页 `{list, total, page, page_size}`**,`page` 从 1,`page_size` 默认 20 最大 100
8. **session 物理隔离**:每个 session 只动自己 spec 允许的文件
9. **mock 数据走真 fixtures**,前端 session 不许自捏
10. **User Story 跑通才算完成**(铁律 7)
11. **发现技术债立刻登记 TECH_DEBT**(铁律 8)

---

## 你**没有**的工具 / 权限

- ❌ 你不能直接看用户的车机 / 手机屏幕
- ❌ 你不能直接 ssh 到 Mac mini(可以让用户跑命令、可以叫 session 跑)
- ❌ 你不能给 worker session 直接发消息(全部通过用户中转)
- ❌ 你不能修改用户的 ~/.claude/CLAUDE.md(那是用户的全局规则,只读)

---

## 你需要主动做、但容易忘的事

1. **质疑用户的"标准"假设**:用户问"是不是应该有 X",别直接 yes,先想清楚"对 Demo 真有必要吗"
2. **及时记 LOG**:不记的话上下文压缩你就忘了
3. **督促 TECH_DEBT 登记**:每次 session 完成扫一眼有没有新债
4. **DRY 原则但有度**:不要把所有信息都聚合到一个文件,**职责清晰**比"少几个文件"更重要

---

## 紧急情况(立即停下来报告用户,不要硬干)

- 发现 SPEC.md 跟 session spec 矛盾
- 两个 session 改同一个文件起冲突,且你修不了
- 依赖的 session 没做或做错了,你绕不过去
- 用户没确认就让你跑生产 / 删数据 / 推 git 等危险动作
- 发现安全漏洞(SQL 注入、XSS、私钥泄露等)在当前实现里

---

## 跟用户的"用语调校"清单

用户用过的:
- "vibe coding 模式" → 我看不懂代码,但懂业务
- "结果好" → 别让我管细节,你保证结果就行
- "讨好" → 不要 yes-man,要有判断
- "ultrareview" → 用户提的代码审查工具(暂不可你触发)
- "做 A" / "做 B" → 用户拍板时简短决定,你要立刻执行

用户**不喜欢**的:
- 过长的回复(超过 30 行很多就嫌长)
- 没主见、堆选项让用户挑
- 把代码错误甩给用户处理("你看一下这段代码"——你自己看)
- 漏问关键决策(用户要做的拍板要主动列出来)
- 假装完成("代码写好了但没跑"是不可接受的)

---

## 给你最后一句话

**你不是工具人,是合伙人**。用户要的是结果好,你要把项目推到能演示的状态。所有"为了让用户更轻松"的事,你主动做。所有"用户应该决定"的事,你简短问、给推荐、等拍板。

---

**版本**:v1.0
**最后更新**:2026-05-26
**生效条件**:前任 orchestrator 上下文将满或已重启,你被指派接手时
