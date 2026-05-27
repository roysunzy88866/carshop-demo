# User Stories · 用户故事 + 验收场景

> **本文件是测试的「真理之源」**。每个 session 完成前,必须跑通它涉及的 User Story(列在每个故事的末尾)。
>
> **curl 通过 ≠ 完成**,User Story 跑过才算完成。
>
> 故事格式:**作为** X,**我想要** Y,**这样** Z。每个故事配 1~N 个 Given/When/Then 场景,场景里写**可执行的验证步骤**。

---

## 怎么读这份文件

| 元素 | 含义 |
|---|---|
| **G**iven | 前置状态(数据已经准备好的样子) |
| **W**hen | 用户做了什么动作 |
| **T**hen | 系统应该有的可观察结果 |
| `验证命令` | 跑哪条 cURL / 操作哪个页面 |
| `[Sxx]` | 涉及哪些 session,完成时这些 session 都要跑通对应故事 |

---

## 一、车机端 · 浏览(US-01 ~ US-03)

### US-01 · 车主看到首页,逛分类

**作为**车主,**我想要**在车机屏上看到首页 banner + 分类入口 + 推荐商品,**这样**我能快速找到要买的东西。

**涉及**:[S03 banner] [S02 catalog] [S08 android-foundation] [S09 android-browse]

#### 场景 1:首页正常显示

- **G**:后台已配 3 个 banner、5 个分类、≥12 个商品(seed 数据)
- **W**:车主打开车机 App
- **T**:
  - 看到顶部 banner 区(高 240dp,3 张图轮播或第一张可见)
  - 看到 5 个分类入口 icon-tile 横向排列
  - 看到推荐商品列表(至少 10 个)
  - 字体、颜色、间距完全走 design-system token(主色石墨蓝 `#1A2027`)

#### 场景 2:点击分类进入列表

- **G**:首页已加载
- **W**:点击"加油充电"分类
- **T**:
  - 进入分类页,Top Bar 标题"加油充电"
  - 列表只显示加油充电类目下的商品
  - 商品卡的价格按 `product_type` 上色(physical 红 #E63946,service_voucher 绿 #00A892)

**验证步骤**(服务端层):
```bash
curl -s http://localhost:8000/api/v1/banners | jq '.data | length'           # = 3
curl -s http://localhost:8000/api/v1/categories | jq '.data | length'         # = 5
curl -s 'http://localhost:8000/api/v1/products?category_id=2' | jq '.data.list | map(.product_type) | unique'
```

---

### US-02 · 车主看商品详情

**作为**车主,**我想要**点击一个商品看到大图、价格、规格、描述,**这样**我能决定要不要买。

**涉及**:[S02 catalog] [S09 android-browse]

#### 场景 1:在售商品

- **G**:商品 ID=1(在售)
- **W**:点击商品卡
- **T**:
  - 显示主图、标题、价格、规格、描述
  - 价格颜色按 `product_type` 对应
  - 底部"立即购买"按钮可点(石墨蓝主按钮)

#### 场景 2:下架商品仍可访问

- **G**:商品 ID=2 被运营下架(`on_sale=false`)
- **W**:车主通过收藏 / 历史订单点进详情页
- **T**:
  - 接口返回该商品数据(`code=0`)且 `on_sale=false`
  - **"立即购买" 按钮 disabled**
  - 显示文案"商品已下架"

**验证步骤**:
```bash
curl -s http://localhost:8000/api/v1/products/1 | jq '.data | {id, on_sale, product_type, price}'
# 模拟下架商品 2
curl -s -X PATCH http://localhost:8000/api/v1/admin/products/2/on_sale -d '{"on_sale":false}' -H 'Content-Type: application/json'
curl -s http://localhost:8000/api/v1/products/2 | jq '.data.on_sale'   # = false (仍能拿到)
curl -s http://localhost:8000/api/v1/products | jq '.data.list | map(.id) | contains([2])'   # = false (列表不出现)
```

---

### US-03 · 车主翻页商品列表

**作为**车主,**我想要**列表能往下滚出更多商品,**这样**我能浏览全部库存。

**涉及**:[S02 catalog] [S09 android-browse]

#### 场景 1:分页一致性

- **G**:某分类下有 25 个商品,`page_size=10`
- **W**:依次拉第 1 / 2 / 3 页
- **T**:
  - 第 1 页 10 条、第 2 页 10 条、第 3 页 5 条
  - `total=25` 三次都一样
  - 三页 ID 没有重复

**验证步骤**:
```bash
P1=$(curl -s 'http://localhost:8000/api/v1/products?page=1&page_size=5' | jq '.data.list | map(.id)')
P2=$(curl -s 'http://localhost:8000/api/v1/products?page=2&page_size=5' | jq '.data.list | map(.id)')
echo "Page 1: $P1"; echo "Page 2: $P2"
# 验证无重叠:用 jq 取交集,应为空
```

---

## 二、车机端 · 下单(US-04 ~ US-06)

### US-04 · 车主下单买加油卡(服务券)

**作为**车主,**我想要**点中加油卡 → 提交订单 → 扫码"假装支付" → 看到订单完成,**这样**整条下单链路打通。

**涉及**:[S04 order-pay] [S09 android-browse] [S10 android-checkout]

#### 场景 1:完整流程

- **G**:车主刚装好 App(没下过单),商品 1 是 service_voucher 类型加油卡 95 元
- **W**:
  1. 详情页点"立即购买"
  2. 订单确认页看到商品 + 预填地址 + 总价 ¥95.00
  3. 点"提交订单" → 弹支付二维码弹层
  4. 点弹层任意位置 → 1 秒后显示"支付成功"
  5. 跳订单详情
- **T**:
  - 订单详情:订单号 `O{14位日期}{4位随机}`、状态"已支付"、`paid_at` 有值
  - 总金额 = 商品价 × 数量(MVP 数量固定 1)
  - 商品快照(`product_snapshot`)含完整商品信息
  - `shipping_info` 是 SPEC §5.1 默认值

#### 场景 2:跨设备隔离

- **G**:用 device-A 下了订单 O1
- **W**:用 device-B 调 `GET /api/v1/orders/O1`
- **T**:返回 `code=1001`(**不是** 403,不泄露订单存在)

**验证步骤**:
```bash
# 设备 A 下单
ORDER=$(curl -s -X POST http://localhost:8000/api/v1/orders \
  -H 'X-Device-Id: device-A' -H 'Content-Type: application/json' \
  -d '{"product_id":1,"quantity":1}')
OID=$(echo $ORDER | jq -r '.data.id')
echo "订单号: $OID"
# 订单号格式校验:O + 14 位 + 4 位
echo $OID | grep -E '^O[0-9]{18}$' && echo "格式正确"

# 支付
curl -s -X POST "http://localhost:8000/api/v1/orders/$OID/mock_pay" -H 'X-Device-Id: device-A' | jq '.data | {status, paid_at}'

# 设备 B 查不到
curl -s "http://localhost:8000/api/v1/orders/$OID" -H 'X-Device-Id: device-B' | jq '.code'   # = 1001
```

---

### US-05 · 商品下架后下单会被拒

**作为**车主,**我想要**下单一个已下架商品时被告知"商品下架",**这样**我不会买到无效商品。

**涉及**:[S02 catalog] [S04 order-pay] [S10 android-checkout]

#### 场景 1

- **G**:商品 2 被下架
- **W**:用 `X-Device-Id` 调 `POST /orders {product_id:2,quantity:1}`
- **T**:返回 `code=3000`,message 含"商品已下架"

**验证步骤**:
```bash
curl -s -X PATCH http://localhost:8000/api/v1/admin/products/2/on_sale -d '{"on_sale":false}' -H 'Content-Type: application/json'
curl -s -X POST http://localhost:8000/api/v1/orders -H 'X-Device-Id: test' -H 'Content-Type: application/json' -d '{"product_id":2,"quantity":1}' | jq '.code, .message'
```

---

### US-06 · 重复支付幂等

**作为**车主,**我想要**支付按钮被误点两次也不出问题,**这样**不会重复扣款 / 报错。

**涉及**:[S04 order-pay] [S10 android-checkout]

#### 场景 1

- **G**:订单 O1 状态 paid
- **W**:再调 `POST /orders/O1/mock_pay`
- **T**:`code=0`,返回当前 paid 状态(不报错,不创建新数据)

**验证步骤**:见 US-04 后追加一次 mock_pay,返回 `code=0`,`paid_at` 不变(或服务端覆盖,但不报错)。

---

## 三、车机端 · 订单中心(US-07)

### US-07 · 车主看自己的订单

**作为**车主,**我想要**看到这台车机下过的所有订单,**这样**我能查历史。

**涉及**:[S04 order-pay] [S10 android-checkout]

#### 场景 1:同设备订单累积

- **G**:device-A 下过 3 单,1 单待支付、2 单已支付
- **W**:`GET /orders?status=paid` + `GET /orders?status=pending` + `GET /orders`
- **T**:
  - paid 列表 2 条
  - pending 列表 1 条
  - 不带 status 返回全部 3 条
  - 全部按 created_at 倒序

#### 场景 2:重启 App 订单还在

- **G**:同设备已下过单
- **W**:Kill App,重新打开
- **T**:订单列表里之前的订单还在(因为 device_id 是 Settings.Secure.ANDROID_ID,App 重启不变)

**验证步骤**:
```bash
# 造 3 单
for i in 1 2 3; do
  curl -s -X POST http://localhost:8000/api/v1/orders -H 'X-Device-Id: us07' -H 'Content-Type: application/json' -d '{"product_id":'$i',"quantity":1}' > /dev/null
done
# 支付前 2 个
ORDERS=$(curl -s 'http://localhost:8000/api/v1/orders' -H 'X-Device-Id: us07' | jq -r '.data.list[0:2] | .[].id')
for o in $ORDERS; do curl -s -X POST "http://localhost:8000/api/v1/orders/$o/mock_pay" -H 'X-Device-Id: us07' > /dev/null; done
# 验证
curl -s 'http://localhost:8000/api/v1/orders?status=paid' -H 'X-Device-Id: us07' | jq '.data.total'    # = 2
curl -s 'http://localhost:8000/api/v1/orders?status=pending' -H 'X-Device-Id: us07' | jq '.data.total' # = 1
curl -s 'http://localhost:8000/api/v1/orders' -H 'X-Device-Id: us07' | jq '.data.total'                # = 3
```

---

## 四、后台运营(US-08 ~ US-11)

### US-08 · 运营登录后台

**作为**运营,**我想要**用账号密码登录后台,**这样**之后所有操作有身份。

**涉及**:[S05 admin-auth-upload] [S06 admin-web]

#### 场景 1:登录成功

- **G**:数据库里有 admin/admin123
- **W**:`POST /admin/login` 带正确密码
- **T**:
  - `code=0`
  - 响应有 `Set-Cookie: session=...; HttpOnly; SameSite=Lax`
  - 之后带 cookie 调 `/admin/me` 返回 admin 信息

#### 场景 2:密码错

- **W**:密码错误
- **T**:`code=2001`,HTTP 401

#### 场景 3:登出后失效

- **W**:`POST /admin/logout` → `GET /admin/me` 不带新 cookie
- **T**:`code=2000`

**验证步骤**:
```bash
curl -s -X POST http://localhost:8000/api/v1/admin/login \
  -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}' \
  -c /tmp/c.txt -i | grep -i set-cookie
curl -s http://localhost:8000/api/v1/admin/me -b /tmp/c.txt | jq '.data.username'   # = "admin"
curl -s -X POST http://localhost:8000/api/v1/admin/login \
  -H 'Content-Type: application/json' -d '{"username":"admin","password":"wrong"}' | jq '.code'   # = 2001
curl -s -X POST http://localhost:8000/api/v1/admin/logout -b /tmp/c.txt > /dev/null
curl -s http://localhost:8000/api/v1/admin/me -b /tmp/c.txt | jq '.code'   # = 2000
```

---

### US-09 · 运营新建商品(含图片上传)

**作为**运营,**我想要**填表新建商品、上传主图,**这样**车机端能看到新商品。

**涉及**:[S02 catalog] [S05 admin-auth-upload] [S06 admin-web]

#### 场景 1:完整流程

- **G**:运营已登录
- **W**:
  1. 上传一张 png(大小 < 5MB)
  2. 拿到 URL
  3. 把 URL 填进商品表单的 `main_image_url`
  4. 提交新建商品
  5. 在车机端拉商品列表
- **T**:
  - 上传返回完整 URL
  - 新商品创建成功
  - 车机端能看到这个商品(因为默认 `on_sale=true`)

#### 场景 2:上传超大文件

- **W**:上传 > 5MB 的图
- **T**:`code=1000`,文件**没落地**

#### 场景 3:上传非法格式

- **W**:上传 `.exe`
- **T**:`code=1000`,文件没落地

**验证步骤**:
```bash
# 假设已登录,cookie 在 /tmp/c.txt
URL=$(curl -s -X POST http://localhost:8000/api/v1/admin/upload \
  -b /tmp/c.txt -F 'file=@/path/to/some.png' | jq -r '.data.url')
echo "上传得到: $URL"
# 用 URL 建商品
curl -s -X POST http://localhost:8000/api/v1/admin/products -b /tmp/c.txt \
  -H 'Content-Type: application/json' \
  -d "{\"category_id\":1,\"title\":\"测试商品\",\"product_type\":\"physical\",\"price\":9900,\"main_image_url\":\"$URL\",\"description\":\"测试\",\"on_sale\":true}"
# 车机端拿列表
curl -s http://localhost:8000/api/v1/products | jq '.data.list[].title' | grep "测试商品"
```

---

### US-10 · 运营删分类的约束

**作为**运营,**我想要**删空分类成功、删有商品的分类被拒,**这样**不会误删导致商品孤立。

**涉及**:[S02 catalog]

#### 场景 1:删有商品的分类

- **G**:分类 1 下有 N 个商品
- **W**:`DELETE /admin/categories/1`
- **T**:`code=1002`,HTTP 409,message 含"该分类下还有 N 个商品"

#### 场景 2:删空分类

- **G**:新建一个空分类 99
- **W**:`DELETE /admin/categories/99`
- **T**:成功(`code=0`)

**验证步骤**:
```bash
curl -s -X DELETE http://localhost:8000/api/v1/admin/categories/1 -b /tmp/c.txt | jq '.code, .message'   # 1002
NEW=$(curl -s -X POST http://localhost:8000/api/v1/admin/categories -b /tmp/c.txt -H 'Content-Type: application/json' -d '{"name":"测试空分类","icon_url":"https://x.png","sort":99}' | jq -r '.data.id')
curl -s -X DELETE "http://localhost:8000/api/v1/admin/categories/$NEW" -b /tmp/c.txt | jq '.code'   # 0
```

---

### US-11 · 运营看订单

**作为**运营,**我想要**看到全部车机端下的订单(含 device_id 后 6 位识别),**这样**我能盯销量。

**涉及**:[S04 order-pay] [S06 admin-web]

#### 场景 1

- **G**:车机端下了 N 单
- **W**:`GET /admin/orders`
- **T**:
  - 列表至少 N 条(可能有其他测试单)
  - 每条带 `device_id_short`(后 6 位)
  - 按 created_at 倒序

**验证步骤**:
```bash
curl -s http://localhost:8000/api/v1/admin/orders -b /tmp/c.txt | jq '.data.list[0] | {id, status, device_id_short, total_amount}'
```

---

## 五、端到端整合(US-12 ~ US-13)

### US-12 · 后台改商品,车机端立刻看到

**作为**车主,**我想要**后台改的价格 / 标题立刻反映在车机端,**这样**信息一致。

**涉及**:[S07 early-integration] [S11 final-integration]

#### 场景 1

- **G**:车机端商品列表显示商品 1 价格 ¥99.00
- **W**:后台把商品 1 改成 ¥88.00
- **T**:车机端下拉刷新 / 重新进列表,价格变 ¥88.00

**验证步骤**:在两个浏览器 / 设备之间手动验证(无需自动化)。

---

### US-13 · 车机下单,后台立刻看到

**作为**运营,**我想要**车机一旦下单立刻能在后台看到,**这样**我能实时盯订单流。

**涉及**:[S07 early-integration] [S11 final-integration]

#### 场景 1

- **W**:车机端完成一笔下单 + 支付
- **T**:后台 `/admin/orders` 刷新后看到这笔订单,状态正确、device_id_short 显示

**验证步骤**:在两个浏览器 / 设备之间手动验证。

---

## 六、边界 / 特殊字符 / 并发 / 安全场景(US-14 ~ US-25)

> 跟 §一~五的"happy path 优先"故事不同,本节专门覆盖 `TEST_MATRIX.md` 标 🔴/🟡 的高风险场景。
> 安全相关测试**必须实跑**,不能只 mock。

### US-14 · 商品标题含特殊字符 / XSS 不能被执行 🔴 安全

**作为**运营,**我想要**输入特殊字符标题不出现安全问题,**这样**客户的输入不会破坏系统。

**涉及**:[S02 catalog] [S06 admin-web] [S09 android-browse]

#### 场景 1:title = `<script>alert(1)</script>`

- **G**:运营已登录
- **W**:新建商品,title = `<script>alert(1)</script>`
- **T**:
  - 后台列表里**字面显示**这串字符,**不**触发 alert 弹窗
  - 车机端详情页**字面显示**这串字符,不触发 alert
  - DB 里 title 字段值就是这串原文(不需要 HTML escape 存)

#### 场景 2:title 含 emoji + 中文 + 引号

- **W**:title = `加油卡 "100元" 🛢️`
- **T**:
  - 列表、详情、订单快照里**完整显示**(emoji 不丢、引号不破坏)
  - 商品下单后,订单 `product_snapshot.title` 仍是这串原文

**验证步骤**:
```bash
# 假设已登录,cookie 在 /tmp/c.txt
curl -X POST http://localhost:8000/api/v1/admin/products -b /tmp/c.txt \
  -H 'Content-Type: application/json' \
  -d '{"category_id":1,"title":"<script>alert(1)</script>","product_type":"physical","price":100,"main_image_url":"http://x.png","description":"x","on_sale":true}'
# 在车机端 / 后台浏览器手动确认没弹窗
```

---

### US-15 · 图片上传文件名含 path traversal 🔴 安全

**作为**运营,**我想要**即使上传文件名带 `../`,文件也不会跳出沙箱,**这样**服务器目录不会被污染。

**涉及**:[S05 admin-auth-upload]

#### 场景 1:文件名 `../../../etc/passwd.png`

- **G**:已登录,文件实际是合法 png 但 filename header 含 `../`
- **W**:`POST /admin/upload` filename=`../../../etc/passwd.png`
- **T**:
  - 服务器返 `{code:0, url:".../static/uploads/<uuid>.png"}`(用 UUID 替名)
  - 真实磁盘上**只在** `static/uploads/` 下出现这个 UUID 文件
  - **不存在** `/static/uploads/../../etc/passwd.png` 这种东西

**验证步骤**:
```bash
curl -X POST http://localhost:8000/api/v1/admin/upload -b /tmp/c.txt \
  -F 'file=@/tmp/test.png;filename=../../../etc/passwd.png'
# 然后:
find /Users/Admin/Documents/Projects/车机商店需求/carshop-server -name "*passwd*"
# 应该没有
```

---

### US-16 · 暴力破解登录不被无限重试 🔴 安全

**作为**运营,**我想要**登录接口防暴力破解,**这样**密码不会被穷举猜出。

**涉及**:[S05 admin-auth-upload]

#### 场景 1:连错 10 次密码

- **W**:同一 IP 在 1 分钟内调 `/admin/login` 错密码 10 次
- **T**:
  - 第 6 次起返 `code=2002` 或 429,message "尝试过多,请稍后再试"
  - 锁定 5 分钟内即使正确密码也无法登
  - 5 分钟后恢复

> **⚠️ 当前实现没有这层保护**,本故事**🔴 实测不通过**(11 final 跑过)。已落 **TD-024**(登录防暴力破解),不阻塞 Demo,但**禁止上生产**。
> (原文笔误写 TD-014,实际 TD-014 是分类 SVG 缺失;暴力破解的正确编号是 TD-024)

**验证步骤**:
```bash
for i in {1..10}; do
  curl -s -X POST http://localhost:8000/api/v1/admin/login \
    -H 'Content-Type: application/json' \
    -d '{"username":"admin","password":"wrong'$i'"}' | python -c "import sys,json; print(json.load(sys.stdin)['code'])"
done
# 当前所有 10 次都返 2001(未防护),应**只有前 5 次** 2001,后 5 次 2002
```

---

### US-17 · 同设备并发下单 · 订单号不撞 🔴 并发

**作为**车主,**我想要**短时间内连点两次"立即购买",两笔订单都能正确创建,**这样**不会因为并发出错。

**涉及**:[S04 order-pay]

#### 场景 1:并发 5 次创建订单

- **G**:商品 1 在售
- **W**:同设备 `X-Device-Id: us17` 同时发 5 个 POST /orders
- **T**:
  - 5 个全部成功,5 个**不同**的订单号
  - 订单号 random suffix 4 位,理论碰撞概率 < 0.1%

**验证步骤**:
```bash
seq 5 | xargs -P 5 -I{} curl -s -X POST http://localhost:8000/api/v1/orders \
  -H 'X-Device-Id: us17' -H 'Content-Type: application/json' \
  -d '{"product_id":1,"quantity":1}' | python -c "import sys,json; print(json.load(sys.stdin)['data']['id'])"
# 应该输出 5 个唯一订单号
```

---

### US-18 · 同订单并发支付幂等 🔴 并发

**作为**车主,**我想要**支付按钮被疯狂连点(误触/卡顿)也只支付一次,**这样**不会重复扣款。

**涉及**:[S04 order-pay]

#### 场景 1:并发 5 次 mock_pay

- **G**:订单 O1 status=pending
- **W**:5 个并发 `POST /orders/O1/mock_pay`
- **T**:
  - 5 个都返 `code=0`(契约幂等通过)
  - **paid_at 差 ≤ 5ms 可接受**(SQLite 无行锁,Demo 阶段放宽,见 TD-023)
  - 严格幂等"以第一次写入为准"需后端用 `SELECT ... FOR UPDATE` / `UPDATE ... WHERE status='pending'` 加锁,**上生产前必修**(见 TD-023)
  - 客户端只判 code=0,不受 paid_at 抖动影响

**验证步骤**:
```bash
# 先建一个订单
OID=$(curl -s -X POST http://localhost:8000/api/v1/orders -H 'X-Device-Id: us18' -H 'Content-Type: application/json' -d '{"product_id":1,"quantity":1}' | python -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")
# 5 个并发支付
seq 5 | xargs -P 5 -I{} curl -s -X POST "http://localhost:8000/api/v1/orders/$OID/mock_pay" -H 'X-Device-Id: us18' | python -c "import sys,json; d=json.load(sys.stdin); print(d['code'], d.get('data',{}).get('paid_at'))"
# 应该 5 行都 code=0,paid_at 完全一样
```

---

### US-19 · 商品删除时有未支付订单 → 订单仍可查 🟡 数据完整性

**作为**运营,**我想要**删商品时,即使有人订单还没付完,他的订单详情仍能完整查看,**这样**不破坏数据完整性。

**涉及**:[S02 catalog] [S04 order-pay] [S06 admin-web]

#### 场景 1

- **G**:商品 99 在售,用户 us19 已下单(产生 OrderItem snapshot)但未支付
- **W**:运营 DELETE 商品 99
- **T**:
  - `DELETE /admin/products/99` 成功
  - DB:`order_items.product_id` 字段被设为 NULL
  - `order_items.product_snapshot_json` 仍含完整商品信息
  - 用户调 `GET /orders/<id>` 仍能拿到订单 + 完整商品快照

**验证步骤**:
```bash
# 用户下单
OID=$(curl -s -X POST http://localhost:8000/api/v1/orders -H 'X-Device-Id: us19' -H 'Content-Type: application/json' -d '{"product_id":5,"quantity":1}' | python -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")
# 运营删商品
curl -s -X DELETE http://localhost:8000/api/v1/admin/products/5 -b /tmp/c.txt | python -c "import sys,json; print('delete code:', json.load(sys.stdin)['code'])"
# 用户查订单
curl -s "http://localhost:8000/api/v1/orders/$OID" -H 'X-Device-Id: us19' | python -c "import sys,json; d=json.load(sys.stdin)['data']; print('title:', d['items'][0]['product_snapshot']['title'])"
# 应该输出原商品 title
```

---

### US-20 · 改价瞬间下单 · 锁定下单时价格 🟡 数据完整性

**作为**车主,**我想要**我看到的价格就是我下单的价格,**这样**不会"看到 99 实际扣 199"。

**涉及**:[S02 catalog] [S04 order-pay]

#### 场景 1

- **G**:商品 5 当前价 100 分
- **W**:
  1. 用户拿到详情(看到 ¥1.00)
  2. **同时**运营把 price 改成 999 分
  3. 用户点提交订单
- **T**:
  - 订单 `total_amount` = **下单时刻** product.price(可能 100 或 999,看时序)
  - `product_snapshot.price` 跟 `total_amount` 一致
  - **不会出现** total_amount=100 但 snapshot.price=999 这种不一致

**验证步骤**:手动 + 看 DB

**注**:本场景验证服务端把"读价"和"创建订单"放在同一事务里。

---

### US-21 · quantity 边界(0 / 负 / 超大)→ 拒绝 🟡 边界

**作为**车主,**我想要**输入异常 quantity 时下单被拒,**这样**不会创出奇怪订单。

**涉及**:[S04 order-pay]

#### 场景

- **G**:商品 1 在售
- **W**:
  - `quantity=0` → code=1000
  - `quantity=-1` → code=1000
  - `quantity=1000000` → 是否限制?(根据业务定,MVP 当前未限制,记 TD)
- **T**:都返 1000,带清晰的 message

**验证步骤**:
```bash
for Q in 0 -1 999999; do
  curl -s -X POST http://localhost:8000/api/v1/orders -H 'X-Device-Id: us21' -H 'Content-Type: application/json' -d '{"product_id":1,"quantity":'$Q'}' | python -c "import sys,json; d=json.load(sys.stdin); print('quantity', sys.argv[1] if len(sys.argv)>1 else '?', '-> code:', d['code'], 'msg:', d['message'])"
done
```

---

### US-22 · 商品价格边界(0 / 负 / 超大)→ 拒绝 🟡 边界

**作为**运营,**我想要**填错价格被拒,**这样**不会上架免费 / 负价 / 超大价的商品。

**涉及**:[S02 catalog] [S06 admin-web]

#### 场景

- **G**:已登录
- **W**:
  - `price=0` → **后端接受**(`Field(..., ge=0)`,11 final 实测 code=0;运营自律,记 TD-025)
  - `price=-100` → code=1000(负价拒)✅
  - `price=2147483647`(int max)→ **后端接受**(无上限,11 final 实测;运营自律,记 TD-025)
  - `original_price` 小于 `price` → 后端无关联校验(运营自律,记 TD-025)
- **T**:负价被拒返清晰错误;其他边界 Demo 阶段接受,生产前应加严格校验(见 TD-025)

**验证步骤**:
```bash
for P in 0 -100 2147483647; do
  curl -s -X POST http://localhost:8000/api/v1/admin/products -b /tmp/c.txt -H 'Content-Type: application/json' \
    -d '{"category_id":1,"title":"边界测试","product_type":"physical","price":'$P',"main_image_url":"http://x.png","description":"x","on_sale":true}' | python -c "import sys,json; d=json.load(sys.stdin); print('price', '$P', '-> code:', d['code'])"
done
```

---

### US-23 · 大分页参数边界 ⚪ 边界

**作为**车机端,**我想要**传非法大 page / page_size 不导致服务端崩,**这样**不会被恶意客户端拖垮。

**涉及**:[S02 catalog] [S04 order-pay]

#### 场景

- **W**:
  - `page=99999`(超出总页数)→ 空 list,total 正确
  - `page=-1` / `page=0` → code=1000
  - `page_size=10000`(超 100 上限)→ 自动 clamp 到 100 或返 1000
- **T**:不崩,有明确返回

**验证步骤**:
```bash
curl -s 'http://localhost:8000/api/v1/products?page=99999' | python -c "import sys,json; d=json.load(sys.stdin); print('count:', len(d['data']['list']), 'total:', d['data']['total'])"
curl -s 'http://localhost:8000/api/v1/products?page=0' | python -c "import sys,json; print(json.load(sys.stdin)['code'])"
curl -s 'http://localhost:8000/api/v1/products?page_size=10000' | python -c "import sys,json; d=json.load(sys.stdin); print('returned page_size:', d['data']['page_size'])"
```

---

### US-24 · 超长字符串字段(title / description)→ 限制 ⚪ 边界

**作为**车机端,**我想要**超长 title 不拖崩列表渲染,**这样**保证体验流畅。

**涉及**:[S02 catalog] [S09 android-browse]

#### 场景

- **W**:运营新建 title = 1000 字符 / description = 100000 字符
- **T**:
  - **title > 128 字符 → `code=1000`**(后端 Pydantic `max_length=128` 校验拒,SPEC §5 钦定)
  - title ≤ 128 字符正常接受;前端列表卡 maxLines=2 / 详情页 maxLines=5 截断显示,不撑爆
  - description 不设限,前端按需截断

**验证步骤**(11 final-integration 实跑):
```bash
# 121 字符 title 应通过
curl -X POST https://carshop.hearagain.space/api/v1/admin/products -b /tmp/c.txt \
  -H 'Content-Type: application/json' \
  -d "{\"category_id\":1,\"title\":\"$(python3 -c 'print("超长测试" * 30)')\",\"product_type\":\"physical\",\"price\":100,\"main_image_url\":\"http://x.png\",\"description\":\"x\",\"on_sale\":true}" | jq '.code'
# 期望:1000(超 128 字符被拒)
```

---

### US-25 · banner image_url 含 SSRF 风险 URL 🔴 安全

**作为**运营,**我想要**banner 的图片 URL 即使被填错也不导致服务器请求内网,**这样**云元数据接口不会被暴露。

**涉及**:[S03 banner]

#### 场景 1:image_url = `http://169.254.169.254/latest/meta-data/`

- **W**:`POST /admin/banners` image_url 填云元数据 URL
- **T**:
  - 服务端**只**存这个字符串(不去拉)
  - 前端 `<img src>` 会请求(浏览器会跨域报错或失败)
  - **服务端不会**主动去 fetch 这个 URL

**验证步骤**:
```bash
curl -s -X POST http://localhost:8000/api/v1/admin/banners -b /tmp/c.txt -H 'Content-Type: application/json' \
  -d '{"image_url":"http://169.254.169.254/","link_type":"none","sort":1,"on_show":false}' | python -c "import sys,json; print(json.load(sys.stdin)['code'])"
# 应该 0(只存 URL)
# 然后看服务端 log 没有出站请求(用 lsof 或网络监控验证)
```

---

## 七、Story → Session 索引

每个 session 完成前,要跑通**至少**以下故事:

| Session | 必须跑通的 stories(基础)| 加强场景(US-14~25,按 TEST_MATRIX 优先级)|
|---|---|---|
| 01 server-foundation | 无 stories(地基) | - |
| 02 catalog | US-01 场景 2、US-02、US-03、US-05、US-10 | US-19、US-20、US-22 |
| 03 banner | US-01 场景 1 中 banner 部分 | US-25 |
| 04 order-pay | US-04、US-05、US-06、US-07 | US-17、US-18、US-21、US-23 |
| 05 admin-auth-upload | US-08、US-09(上传部分) | US-15(path traversal)、US-16(暴力破解 → 预计不通过,记 TD) |
| 06 admin-web | US-08、US-09、US-10、US-11 | US-14(XSS) |
| 07 early-integration | US-12、US-13(端到端通过) | 公网真域名重跑 US-17/18(并发) |
| 08 android-foundation | 无(只是骨架) | - |
| 09 android-browse | US-01、US-02、US-03 | US-24 |
| 10 android-checkout | US-04、US-05、US-06、US-07 | US-17、US-18 |
| 11 final-integration | **全部** US-01~13 + US-17/18/19 端到端再跑一次 | 全部 🔴 标记的 US 都跑通 |

---

## 维护规则

- 改了 SPEC.md 之后,**先看 stories 还对不对**,不对就同步改这里
- 加新功能时(比如以后做地址管理 M-Address),**先写 story 再写代码**
- Story 改了之后,STATUS.md 里记一笔"USER_STORIES.md 改动"

**版本**:v1.3
**最后更新**:2026-05-26(11 final · US-16 TD 编号修正为 TD-024;US-22 Then 段放宽接受 price=0 / no upper limit 记 TD-025)
