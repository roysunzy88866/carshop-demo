# Session Q2 · 安卓开发环境装机(辅助 session)

> **Q 系列辅助 session**,不在主 12 个 session 序列里。本 session 跟业务无关,只装环境 + 实跑 00 session 留的 design-system 验证可编译。
>
> **跟 07 早集成可以并行**:两个互不干扰(07 在 Mac mini 跑,Q2 在本机 M4)。

---

## 1. 你要做什么

在用户的 **Apple Silicon Mac(M4)+ zsh** 上,**从零装一个能跑 Jetpack Compose 的安卓开发环境**,并且跑通 `carshop-android/`(00 session 留的设计系统 demo),在 Pixel Tablet 模拟器看到 demo 页。

### 1.1 装机顺序(严格按此顺序)

#### Step 1:检查现有环境

```bash
# 看是否已装(避免重复装坏现有 setup)
brew --version           # 应该有 Homebrew(用户已有)
java -version            # 看是否已装 JDK 以及版本
which adb                # 看是否已有 Android SDK
xcode-select --version   # 看是否有 Command Line Tools
```

如果**已装过 JDK 或 Android Studio**:
- 不要覆盖,先问用户("你之前装过吗?要不要保留?")
- 跳过对应 step

#### Step 2:装 JDK 17(ARM64 原生)

```bash
# Temurin JDK 17(LTS 长期支持版,广泛兼容 AGP 8.5+)
brew install --cask temurin@17

# 验证
java -version            # 应输出 17.x.x
echo $JAVA_HOME
```

如果 `JAVA_HOME` 没设,**在 `~/.zshrc` 末尾加**(如果用户用 zsh):

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH
```

然后 `source ~/.zshrc` 验证 `echo $JAVA_HOME` 输出对的路径。

#### Step 3:装 Android Studio

```bash
brew install --cask android-studio
```

下载 ~6GB,装 ~10-15 分钟,取决于网速。

#### Step 4:Studio 首次启动配置(需要用户参与)

**告诉用户**:打开 Android Studio,会走一个 setup wizard:

1. "Welcome to Android Studio" → Next
2. Install Type → 选 **Standard**(默认)
3. UI Theme → 用户自选(Dark / Light)
4. Verify Settings → 看到要下载 ~3GB 的 SDK 组件(Platform Tools / SDK Platform / Emulator),点 Next → Accept All Licenses → Finish
5. 等下载完成(5~10 分钟,根据网速)

**用户做完这步后,告诉你"Studio 装好了"再继续**。

#### Step 5:配置 SDK 路径 + Gradle 设置

打开 Studio → Preferences(⌘,)→ Languages & Frameworks → Android SDK,看到的 SDK Location 路径(默认 `~/Library/Android/sdk`)。

在 `~/.zshrc` 末尾再加:

```bash
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH
```

`source ~/.zshrc`,然后:
```bash
adb --version            # 应输出 ABD 35.x
sdkmanager --version     # 应输出 sdkmanager 数字版本
```

#### Step 6:创建 AVD(Android 虚拟设备)· 车机风格

Studio → Tools → Device Manager → Create Virtual Device:

**关键配置**(模仿真实车机):
- **Hardware**:选 **Pixel Tablet**(11 寸 2560×1600,接近车机比例)
- **System Image**:**API 34** (Android 14, arm64-v8a,Apple Silicon 原生)
- **Name**:`carshop-tablet`
- **Orientation**:**Landscape**(横屏)
- 其他默认

创建完,**立即启动一次模拟器**(Tools → Device Manager → ▶️),确认能进系统、能滑屏、能看到主屏幕。第一次启动 1~2 分钟。

#### Step 7:用 Gradle Wrapper 跑 carshop-android

```bash
cd /Users/Admin/Documents/Projects/车机商店需求/carshop-android
ls gradlew                # 看是否有 wrapper 脚本
# 如果没有(00 session 提到可能要生成),用 Studio 打开项目让它自动生成:
#   File → Open → 选 carshop-android/,等 Studio 同步完成

# 编译
./gradlew assembleDebug   # 第一次跑会下载 Gradle + AGP + Compose 依赖,5~15 分钟
```

#### Step 8:装到模拟器 + 看 demo 页

```bash
# 确认模拟器跑着
adb devices               # 看到 emulator-5554 device

# 装 APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 启动 App
adb shell am start -n com.carshop.android/.MainActivity
```

**在模拟器里应该看到**:
- 左侧 Rail(240dp 宽,4 个 item 含 icon + 选中态 3dp 海泡青条)
- Top Bar(80dp 高,"商店 · 设计系统 Demo")
- 主内容区:
  - 商品卡(两张,实物红价 / 服务券绿价)
  - 按钮组(Primary / Secondary / Text / compact)
  - Chip & Tag
  - 价格三种尺寸
  - 列表项 / Toast / 二维码框

**这就是 00 session 的 demo 页**,跑通 = 00 session 的 build 实跑也补完了。

### 1.2 同时还债:补 TD-002 / Session 00 Android build 实跑

session 00 artifact §7 待办里有一条:
> [ ] 跑 `./gradlew assembleDebug` 验证 Android build

跑通的话:
- 在 `artifacts/00-design-system.md` 末尾追加一节"§9 build 实跑验证(Q2 session 完成)"
- 列:Gradle 版本 / AGP 版本 / 模拟器型号 / APK 大小 / **模拟器截图 1 张**(自带 adb screencap)
- 改 STATUS.md:00 状态备注"Android build 留到 08"删掉

> ⚠️ 例外:这是允许的跨 session 修改(完善已完成 session 的产物),按 CLAUDE.md §铁律 9 例外。在 Q2 artifact 里写清楚"补的是 00 的债"。

---

## 2. 你不要做什么

- ❌ **不要装 JDK 21 / 22**(AGP 8.5 兼容性最稳是 17)
- ❌ **不要装 Android Studio Canary / Beta**(用 Stable)
- ❌ **不要装 Intel x86 模拟器镜像**(M4 是 arm64,装 x86 巨慢)
- ❌ **不要改 carshop-android/ 任何业务代码**(本 session 只装环境 + 验证 build,不动 00 留的 design-system 代码)
- ❌ **不要在用户 `~/.zshrc` 里加大段配置**,只加必要的 3 行(JAVA_HOME + ANDROID_HOME + PATH)
- ❌ **不要跑 Studio 的 "Sync Project with Gradle Files" 之外的 IDE 操作**(IDE 操作让用户做,Claude Code 主要走 CLI)
- ❌ **不要装 NDK / CMake / Vulkan SDK 等附加组件**(SDK Manager 里那一堆 optional 不需要)

---

## 3. 输入

- `CLAUDE.md`、`README.md`(项目入口)
- `sessions/Q2-android-env-setup.md`(本文件)
- `artifacts/00-design-system.md`(知道要跑的是啥)
- `carshop-android/`(00 session 产物)

---

## 4. 输出 / 交付物

### 装好的环境
- `/Applications/Android Studio.app` 安装完成
- `~/Library/Android/sdk/` SDK 就位
- `~/.zshrc` 加 JAVA_HOME / ANDROID_HOME / PATH 三行
- AVD `carshop-tablet` 创建好,跑过一次能进系统

### 编译产物
- `carshop-android/app/build/outputs/apk/debug/app-debug.apk` 文件存在,大小 < 30MB
- 模拟器里装上能开,demo 页正常显示

### 文档产物
- `artifacts/Q2-android-env-setup.md`:
  - 装机过程实录(每个 step 的命令 + 输出 + 时间)
  - 装的版本号(JDK / Android Studio / SDK Platform / AGP)
  - AVD 配置截图
  - **模拟器跑 demo 页的截图**(用 `adb shell screencap -p /sdcard/screen.png && adb pull /sdcard/screen.png`)
  - 踩坑记录(必有,Apple Silicon 第一次装环境的坑)
- 更新 `artifacts/00-design-system.md`:加 §9 build 实跑验证
- 更新 `STATUS.md`:00 备注去掉"Android build 留到 08";加 Q2 状态
- 更新 `TECH_DEBT.md`:TD-002 状态从 🔴 → 🟢 已解决(如果思源黑体 fallback 字体效果可接受),或保持 🔴(如果发现真要打包字体)
- 更新 `ORCHESTRATOR_LOG.md`:Q2 完成记一笔

---

## 5. 验收标准(对照 USER_STORIES + TEST_MATRIX)

1. ✅ `java -version` 输出 17.x.x
2. ✅ `adb --version` 输出 35.x(SDK Platform Tools 装好)
3. ✅ `~/.zshrc` 有 JAVA_HOME + ANDROID_HOME 配置,新开 terminal `echo $ANDROID_HOME` 输出对路径
4. ✅ AVD `carshop-tablet` 存在,启动后能进 Android 14 主屏幕
5. ✅ `cd carshop-android && ./gradlew assembleDebug` 跑完无报错,APK 生成
6. ✅ `adb install -r app-debug.apk` 装到模拟器成功
7. ✅ **模拟器跑 demo 页**,看到 Rail + 商品卡 + 按钮组 + 价格演示(对照 00 session demo 截图)
8. ✅ **截图存到** `artifacts/Q2-android-env-setup/emulator-demo.png`
9. ✅ Q2 artifact 写完 + 00 artifact §9 补完
10. ✅ STATUS / TECH_DEBT / ORCHESTRATOR_LOG 全部更新

---

## 6. 依赖

- **上游**:00 session 留的 `carshop-android/` 代码
- **下游**:08 android-foundation(必须 Q2 完成后才能跑,因为 08 要 `./gradlew` build)、09/10/11

## 7. Mock 策略

不需要(纯环境装机)。

## 8. 已知坑 / Apple Silicon 注意

1. **JDK 必须 ARM64 原生**:Temurin / Azul Zulu / OpenJDK 都有 ARM64 build,Oracle JDK 也行但条款复杂。**brew install --cask temurin@17** 是最简单干净的方式
2. **模拟器必须 arm64-v8a 系统镜像**:Studio SDK Manager 里 System Images,选 API 34 的 **"ARM 64 v8a"** 那一行,**不要选 "Intel x86_64"**(M4 跑 x86 模拟器要走 Rosetta 极慢)
3. **Gradle wrapper 可能不在 git 里**:00 session 没生成 gradlew(artifact §8.2 提到),首次用 Studio 打开项目会自动生成。如果不想用 Studio,可以 `gradle wrapper --gradle-version 8.7` 一行命令生成(需要先 `brew install gradle`)
4. **Android Studio 第一次启动会要下 3GB+ SDK 组件**:别中途取消,中途取消下次启动重来要清空 `~/Library/Android/Sdk` 重装
5. **`adb devices` 列不出模拟器**:确认模拟器跑着(Studio Device Manager 看到 ▶ 在转);如果还不行,`adb kill-server && adb start-server`
6. **第一次跑 `./gradlew assembleDebug` 慢**:要下 ~1GB Gradle + AGP + Compose + Coil 等依赖,**5~15 分钟正常**。第二次 30 秒内
7. **碰到 "compileSdk 35 / 36 not found" 错误**:打开 SDK Manager 装对应 Platform。00 spec 用的 compileSdk 34
8. **思源黑体效果**:00 session 决策不打包字体,模拟器系统字体是 Noto Sans CJK,渲染应该 OK。如果发现笔画明显异常,记一条 TD 升级 TD-002 状态
9. **横屏锁定**:MainActivity Manifest 已经写了 `screenOrientation="landscape"`,启动时模拟器自动转横屏

---

## 9. 跟用户交互的点(主动告诉用户什么时候等你 / 什么时候做事)

| 阶段 | 用户做啥 | Claude Code 做啥 |
|---|---|---|
| Step 1~2 检查 + 装 JDK | 等(brew 自动)| 跑命令、看输出 |
| Step 3 brew 装 Studio | **等下载**(~6GB,10-15min)| 等 |
| Step 4 Studio 首次启动 | ⚠️ **用户操作**:点开 Studio,走 setup wizard | 等用户说"装好了" |
| Step 5 配 PATH | 等 | 改 `~/.zshrc` |
| Step 6 创建 AVD | ⚠️ **用户操作**:Studio Device Manager 创建,Claude Code 没法 UI 操作 | 等用户说"AVD 创好了"+ 给 AVD 名 |
| Step 7 gradle build | 等(可能要让 Studio 打开项目生成 gradlew) | 跑 build,看输出 |
| Step 8 安装 + 截图 | 等 | adb install + adb screencap |
| Step 9~10 写 artifact | 等 | 写文档 |

**预计总时间**:1.5~2.5 小时(主要下载等待)
