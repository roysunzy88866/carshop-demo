# Artifact · Session Q2 · 安卓开发环境装机

> **状态**:✅ 已完成 · 环境就位 + 00 design-system demo 实跑通过
> **日期**:2026-05-26
> **执行机**:Apple Silicon Mac mini-2 (M4, Darwin 25.3.0, zsh)
> **本 session 修了 00 session 一个 build bug**(顶层 build.gradle.kts 幽灵 plugin 声明,见 §6)

---

## 1. 交付概览(对照 session spec 验收清单)

| # | 验收项 | 状态 | 实际 |
|---|---|---|---|
| 1 | `java -version` 输出 17.x.x | ✅ | OpenJDK Temurin 17.0.19+10 ARM64 |
| 2 | `adb --version` 输出 35.x(SDK Platform Tools 装好) | ✅ | adb 1.0.41 / Platform-Tools 37.0.0 ARM64 |
| 3 | `~/.zshrc` 有 JAVA_HOME + ANDROID_HOME 配置 | ✅ | 末尾追加 4 行(注释 + 3 export) |
| 4 | AVD `carshop-tablet` 存在,启动后能进 Android 14 主屏幕 | ✅ | Pixel Tablet 2560×1600 / Android 14 / arm64-v8a / Landscape |
| 5 | `./gradlew assembleDebug` 跑完无报错,APK 生成 | ✅ | BUILD SUCCESSFUL · 15M `app-debug.apk` |
| 6 | `adb install -r app-debug.apk` 装到模拟器成功 | ✅ | `Performing Streamed Install\nSuccess` |
| 7 | 模拟器跑 demo 页,看到 Rail + 商品卡 + 按钮组 + 价格演示 | ✅ | 见 §5 截图与组件清单 |
| 8 | 截图存到 `artifacts/Q2-android-env-setup/emulator-demo.png` | ✅ | 233K PNG · `adb screencap` 真截屏 |
| 9 | Q2 artifact 写完 + 00 artifact §9 补完 | ✅ | 本文件 + 00 artifact 已追加 §9 |
| 10 | STATUS / TECH_DEBT / ORCHESTRATOR_LOG 全部更新 | ✅ | 见 §7 |

---

## 2. 装的版本清单

| 组件 | 版本 | 来源 | 备注 |
|---|---|---|---|
| **JDK** | Eclipse Temurin **17.0.19+10** ARM64 | `brew install --cask temurin@17` | 装在 `/Library/Java/JavaVirtualMachines/temurin-17.jdk/`(sudo) |
| **Android Studio** | **2025.3.4.7 "Panda" Patch 1** | `brew install --cask android-studio` | 6GB 下载,装在 `/Applications/` |
| **Android SDK Platform-Tools** | **37.0.0** (adb 1.0.41) | Studio Setup Wizard | `~/Library/Android/sdk/platform-tools/` |
| **SDK Platform** | **android-34**(UpsideDownCake)+ `android-36.1` | Setup Wizard 默认装 36.1;后续 GUI 装 34 | 项目 compileSdk=34 |
| **System Image** | `google_apis_arm64-v8a` for API 34 | AVD 创建时下载(~1.5GB) | 选 Google APIs flavor,不带 Play Store(更小) |
| **AGP** | 8.5.0 | `build.gradle.kts`(00 session) | 跟 Gradle 8.7 配套 |
| **Gradle (wrapper)** | **8.7** | `gradle wrapper --gradle-version 8.7 --distribution-type bin` | bootstrap 用的是系统 brew gradle 9.5.1 |
| **System gradle** | **9.5.1** | `brew install gradle` | 仅用于一次性生成 wrapper,后续都走 `./gradlew` |
| **brew 顺带的 OpenJDK** | **26.0.1** | `brew install gradle` 拉的依赖 | 不影响 build · `JAVA_HOME` 始终指 Temurin 17 |
| **Kotlin** | 1.9.24 | `build.gradle.kts`(00 session) | 跟 Compose Compiler Extension 1.5.14 配套 |
| **Compose BOM** | 2024.06.00 | `app/build.gradle.kts`(00 session) | M3 |

---

## 3. `~/.zshrc` 改动(完整 diff)

新追加 5 行到末尾,**不动任何已有内容**:

```bash
# Android dev env (Q2 session, 2026-05-26)
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH
```

新开 terminal 后:
- `echo $JAVA_HOME` → `/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home`
- `echo $ANDROID_HOME` → `/Users/Admin/Library/Android/sdk`
- `adb --version` → `1.0.41 / 37.0.0-14910828`
- `java -version` → `17.0.19`

---

## 4. AVD 配置

| 项 | 值 |
|---|---|
| Name | `carshop-tablet` |
| Device | Pixel Tablet (10.95", 2560×1600, 320dpi) |
| API | 34 (Android 14 "UpsideDownCake") |
| Services | Google APIs(无 Play Store) |
| ABI | **arm64-v8a**(M4 原生,不走 Rosetta) |
| Device skin | `pixel_tablet` |
| Default boot | Quick(走快照,启动 10 秒) |
| Internal storage | 6GB |
| Orientation | **Landscape** |

启动后 `adb devices` 列出 `emulator-5554 device`。

---

## 5. Demo 页跑通的实际效果

`adb shell am start -n com.carshop.android/.MainActivity` 启动后,截图保存在
**`artifacts/Q2-android-env-setup/emulator-demo.png`**(233K · 真实 PNG)。

对照 session spec 验收项 #7 + 00 artifact §1 验收项 #2 的元素清单:

| 设计系统元素 | 实际可见 | tokens.json 验证点 |
|---|---|---|
| Rail 240dp + 4 item | ✅ 商店 / 加油充电 / 洗车保养 / 汽车用品(含 icon)| Rail 自写,不是 NavigationRail |
| 选中态 3dp 海泡青强调条 | ✅ 选中"商店"项左侧有强调条 | `Seafoam500 #00C2A8` |
| TopBar 80dp | ✅ "商店 · 设计系统 Demo" | titleLarge 28sp |
| 实物商品卡(红价 + 划线原价)| ✅ ¥899.00 + 划线 ¥1099.00 | `TextPrice #E63946` + `TextPriceStrike #B8C5CF` |
| 服务券商品卡(绿价)| ✅ ¥95.00 绿色 | `TextPriceEnergy #00A892` |
| Card 选中态描边 | ✅ 卡片描边 1-2dp,服务券卡有海泡青描边 | `BorderAccent #00C2A8` |
| 按钮 4 variant | ✅ 立即购买 (Primary 黑) / 加入收藏 (Secondary outline) / 查看更多 (Text) / 确认 (Primary 黑) | `Steel800 #1A2027` primary |
| Chip(可触控) | ✅ 全部 / 在售(选中,海泡青底)/ 已售罄 | 48dp 胶囊,Selected 状态 |
| Tag(静态) | ✅ 在售 / 充电中 / 5折券 / 推荐 | 5 种 kind 全见 |
| 价格三种尺寸 | ✅ Display 44sp ¥899.00 / Card 30sp ¥95.00 / LineItem 22sp ¥128.00 | 3 种 `CarshopPriceSize` |
| 列表项 / Toast / 二维码框 | ⏸ 节标题可见,具体组件滚到折叠以下(spec 只要 1 张截图,这张已覆盖核心) | - |

**字体 fallback 实测**:中文渲染清晰、笔画完整、字号一致。Noto Sans CJK 系统字体在 Pixel Tablet 模拟器上效果**可接受**,**TD-002 维持 🔴 待还** 不升级(等真车机上测了再说,模拟器跟真硬件可能不一样)。

---

## 6. 踩坑实录(给未来 session)

### 坑 1 · Temurin cask 装需要 sudo 密码,后台 shell 无法弹密码框

`brew install --cask temurin@17` 在最后会 `sudo /usr/sbin/installer -pkg ...`,**Claude Code 后台 shell 无法弹 sudo 密码框**。
**绕过**:让用户在自己 terminal 里手动跑这一行(秒装,只需密码)。
**未来 session**:任何 cask 装到 `/Library/` 的(JDK / 系统级 driver 等)都会有这问题。Studio 装 `/Applications/` 不需要 sudo,无此坑。

### 坑 2 · 00 session 留的幽灵 plugin 声明,build 直接挂

`carshop-android/build.gradle.kts` 第 5 行声明:
```kotlin
id("org.jetbrains.kotlin.plugin.compose") version "1.9.24" apply false
```
但**这个插件是 Kotlin 2.0+ 才引入的**,Kotlin 1.9.x 没有。Gradle 即使 `apply false` 也校验插件存在,直接报 BUILD FAILED:
```
Plugin [id: 'org.jetbrains.kotlin.plugin.compose', version: '1.9.24', apply: false] was not found
```
同时 `app/build.gradle.kts` 没 apply 这个插件,用的是老式 `composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }`(Kotlin 1.9.x 正确写法)。

**判定**:00 session bug,这行是幽灵声明,没用但挡 build。
**修复(用户确认 A 方案后)**:删 `build.gradle.kts` 第 5 行,补 3 行注释说明为什么。**这是允许的跨 session 修改**(Q2 spec §1.2 例外:补 00 session 漏的 build 实跑)。
**未来 session 提示**:如果 08 升级 Kotlin 到 2.0+,要把这行加回来,并把 app 的 `composeOptions` 块删掉换成 `plugins { id("org.jetbrains.kotlin.plugin.compose") }`。

### 坑 3 · Gradle 后台执行时 `ANDROID_HOME` 找不到

Claude Code 后台 `Bash` 用的是 session 启动时的 zsh 环境快照,**`source ~/.zshrc` 的改动不会被这个快照看到**。第一次跑 `./gradlew assembleDebug` 直接挂:
```
SDK location not found. Define a valid SDK location with an ANDROID_HOME environment variable
or by setting the sdk.dir path in your project's local properties file at '.../local.properties'.
```
**绕过**:在 `carshop-android/` 建 `local.properties`(已在 `.gitignore`,**不入库**):
```
sdk.dir=/Users/Admin/Library/Android/sdk
```
**未来 session 提示**:08+ session 进 carshop-android/ 第一件事**确认 `local.properties` 存在且 `sdk.dir` 对**。任何换电脑或换 SDK 路径都要更新这一行(per-developer 配置)。

### 坑 4 · Setup Wizard 默认装 SDK Platform 36.1,不装 34

Studio 2025.3 的 Setup Wizard "Standard" 装的是最新版 36.1,**不会自动装 34**。
**绕过**:用户在 SDK Manager GUI 里勾 "Android 14 API 34" 单独装。
**未来 session 提示**:任何 compileSdk 不是最新版的项目,都要手动装对应 SDK Platform。如果改 compileSdk,要同步装新 Platform。

### 坑 5 · Setup Wizard 不装 cmdline-tools

Studio 2025.3 装出来的 SDK 里**没有 `cmdline-tools/`** 目录,所以 `sdkmanager` CLI 不可用。要么:
- GUI 装(SDK Manager → SDK Tools → 勾 "Android SDK Command-line Tools (latest)")
- 或者后面用得到时再装

**本 session 没装**,因为 Studio GUI 已经能完成所有装包工作。**未来 session 如果要走 CI / 自动化 build,必须装 cmdline-tools**。

### 坑 6 · Gradle 9.5.1 (brew) 拉了 OpenJDK 26 当依赖

`brew install gradle` 顺带装了 `/opt/homebrew/Cellar/openjdk/26.0.1/`。这只是 brew 给 gradle CLI 用的 JVM,**不影响项目 build**:
- `./gradlew` 启动的 daemon 用的是 `JAVA_HOME` 指的 **Temurin 17**(项目要求)
- 系统 `gradle` CLI 用 OpenJDK 26(只 bootstrap wrapper 那一次用了)

**未来 session 提示**:别被 `gradle --version` 输出的 `Launcher JVM: 26.0.1` 迷惑,项目实际编译用的是 wrapper 自己启动的 Temurin 17 daemon。

### 坑 7 · 横屏强制后 Pixel Tablet 时钟 widget 倒过来

模拟器启动后,默认锁屏 / 主屏的 Pixel Tablet 时钟 widget 是上下颠倒的(因为这屏幕原生 portrait,我们强制 landscape)。**不影响我们 App**(App 在 Manifest 锁了 landscape,启动后全屏正向显示)。Cosmetic,不修。

---

## 7. 同时修的债 / 文档同步

### 改的文件清单

- ✅ `~/.zshrc` —— 末尾追加 5 行(JAVA_HOME / ANDROID_HOME / PATH 注释)
- ✅ `carshop-android/build.gradle.kts` —— 删幽灵 plugin 那行,补 3 行注释。**这是允许的跨 session 修改**(Q2 spec §1.2 例外)
- ✅ `carshop-android/local.properties` —— **新建**(`.gitignore` 已排除,per-developer)
- ✅ `carshop-android/gradlew` + `gradle/wrapper/*` —— **新生成**(Gradle 8.7 wrapper)
- ✅ `artifacts/Q2-android-env-setup.md` —— 本文(新建)
- ✅ `artifacts/Q2-android-env-setup/emulator-demo.png` —— 截图
- ✅ `artifacts/00-design-system.md` —— 追加 §9 build 实跑验证小节
- ✅ `STATUS.md` —— 00 备注去掉"Android build 留到 08";加 Q2 这一行
- ✅ `TECH_DEBT.md` —— 加 TD-017(local.properties 不入库)+ TD-018(cmdline-tools 未装)+ 复盘 TD-002
- ✅ `ORCHESTRATOR_LOG.md` —— 加 Q2 完成一笔

### TD 复盘

- **TD-002 思源黑体**:模拟器实测 Noto Sans CJK 渲染清晰、笔画完整,**模拟器层面可接受**。但模拟器 ≠ 真车机,**保持 🔴 待还**,触发条件改为"真车机上测发现明显不一致时"。
- **新 TD-017**(本 session 加):`local.properties` 每个开发者机器都要重建,没自动化脚本提示。低影响。
- **新 TD-018**(本 session 加):没装 cmdline-tools,无法 CI 自动化 build。中影响,触发于 11 final-integration 想跑 CI 时。

---

## 8. 给下游 session 的接力(08 android-foundation 用)

### 现在你可以直接跑

```bash
cd /Users/Admin/Documents/Projects/车机商店需求/carshop-android

# 编译
./gradlew assembleDebug

# 装到模拟器
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 启动
adb shell am start -n com.carshop.android/.MainActivity
```

第二次以后 build 应该 **30 秒以内**(daemon + 缓存)。

### 08 session 启动时要先确认

1. `adb devices` 列出 `emulator-5554 device`(没列出 → Studio Device Manager 启动 `carshop-tablet`)
2. `cd carshop-android && cat local.properties` 有 `sdk.dir=/Users/Admin/Library/Android/sdk`(没有 → 抄过来)
3. `./gradlew assembleDebug` 能跑(挂了 → 检查 ANDROID_HOME / JAVA_HOME / 是否新开 terminal)

### 08 session 不要踩的坑

- ❌ 别升 Kotlin 到 2.0+ 又不改 `app/build.gradle.kts` 的 `composeOptions` 块 → build 会挂
- ❌ 别动 `local.properties` 加到 git → 已 .gitignore,但脑子要记着
- ❌ 别装 x86_64 模拟器镜像 → M4 跑 x86 极慢

### 08 session 加业务代码前可以做的小升级

- 装 cmdline-tools(GUI:SDK Manager → SDK Tools → 勾)→ 解锁 sdkmanager / avdmanager CLI
- 升 SDK Platform 35 / 36 作为可选(项目主线还是 compileSdk 34)
- 装 `system-images;android-34;google_apis;x86_64` ← **不要**(M4 是 ARM)

---

## 9. 时长 / 网速实录

| 步骤 | 时长 | 备注 |
|---|---|---|
| Step 2 装 JDK 17 | ~30 秒(下载已就位)| Temurin cask · sudo |
| Step 3 改 zshrc | ~10 秒 | Edit 工具 |
| Step 4 装 Android Studio | ~2 分钟(6GB 下载快)| 用户网速好 |
| Step 5 Studio Setup Wizard | ~5 分钟 | 用户操作 + 等下载 |
| Step 5b SDK Platform 34 + cmdline-tools | (cmdline-tools 没装)| 跳过 |
| Step 6 创 AVD + 下 system image | ~5-10 分钟 | 用户操作 + 1.5GB 下载 |
| Step 6 启动模拟器 | ~1 分钟 | 首次冷启动 |
| Step 7 gradle wrapper 生成 | ~85 秒 | 系统 gradle 9.5.1 bootstrap |
| Step 7 assembleDebug(第二次,本地有缓存) | ~2.5 分钟 | 35 tasks 全跑 |
| Step 8 adb install + 启动 + 截图 | ~10 秒 | streamed install |
| Step 9 写 artifact + 更新文档 | ~20 分钟 | 文档 |

**总计**:约 50 分钟(spec 预计 1.5~2.5 小时,**比预估快**,因为用户网速好 + 全程没遇到无法绕过的卡点)。

---

## 10. 已知偏离 / 不修

| # | 偏离 | 为啥不修 |
|---|---|---|
| 1 | SDK Platform 36.1 也装上了(setup wizard 装的)| 不影响,占 ~500MB,留着备用 |
| 2 | OpenJDK 26 当 gradle CLI 依赖 | brew 自动管,不动 |
| 3 | cmdline-tools 没装 | 本 session 用 GUI 就够,**记 TD-018**,11 session 上 CI 再装 |
| 4 | 截图只有 1 张(没截滚动到下方的列表项 / Toast / 二维码框)| spec 只要 1 张,**第一屏已覆盖 8/9 个核心元素** |
| 5 | 模拟器有顶部状态栏 + 时钟 widget 倒着 | M3 主屏的默认渲染,不影响 App |

---

**版本**:v1.0
**完成日期**:2026-05-26
**执行人**:Claude Code (Q2 session)
