# GitHub 自动构建指南

## 📋 3 步获取 APK

### 第 1 步：创建 GitHub 仓库

1. 访问 https://github.com
2. 登录你的 GitHub 账号（没有就注册一个）
3. 点击右上角 **+** → **New repository**
4. 填写：
   - **Repository name**: `DexUnpacker`
   - **Description**: `免 Root Dex 脱壳工具`
   - **Visibility**: Public 或 Private（推荐 Public）
   - **不要勾选** "Initialize this repository with a README"
5. 点击 **Create repository**

---

### 第 2 步：上传代码到 GitHub

#### 方法 A: 使用网页上传（最简单）

1. 在刚创建的仓库页面，点击 **uploading an existing file**
2. 打开手机文件管理器，进入 `/storage/emulated/0/Download/DexUnpacker/`
3. **选择所有文件**（包括隐藏文件 .github）
4. 拖拽到 GitHub 上传区域
5. 在 Commit message 输入：`Initial commit - DexUnpacker project`
6. 点击 **Commit changes**

#### 方法 B: 使用 Git 命令（如果你有 Git 基础）

```bash
cd /workspace/DexUnpacker

# 初始化 Git
git init

# 添加所有文件
git add .

# 提交
git commit -m "Initial commit - DexUnpacker project"

# 添加远程仓库（替换 YOUR_USERNAME）
git remote add origin https://github.com/YOUR_USERNAME/DexUnpacker.git

# 推送
git branch -M main
git push -u origin main
```

---

### 第 3 步：等待自动构建

1. 上传完成后，点击仓库顶部的 **Actions** 标签
2. 你会看到 **Build APK** 工作流正在运行（绿色或黄色图标）
3. 等待 5-10 分钟，直到状态变成 **绿色 ✓**
4. 点击运行的工作流（例如 `Build APK #1`）
5. 在页面底部 **Artifacts** 区域，点击 **app-debug.apk**
6. 下载 APK 到手机并安装

---

## 📱 安装 APK

```bash
# 方法 1: 直接点击安装
# 在手机上打开下载的 app-debug.apk 文件

# 方法 2: 使用 ADB（如果有电脑）
adb install -r app-debug.apk
```

**注意:** Android 会提示"未知来源应用"，需要授权安装。

---

## 🔧 常见问题

### Q1: Actions 显示红色 ✗ 失败

**可能原因:**
- Gradle 依赖下载失败
- 构建超时
- 代码有错误

**解决:**
1. 点击失败的工作流
2. 查看 **build** 步骤的日志
3. 根据错误信息修复代码
4. 重新推送代码会自动触发新的构建

### Q2: 找不到 .github 目录

**原因:** 隐藏文件在某些文件管理器中不显示

**解决:**
```bash
# 在文件管理器中开启"显示隐藏文件"
# 或使用终端
ls -la /workspace/DexUnpacker/.github/
```

### Q3: 构建成功但安装失败

**可能原因:**
- Android 版本不兼容
- 签名问题
- 架构不匹配（arm64 vs x86）

**解决:**
- 检查 `app/build.gradle` 中的 `minSdk` 和 `abiFilters`
- 尝试在 `build.yml` 中启用更多架构

---

## 📊 构建配置说明

### 支持的架构

在 `app/build.gradle` 中配置：

```gradle
ndk {
    abiFilters 'arm64-v8a', 'armeabi-v7a', 'x86', 'x86_64'
}
```

- **arm64-v8a**: 现代 Android 手机（推荐）
- **armeabi-v7a**: 老旧手机
- **x86/x86_64**: 模拟器

### 构建产物

构建成功后会生成：
- `app/build/outputs/apk/debug/app-debug.apk` - 调试版
- `app/build/outputs/apk/release/app-release-unsigned.apk` - 发布版（需签名）

---

## 🔐 签名配置（可选）

如果要发布正式版，需要配置签名：

1. **生成密钥库**
```bash
keytool -genkey -v -keystore dexunpacker.keystore -alias dexunpacker -keyalg RSA -keysize 2048 -validity 10000
```

2. **创建 gradle.properties**
```properties
DEXUNPACKER_STORE_FILE=dexunpacker.keystore
DEXUNPACKER_STORE_PASSWORD=你的密码
DEXUNPACKER_KEY_ALIAS=dexunpacker
DEXUNPACKER_KEY_PASSWORD=你的密码
```

3. **修改 build.gradle**
```gradle
android {
    buildTypes {
        release {
            signingConfig signingConfigs.release
        }
    }
    signingConfigs {
        release {
            storeFile file(DEXUNPACKER_STORE_FILE)
            storePassword DEXUNPACKER_STORE_PASSWORD
            keyAlias DEXUNPACKER_KEY_ALIAS
            keyPassword DEXUNPACKER_KEY_PASSWORD
        }
    }
}
```

---

## 📞 需要帮助？

- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [Android Gradle 插件文档](https://developer.android.com/studio/build)
- [VirtualApp 集成指南](VA_INTEGRATION.md)
