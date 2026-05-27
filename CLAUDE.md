# 车机商店项目 · Claude Code Session 协作规范

> **这个文件会被 Claude Code 在本目录下自动加载,你不读也得读。**
>
> 你(Claude Code)进入这个目录,意味着你要参与「车机商店 Demo」项目的某个 session。这份文件定义了你在这个项目里**必须遵守**的行为规范。

---

## 你必须知道的项目背景

这是一个**车机电商 Demo** 项目:
- 装在安卓车机上的店铺,卖加油卡/洗车/保养/车装/应急用品
- 三大组件:**车机端 Android App**、**Web 后台**、**FastAPI 服务端**
- Demo 级别:无登录、无真实支付、无真实发货,但流程视觉上跑通

详细需求看 `SPEC.md`。**这是项目唯一的真理之源。**

---

## 启动协议(每次 session 开始**必做**,不许跳)

```
必读(每个 session 都要读):
1. 读 SPEC.md             ← 项目全貌、数据模型、API 契约、共享约定
2. 读 STATUS.md            ← 当前进度
3. 读 USER_STORIES.md      ← 找到你 session 涉及的 stories(§七 索引表)
4. 读 TEST_MATRIX.md       ← 看你 session 覆盖的测试维度门槛(§五)
5. 读 CHANGE_MAP.md        ← **改任何东西前**查实体地图,不要全文 grep
6. 读 TECH_DEBT.md         ← 看相关债务,新踩的坑要立刻补一条
7. 读你的 session spec     ← sessions/XX-name.md
8. 读 artifacts/           ← 已完成 session 留下的产物

按需查阅(用到时再读,不强制):
- GLOSSARY.md              ← 术语不确定时查
- DECISIONS.md             ← 想知道"为啥这么定"时查
- README.md                ← 给人类看的,Claude 一般不读

完成后:
9. 跟用户对齐:
   - 一句话回放:你要做什么
   - 依赖是否就绪
   - 你的验收标准 + 打算跑通的 User Stories
   - 触及哪些 CHANGE_MAP 条目
   - 不确定的点
10. 等用户说「开始」,再动手
```

**任何 session 不执行这套协议直接动手,都属于违规。**

---

## 协作铁律

### 铁律 1:`SPEC.md` 是真理之源,不许偷偷改契约

- 数据模型(§5)、API 契约(§6)、错误码(§12)、URL 规范(§13)全在 SPEC.md
- 你的 session 里**不许**:
  - 发明新接口路径
  - 改字段名 / 字段类型
  - 自定义错误码
  - 偷偷加 / 删字段
- 发现 SPEC 写错 / 写漏 → **停下来**,告诉用户,先改 SPEC.md,再继续

### 铁律 2:各 session 物理隔离

- 你只能改自己 session spec 里**显式列明**的文件路径
- 不许跨 session 改别人的代码
- 例外:更新 SPEC.md / STATUS.md / 你自己的 artifact —— 这是允许的

### 铁律 3:必须留 artifact

session 结束前**必须**在 `artifacts/<your-session-id>.md` 留下产物文件,至少包括:
- 你实际实现了什么(对照 session spec 一项项打勾)
- 实际接口的 URL、参数、响应示例(如果是后端)
- 实际组件 API、props(如果是前端)
- 已踩过的坑、绕过的临时方案、需要后续优化的点

模板看 `artifacts/README.md`。

### 铁律 4:必须更新 STATUS.md

session 结束前把自己的状态推进:`⏳ 待开始` → `✅ 已完成`,写下交付日期和产物链接。

### 铁律 5:Mock 优先,联调最后

- 依赖未就绪 → 用 Mock 数据先跑通自己
- **不许等**,不许"先空着"
- 联调由专门的 `integration` session 兜底

### 铁律 6:小步快跑,边做边自检

- 别一口气写完 1000 行才跑
- 每个完整功能写完就跑一遍验证(后端跑 curl,前端真在浏览器/模拟器里点)
- 卡住 15 分钟内还没头绪 → 停下来,告诉用户,不要硬猜

### 铁律 7:User Story 是「完成」的硬门槛(curl 通过 ≠ 完成)

- 完成前必须跑通 `USER_STORIES.md §六` 索引表里你 session 对应的所有 stories
- **跑通**的标准是 Then 段的每一条都能观察到,不是"接口返 200"就算
- 跑不通 → session 没完成,**不许**在 STATUS 标 ✅
- 在 artifact 里**逐 story 列出验证证据**(命令输出 / 截图 / 屏幕录像)

### 铁律 8:发现技术债立刻登记到 TECH_DEBT.md

- 任何"为了赶进度做妥协"的代码、绕过、写死、占位、TODO,**立刻**在 `TECH_DEBT.md` 加一条
- 必须填:类别 / 描述 / 影响等级 / 为啥这么干 / 触发还债条件 / 工作量预估
- 不许只在 artifact 里写"已知偏离"就完事 —— 同步抄进 TECH_DEBT.md
- 漏一条,等于把暗坑留给未来

### 铁律 9:规范升级时 · 默认 Forward-only,不回溯旧 session

- 当 SPEC / USER_STORIES / TEST_MATRIX 等"标准"升级时,**已完成的 session 不重新跑**
- 影响新 session 即可(从规范升级日期之后启动的)
- 旧 session 因为新规范产生的差距,**列入 11 final-integration 的"必跑"清单**做兜底
- 必须做:在 DECISIONS.md 加一条 ADR + 在 ORCHESTRATOR_LOG 记一笔 + 把"待兜底"的 USER_STORIES 加进 11 spec
- **例外**:涉及安全 / 合规 / 数据完整性的升级,Orchestrator 评估是否需要回溯单 session

---

## 共享技术约定(违反任何一条 = session 没做完)

| # | 约定 | 后果 |
|---|---|---|
| 1 | **金额单位用「分」**:DB、API、内部传递全是 int(分)。只在前端显示时除 100 | 违反 → 价格全错 |
| 2 | **图片走 URL**,不走 base64:上传专走 `/api/v1/admin/upload` 拿 URL,其他字段存 URL | 违反 → 数据库爆炸 |
| 3 | **设备 ID 走 `X-Device-Id` header**:车机端注入,服务端从 header 取,不从 body | 违反 → 订单关联错乱 |
| 4 | **错误码用 SPEC.md §12 的码表**,不许发明新 code | 违反 → 前端判断错误 |
| 5 | **URL 前缀按 SPEC.md §13**:公开 `/api/v1/<r>`,后台 `/api/v1/admin/<r>` | 违反 → 接口找不到 |
| 6 | **图片 URL 返回完整 URL**(含 host),不返回相对路径 | 违反 → 前端图片裂 |
| 7 | **分页参数 `page` 从 1 开始,`page_size` 默认 20** | 违反 → 数据漏 |
| 8 | **时间格式 ISO 8601 字符串**,不要 timestamp | 违反 → 前端解析错 |
| 9 | **后端 session 必须导出 fixtures**(见 SPEC §15.1):artifact 含 OpenAPI 片段 + cURL 调用例 + 真实响应 JSON + `artifacts/fixtures/` 目录 | 违反 → 前端无 mock 可用 |
| 10 | **前端 session 的 mock 必须取自后端 fixtures**,不允许自己捏字段 | 违反 → 契约漂移,联调爆雷 |

---

## 不许做的事

- ❌ 跳过启动协议直接动手
- ❌ 改 SPEC 之外的契约(自己发明 API 路径、错误码、字段名)
- ❌ 跨 session 共享代码(包括类型定义、工具函数、组件)——**每个 session 自己写一份**
- ❌ 顺手修别处的 bug(发现了记下来告诉用户,不要顺手改)
- ❌ 跳过 artifact 和 STATUS 更新就声称完成
- ❌ 删除别的 session 的代码 / 数据库 / 文件
- ❌ 不跑测试就声称"完成了"
- ❌ 编造接口字段、编造返回结构——不确定就停下来问

---

## 收尾自检清单(session 结束前必须自问)

```
□ 我读过 SPEC.md,我的实现符合 §11~§14
□ 我读过 USER_STORIES.md,跑通了我 session 涉及的全部 stories
□ artifact 里逐 story 列了 Then 段的验证证据(不是「应该能跑」)
□ 我没违反任何一条共享技术约定
□ 我只动了自己 session spec 允许动的文件路径
□ 如果改了接口契约,我已经更新 SPEC.md §5/§6/§12/§13
□ 如果改了 User Stories,我已经更新 USER_STORIES.md
□ 我已经在 artifacts/<my-session-id>.md 留下产物
□ 我已经更新 STATUS.md
□ 不确定 / 临时绕过的点,我已经记在 artifact 里
```

**少一条 = session 没做完。**

---

## 上下文过长 / 跨 session 协同问题

如果你是新开的 Claude Code 实例,**你什么都不记得**。这是设计的:

- 项目记忆**全部沉淀在文件里**(SPEC / STATUS / sessions / artifacts)
- 不依赖对话历史
- 你读完启动协议的 4 个文件后,应该完整知道:做什么、为什么这么做、不能踩哪些坑

如果读完这 4 个文件还不够,说明文件本身没写好 —— **停下来告诉用户**,补齐文件再开干。

---

## 紧急情况(以下情况立即停下来报告用户,不许自己决定)

- 发现 SPEC.md 跟 session spec 矛盾
- 发现两个不同 session 改同一个文件
- 依赖的 session 没做或做错了,你绕不过去
- 用户没确认就让你跑生产命令(部署、清库、推 git 等)
- session spec 没写清楚的关键决策(如选哪个库、用哪个 pattern)

**报告比硬猜更便宜**。猜错的代价是返工 30 分钟,问一次的代价是 30 秒。

---

**版本**:v1.0
**最后更新**:2026-05-25
