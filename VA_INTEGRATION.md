# VirtualApp 集成指南

## 概述

VirtualApp (VA) 是一个 Android 应用虚拟化引擎，允许在免 Root 情况下在应用内运行其他 APK。集成 VA 后，DexUnpacker 可以脱壳任意目标应用。

## 方案选择

### 方案 A: 完整集成 VirtualApp (推荐)

**优点:**
- ✅ 完整的虚拟化环境
- ✅ 支持多开、插件化
- ✅ 成熟的 API 和文档

**缺点:**
- ⚠️ 需要编译 VA 源码
- ⚠️ 增加 APK 体积 (~5MB)

**步骤:**

1. **获取 VirtualApp 源码**
```bash
git clone https://github.com/asLody/VirtualApp.git
cd VirtualApp
git checkout dev  # 或最新稳定分支
```

2. **编译 VA 库**
```bash
# 用 Android Studio 打开 VirtualApp 项目
# 修改 app/build.gradle 中的 applicationId
# 构建 VA 库
./gradlew :VirtualApp:assembleRelease
```

3. **集成到 DexUnpacker**

在 `app/build.gradle` 中添加:
```gradle
dependencies {
    // 方式 1: 本地 AAR
    implementation fileTree(dir: 'libs', include: ['*.aar', '*.jar'])
    
    // 方式 2: 源码依赖
    implementation project(':VirtualApp')
}
```

4. **初始化 VA**

在 `Application.onCreate()`:
```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 初始化 VirtualApp
        VirtualCore.get().startup(this);
        
        // 初始化 DexUnpacker
        DexUnpacker.init();
    }
}
```

5. **使用示例**

```java
// 安装 APK 到虚拟环境
int userId = VirtualCore.get().installPackage(apkPath, 0);

// 启动虚拟环境中的应用
VirtualCore.get().startActivity(
    userId, 
    "com.target.app", 
    ".MainActivity", 
    null
);

// 等待应用加载完成
Thread.sleep(3000);

// 获取虚拟环境的 ClassLoader
ClassLoader vaCL = VirtualCore.get().getContext("com.target.app")
                                .getClassLoader();

// 脱壳
String outputDir = getExternalFilesDir("unpacked").getAbsolutePath();
DexUnpacker.dumpDex(vaCL, outputDir);
```

---

### 方案 B: 精简版 VA 集成 (轻量)

如果只需要脱壳功能，可以只集成 VA 的核心模块:

**需要的文件:**
```
libs/
├── va-core.aar      # VA 核心
├── va-client.aar    # VA 客户端
└── va-engine.aar    # VA 引擎
```

**最小化配置:**
```gradle
android {
    defaultConfig {
        multiDexEnabled true
    }
    packagingOptions {
        pickFirst 'lib/*/libart-*.so'
    }
}

dependencies {
    implementation 'com.github.virtualapp:va-core:1.0.0'
    implementation 'com.github.virtualapp:va-client:1.0.0'
}
```

---

### 方案 C: 使用 BlackDex 方案 (无需 VA)

**BlackDex** 是一个独立的脱壳工具，不需要 VA 框架:

**集成方式:**
```gradle
dependencies {
    implementation 'com.github.CodingGay:BlackDex:1.2.0'
}
```

**使用:**
```java
BlackDexApi.unpackDex(
    context,
    "com.target.app",
    outputDir
);
```

---

## 权限配置

在 `AndroidManifest.xml` 中添加:

```xml
<!-- 存储权限 -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="29" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />

<!-- Android 11+ 文件管理权限 -->
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"
    android:minSdkVersion="30" />

<!-- VA 需要的权限 -->
<uses-permission android:name="android.permission.GET_PACKAGE_SIZE" />
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
```

---

## 完整使用流程

### 1. 初始化
```java
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 初始化 VA
        VirtualEnv.init(this);
        
        // 检查 VA 是否可用
        if (!VirtualEnv.isVAAvailable()) {
            Toast.makeText(this, "VA 不可用，仅支持当前应用脱壳", 
                          Toast.LENGTH_LONG).show();
        }
    }
}
```

### 2. 安装目标 APK
```java
String apkPath = "/sdcard/Download/target.apk";
boolean success = VirtualEnv.installApk(apkPath);

if (success) {
    Log.d("VA", "APK 安装成功");
} else {
    Log.e("VA", "APK 安装失败");
}
```

### 3. 启动并脱壳
```java
// 启动目标应用
VirtualEnv.launchActivity("com.target.app", ".MainActivity");

// 等待 dex 加载
new Handler(Looper.getMainLooper()).postDelayed(() -> {
    // 执行脱壳
    String outputDir = getExternalFilesDir("unpacked").getAbsolutePath();
    List<String> results = VirtualEnv.unpackTarget("com.target.app", outputDir);
    
    // 显示结果
    for (String path : results) {
        Log.d("Unpack", "Saved: " + path);
    }
}, 3000); // 3 秒延迟
```

---

## 常见问题

### Q1: VA 初始化失败
**原因:** VA 库未正确集成或签名不匹配

**解决:**
```bash
# 检查 VA 库是否存在
ls -la app/libs/

# 重新编译 VA
cd VirtualApp && ./gradlew clean assembleRelease
```

### Q2: 脱壳失败 (无 dex 文件)
**原因:** 目标应用使用了加固/加壳

**解决:**
- 先使用 FART/BlackDex 脱壳
- 或使用 Frida 动态 Hook

### Q3: Android 11+ 无法访问文件
**原因:** 分区存储限制

**解决:**
```java
// 请求 MANAGE_EXTERNAL_STORAGE 权限
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    if (!Environment.isExternalStorageManager()) {
        Intent intent = new Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }
}
```

---

## 参考资源

- [VirtualApp GitHub](https://github.com/asLody/VirtualApp)
- [BlackDex GitHub](https://github.com/CodingGay/BlackDex)
- [FART 脱壳](https://github.com/hanbinglengyue/FART)
- [ART 源码分析](https://android.googlesource.com/platform/art/)
