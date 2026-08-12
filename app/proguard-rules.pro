# DexUnpacker ProGuard 规则

# 保留 Native 方法签名
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留 DexUnpacker 类
-keep class com.dexunpacker.** { *; }

# 保留 JNI 引用
-keepclassmembers class com.dexunpacker.DexUnpacker {
    native <methods>;
}

# 保留 VirtualApp 相关类 (如果使用)
-keep class com.lody.virtual.** { *; }
-dontwarn com.lody.virtual.**

# 保留 ART 运行时访问相关
-keep class dalvik.system.** { *; }
-keep class java.lang.ClassLoader { *; }

# 保留文件操作相关
-keep class java.io.** { *; }
-keep class java.nio.** { *; }

# 保留日志
-keepclassmembers class * {
    @android.util.Logs.* <methods>;
}

# 优化选项
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# 保留行号用于调试
-keepattributes SourceFile,LineNumberTable

# 保留泛型信息
-keepattributes Signature

# 保留异常信息
-keepattributes *Exception*
