#!/bin/bash

# DexUnpacker 构建脚本
# 需要：Android SDK, Gradle 8.2+, NDK 25.2+

set -e

echo "======================================"
echo "  DexUnpacker - 构建脚本"
echo "======================================"

# 检查环境变量
if [ -z "$ANDROID_HOME" ]; then
    echo "❌ 错误：未设置 ANDROID_HOME 环境变量"
    echo "   请设置：export ANDROID_HOME=/path/to/android/sdk"
    exit 1
fi

if [ -z "$ANDROID_NDK_HOME" ]; then
    echo "⚠️  警告：未设置 ANDROID_NDK_HOME"
    echo "   建议使用 NDK 版本：25.2.9519653"
fi

# 检查 Gradle
if ! command -v gradle &> /dev/null; then
    if [ -f "./gradlew" ]; then
        GRADLE_CMD="./gradlew"
    else
        echo "❌ 错误：未找到 Gradle"
        echo "   请安装 Gradle 8.2+ 或下载 gradlew"
        exit 1
    fi
else
    GRADLE_CMD="gradle"
fi

echo "✓ Android SDK: $ANDROID_HOME"
echo "✓ Gradle: $GRADLE_CMD"
echo ""

# 清理
echo "🧹 清理旧构建..."
$GRADLE_CMD clean --quiet

# 构建 Debug 版
echo "🔨 构建 Debug APK..."
$GRADLE_CMD assembleDebug --stacktrace

# 检查输出
DEBUG_APK="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$DEBUG_APK" ]; then
    echo ""
    echo "✅ 构建成功!"
    echo "📦 Debug APK: $DEBUG_APK"
    echo "📊 文件大小：$(ls -lh $DEBUG_APK | awk '{print $5}')"
else
    echo "❌ 构建失败：未找到输出文件"
    exit 1
fi

# 构建 Release 版 (可选)
if [ "$1" == "--release" ]; then
    echo ""
    echo "🔨 构建 Release APK..."
    $GRADLE_CMD assembleRelease --stacktrace
    
    RELEASE_APK="app/build/outputs/apk/release/app-release-unsigned.apk"
    if [ -f "$RELEASE_APK" ]; then
        echo "✅ Release APK: $RELEASE_APK"
        echo "⚠️  需要签名才能安装"
    fi
fi

echo ""
echo "======================================"
echo "  构建完成"
echo "======================================"
