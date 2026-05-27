# Session 11 · Final Integration(终极联调 + 部署)

> 把车机端、后台、服务端全部串起来,跑端到端,APK 出货,部署上线。Demo 的"完成"。

---

## 1. 你要做什么

### 1.1 端到端联调(必须真后端 + 真车机端)

按这个顺序跑一次完整体验,**找到任何不对的地方修掉**:

#### A. 业务流程场景(基础)

| # | 体验路径 | 期望 |
|---|---|---|
| 1 | **运营准备数据**:打开后台公网 URL,登录,创建 1 个新分类、上传 3 张图、创建 5 个商品(含实物 + 服务券)、配 2 个 banner | 数据落库,在前台可看到 |
| 2 | **车机端启动**:装好 APK 的车机/平板,打开 App | 首页能看到运营刚配的 banner、5 个分类、5 个商品 |
| 3 | 点 banner(指向商品) | 跳详情 |
| 4 | 详情 → 立即购买 → 提交订单 → 模拟支付 | 看到"支付成功"动画,跳订单详情 |
| 5 | 我的订单 | 这单在列表里 |
| 6 | 后台看订单列表 | 这单在后台也能看到,device_id 后 6 位显示 |
| 7 | 后台下架一个商品 | 车机端商品列表不显示这个,详情仍能进但"立即购买" disabled |
| 8 | 后台删一个 banner | 车机端首页 banner 减少 |
| 9 | 重启车机端 | 订单依旧在,数据依旧从后端拉 |
| 10 | **跨设备**:用另一台车机/模拟器装 APK | 看不到第一台的订单(device_id 隔离) |

#### B. 强化场景(USER_STORIES §六 ⭐ 必跑)

> 2026-05-26 加的硬门槛。02~10 session 时这些 stories **未被旧 session 覆盖**(规范升级后才加),11 必须**统一兜底**。

| Story | 一句话 | 跑通的判据 |
|---|---|---|
| **US-14** XSS 安全 | title 含 `<script>` 不执行 | 后台列表 / 车机详情字面显示,无弹窗 |
| **US-15** path traversal | 上传文件名 `../../etc/passwd.png` | 服务器 only 写入 `static/uploads/<uuid>.png`,无残留 |
| **US-16** 暴力破解 | 错密码 10 次 | 当前预计**不通过**,记 TD-014,**禁止上生产** |
| **US-17** 并发下单 | 同设备并发 5 次 POST /orders | 5 个唯一订单号,无碰撞 |
| **US-18** 并发支付幂等 | 同订单并发 5 次 mock_pay | 5 次 code=0,paid_at 全部一致 |
| **US-19** 删商品+订单 | 删商品时有 pending 订单 | OrderItem.product_id=NULL,snapshot 完整 |
| **US-20** 改价并发 | 用户看到 ¥1.00 时运营改成 ¥9.99 | 订单 total_amount 跟 snapshot.price 一致 |
| **US-21** quantity 边界 | quantity=0 / -1 | 返 code=1000 |
| **US-22** price 边界 | price=0 / -100 / 2147483647 | 返 code=1000 或限制 |
| **US-23** 大分页参数 | page=99999 / page_size=10000 | 不崩,明确返回 |
| **US-24** 超长字符串 | title=1000 字符 | UI 不崩,或服务端拒(看实现)|
| **US-25** banner SSRF | image_url=`http://169.254.169.254/` | 只存字符串,服务端不主动 fetch |

**Then 段证据**:逐 story 在 artifact 里列 curl 输出 / 截图 / DB 状态。**不允许"应该能跑"敷衍。**

#### C. 端到端集成 USER_STORIES

| Story | 一句话 |
|---|---|
| **US-12** | 后台改商品,车机端立刻看到 |
| **US-13** | 车机下单,后台立刻看到 |

### 1.2 修 bug

每跑一个场景出现问题:
1. 记录到 `artifacts/11-final-integration.md`
2. 判断改哪个层(服务端 / 后台 / 车机端 / SPEC)
3. 修完 → 重跑场景
4. 不允许"先记着不修"

如果发现 SPEC 不对 → 改 SPEC + 改对应代码 + 重新跑相关场景

### 1.3 APK 打包

```bash
cd carshop-android
./gradlew assembleRelease
# 产出 app/build/outputs/apk/release/app-release.apk
```

需要:
- 给一个 debug keystore 即可(Demo,不需要正式签名)
- BuildConfig 里 USE_MOCK=false、BASE_URL 指向公网
- 文件大小 < 20 MB 为佳(图片不要打进去)

把 APK 上传到 Mac mini 的某个静态目录,提供 HTTPS 下载链接(沿用 add_static_site.sh 模式,新建 `carshop-download.hearagain.space` 或者放到现有静态站子路径)。

### 1.4 录屏 / 截图

录一段 1~2 分钟的车机端使用视频(MP4),展示完整流程。保存到 `artifacts/11-final-integration/demo.mp4`。

### 1.5 写交付清单

`artifacts/11-final-integration.md` 必须包含:

- **公网 URL**:
  - 服务端 API:`https://<sub>.hearagain.space/api/v1`
  - 后台 Admin Web:`https://<sub>-admin.hearagain.space`(或本地)
  - APK 下载:`https://<sub>-download.hearagain.space/carshop-release.apk`
- **管理员账号**:admin / admin123
- **测试数据**:列出已建的分类 / 商品 / banner
- **端到端跑过的场景清单**(逐项打勾)
- **已知问题**(必须诚实列出,如:某分辨率下错位、某机型上图片裂等)
- **未做的 / 后续模块**:M-Address、购物车、券核销
- **如何继续开发**:点出 SPEC.md / sessions/ 目录给后来人

### 1.6 更新 STATUS.md

把所有 session 状态推到 ✅ 已完成,在"项目当前阶段"写"**MVP 已交付**"。

---

## 2. 你不要做什么

- ❌ 加新功能(本 session 只做联调和部署)
- ❌ 大重构(发现的问题最小化修复)
- ❌ 跳过任何验收场景
- ❌ 假装"完成"——必须真跑过
- ❌ 把 release keystore 提交到 git(用 .gitignore)
- ❌ 在 release APK 里留 debug log / mock 数据

---

## 3. 输入

- `CLAUDE.md`、`SPEC.md`、`STATUS.md`
- **全部前序 session 的产物**:01~10
- **全部 artifacts**
- 用户的 CLAUDE.md 部署相关章节

---

## 4. 输出

- 端到端通的 Demo(真实可用)
- `carshop-release.apk`
- `artifacts/11-final-integration.md`(交付清单)
- `artifacts/11-final-integration/demo.mp4` 或截图集
- 更新后的 STATUS.md
- 已修复的 bug 清单(每条:发现的 → 修在哪 → 重跑验证)

---

## 5. 验收

1. ✅ 1.1 表里 10 个场景**全部**跑过且通过
2. ✅ APK 装到一台陌生车机/平板上,从 0 到完成一笔订单,**用户什么都不用配**
3. ✅ 后台所有 CRUD 都能在公网真跑
4. ✅ 重启 Mac mini,所有服务自动起,公网仍可用(launchd 验证)
5. ✅ artifacts/11-final-integration.md 交付清单完整
6. ✅ STATUS.md 全部 ✅
7. ✅ 用户能用一个**陌生 Claude Code session**,只读 SPEC + STATUS + sessions/ + artifacts 就能理解项目状态(模拟下:让 Claude 描述项目当前阶段,描述对就过)

---

## 6. 依赖

- **上游**:全部(01~10)
- **下游**:M-Address、购物车、券核销等后续独立模块(本 session 不涉及)

## 7. Mock 策略

**不允许**。本 session 必须真后端、真车机、真公网。

## 8. 已知坑

1. **APK 签名**:debug keystore 装得上,但 release 需要自己生成一个 keystore(密码记好,以后更新 APK 要用)
2. **公网 HTTPS + 明文 HTTP**:release APK 别开 `usesCleartextTraffic`,公网就是 HTTPS
3. **Mac mini 重启 → 服务起不来**:重点验 launchd plist 的 `RunAtLoad=true`、`KeepAlive=true`
4. **图片域名不对**:确保 `BASE_URL` 是公网,不是 localhost(否则车机端图片裂)
5. **新车机端 device_id 跟测试机不同**:正常,Demo 不解决跨设备同步
6. **录屏分辨率**:车机一般 1920×1080,横屏录屏不要走默认手机竖屏
7. **APK 下载页**:用 add_static_site.sh 模式部署一个简单的下载页,放个 "下载 APK" 按钮即可
8. **整个项目交付物清单**:用户最后想看到什么——一个能装的 APK + 一个能进的后台 + 一份能复现项目状态的文档。除此之外都是花活
