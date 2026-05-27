# Artifacts · session 间产物

> 每个 session 完成后,在本目录下创建 `<session-id>-<name>.md`,记录实际交付物。
>
> **下一个 session 启动时会读这里,确认依赖到位、知道实际接口长什么样。**

---

## 为什么需要 artifact?

session spec 描述的是「计划做什么」,artifact 记录的是「实际做了什么」。

两者会有差异:
- 接口字段实际新增了一个,但 SPEC 没改
- 某个边界 case 用了临时方案
- 某段实现绕过了一个原本预想的方式

下一个 session 不能依赖"计划",必须依赖"实际"。所以每个 session 完成时必须留下 artifact。

---

## Artifact 模板

每个 session 完成后,新建 `artifacts/<id>-<name>.md`,按以下结构写:

```markdown
# Artifact: <session id> <name>

**完成日期**:YYYY-MM-DD
**对应 session spec**:`sessions/<id>-<name>.md`

## 一句话总结
(这个 session 做完了什么,不超过 2 行)

## 实际交付物清单
- 文件 / 目录 / 代码模块
- 接口 / 端点
- 组件 / 页面
- ...

## 接口对照(后端 session 必填,前端 session 选填)

对照 SPEC.md §6,实际实现的接口:

| Method | Path | 状态 | 备注 |
|---|---|---|---|
| GET | `/api/v1/xxx` | ✅ 已实现 | 字段 / 行为 100% 跟 SPEC 一致 |
| POST | `/api/v1/xxx` | ⚠️ 偏离 | 实际新增了字段 `zzz`,SPEC.md §6 已同步更新 |
| ...

每个接口至少给一个**实际请求和响应示例**(curl 或 HTTP 报文)。

## 组件对照(前端 session 必填)

| 组件 / 页面 | 位置 | props / 入参 | 状态 |
|---|---|---|---|

## SPEC.md 改动记录

如果改了 SPEC.md,列在这里:
- §X 改了什么、为什么

如果没改,写"无"。

## 已踩过的坑 / 临时方案

- 坑 1:xxx,绕过方式:yyy,后续需要 zzz
- 坑 2:...

## 下一个 session 需要注意的点

- 接入本 session 产物的 session 必须知道:xxx
- 不要假设:yyy
```

---

## Artifact 是「合同的回执」

把它想成 session 之间的"已读回执":
- session A 完成 → 留下 artifact A
- session B 启动 → 读 artifact A,确认它真的实现了 B 需要的东西
- 发现不对 → 报告用户,不要硬上

---

**版本**:v1.0
