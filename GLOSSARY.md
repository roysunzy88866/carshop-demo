# Glossary · 术语表

> **目的**:同一概念用同一个词,不出现"device_id / 设备 ID / Android ID / X-Device-Id"这种四种叫法表达同一件事的情况。
>
> 任何 session 引入新术语,**必须**先在这里注册。

---

## 一、领域 / 业务术语

| 标准术语 | 中文叫法 | 别名 / 易混 | 定义 |
|---|---|---|---|
| **product**(商品) | 商品 | 货品 / item / SKU | 后台运营录入的、车机端可购买的单元;数据模型见 [SPEC.md §5](SPEC.md) |
| **product_type** | 商品类型 | 品类(❌ 别用,易跟 category 混)| enum:`physical`(实物)/ `service_voucher`(服务券)。决定价格颜色规则 |
| **category**(分类) | 分类 | 类目 / 类别 | 商品的归属分类(汽车用品 / 加油充电 / 洗车保养 / 周边餐饮 / 旅行服务)|
| **banner**(轮播图) | Banner | 首图 / 广告位 | 车机端首页顶部的轮播图,可跳商品 / 分类 / 无跳转 |
| **order**(订单) | 订单 | - | 用户在车机端创建的购买记录 |
| **OrderItem**(订单项) | 订单项 | - | 订单内的商品行,含**商品快照**(`product_snapshot_json`)|
| **product_snapshot**(商品快照) | 商品快照 | - | 下单时刻 product 的完整数据备份,保证商品被改名 / 删除后订单仍可看 |
| **shipping_info**(收货信息) | 收货信息 | 地址 / 联系方式 | Demo 阶段固定为 SPEC §5.1 默认值;后续 M-Address 模块单独支持 |
| **admin**(运营) | 运营 / 管理员 | 后台用户 | 后台 Web 的登录账号,Demo 阶段只有一个 `admin/admin123` |
| **device_id**(设备 ID) | 设备 ID | 车机 ID / Android ID | 车机的唯一标识,从 `Settings.Secure.ANDROID_ID` 取,**用 `X-Device-Id` header 传给服务端**(不是 body)|

---

## 二、协议 / 字段格式

| 标准术语 | 含义 / 格式 |
|---|---|
| **订单号** | 格式 `O{YYYYMMDDHHMMSS}{4位随机数字}`,例 `O202605261234567890`。生成在 `app/services/order_service.py` |
| **金额单位** | 一律用「分」(int),前端显示时除 100。详见 [SPEC §11.3](SPEC.md) |
| **时间格式** | ISO 8601 带时区字符串,例 `2026-05-26T12:00:00+08:00`。详见 SPEC §11.8 |
| **图片 URL** | 永远是**完整 URL**(含 host),不是相对路径。SPEC §11.6 |
| **分页 envelope** | `{list: T[], total: int, page: int, page_size: int}`。`page` 从 1 起,`page_size` 默认 20 最大 100 |
| **响应壳** | `{code: int, data: T \| null, message: string}`。`code=0` 成功,其他失败 |

---

## 三、错误码(SPEC §12 完整码表 · 这里只列常用)

| code | 含义 | HTTP | 触发场景 |
|---|---|---|---|
| **0** | 成功 | 200 | - |
| **1000** | 参数非法 | 400 | 缺字段、值非法、文件超限、格式不对 |
| **1001** | 资源不存在 | 404 | id 查不到、跨设备查订单(不泄露存在性)|
| **1002** | 资源冲突 | 409 | 删有商品的分类、其他业务规则冲突 |
| **2000** | 未登录 | 401 | cookie 失效、缺 session 等 |
| **2001** | 用户名 / 密码错 | 401 | 登录密码错 |
| **2002** | 暴力破解保护(预留) | 429 | 当前未实现,见 TD-014 |
| **3000** | 业务规则不允许 | 409 | 商品下架还想下单、订单状态不允许支付 |
| **5000** | 服务端内部错 | 500 | 兜底 |

---

## 四、Session / 流程术语

| 标准术语 | 含义 |
|---|---|
| **session** | 一次独立的 Claude Code 开发任务,有自己的 spec(在 `sessions/`)和 artifact(在 `artifacts/`)|
| **波**(wave) | 一组可并行的 sessions,例"波 2" = 02/03/04 三个并行 |
| **orchestrator** | 这边管全局的 Claude(不写代码,管 sessions),跟 vibe-coder 用户对话 |
| **worker session** | 在 Claude Code 窗口跑的 session,严格按 spec 执行任务 |
| **artifact**(产物) | session 完成后留下的总结文档:实际接口、fixtures、截图、踩坑 |
| **fixtures** | 后端真实响应的 JSON 副本,前端 session 取来做 mock。**禁止自捏** |
| **桩**(stub) | 临时占位实现,后续 session 替换。例:02 的 `require_admin` 桩 → 05 替换 |
| **zero-touch 替换** | 改一个依赖函数的实现,但调用方一行不动。例:05 改 `require_admin` 让 02/03/04 后台接口自动启用真鉴权 |

---

## 五、技术 / 工具术语

| 标准术语 | 含义 |
|---|---|
| **MSW** | Mock Service Worker,前端拦截 HTTP 请求返 fixtures。`carshop-admin/` 用 |
| **MockWebServer** | OkHttp 的本地假服务端。车机端 08+ 用 |
| **AAOS** | Android Automotive OS,谷歌官方车机系统(我们**不**用,我们用通用 Android)|
| **vite proxy** | dev 模式下 vite 把 `/api/*` 转发到 localhost:8000,实现同源 |
| **Cloudflared tunnel** | 把 Mac mini 暴露到公网的隧道,沿用 `panqian-tunnel`,不新建 |
| **launchd** | macOS 守护进程管理,服务端跑这个保证开机自启 |

---

## 六、设计 token / UI 术语

| 标准术语 | 含义 |
|---|---|
| **石墨蓝** | 主色 `#1A2027`,SPEC 钦定品牌方向 C |
| **海泡青** | 强调色 `#00C2A8`,选中态 / hover |
| **信号红** | `#E63946`,physical 商品价格色 + error |
| **品牌方向 C** | "海泡青电车感 · Light only · no motion",见 `design/安卓商城/tokens.json` |
| **CarshopRail** | 车机端自定义左侧导航,240dp 宽,3dp 选中条。**不要用** Material 的 `NavigationRail`(80dp 太窄)|
| **CarshopPrice** | 价格组件,按 `product_type` 自动上色。前端任何价格显示**禁止**直接写 `<span style="color:red">` |
| **CarshopQrCodeBox** | 二维码弹层,480dp + mono 倒计时字体 |

---

## 七、易混 / 红线词

❌ **不要说"品类"** → 应说 **`product_type`**(商品类型)或 **`category`**(分类),"品类"二义性强  
❌ **不要说"金额是浮点"** → 一律 int(分)  
❌ **不要说"图片 base64"** → 一律 URL  
❌ **不要说"前端把分转元"** → 只有 `PriceInput` / `PriceDisplay` / `CarshopPrice` 三处转,其他地方禁止散落  
❌ **不要说"购物车"** → MVP 没有购物车,详情页"立即购买"直进确认页  
❌ **不要说"搜索框"** → MVP 砍掉了搜索,见 [DECISIONS.md](DECISIONS.md) ADR-003

---

## 维护规则

- 引入新概念 / 新别名 / 新缩写时,**立刻**在此注册
- 发现两个 session 用不同词表达同一概念时,**抓一个进来**记进易混区
- 修订日期 + 修订原因要追加

---

**版本**:v1.0
**最后更新**:2026-05-26
