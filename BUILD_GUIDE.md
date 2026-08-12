# DexUnpacker 构建指南

## 📁 完整项目结构

```
DexUnpacker/
├── README.md                      # 项目说明
├── BUILD_GUIDE.md                 # 本文件
├── settings.gradle                # Gradle 设置
├── build.gradle                   # 根项目构建配置
├── gradle.properties              # Gradle 属性
│
└── app/
    ├── build.gradle               # 应用构建配置
    ├── proguard-rules.pro         # ProGuard 规则
    │
    └── src/
        └── main/
            ├── AndroidManifest.xml
            │
            ├── java/com/dexunpacker/
            │   ├── MainActivity.java
            │   ├── DexUnpacker.java
            │   └── UnpackResult.java
            │
            ├── cpp/
            │   ├── CMakeLists.txt
            │   ├── native-lib.cpp
            │   ├── VmCore.cpp
            │   └── DexDump.cpp
            │
            └── res/
                ├── values/strings.xml
                ├── values/themes.xml
                └── xml/file_paths.xml
```

## 🔧 构建步骤

### 1. 环境准备

```bash
# 安装 Android Studio
# 下载地址：https://developer.android.com/studio

# 或通过命令行工具
sdkmanager "platform-tools" "platforms;android-34"
sdkmanager "build-tools;34.0.0"
sdkmanager "ndk;25.2.9519653"
sdkmanager "cmake;3.22.1"
```

### 2. 创建缺失文件

#### settings.gradle
```gradle
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "DexUnpacker"
include ':app'
```

#### gradle.properties
```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=true
android.nonTransitiveRClass=false
```

#### app/proguard-rules.pro
```proguard
# 保留 native 方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留 DexUnpacker 类
-keep class com.dexunpacker.** { *; }
```

#### app/src/main/res/values/strings.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">DexUnpacker</string>
</resources>
```

#### app/src/main/res/values/themes.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.DexUnpacker" parent="Theme.MaterialComponents.DayNight.DarkActionBar">
        <item name="colorPrimary">@color/purple_500</item>
        <item name="colorPrimaryVariant">@color/purple_700</item>
        <item name="colorOnPrimary">@android:color/white</item>
    </style>
</resources>
```

#### app/src/main/res/values/colors.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="purple_200">#FFBB86FC</color>
    <color name="purple_500">#FF6200EE</color>
    <color name="purple_700">#FF3700B3</color>
    <color name="teal_200">#FF03DAC5</color>
    <color name="teal_700">#FF018786</color>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
</resources>
```

#### app/src/main/res/xml/file_paths.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-files-path name="unpacked" path="unpacked/" />
    <external-path name="external" path="." />
</paths>
```

### 3. 构建 APK

```bash
# 命令行构建
cd DexUnpacker
./gradlew assembleDebug

# 输出位置
# app/build/outputs/apk/debug/app-debug.apk
```

### 4. 安装测试

```bash
# 安装到设备
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 查看日志
adb logcat -s DexUnpacker
```

## ⚠️ 重要说明

### 免 Root 限制

由于不使用 root 权限，本工具存在以下限制：

1. **只能脱壳当前应用** - 无法直接脱壳其他已安装应用
2. **需要 VirtualApp 框架** - 如需脱壳其他应用，需集成 VA 框架加载目标 APK
3. **存储权限** - Android 10+ 需要使用分区存储或申请 MANAGE_EXTERNAL_STORAGE

### ART 版本适配

不同 Android 版本的 ART 内部结构不同：

| Android 版本 | ART 版本 | 适配难度 |
|-------------|---------|---------|
| 8.0-8.1     | 7.0     | ⭐⭐ |
| 9.0         | 8.0     | ⭐⭐ |
| 10          | 9.0     | ⭐⭐⭐ |
| 11          | 10.0    | ⭐⭐⭐ |
| 12-12L      | 11.0    | ⭐⭐⭐⭐ |
| 13          | 12.0    | ⭐⭐⭐⭐ |
| 14          | 13.0    | ⭐⭐⭐⭐⭐ |

需要在 `VmCore.cpp` 中根据版本做不同处理。

### 加固应用

对于加固/加壳应用：
- 需要先脱壳（FART/BlackDex）
- 再使用本工具导出
- 或使用 Frida 动态 Hook

## 🔗 相关资源

- [VirtualApp 框架](https://github.com/asLody/VirtualApp)
- [FART 脱壳](https://github.com/hanbinglengyue/FART)
- [BlackDex](https://github.com/CodingGay/BlackDex)
- [ART 源码](https://android.googlesource.com/platform/art/)
