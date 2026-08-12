# DexUnpacker - 免 Root Dex 脱壳工具

基于 GreenDex/VirtualApp 技术的独立脱壳 APP，**无需 root 权限**。

## 📋 功能特性

- ✅ **免 Root** - 使用应用内虚拟化技术
- ✅ **Hook 脱壳** - 拦截 ClassLoader 加载过程
- ✅ **Cookie 脱壳** - 直接读取 ART DexFile 内存结构
- ✅ **支持 Android 8.0-14** - 适配多版本 ART 运行时
- ✅ **一键导出** - 脱壳文件自动保存到应用目录

## ⚠️ 限制说明

由于免 root，存在以下限制：
1. 只能脱壳**当前应用进程**内的 dex
2. 无法直接脱壳其他已安装应用（需使用 VA 框架加载目标 APK）
3. 输出文件只能写入应用私有目录或用户授权的外部存储

## 🏗️ 项目结构

```
DexUnpacker/
├── app/
│   ├── src/main/
│   │   ├── java/com/dexunpacker/
│   │   │   ├── MainActivity.java
│   │   │   ├── DexUnpacker.java      # Java 调用层
│   │   │   └── UnpackResult.java
│   │   ├── cpp/
│   │   │   ├── CMakeLists.txt
│   │   │   ├── VmCore.cpp            # 空 Dex 加载
│   │   │   ├── DexDump.cpp           # 脱壳核心
│   │   │   └── native-lib.cpp        # JNI 注册
│   │   ├── AndroidManifest.xml
│   │   └── build.gradle
│   └── build.gradle
├── gradle.properties
└── settings.gradle
```

## 🚀 快速开始

### 1. 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高
- NDK r25c 或更高
- CMake 3.22+
- 目标 SDK: 34 (Android 14)
- 最低 SDK: 26 (Android 8.0)

### 2. 构建步骤
```bash
# 克隆项目
git clone https://github.com/your-repo/DexUnpacker.git
cd DexUnpacker

# 用 Android Studio 打开
# 或命令行构建
./gradlew assembleDebug
```

### 3. 使用方法

#### 方式一：脱壳当前应用
```java
// 获取当前应用所有 dex cookies
long[] cookies = DexUnpacker.getDexCookies();

// 脱壳每个 dex
for (long cookie : cookies) {
    String outputPath = DexUnpacker.dumpDex(cookie, "/sdcard/Download/");
    Log.d("Unpack", "Saved to: " + outputPath);
}
```

#### 方式二：脱壳指定 ClassLoader
```java
ClassLoader cl = getClassLoader();
long[] cookies = DexUnpacker.getDexCookiesFromClassLoader(cl);
DexUnpacker.dumpAll(cookies, "/sdcard/Download/myapp/");
```

#### 方式三：使用 VA 框架脱壳其他应用
```java
// 加载目标 APK 到虚拟环境
VaClient.installApk("/sdcard/app/target.apk");
VaClient.launchApp("com.target.app");

// 在虚拟进程内执行脱壳
long[] cookies = DexUnpacker.getDexCookies();
DexUnpacker.dumpAll(cookies, "/sdcard/Download/target/");
```

## 🔧 核心原理

### Hook 脱壳流程
```
1. 拦截 ClassLoader.loadClass()
2. 获取 DEX 文件路径
3. 读取完整 DEX 到内存
4. 修复 DEX 头部校验和
5. 保存到文件系统
```

### Cookie 脱壳流程
```
1. 遍历 ART 运行时 DexFile 链表
2. 获取每个 DexFile 的 cookie 标识符
3. 通过 cookie 读取内存中的完整 DEX 结构
4. 直接 dump 到文件（无需修复）
```

## 📦 依赖

```gradle
dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    // VirtualApp 框架（可选，用于脱壳其他应用）
    // implementation 'com.github.virtualapp:va:1.0.0'
}
```

## 📝 许可证

MIT License - 仅供安全研究使用
