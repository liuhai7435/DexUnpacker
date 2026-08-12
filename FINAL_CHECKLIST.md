# DexUnpacker 项目完成清单

## ✅ 项目文件总览

### 📁 根目录文件

| 文件 | 说明 | 状态 |
|------|------|------|
| `README.md` | 项目说明 | ✅ |
| `GITHUB_SETUP.md` | GitHub 上传指南 | ✅ |
| `BUILD_GUIDE.md` | 本地构建指南 | ✅ |
| `VA_INTEGRATION.md` | VirtualApp 集成指南 | ✅ |
| `PROJECT_SUMMARY.md` | 项目总结 | ✅ |
| `FINAL_CHECKLIST.md` | 本文件 | ✅ |
| `settings.gradle` | Gradle 设置 | ✅ |
| `build.gradle` | 根项目构建配置 | ✅ |
| `gradle.properties` | Gradle 属性 | ✅ |
| `gradlew` | Gradle Wrapper 脚本 | ✅ |
| `.gitignore` | Git 忽略文件 | ✅ |
| `.github/workflows/build.yml` | GitHub Actions 配置 | ✅ |

### 📁 app/src/main/java/com/dexunpacker/

| 文件 | 说明 | 状态 |
|------|------|------|
| `MainActivity.java` | 主界面 | ✅ |
| `DexUnpacker.java` | Java API 调用层 | ✅ |
| `VirtualEnv.java` | VirtualApp 环境管理 | ✅ |

### 📁 app/src/main/cpp/

| 文件 | 说明 | 状态 |
|------|------|------|
| `CMakeLists.txt` | NDK 构建配置 | ✅ |
| `native-lib.cpp` | JNI 注册入口 | ✅ |
| `VmCore.cpp` | ART 运行时交互 | ✅ |
| `DexDump.cpp` | 脱壳核心逻辑 | ✅ |

### 📁 app/src/main/res/

| 文件 | 说明 | 状态 |
|------|------|------|
| `values/strings.xml` | 字符串资源 | ✅ |
| `values/colors.xml` | 颜色资源 | ✅ |
| `values/themes.xml` | 主题资源 | ✅ |
| `xml/file_paths.xml` | 文件路径配置 | ✅ |

### 📁 app/

| 文件 | 说明 | 状态 |
|------|------|------|
| `build.gradle` | 应用构建配置 | ✅ |
| `proguard-rules.pro` | ProGuard 规则 | ✅ |
| `src/main/AndroidManifest.xml` | 应用清单 | ✅ |

---

## 🚀 3 步获取 APK

### 第 1 步：创建 GitHub 仓库

1. 访问 https://github.com/new
2. 仓库名：`DexUnpacker`
3. 可见性：Public 或 Private
4. **不要**勾选"Initialize with README"
5. 点击 **Create repository**

### 第 2 步：上传所有文件

**方法 A: 网页上传（推荐）**

1. 在仓库页面点击 **uploading an existing file**
2. 选择 `/workspace/DexUnpacker/` 目录下**所有文件**
3. 包括隐藏文件 `.github/` 和 `.gitignore`
4. Commit message: `Initial commit`
5. 点击 **Commit changes**

**方法 B: 使用 Git 命令**

```bash
cd /workspace/DexUnpacker
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/YOUR_USERNAME/DexUnpacker.git
git push -u origin main
```

### 第 3 步：下载构建好的 APK

1. 点击仓库顶部的 **Actions** 标签
2. 等待 **Build APK** 工作流完成（约 5-10 分钟）
3. 点击绿色的工作流（例如 `Build APK #1`）
4. 在页面底部 **Artifacts** 区域点击 **app-debug**
5. 下载 `app-debug.apk` 并安装到手机

---

## 📱 安装和使用

### 安装 APK

```bash
# 在手机上打开下载的 app-debug.apk
# 或
adb install -r app-debug.apk
```

### 使用步骤

1. **打开应用** - DexUnpacker
2. **授予存储权限** - 用于保存脱壳文件
3. **点击"开始脱壳"** - 自动脱壳当前应用
4. **查看结果** - 脱壳文件保存在应用私有目录

### 输出目录

```
/storage/emulated/0/Android/data/com.dexunpacker/files/unpacked/dex_时间戳/
```

---

## ⚠️ 重要说明

### 免 Root 限制

- ✅ 可以脱壳**当前应用**的 Dex
- ⚠️ 脱壳**其他应用**需要集成 VirtualApp 库
- ⚠️ 输出文件只能写入应用私有目录或授权目录

### VirtualApp 集成

如需脱壳其他应用，参考 [`VA_INTEGRATION.md`](VA_INTEGRATION.md)：

1. 下载 VirtualApp 源码
2. 编译 VA 库
3. 集成到项目
4. 使用 `VirtualEnv.installApk()` 和 `VirtualEnv.unpackTarget()`

### Android 版本支持

- **最低版本**: Android 8.0 (API 26)
- **目标版本**: Android 14 (API 34)
- **支持架构**: arm64-v8a, armeabi-v7a, x86, x86_64

---

## 🔧 故障排除

### 构建失败

1. 检查 GitHub Actions 日志
2. 确认所有文件都已上传
3. 确认 `.github/workflows/build.yml` 存在

### 安装失败

1. 开启"未知来源应用"权限
2. 检查 Android 版本是否 >= 8.0
3. 尝试 release 版本（如果 debug 失败）

### 脱壳失败

1. 确认已授予存储权限
2. 检查输出目录是否可写
3. 某些加固应用需要特殊处理

---

## 📞 获取帮助

- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [Android 开发者文档](https://developer.android.com)
- [VirtualApp GitHub](https://github.com/asLody/VirtualApp)
- [GreenDex 分析报告](../greendex_analysis_report.md)

---

## 📄 许可证

本项目仅供学习和安全研究使用。

**免责声明**: 请勿用于非法用途。脱壳他人应用可能违反相关法律法规，请确保你拥有目标应用的合法授权。
