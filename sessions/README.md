# Sessions 索引与依赖图

> 每个 session 是一次独立的 Claude Code 开发任务,有清晰的边界、依赖、验收标准。
>
> 启动任何 session 前请先读项目根目录的 `CLAUDE.md`。

---

## Session 一览(共 12 个)

| ID | 名称 | 一句话 | 主要交付物 |
|---|---|---|---|
| **00** | design-system | 把设计稿沉淀成可复用的设计系统(token + 组件) | Android Compose 主题 + 核心组件、AntD theme config |
| **01** | server-foundation | 服务端项目骨架 + 数据库模型 + 初始化数据 | FastAPI 项目、SQLAlchemy models、migration、seed 数据 |
| **02** | server-catalog-api | 分类 + 商品接口(公开 + 后台) | `/api/v1/categories`、`/api/v1/products`、`/api/v1/admin/categories`、`/api/v1/admin/products` + fixtures |
| **03** | server-banner-api | Banner 接口(公开 + 后台) | `/api/v1/banners`、`/api/v1/admin/banners` + fixtures |
| **04** | server-order-pay-api | 订单 + 模拟支付 | `/api/v1/orders`、`/api/v1/orders/:id/mock_pay` + fixtures |
| **05** | server-admin-auth-upload | 后台登录 + 图片上传 | `/api/v1/admin/login`、`/api/v1/admin/upload` + 中间件 + fixtures |
| **06** | admin-web | React + AntD 后台,所有 CRUD 页面 | `carshop-admin/` 完整 React 项目 |
| **07** | **early-integration** ⭐ | **后端 + 后台首次真集成,部署到 Mac mini,暴露契约漂移** | 公网可访问 admin web、契约漂移修复记录 |
| **08** | android-foundation | 车机端项目 + 主题 + 导航 + API client + 设备 ID | Android 项目骨架、Retrofit client、theme、navigation |
| **09** | android-browse | 车机端首页 + 分类页 + 商品详情 | Browse 模块,从首页能逛到商品详情 |
| **10** | android-checkout | 订单确认 + 模拟支付 + 我的订单 | Checkout 模块,完成"下单→支付→看单"链路 |
| **11** | final-integration | 端到端联调、APK 打包、最终部署 | 公网 admin + 安装好的车机端 + 录屏 |

---

## 依赖关系图

```
波 1:   [00 design-system]      [01 server-foundation]
              │                            │
              │                  ┌─────────┼──────────┬──────────┐
              │                  ▼         ▼          ▼          ▼
波 2:         │            [02 catalog][03 banner][04 order-pay][05 admin-auth-upload]
              │                  │         │          │          │
              │                  └─────────┴─────┬────┴──────────┘
              │                                  ▼
波 3:         │                            [06 admin-web]
              │                                  │
              │                                  ▼
波 4:         │                ⭐ [07 early-integration · 串行检查站]
              │                                  │
              │           ┌──────────────────────┘
              ▼           ▼
波 5:        [08 android-foundation]
                          │
                ┌─────────┴──────────┐
                ▼                    ▼
波 6:    [09 android-browse]  [10 android-checkout]
                │                    │
                └────────┬───────────┘
                         ▼
波 7:           [11 final-integration]
```

## 并行机会(同一波可以同时开多个 Claude Code session 跑)

| 波次 | 可并行 session | 备注 |
|---|---|---|
| **波 1** | 00、01 | 设计系统(00)和服务端骨架(01)完全无关,可同时开 |
| **波 2** | 02、03、04 | 后端 3 个领域接口,都依赖 01,互相无依赖,可同时跑 |
| **波 2.5** | **05**(独占) | ⚠️ 不能跟 02/03/04 并行。05 的工作是替换它们留在 `app/deps.py` 的 `require_admin` 桩,并行会冲突 |
| **波 3** | 06 | 后台 Web,依赖波 2 全部完成(用真接口或 fixture 都行) |
| **波 4** | **07 早集成**(独占) | ⭐ 第一次真集成,**必须串行**,会修 SPEC 和 fixtures。任何并发都不许 |
| **波 5** | 08 | 车机骨架,依赖 00(主题)+ 07(已经修过的契约) |
| **波 6** | 09、10 | 车机两个 feature,都依赖 08,互相无依赖,可同时开 |
| **波 7** | 11 | 终集成,串行 |

> 同时开多个 session 时,每个 session **只动自己 spec 允许的文件**,避免冲突。
> **07 是检查站,不能跳过、不能并行**。它会找出契约漂移并修 SPEC 和 fixtures,所有后续 session(08/09/10)用修过的版本。

---

## 每个 session 的开工方法

打开一个新的 Claude Code 会话(或对话),把下面这段贴进去:

```
我在做车机商店项目,工作目录 /Users/Admin/Documents/Projects/车机商店需求。

请按 CLAUDE.md 的启动协议来:
1. 读 CLAUDE.md
2. 读 SPEC.md
3. 读 STATUS.md
4. 读 sessions/XX-name.md  ← 这里填你这次要做的 session
5. 读 artifacts/(已完成的 session 产物)

读完后告诉我:
- 一句话回放你要做什么
- 你看到的依赖是否都就绪了
- 你的验收标准
- 不确定的点

等我说「开始」再动手。
```

---

## 如何修改 session 拆分

如果在开发过程中发现拆分不合理:
1. **不要在 session 内部偷偷扩大范围**
2. 停下来,跟用户讨论
3. 修改 `sessions/README.md`(本文件)和具体的 session spec
4. 更新 `STATUS.md` 记录决策

---

**版本**:v1.0
**最后更新**:2026-05-25
