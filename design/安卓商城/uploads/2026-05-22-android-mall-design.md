# 安卓商城 · 设计文档(Spec)

> **日期**:2026-05-22
> **阶段**:Brainstorming → Spec(等待用户审核)
> **下一步**:用户审核 → 走 `superpowers:writing-plans` 写实施计划

---

## 〇、项目概述

| 项 | 决策 |
|---|---|
| **项目目的** | 真要上架的产品(代码质量、性能、可维护性都得过关) |
| **范围** | 标准版安卓商城 8-10 核心页面 + 配套后端 |
| **业务参考** | 淘宝/京东主逻辑(不包含直播/主播) |
| **平台模式** | 纯自营(平台即商家,无第三方入驻) |
| **协作模式** | Vibe coding · 用户负责产品决策,AI 负责技术实现 |
| **代码量预估** | 前端约 25-30 模块 / 后端约 15-18 业务模块 |

---

## 一、业务需求(13 轮调研结果)

### 1.1 用户与账户体系

| 项 | 决策 |
|---|---|
| 登录方式 | 手机号 + 短信验证码;第三方(微信/支付宝/Apple);**测试游客入口**(`BuildConfig.DEBUG` 才显示,release 自动隐藏) |
| 注册流程 | 手机号首次验证码登录自动注册,无独立注册流程 |
| 实名认证 | 触发式 —— 平时不要求,下单/退款/提现金额超过阈值时弹出(阈值后端 `system_config` 表可配,默认 ¥1000) |
| 多账号 | 一手机号一账号 |
| 账户安全 | 改密 / 换手机 / 改头像昵称 / 注销账号(完整功能);**登录设备管理(UI 占位,上线前补真实实现)** |

### 1.2 商品体系

| 项 | 决策 |
|---|---|
| 商品模型 | **SPU/SKU 规格模型** —— 一个商品多个规格(颜色/尺码),每个规格独立价格独立库存 |
| 分类层级 | 三级分类(一级女装 → 二级连衣裙 → 三级雪纺) |
| 库存策略 | **下单预占库存**,15 分钟未付款自动回库(`system_config.payment_timeout_minutes` 后端可配) |
| 商品标签 | 状态标签:新品 / 热卖 / 在售 / 预售(优惠标、品质标 v1 不做,数据库字段预留) |
| 价格字段 | `original_price`(原价)+ `sale_price`(现价)—— 运营可手动改 sale_price 实现"营销活动" |

### 1.3 店铺与平台模式

| 项 | 决策 |
|---|---|
| 平台模式 | **纯自营** —— 所有商品平台直管,无第三方商家入驻 |
| 品牌体系 | 商品必填品牌字段 + 品牌主页(搜品牌、看同品牌所有商品) |
| 店铺概念 | 不做(纯自营无店铺) |
| 客服 | 平台统一客服 —— UI 一个"联系客服"按钮,实际接入方案(微信客服 / 腾讯云 IM)上线前定 |

### 1.4 首页运营位

| 项 | 决策 |
|---|---|
| Banner 轮播 | 做 + 后台可配置(运营上传图、设跳转链接、调顺序) |
| 频道入口 | icon 网格,**后台可配**(选哪些分类上首页、换 icon、调顺序) |
| 楼层数量 | **4 个楼层**:新品 + 热卖品牌 + 品类聚合 + 推荐(**无秒杀** —— 第 10 轮删除) |
| 楼层实施节奏 | 接口和组件按完整版做,**v1 上线只激活 1-2 个楼层**,其他楼层运营后台未配置即不渲染 |
| 信息流 | **个性化推荐** —— 接口按个性化设计,埋点同步开发收数据;**v1 算法降级:加权销量 + 新品**(冷启动期所有用户看到相同信息流),3-6 个月数据积累后切真个性化 |

### 1.5 搜索(电商 50% 流量入口,深度挖)

| 项 | 决策 |
|---|---|
| **v1 引擎** | **PostgreSQL 全文索引 + zhparser 中文分词扩展 + 同义词词典(后台维护)** |
| **架构** | **Repository 层抽象「搜索服务接口」`SearchService`** —— v2 切 Elasticsearch 时只换 implementation,业务代码不动 |
| **v1 精度** | 分词(zhparser)+ 同义词(后台维护词典)。**纠错和拼音 v1 不做**(技术栈限制),v2 切 ES 后补 |
| **v2 升级触发** | SKU > 5 万 或 用户体验明显差 → 切 ES,补纠错(`fuzzy` query) + 拼音搜索(拼音字段索引) |
| 搜索辅助 | 历史(用户维度持久化) + 后台可配热词 + 输入联想(后端维护联想词库) |
| 筛选项 | 三级分类 + 价格区间 + **品牌(chip + "更多品牌"抽屉)**;属性筛选(颜色/尺码)v1 不做,v2 补 |
| 排序项 | 综合(默认) + 销量↓ + 价格↑↓ |
| 综合排序权重 | `销量×0.5 + 新品×0.2 + 评分×0.2 + 有库存×0.1` —— 全用户一致,v2 加用户偏好品类 30% |
| 结果页样式 | 双列卡片 |
| 空结果处理 | ① 同义词扩展自动重搜 ② 失败后推荐相似品类 |

### 1.6 购物车

| 项 | 决策 |
|---|---|
| 同步范围 | **云端跨设备同步**(登录后) |
| 失效商品 | 打包到购物车底部「失效商品」区,不能勾选结算,可查看可手动移除可看相似商品 |
| 加购上限 | 单 SKU ≤ 库存 + 购物车总计 ≤ 100 个 SKU |
| 游客购物车 | 可加,本地 Room 数据库存,登录后调 `POST /cart/merge` 合并到云端 |

### 1.7 下单流程

| 项 | 决策 |
|---|---|
| 拆单策略 | **不拆单**,一个订单到底 |
| 优惠系统 | **v1 不做**(优惠券/红包/满减全砍);但保留商品 `sale_price` 改价支持运营促销;**数据库字段预留**(`coupon`, `promotion`, `discount` 等表先建空,v2 启用) |
| 发票 | **UI 占位 + 标记"上线前接税务局接口"** —— 数据库表 `order_invoice` 建好,接寒柏德/航信前展示"开票中" |
| 地址智能识别 | **做 + 接腾讯云/百度云 NER API** —— 用户粘贴"张三 13800138000 北京市海淀区中关村大街 1 号"自动拆姓名/手机/地址 |
| 支付倒计时 | 提交订单后 **15 分钟**未支付自动取消(`system_config.payment_timeout_minutes` 可配) |

### 1.8 支付

| 项 | 决策 |
|---|---|
| 渠道(上线) | 微信支付 + 支付宝(企业资质下来后接入) |
| 开发期方案 | **UI 占位"模拟成功"** —— 点支付按钮直接调 `POST /payments/{id}/mock` 把订单状态改为「已付款」 |
| 钱包/余额 | **不做**(金融合规雷区,需要 ICP / 资金托管 / 反洗钱风控,个人开发者搞不起) |
| 超时回库 | 与下单倒计时一致,15 分钟 |

### 1.9 订单与售后

| 项 | 决策 |
|---|---|
| 物流跟踪 | **接快递 100 / 阿里云快递查询 API** —— 自动同步全路径(已揽件/运输中/派送中/已签收),¥0.2/查询 |
| 自动确认收货 | 发货后 **7 天**(`system_config.auto_confirm_days` 可配) |
| 退款流程 | **未发货** = 用户申请 → 系统自动同意 → 原路退款<br>**已发货** = 用户申请 → 人工审核 → 同意 → 用户寄回 → 平台验货 → 原路退款<br>**收货后** = 同已发货(7 天无理由) |
| 仅退款 | **不做**(自营场景损失全平台扛,无意义) |
| **退货后库存** | **库存自动 +N**(`UPDATE stock = stock + N`)—— spec 强制 |

### 1.10 营销体系

| 项 | 决策 |
|---|---|
| 积分 | **不做**(数据库字段 `users.points` 预留 + `points_history` 表预留) |
| 会员等级 / VIP | **不做**(`users.member_level` 字段预留) |
| 秒杀 | **不做**(`seckill_activity` 表预留) |
| 优惠券 / 红包 / 满减 | **不做**(`coupon`, `promotion` 表全预留) |
| 商品改价 | **做** —— `sku.sale_price` 字段,运营后台可改,商品详情显示原价划线 + 现价 |

### 1.11 评价、收藏、消息

| 项 | 决策 |
|---|---|
| 评价完整度 | **完整版**:评分(5 星) + 文字 + 图片(最多 9 张) + 视频(1 个) + 30 天内可追评一次 + 评价筛选(全部/有图/差评) + 商家(=平台)回复 |
| 评价门槛 | **只购买者 + 收货后 30 天内**(`order.confirm_at` 起算) |
| 收藏 | 只做**商品收藏**(品牌关注 / 浏览历史 / 加购降价提醒 v1 不做) |
| 消息推送 | **v1 不做** —— `notification` 表预留,v2 接极光推送 / 腾讯信鸽 |

### 1.12 库存并发控制

| 项 | 决策 |
|---|---|
| 减库存 | **原子 SQL**:`UPDATE sku SET stock = stock - N WHERE sku_id = ? AND stock >= N` —— 返回 0 行表示库存不足 |
| 回库策略 | **双保险**:订单状态变更事件主驱动 + 5 分钟定时任务扫超时未付订单兜底 |
| 缓存策略 | 商品详情(名字/价格/图片) Redis 缓存 5 分钟;**库存独立实时查**(`GET /products/{id}/inventory`,不走缓存) |
| 限购 | 同一用户同商品限购 N 件(`system_config.purchase_limit_per_sku` 可配,默认 99) |

### 1.13 订单+支付边界场景

| 项 | 决策 |
|---|---|
| 回调丢失 | **主动轮询 + 被动回调双保险** —— 后端定时任务每分钟扫"已提交未付款"订单,主动调微信/支付宝查单接口,确认是否已支付 |
| 幂等性 | **订单状态机校验**(只允许合法状态跳转,如 待付→已付 √,已付→待付 ✗)+ **支付流水号数据库唯一索引** |
| 超时边界 | **以支付为准** —— 系统取消任务执行时先检查"是否已有支付记录",有则不取消;若已取消但用户随后支付成功,自动原路退款 |
| 退款失败 | 支付平台退款失败 → 进入后台**「人工兜底」列表**,运营手动处理(联系用户、改用其他方式退) |
| 退款金额校验 | 退款总金额 ≤ 原订单金额(多次退款累计) |

---

## 二、技术架构 + 范式选择原因

### 2.1 整体架构 · Clean Architecture(三层分离)

```
┌─────────────────────────────────────┐
│  Presentation Layer(界面层)         │  Compose 页面 + ViewModel
│  · Compose Screen                    │  · 只管渲染、用户事件
│  · ViewModel + UI State              │  · 不知道数据从哪来
└─────────────↑↓──────────────────────┘
              │ 调用 UseCase
┌─────────────────────────────────────┐
│  Domain Layer(业务层)                │  纯 Kotlin,无 Android 依赖
│  · Entity(业务实体)                  │  · 只管业务规则
│  · UseCase(业务用例)                 │  · 不知道 UI / DB / 网络
│  · Repository Interface              │
└─────────────↑↓──────────────────────┘
              │ 接口(domain 声明,data 实现)
┌─────────────────────────────────────┐
│  Data Layer(数据层)                  │  Retrofit/Room/DataStore
│  · Repository Impl                   │  · 决定数据从网络/本地哪来
│  · RemoteDataSource (Retrofit)       │  · 缓存策略
│  · LocalDataSource (Room/DataStore)  │
└─────────────────────────────────────┘
```

### 2.2 范式选择原因

| 决策 | 选择 | 原因 |
|---|---|---|
| **前端架构** | Clean Architecture + MVVM | 谷歌官方推(Now in Android 范本);上架产品要经得起折腾;每个文件职责单一,AI 写错概率低 |
| **前端 UI** | Jetpack Compose | 谷歌主推的新方式;声明式 UI,AI 写起来比 XML 更顺;Material 3 主题系统现代 |
| **前端 DI** | Hilt | Compose + Hilt 官方推荐组合 |
| **模块化策略** | 按 feature 拆模块 | 删一个功能 = 删一个文件夹;两个 AI 会话改不同 feature 不会打架 |
| **后端语言** | Kotlin | 跟前端一致,认知负担最低;Kotlin 比 Java 更现代 |
| **后端框架** | Spring Boot 3 | 全球最主流的服务器框架,生态最深;上架级稳定性 |
| **后端架构** | DDD 分层(domain / api / config / common) | 业务核心(domain)和接口暴露(api)分离;数据库结构 ≠ 接口结构,避免泄漏 |
| **数据库** | PostgreSQL 16 | 中文搜索能力(zhparser)、JSONB、事务都强;比 MySQL 更适合电商 |
| **数据库版本** | Flyway | 上线后改表结构不丢老数据,迁移脚本版本化 |
| **构建工具** | Gradle Kotlin DSL + Version Catalog | `gradle/libs.versions.toml` 统一版本,16 个模块改版本只改一处 |
| **数据三模型** | DTO(网络) / Entity(数据库) / UiModel(界面) | 三个层次数据形态不同,分开避免改一处崩三处 |
| **API 协议** | REST + JSON,统一返回 `{code, msg, data}` | 业界标准,Postman/curl 调试方便;AI 写起来最熟 |
| **鉴权** | JWT(Access + Refresh)Bearer Token | 无状态;后端水平扩展不需要 session 同步 |
| **时间** | ISO 8601 字符串(UTC) | 跨时区无歧义 |
| **金额** | 整数(分单位) | 浮点数算钱会丢精度,业界铁律 |

### 2.3 关键技术清单

#### 前端依赖(`gradle/libs.versions.toml` 主要项)

```toml
[versions]
kotlin = "2.0.21"
compose-bom = "2024.12.01"
hilt = "2.52"
retrofit = "2.11.0"
okhttp = "4.12.0"
room = "2.6.1"
datastore = "1.1.1"
coil = "2.7.0"
navigation = "2.8.4"
serialization = "1.7.3"
coroutines = "1.9.0"
junit = "4.13.2"
mockk = "1.13.13"
turbine = "1.2.0"  # Flow 测试
```

#### 后端依赖(`gradle/libs.versions.toml`)

```toml
[versions]
kotlin = "2.0.21"
spring-boot = "3.4.0"
spring-dependency = "1.1.6"
flyway = "10.20.0"
postgresql = "42.7.4"
zhparser-pg = "2.2"          # PG 中文分词扩展(数据库层装)
jjwt = "0.12.6"              # JWT
mapstruct = "1.6.3"          # Entity ↔ DTO 转换
springdoc = "2.7.0"          # OpenAPI/Swagger
testcontainers = "1.20.4"    # 集成测试用真 PG
```

#### 外部服务清单

| 服务 | 用途 | 选型(待最终确认) | 费用估算 |
|---|---|---|---|
| 短信 | 登录验证码 | 阿里云 / 腾讯云 SMS | ¥0.04/条 |
| NER 地址识别 | 智能识别地址 | 腾讯云自然语言处理 | ¥0.001-0.002/次 |
| 物流查询 | 订单物流跟踪 | 快递 100 | ¥0.2/查询 |
| 对象存储 | 商品图/评价图/视频 | 阿里云 OSS / 七牛云 | ¥0.12/GB·月 + 流量 |
| 微信支付 | 支付渠道 | 官方 SDK | 0.6% 手续费 |
| 支付宝 | 支付渠道 | 官方 SDK | 0.6% 手续费 |
| Redis | 缓存 + 限流 | 自建 / 阿里云 Redis | 自建 ¥0,云 ¥30/月起 |

---

## 三、项目文件结构

### 3.1 前端(`mall-android/`)

```
mall-android/
├── app/                                # 主壳工程(APK 打包入口)
│   ├── src/main/
│   │   ├── java/com/mall/
│   │   │   ├── MallApplication.kt      # @HiltAndroidApp 启动入口
│   │   │   ├── MainActivity.kt         # 唯一 Activity(单 Activity 架构)
│   │   │   └── navigation/
│   │   │       ├── MallNavHost.kt      # 全局路由
│   │   │       └── TopLevelDestination.kt   # 底部 4 个 Tab
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
│
├── build-logic/                        # 构建配置统一(16 模块共用)
│   └── convention/
│       └── src/main/kotlin/
│           ├── AndroidApplicationConventionPlugin.kt
│           ├── AndroidLibraryConventionPlugin.kt
│           ├── AndroidFeatureConventionPlugin.kt
│           └── AndroidHiltConventionPlugin.kt
│
├── core/                               # 共享基础
│   ├── common/                         # 工具:扩展函数、Result 包装、常量
│   ├── designsystem/                   # 颜色、字体、间距、Material 3 Theme
│   ├── ui/                             # 通用 Compose 组件
│   │   ├── ProductCard.kt              # 商品卡(双列搜索 + 楼层都用)
│   │   ├── PriceText.kt                # 价格组件(原价划线 + 现价)
│   │   ├── EmptyState.kt               # 空状态
│   │   ├── LoadingState.kt
│   │   ├── ErrorState.kt
│   │   └── ...
│   ├── network/                        # Retrofit + OkHttp + JWT 拦截器
│   │   ├── di/NetworkModule.kt
│   │   ├── interceptor/AuthInterceptor.kt
│   │   ├── interceptor/RefreshTokenAuthenticator.kt
│   │   └── ApiResponse.kt              # 统一 {code, msg, data} 包装
│   ├── database/                       # Room(本地购物车/搜索历史)
│   │   ├── MallDatabase.kt
│   │   ├── dao/CartDao.kt
│   │   └── dao/SearchHistoryDao.kt
│   ├── datastore/                      # 偏好存储(token/设置)
│   │   ├── UserPreferences.kt
│   │   └── AppPreferences.kt
│   ├── data/                           # Repository 实现 + DataSource
│   │   └── repository/                 # 实现 domain 层的 Repository 接口
│   ├── domain/                         # 业务实体 + UseCase + Repository 接口
│   │   ├── model/                      # Domain 实体(Product, Order, ...)
│   │   ├── repository/                 # Repository 接口
│   │   └── usecase/                    # UseCase(下单/算价/...)
│   ├── model/                          # 三种数据模型基类 + 共享 DTO
│   └── testing/                        # 测试工具:FakeRepository、TestData
│
└── feature/                            # 业务功能(每个独立模块)
    ├── home/                           # 首页(banner + 频道 + 4楼层 + 信息流)
    ├── category/                       # 三级分类树 + 分类商品流
    ├── product/                        # 商品详情(图片墙 + 规格选择 + 评价 + 详情图)
    ├── search/                         # 搜索(历史/热词/联想/结果页+品牌筛选)
    ├── cart/                           # 购物车(增删改 + 失效区 + 批量结算)
    ├── order/                          # 订单(创建 + 列表 + 详情 + 物流跟踪)
    ├── payment/                        # 支付(微信/支付宝/模拟)
    ├── aftersale/                      # 售后(申请 + 列表 + 详情 + 寄回单号填写)
    ├── auth/                           # 登录(手机号 + 第三方 + 测试游客)
    ├── profile/                        # 个人中心(用户信息 + 订单入口 + 收藏入口)
    ├── address/                        # 地址管理(增删改 + 智能识别)
    ├── review/                         # 评价(发表 + 查看 + 追评 + 筛选)
    ├── favorite/                       # 商品收藏
    ├── brand/                          # 品牌主页
    └── customer-service/               # 客服 UI 占位
```

#### 每个 feature 模块的内部结构(标准化)

```
feature/home/src/main/java/com/mall/feature/home/
├── HomeRoute.kt                        # 入口(连接 ViewModel 和 Screen)
├── HomeScreen.kt                       # 纯 UI(无状态)
├── HomeViewModel.kt                    # 状态管理 + UseCase 调用
├── HomeUiState.kt                      # UI 状态密封类
│   //  Loading | Success(data) | Error(message)
├── components/                         # 仅本页用的子组件
│   ├── BannerCarousel.kt
│   ├── ChannelGrid.kt
│   ├── FloorSection.kt
│   └── RecommendationFeed.kt
└── di/HomeModule.kt                    # 本模块 Hilt 模块
```

### 3.2 后端(`mall-backend/`)

```
mall-backend/
├── build.gradle.kts                    # Gradle Kotlin DSL
├── settings.gradle.kts
├── gradle/libs.versions.toml           # 依赖版本统一
│
└── src/
    ├── main/
    │   ├── kotlin/com/mall/
    │   │   ├── MallApplication.kt      # @SpringBootApplication 启动入口
    │   │   │
    │   │   ├── config/                 # 全局配置
    │   │   │   ├── SecurityConfig.kt   # Spring Security + JWT 过滤链
    │   │   │   ├── WebConfig.kt        # CORS、拦截器、统一异常处理
    │   │   │   ├── OpenApiConfig.kt    # Swagger 自动生成接口文档
    │   │   │   ├── RedisConfig.kt
    │   │   │   ├── JacksonConfig.kt    # snake_case ↔ camelCase 自动转
    │   │   │   └── AsyncConfig.kt      # 线程池 + 事件总线
    │   │   │
    │   │   ├── common/                 # 通用基础
    │   │   │   ├── ApiResponse.kt      # 统一返回格式 {code, msg, data}
    │   │   │   ├── exception/
    │   │   │   │   ├── BusinessException.kt
    │   │   │   │   ├── ErrorCode.kt    # 错误码常量
    │   │   │   │   └── GlobalExceptionHandler.kt  # @ControllerAdvice
    │   │   │   ├── util/
    │   │   │   │   ├── JwtUtil.kt
    │   │   │   │   ├── SnowflakeId.kt  # 订单号 / 支付流水号生成
    │   │   │   │   └── PhoneUtil.kt    # 手机号验证 / 脱敏
    │   │   │   ├── constant/
    │   │   │   └── annotation/
    │   │   │       └── RateLimit.kt    # 限流注解
    │   │   │
    │   │   ├── domain/                 # 业务核心(按业务模块拆)
    │   │   │   ├── user/
    │   │   │   │   ├── User.kt         # @Entity(JPA)
    │   │   │   │   ├── UserRepository.kt    # Spring Data JPA
    │   │   │   │   └── UserService.kt  # 业务逻辑
    │   │   │   ├── product/
    │   │   │   │   ├── Spu.kt
    │   │   │   │   ├── Sku.kt
    │   │   │   │   ├── Category.kt
    │   │   │   │   ├── ProductImage.kt
    │   │   │   │   ├── ProductService.kt
    │   │   │   │   └── ...
    │   │   │   ├── brand/
    │   │   │   ├── inventory/          # 库存独立 service(原子减库存 + 限购检查)
    │   │   │   │   ├── Inventory.kt
    │   │   │   │   ├── InventoryService.kt
    │   │   │   │   └── InventoryRepository.kt
    │   │   │   ├── cart/
    │   │   │   ├── order/
    │   │   │   │   ├── Order.kt
    │   │   │   │   ├── OrderItem.kt
    │   │   │   │   ├── OrderStateMachine.kt    # 状态机校验
    │   │   │   │   └── OrderService.kt
    │   │   │   ├── payment/
    │   │   │   │   ├── PaymentRecord.kt
    │   │   │   │   ├── PaymentService.kt
    │   │   │   │   ├── WechatPayClient.kt
    │   │   │   │   ├── AlipayClient.kt
    │   │   │   │   └── MockPayClient.kt        # 开发期占位
    │   │   │   ├── address/
    │   │   │   ├── aftersale/
    │   │   │   ├── review/
    │   │   │   ├── favorite/
    │   │   │   ├── search/             # 搜索服务(SearchService 抽象,v1 用 PG 实现)
    │   │   │   │   ├── SearchService.kt        # 接口
    │   │   │   │   ├── PostgresSearchService.kt    # v1 实现
    │   │   │   │   ├── ElasticsearchSearchService.kt    # v2 占位
    │   │   │   │   ├── SearchHistoryService.kt
    │   │   │   │   └── SynonymService.kt
    │   │   │   ├── logistics/          # 物流查询(接快递100/阿里云)
    │   │   │   ├── home/               # 首页聚合
    │   │   │   │   ├── BannerService.kt
    │   │   │   │   ├── ChannelService.kt
    │   │   │   │   ├── FloorService.kt
    │   │   │   │   └── RecommendationService.kt
    │   │   │   ├── recommendation/     # 推荐算法(v1 加权销量,v2 个性化)
    │   │   │   ├── notification/       # 消息(v1 占位)
    │   │   │   ├── file/               # 文件上传(OSS)
    │   │   │   └── system/             # SystemConfig 配置中心
    │   │   │
    │   │   ├── api/                    # 对外接口(REST Controller)
    │   │   │   ├── auth/
    │   │   │   │   ├── AuthController.kt
    │   │   │   │   ├── dto/
    │   │   │   │   │   ├── LoginRequest.kt
    │   │   │   │   │   ├── TokenResponse.kt
    │   │   │   │   │   └── SmsCodeRequest.kt
    │   │   │   ├── user/
    │   │   │   ├── product/
    │   │   │   ├── category/
    │   │   │   ├── brand/
    │   │   │   ├── search/
    │   │   │   ├── home/
    │   │   │   ├── cart/
    │   │   │   ├── address/
    │   │   │   ├── order/
    │   │   │   ├── payment/
    │   │   │   ├── aftersale/
    │   │   │   ├── review/
    │   │   │   ├── favorite/
    │   │   │   ├── file/
    │   │   │   └── admin/              # 运营后台 API(v1 简化:CRUD only)
    │   │   │       ├── AdminBannerController.kt
    │   │   │       ├── AdminChannelController.kt
    │   │   │       ├── AdminFloorController.kt
    │   │   │       ├── AdminProductController.kt
    │   │   │       ├── AdminOrderController.kt
    │   │   │       └── AdminKeywordController.kt
    │   │   │
    │   │   ├── infrastructure/         # 基础设施实现
    │   │   │   ├── persistence/        # JPA Repository 实现
    │   │   │   ├── cache/              # Redis 操作封装
    │   │   │   ├── sms/                # 阿里云/腾讯云 SMS Client
    │   │   │   ├── oss/                # 阿里云 OSS Client
    │   │   │   ├── ner/                # 腾讯云 NER 地址识别 Client
    │   │   │   ├── express/            # 快递100/阿里云物流 Client
    │   │   │   └── pay/                # 支付平台 Client(微信/支付宝)
    │   │   │
    │   │   └── job/                    # 定时任务
    │   │       ├── OrderTimeoutCleanupJob.kt    # 5分钟扫一次,超时未付订单回库
    │   │       ├── PaymentPollingJob.kt         # 1分钟主动查支付平台
    │   │       └── AutoConfirmReceiveJob.kt     # 每天扫,发货7天自动确认收货
    │   │
    │   └── resources/
    │       ├── application.yml                  # 公共配置
    │       ├── application-dev.yml              # 开发环境(连本地 PG)
    │       ├── application-prod.yml             # 生产环境(连云数据库)
    │       └── db/migration/                    # Flyway 数据库版本脚本
    │           ├── V1__init_user_tables.sql
    │           ├── V2__init_product_tables.sql
    │           ├── V3__init_cart_order_tables.sql
    │           ├── V4__init_review_aftersale_tables.sql
    │           ├── V5__init_home_search_tables.sql
    │           ├── V6__init_promotion_placeholder_tables.sql  # v1 不用但表先建
    │           ├── V7__init_zhparser_extension.sql            # PG 中文分词
    │           ├── V8__init_system_config.sql                 # 配置表 + 初始数据
    │           └── V9__init_indexes.sql                       # 性能索引
    │
    └── test/kotlin/com/mall/
        ├── domain/                              # 单元测试
        ├── api/                                 # 集成测试(Testcontainers 起真 PG)
        └── e2e/                                 # 端到端测试
```

---

## 四、核心配置文件清单

| 文件 | 作用 | 备注 |
|---|---|---|
| **前端** | | |
| `mall-android/settings.gradle.kts` | 多模块声明 | 列出所有 16 个 module |
| `mall-android/build.gradle.kts`(顶层) | 全局插件版本 | Kotlin / AGP / Hilt 等 |
| `mall-android/app/build.gradle.kts` | 主壳依赖 + 签名 + ProGuard | minSdk = 24, target = 35 |
| `mall-android/gradle/libs.versions.toml` | 版本目录 | 所有版本号统一管理 |
| `mall-android/app/src/main/AndroidManifest.xml` | App 清单 | 权限 / Activity / Application |
| `mall-android/app/proguard-rules.pro` | 代码混淆规则 | Retrofit / Kotlinx Serialization 等保留规则 |
| `mall-android/.gitignore` | Git 忽略 | build / .gradle / local.properties |
| **后端** | | |
| `mall-backend/build.gradle.kts` | Gradle 主构建 | Spring Boot + Kotlin + JPA |
| `mall-backend/settings.gradle.kts` | 项目名 | |
| `mall-backend/gradle/libs.versions.toml` | 版本目录 | |
| `mall-backend/src/main/resources/application.yml` | 主配置 | server.port / spring profiles |
| `mall-backend/src/main/resources/application-dev.yml` | 开发环境 | 本地 PG + 本地 Redis |
| `mall-backend/src/main/resources/application-prod.yml` | 生产环境 | 云 PG + 云 Redis(从环境变量读) |
| `mall-backend/src/main/resources/db/migration/V*.sql` | 数据库迁移 | Flyway 版本化脚本 |
| `mall-backend/Dockerfile` | 容器化 | 多阶段构建 |
| `mall-backend/docker-compose.yml` | 本地一键起 PG + Redis | 开发期方便 |
| **全局** | | |
| `安卓商城/README.md` | 项目说明 | 给接手的人看 |
| `安卓商城/docs/api/openapi.yaml` | API 文档 | Swagger 自动生成 + 手工维护 |

---

## 五、数据库 Schema(核心表清单)

### 5.1 v1 必建表(开发用)

| 表名 | 中文 | 主要字段 |
|---|---|---|
| `users` | 用户 | id, phone, password_hash, nickname, avatar_url, identity_verified, member_level**(预留)**, points**(预留)**, created_at |
| `user_address` | 收货地址 | id, user_id, name, phone, province, city, district, detail, is_default |
| `user_login_log` | 登录日志 | id, user_id, device_info, ip, login_at |
| `system_config` | 系统配置 | key, value, description |
| `category` | 三级分类 | id, parent_id, name, icon_url, sort, level(1/2/3) |
| `brand` | 品牌 | id, name, logo_url, description |
| `spu` | 商品 SPU | id, name, category_id, brand_id, description_html, status, sales_count, score, created_at |
| `spu_image` | SPU 图片墙 | id, spu_id, url, sort |
| `spu_attribute` | 商品规格属性(颜色/尺码) | id, spu_id, attr_name, attr_values_json |
| `sku` | 商品 SKU | id, spu_id, attr_combination_json, original_price, sale_price, image_url |
| `inventory` | 库存(SKU 维度) | sku_id (PK), stock, reserved_stock(预留预占) |
| `cart_item` | 购物车 | id, user_id, sku_id, quantity, selected, created_at |
| `orders` | 订单 | id, order_no(雪花), user_id, status, total_amount, paid_amount, address_snapshot_json, created_at, paid_at, shipped_at, confirmed_at |
| `order_item` | 订单项 | id, order_id, sku_id, spu_snapshot_json(快照), quantity, unit_price |
| `order_state_log` | 订单状态变更日志 | id, order_id, from_status, to_status, operator, reason, created_at |
| `payment_record` | 支付记录 | id, order_id, channel(wechat/alipay/mock), transaction_no(UNIQUE), amount, status, raw_response_json, created_at |
| `shipment` | 物流单 | id, order_id, express_company, express_no, tracking_json, current_status |
| `aftersale` | 售后单 | id, order_id, type(refund_only/return_refund), reason, status, refund_amount, return_express_no |
| `review` | 评价 | id, order_id, sku_id, user_id, rating, content, can_append_until, created_at |
| `review_image` | 评价图/视频 | id, review_id, media_type(image/video), url, sort |
| `review_reply` | 评价回复 | id, review_id, content, replier_id, created_at |
| `review_append` | 追评 | id, review_id, content, created_at |
| `favorite` | 收藏 | id, user_id, spu_id, created_at,UNIQUE(user_id, spu_id) |
| `banner` | 首页 Banner | id, image_url, link_type, link_target, sort, status, start_at, end_at |
| `channel_entry` | 频道入口 | id, name, icon_url, link_target, sort, status |
| `floor` | 楼层 | id, type(new/hot_brand/category_aggregation/recommendation), title, sort, status |
| `floor_item` | 楼层商品/品牌 | id, floor_id, target_type, target_id, sort |
| `search_keyword_hot` | 热门搜索词 | id, keyword, sort, status |
| `search_synonym` | 同义词词典 | id, word, synonyms_json |
| `user_behavior_log` | 用户行为埋点 | id, user_id, action_type, target_id, context_json, created_at |

### 5.2 v1 预留表(建表不用,v2 启用)

| 表名 | 中文 | 用途 |
|---|---|---|
| `points_history` | 积分流水 | v2 积分系统 |
| `coupon` | 优惠券模板 | v2 |
| `user_coupon` | 用户优惠券 | v2 |
| `promotion` | 营销活动 | v2 满减/秒杀 |
| `seckill_activity` | 秒杀活动 | v2 |
| `notification` | 消息推送 | v2 接极光/腾讯推送 |
| `member_level_rule` | 会员等级规则 | v2 |
| `order_invoice` | 订单发票 | v2 接税务局接口后启用 |

### 5.3 关键索引

```sql
-- 搜索性能(v1 PG 全文索引)
CREATE INDEX idx_spu_name_zhparser ON spu USING gin(to_tsvector('chinese', name));
CREATE INDEX idx_spu_description_zhparser ON spu USING gin(to_tsvector('chinese', description_html));

-- 订单查询性能
CREATE INDEX idx_orders_user_status ON orders(user_id, status, created_at DESC);
CREATE INDEX idx_orders_status_created ON orders(status, created_at) WHERE status = 'PENDING_PAYMENT';

-- 库存防超卖(原子操作辅助)
CREATE INDEX idx_inventory_sku_stock ON inventory(sku_id) INCLUDE (stock);

-- 评价查询
CREATE INDEX idx_review_sku_created ON review(sku_id, created_at DESC);

-- 唯一索引(幂等防重)
CREATE UNIQUE INDEX uniq_payment_transaction ON payment_record(transaction_no);
CREATE UNIQUE INDEX uniq_favorite_user_spu ON favorite(user_id, spu_id);
```

### 5.4 扩展性骨架表(v1 渐进升级新增,详见 §十一)

| 表名 | 中文 | 用途 |
|---|---|---|
| `page_module` | 页面模块(楼层) | 替代固定 floor,后端配置模块类型 + 数据源,前端动态渲染 |
| `tab_config` | 底部 Tab 配置 | 主导航 Tab 后端可配,调整顺序/Icon 不发版 |
| `feature_flag` | 功能开关 | A/B 测试 + 灰度发布 |

> **关于老表**:`banner` / `channel_entry` / `floor` / `floor_item` 在 v1 启动期保留(老接口共存),后续渐进迁移到 `page_module`。

---

## 六、API 端点清单(概览)

> 完整 OpenAPI 规范在 `docs/api/openapi.yaml`(Swagger 自动生成 + 手工补全)。下方仅列主要端点。

### 6.1 鉴权 `/api/v1/auth`

```
POST    /auth/sms-code              发送验证码(限流:同手机号 60s 一次)
POST    /auth/login                 手机号+验证码登录(首次自动注册)
POST    /auth/login/wechat          微信登录
POST    /auth/login/alipay          支付宝登录
POST    /auth/login/apple           Apple 登录
POST    /auth/login/guest           测试游客(只在 spring.profiles=dev 开放)
POST    /auth/refresh               Refresh Token 换 Access Token
POST    /auth/logout
```

### 6.2 用户 `/api/v1/users`

```
GET     /users/me                   当前用户信息
PUT     /users/me                   改昵称/头像
PUT     /users/me/phone             换绑手机(走验证码)
PUT     /users/me/password
POST    /users/me/identity          实名认证
DELETE  /users/me                   注销账号
GET     /users/me/login-history     登录设备列表(v1 UI 占位,接口返回真实数据)
```

### 6.3 商品 / 分类 / 品牌

```
GET     /categories                          三级分类树
GET     /brands?category_id=                  品牌列表
GET     /brands/{id}                          品牌主页
GET     /brands/{id}/products                 品牌商品

GET     /products?q=&category_id=&brand_id=&min_price=&max_price=&sort=&page=&size=
                                              主搜索+筛选+分页
GET     /products/{spu_id}                    商品详情(含全部 SKU + 图片 + 详情HTML)
GET     /products/{sku_id}/inventory          库存实时查询(不走缓存)
```

### 6.4 搜索 `/api/v1/search`

```
GET     /search/suggest?q=                    输入联想
GET     /search/hot                            热门搜索词
GET     /search/history                       我的搜索历史
DELETE  /search/history                       清空我的搜索历史
```

### 6.5 首页 `/api/v1/home`

```
GET     /home                                  一次返回 banner+频道+楼层(聚合接口)
GET     /home/recommendation?page=             个性化推荐瀑布流
POST    /home/behavior                          用户行为埋点(浏览/点击/停留时长)
```

### 6.6 购物车 `/api/v1/cart`

```
GET     /cart                                 我的购物车
POST    /cart/items                           加入购物车
PUT     /cart/items/{id}                      改数量 / 改选中状态
DELETE  /cart/items/{id}
DELETE  /cart/items                            按 IDs 批量删
POST    /cart/merge                           游客本地购物车合并到云端(登录后)
```

### 6.7 地址 `/api/v1/addresses`

```
GET     /addresses
POST    /addresses
PUT     /addresses/{id}
DELETE  /addresses/{id}
PUT     /addresses/{id}/default                设为默认
POST    /addresses/parse                       NER 智能识别("张三 13800138000 北京...")
```

### 6.8 订单 `/api/v1/orders`

```
POST    /orders                               创建订单(预占库存)
GET     /orders?status=                        我的订单列表
GET     /orders/{id}                           订单详情
POST    /orders/{id}/cancel                    取消订单(回库)
POST    /orders/{id}/confirm                   确认收货
GET     /orders/{id}/logistics                 物流跟踪
```

### 6.9 支付 `/api/v1/payments`

```
POST    /payments/{order_id}/wechat            发起微信支付(返回预支付参数)
POST    /payments/{order_id}/alipay
POST    /payments/{order_id}/mock              开发期模拟支付(dev profile 开放)
POST    /payments/webhook/wechat               微信回调(签名验证)
POST    /payments/webhook/alipay               支付宝回调
GET     /payments/{order_id}/status            主动查询支付状态(前端用 + 定时任务用)
```

### 6.10 售后 `/api/v1/aftersales`

```
POST    /aftersales                            申请售后(退货/退款)
GET     /aftersales                            我的售后列表
GET     /aftersales/{id}
PUT     /aftersales/{id}/ship-back              用户填寄回单号
```

### 6.11 评价 `/api/v1/reviews`

```
POST    /reviews                              发表评价
GET     /reviews?sku_id=&filter=                商品评价列表(filter: all/with_image/bad)
GET     /reviews/me                            我的评价
POST    /reviews/{id}/append                   追评(30 天内可)
GET     /reviews/can-review?order_item_id=     检查是否可评
```

### 6.12 收藏 `/api/v1/favorites`

```
GET     /favorites
POST    /favorites?spu_id=
DELETE  /favorites/{spu_id}
```

### 6.13 文件 `/api/v1/files`

```
POST    /files/upload                          上传图片/视频(返回 OSS URL)
```

### 6.14 扩展性骨架接口(v1 新增,详见 §十一)

```
GET     /pages/{page_code}/modules               用户端拉页面模块(home/category_detail 等)
GET     /tabs                                     客户端启动拉 Tab 配置(缓存 24h)
GET     /feature-flags                            当前用户的 flag 状态
POST    /admin/pages/{page_code}/modules          运营 CRUD 页面模块
POST    /admin/tabs                               运营 CRUD Tab
POST    /admin/feature-flags                      运营 CRUD Feature Flag
```

### 6.15 运营后台 `/api/v1/admin` (v1 简化版)

```
POST    /admin/banners                         Banner CRUD
POST    /admin/channels                        频道入口 CRUD
POST    /admin/floors                          楼层 CRUD
POST    /admin/products                        商品 CRUD(创建 SPU/SKU/库存)
PUT     /admin/products/{id}/sale-price        改价(运营促销)
GET     /admin/orders                          所有订单查询
POST    /admin/orders/{id}/ship                发货(填快递单号)
POST    /admin/aftersales/{id}/approve         审核售后(同意/拒绝)
GET     /admin/refunds/pending-manual          人工兜底退款列表
POST    /admin/keywords                        热词管理
POST    /admin/synonyms                        同义词词典管理
```

> **管理后台访问控制**:`/api/v1/admin/**` 路径需要 `ROLE_ADMIN` 角色(`users.role` 字段),v1 通过 SQL 手工授予;v2 做后台 PC 端。

---

## 七、关键状态机

### 7.1 订单状态机

```
                ┌─────────────────┐
                │  PENDING_PAYMENT │ ← 用户提交订单(预占库存)
                │  (待付款)         │
                └────┬──────┬─────┘
                     │      │
            支付成功  │      │ 用户取消 / 15分钟超时
                     ↓      ↓
        ┌─────────────────┐ ┌─────────────────┐
        │  PENDING_SHIP    │ │  CANCELLED       │
        │  (待发货)         │ │  (已取消,回库)  │
        └────┬─────────────┘ └─────────────────┘
             │
   运营发货  │
             ↓
        ┌─────────────────┐
        │  SHIPPED         │ ← 售后:已发货状态可申请退货退款
        │  (待收货)         │
        └────┬──────┬─────┘
             │      │
   用户确认  │      │ 7 天自动确认
             ↓      ↓
        ┌─────────────────┐
        │  COMPLETED       │ ← 售后:30 天内仍可申请退款
        │  (已完成)         │
        └────┬─────────────┘
             │
    售后退款 │
             ↓
        ┌─────────────────┐
        │  REFUNDED        │
        │  (已退款)         │
        └─────────────────┘
```

**状态转换规则**(`OrderStateMachine.kt` 强制校验):
- `PENDING_PAYMENT` → `PENDING_SHIP` 或 `CANCELLED`
- `PENDING_SHIP` → `SHIPPED` 或 `REFUNDED`(未发货退款)
- `SHIPPED` → `COMPLETED` 或 `REFUNDED`(已发货退货退款)
- `COMPLETED` → `REFUNDED`(7-30 天内售后)
- 其他跳转一律抛 `IllegalStateTransition` 异常

### 7.2 售后状态机

```
APPLIED(已申请) → APPROVED(同意,等用户寄回)
              ↘  REJECTED(拒绝,结束)

APPROVED → SHIPPED_BACK(用户填寄回单号) → INSPECTED(平台验货通过) → REFUNDED(退款成功) | REFUND_FAILED(人工兜底)
                                       ↘ INSPECT_FAILED(验货失败,拒退,结束)
```

---

## 八、验收标准(用户能不能感知"做完了")

按 CLAUDE.md「开工协议」要求,列 5 条人话验收标准。每条都是"如果 X 发生,应该 Y"格式:

1. **完整购物路径走通**:游客打开 App → 浏览商品 → 加入购物车 → 触发登录(测试游客 / 手机号 / 微信)→ 选地址提交订单 → 调 `mock` 支付接口 → 看到订单进入「待发货」状态;运营在后台标"已发货"+ 填快递单号 → 用户在订单详情看到物流轨迹 → 7 天后系统自动确认收货 → 30 天内用户能提交「评分+文字+图片(9 张)+视频」评价
2. **库存防超卖**:100 个用户同时提交订单抢同一 SKU(库存 80),压力测试结果:成功订单数 = 80,失败 20 个返回明确"库存不足"提示,数据库 `inventory.stock` 最终为 0,无超卖
3. **搜索基本能力达标**:
   - 输入"鸡腿"能搜到名称含"鸡腿/鸡肉腿/鸡腿肉"的商品(分词)
   - 输入"运动鞋"能搜到名称只有"球鞋"的商品(同义词词典生效)
   - 没搜到结果时显示「猜你想找:[相关品类]」+ 用同义词重搜一次
   - 搜索结果支持「品牌 chip + 价格区间 + 三级分类」三种筛选
4. **订单状态机不出错**:
   - 同一订单不能从"已完成"反向变回"待付款"(测试用例)
   - 微信支付平台重复回调三次同一订单,数据库 `payment_record.transaction_no` 唯一索引拦截,订单状态只变更一次
   - 用户在 14 分 59 秒付款,系统 15:00 整去取消订单,取消任务先查 `payment_record` 表,发现已有支付记录则不取消
5. **可维护性达标**:
   - 16 个前端 feature 模块每个能独立编译
   - 任何 ViewModel 不直接调 Retrofit / Room,必须通过 domain 层的 Repository 接口
   - Flyway 版本号严格递增,从 V1 到当前不允许跳号
   - 后端 `domain/` 包不依赖 `api/` 包(单向依赖)

---

## 九、上线前待补清单(v1 不做,但 spec 标记)

| 模块 | 状态 | v2 计划 |
|---|---|---|
| **运营后台 PC 端** | v1 简化为 admin API + SQL 工具 | v2 用 React + Ant Design Pro 做独立后台 |
| **隐私政策 / 用户协议 / 个保法合规** | v1 缺位 | 上架前必补:隐私政策文案 / 用户协议 / SDK 备案 / 应用市场审核要求 |
| **优惠系统**(券/红包/满减/积分/VIP/秒杀) | DB 字段表预留,代码不写 | v2 启用 |
| **拼写纠错 + 拼音搜索** | v1 不做(PG 限制) | v2 切 ES 后补 |
| **属性筛选**(颜色/尺码) | v1 不做 | v2 做"属性聚合"接口 |
| **个性化推荐真算法** | v1 加权销量 fallback | v2 用积累的 3-6 月行为数据切协同过滤 |
| **消息推送**(系统/营销) | v1 不做,数据库表预留 | v2 接极光 / 腾讯信鸽 |
| **登录设备管理** | v1 UI 占位 | 上线前补真实实现(查 `user_login_log` 表) |
| **客服 IM** | v1 UI 占位 | 上线前接入微信客服 / 腾讯云 IM |
| **国际化 / 多币种** | 不做 | 未规划 |
| **监控告警**(APM/错误堆栈) | **v1 升级**(选 C 渐进路径) | 接 Sentry SaaS 免费版(详见 §十一.6);v2 进阶 SkyWalking / Datadog |
| **完整 CI/CD** | v1 手工构建 | v2 GitHub Actions |
| **扩展性骨架**(楼层组件化/Tab可配/Feature Flag/限流/CDN/APM) | **v1 已升级**(选 C 渐进路径新增) | 详见 §十一 |

---

## 十、范式选择原因 · 一句话总结

> **"按 Google 推的方式做,因为这是上架级项目;按 feature 拆模块,因为这样 AI 协作不打架;前后端都 Kotlin,因为这样上下文一致;数据库用 Flyway 版本化,因为上线后改表不能丢用户。"**

具体每项决策的理由已在 §2.2 表格中给出。

---

## 十一、扩展性骨架(v1 新增 · 渐进升级 C 路径)

> **目标**:让"加运营位 / 加 Tab / A/B 测试"不发版;让限流/CDN/APM 这些基础能力 v1 就到位。

### 11.1 楼层组件注册机制(替代固定 4 楼层)

**问题**:原 `floor` 表只有 4 个固定类型,加新类型(图文导购/短视频/直播位)必须改代码。

**升级方案**:**页面 = 模块列表**;后端配置组件类型 + 数据源,前端按 `type` 动态渲染。**4 种现有楼层降级为 4 种 `module_type`,不再是独立表**。

**数据表**:
```sql
CREATE TABLE page_module (
    id BIGSERIAL PRIMARY KEY,
    page_code VARCHAR(50) NOT NULL,        -- 'home' / 'category_detail' / 'profile' 等
    module_type VARCHAR(50) NOT NULL,      -- 'banner' / 'channel_grid' / 'product_grid' /
                                           --  'image_text' / 'video_feed' / 'live' / 'recommendation_feed'
    title VARCHAR(100),
    sort INT NOT NULL DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ENABLED',
    data_source_type VARCHAR(50),          -- 'manual' / 'category' / 'tag' / 'algorithm'
    data_source_config JSONB,              -- {"category_id": 123} / {"tag": "new"} / {"algorithm": "weighted_sales"}
    ui_config JSONB,                       -- {"columns": 2, "show_price": true, "background_color": "#FF0000"}
    valid_from TIMESTAMP,
    valid_to TIMESTAMP,
    created_at TIMESTAMP DEFAULT now()
);
CREATE INDEX idx_page_module_active ON page_module(page_code, status, sort);
```

**API**:
```
GET   /api/v1/pages/{page_code}/modules        用户端:返回页面所有启用模块,按 sort 排序
POST  /api/v1/admin/pages/{page_code}/modules   运营端:CRUD
```

**前端注册机制**(`core/ui/PageModuleRegistry.kt`):
```kotlin
object PageModuleRegistry {
    private val renderers = mapOf<String, @Composable (PageModule) -> Unit>(
        "banner"              to { BannerCarousel(it) },
        "channel_grid"        to { ChannelGrid(it) },
        "product_grid"        to { ProductGrid(it) },
        "image_text"          to { ImageTextSection(it) },
        "video_feed"          to { VideoFeed(it) },
        "recommendation_feed" to { RecommendationFeed(it) },
        // 加新模块类型,只在此注册一行 + 实现 Composable
    )

    @Composable
    fun render(module: PageModule) {
        renderers[module.moduleType]?.invoke(module)
            ?: UnsupportedModulePlaceholder(module.moduleType)  // 旧版本 App 未注册的类型自动降级隐藏
    }
}
```

**老表怎么办**:
- 原 `banner` / `channel_entry` / `floor` / `floor_item` 表:**v1 启动期共存**,内部 `BannerService` / `ChannelService` / `FloorService` 改为读 `page_module`(以 type=banner/channel_grid 等过滤)
- 渐进迁移:v1.1 切完后老表退役

---

### 11.2 Tab 后端可配(替代硬编码)

**问题**:主导航 Tab 写死,加 Tab 必发版。

**数据表**:
```sql
CREATE TABLE tab_config (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,        -- 'home' / 'category' / 'cart' / 'profile'
    name VARCHAR(50) NOT NULL,
    icon_url VARCHAR(255) NOT NULL,
    icon_selected_url VARCHAR(255) NOT NULL,
    route_path VARCHAR(100) NOT NULL,        -- 客户端 NavGraph 路由
    sort INT NOT NULL,
    status VARCHAR(20) DEFAULT 'ENABLED',
    min_app_version VARCHAR(20),             -- 如 "1.2.0",App 版本 < 此值则隐藏(避免 crash)
    badge_strategy VARCHAR(50),              -- 'unread_message' / 'cart_count' / null
    created_at TIMESTAMP DEFAULT now()
);
```

**API**:`GET /api/v1/tabs`(客户端启动时调,缓存 24 小时到 DataStore)

**机制**:
- 客户端启动拉取 → `MainActivity` 根据 Tab 列表动态构建 `NavigationBar`
- **调整 Tab 数量/顺序/Icon 不发版**;新增 Tab 仍需客户端预实现对应 `route_path` 的页面(还是发版,但代码已存在)
- 旧版本 App 通过 `min_app_version` 自动隐藏

---

### 11.3 Feature Flag(A/B 测试 + 灰度发布)

**数据表**:
```sql
CREATE TABLE feature_flag (
    id BIGSERIAL PRIMARY KEY,
    flag_key VARCHAR(100) UNIQUE NOT NULL,    -- 'new_payment_flow' / 'home_v2_layout'
    enabled BOOLEAN DEFAULT false,            -- 全局开关
    rollout_percentage INT DEFAULT 0,         -- 0-100,按 user_id hash 灰度
    user_whitelist JSONB,                     -- 强制开通用户 [123, 456]
    user_blacklist JSONB,                     -- 强制关闭用户
    description TEXT,
    updated_at TIMESTAMP DEFAULT now()
);
```

**API**:
```
GET   /api/v1/feature-flags                  返回当前用户的 flag 状态映射(带 JWT)
POST  /api/v1/admin/feature-flags            运营 CRUD
```

**前端使用**:
```kotlin
if (FeatureFlags.isEnabled("new_payment_flow")) {
    NewPaymentScreen()
} else {
    OldPaymentScreen()
}
```

---

### 11.4 限流(防爬虫 / 防恶意刷)

**实施**:
- 全局拦截:`HandlerInterceptor` + Redis 滑动窗口
- 注解级:`@RateLimit(key = "ip", limit = 60, period = "1m")`
- 依赖:`spring-boot-starter-data-redis` + 自研 `RateLimitAspect`

**默认配置**:

| 接口 | 限流策略 |
|---|---|
| `POST /auth/sms-code` | 单手机号 60s/次 + 单 IP 10/分钟 |
| `POST /orders` | 单用户 5 次/分钟 |
| `POST /payments/*` | 单订单并发 1(Redis 分布式锁) |
| `GET /search` | 单 IP 30/分钟 + 单用户 100/分钟 |
| `POST /reviews` | 单用户 1 次/订单(数据库 UNIQUE) |
| `POST /files/upload` | 单用户 20/分钟 |

---

### 11.5 CDN

**实施**:OSS + 云 CDN(阿里云 OSS 一键开启 CDN 加速,或七牛云内置)。

**改动**:
- `application.yml` 加 `oss.cdn-domain: https://cdn.mall.com`
- 文件上传后返回 URL 时用 `cdn-domain` 替换 OSS 域名
- 图片处理:`?x-oss-process=image/resize,w_750`(OSS 自带图片处理)
- 视频:CDN 自带边缘节点

---

### 11.6 APM 监控

**v1 接入 Sentry**(SaaS 免费版,5000 events/月够初期用):

- **前端**:`io.sentry:sentry-android:7.x`,捕获 crash + ANR + 慢页面
- **后端**:`sentry-spring-boot-starter:7.x`,捕获 unhandled exception + 慢接口(>1s 告警)
- **后台告警**:重大错误 / P99 飙升 / 5xx 比例升高 → Sentry 邮件 / Slack 通知

**v2 进阶**:SkyWalking(自建,适合大规模)或 Datadog(付费 SaaS,运维省心)。

---

## 十二、大规模性能升级路线(v2 · 触发后启动)

> **触发条件**:任意一项达成 → 启动对应升级
> - DAU 持续 > 5 万
> - 数据库 P99 > 200ms
> - Redis 内存 > 80%
> - 订单量 > 10 万/天
> - 搜索 P99 > 500ms

### 12.1 搜索引擎:PG → Elasticsearch

**触发**:SKU > 5 万 或 搜索 P99 > 500ms

**步骤**(v1 已经预留了 `SearchService` 接口和 `ElasticsearchSearchService` 占位):
1. 部署 ES 集群(自建 8GB×3 或云托管 ¥600/月起)
2. 实现 `ElasticsearchSearchService`(继承现有接口,业务代码不动)
3. Spring 切换 `@Primary` binding
4. 通过 Canal(MySQL binlog) 或 Debezium(PG WAL) 增量同步商品数据到 ES
5. 补 v1 没做的:**拼写纠错**(`fuzzy` query) + **拼音搜索**(拼音字段)

---

### 12.2 消息队列:解耦订单链路

**问题**:订单创建 → 减库存 → 写支付记录 → 物流通知,同步链路慢。

**升级**:引入 Kafka 或 RabbitMQ(中小规模 RabbitMQ 即可)
- 订单创建 → 发"OrderCreated"事件
- 库存服务监听 → 异步减库存(失败回滚)
- 支付服务监听 → 准备支付
- 推送服务监听 → 通知用户

---

### 12.3 数据库主从读写分离 + 分库分表

**主从分离**:1 主 N 从,Spring Data JPA + `AbstractRoutingDataSource` 切流量。

**分库分表**(订单表):订单累计 > 5000 万触发,ShardingSphere-JDBC 按 `user_id % N` 分库 + 按月分表。

---

### 12.4 Redis Cluster

Redis 单实例 → 6 节点 Cluster(3 主 3 从)。Spring Data Redis 配置无缝切换。

---

### 12.5 库存查询多级缓存

**问题**:`GET /products/{id}/inventory` 每次走数据库,高 QPS 热点。

**升级**:
- L1:Caffeine 本地缓存(JVM 内,30s)
- L2:Redis 分布式缓存(5min)
- 数据库:权威源
- 库存变更时 Redis Pub/Sub 广播,各节点失效本地缓存

---

### 12.6 首页接口预聚合到 Redis

**问题**:`GET /home` 组合 banner+频道+楼层+模块,数据库压力。

**升级**:
- 运营后台变更模块时,**主动刷** Redis 缓存 `cache:home:v1`(整页 JSON)
- 用户请求直接读 Redis
- 1 小时兜底失效

---

### 12.7 支付主动轮询改延时队列

**问题**:每分钟扫所有"待支付"订单,数据库杀手。

**升级**:
- 订单创建时投递 Redis Delayed Queue(15 分钟到期)
- 到期检查:已付不处理,未付主动查询支付平台 + 取消
- O(1) 投递,不扫表

---

### 12.8 CI/CD + 灰度发布

- GitHub Actions / GitLab CI 流水线
- Docker 容器化
- K8s 编排(滚动更新 + 金丝雀)
- 灰度策略:按用户百分比 + 灰度环境

---

## 附录 A:整体技术栈一图

```
┌──────────────────────────────────────────────────────────┐
│ 安卓客户端 (Kotlin + Jetpack Compose)                      │
│ Clean Architecture: presentation → domain → data           │
│ Hilt DI · Navigation Compose · Coil · Room · DataStore     │
└────────────────────────────┬─────────────────────────────┘
                             │ HTTPS + REST + JSON
                             │ JWT Bearer Token
                             ↓
┌──────────────────────────────────────────────────────────┐
│ 后端 (Spring Boot 3 + Kotlin)                              │
│ DDD: config / common / domain / api / infrastructure / job │
│ Spring Security + JWT · Spring Data JPA · MapStruct        │
└─────┬────────────┬────────────────┬───────────────────────┘
      │            │                │
      ↓            ↓                ↓
┌──────────┐ ┌──────────┐ ┌────────────────────────────────┐
│ PostgreSQL│ │  Redis   │ │ 外部服务                          │
│ + zhparser│ │  缓存/限流│ │ 短信 / NER / 物流 / OSS / 支付    │
│ Flyway 版本│ │          │ │                                  │
└──────────┘ └──────────┘ └────────────────────────────────┘
```

---

## 附录 B:文档历史

| 日期 | 版本 | 变更 |
|---|---|---|
| 2026-05-22 | v0.1 | 初稿,13 轮业务调研后产出 |
| 2026-05-22 | v0.2 | 用户提"大规模用户群 + 业务扩展性"质疑后,选 C 渐进升级路径 → 新增 §十一 扩展性骨架(楼层组件注册 / Tab 后端可配 / Feature Flag / 限流 / CDN / APM)+ §十二 大规模性能升级路线(ES / MQ / 主从 / Redis Cluster / 多级缓存 / 预聚合 / 延时队列 / CI-CD) |

---

**END · 等用户审核 · 审核通过后调用 `superpowers:writing-plans` 写实施计划**
