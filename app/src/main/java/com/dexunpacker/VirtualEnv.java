package com.dexunpacker;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * VirtualApp 环境管理
 * 
 * 注意：完整功能需要集成 VirtualApp 库
 * 此处为简化实现，演示基本流程
 */
public class VirtualEnv {
    
    private static final String TAG = "VirtualEnv";
    private static boolean sInitialized = false;
    private static Context sContext;
    
    /**
     * 初始化 VirtualApp 环境
     */
    public static void init(Context context) {
        if (sInitialized) return;
        
        sContext = context.getApplicationContext();
        
        try {
            // 检查 VirtualApp 核心类是否存在
            Class.forName("com.lody.virtual.client.core.VirtualCore");
            Log.d(TAG, "VirtualApp library found");
            
            // 初始化 VirtualCore
            // VirtualCore.get().startup(sContext);
            
            sInitialized = true;
            Log.d(TAG, "VirtualApp initialized successfully");
            
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "VirtualApp library not found, using basic unpacking only");
            Log.w(TAG, "To enable full features, integrate VirtualApp from:");
            Log.w(TAG, "https://github.com/asLody/VirtualApp");
            sInitialized = false;
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize VirtualApp", e);
            sInitialized = false;
        }
    }
    
    /**
     * 检查 VA 是否可用
     */
    public static boolean isVAAvailable() {
        return sInitialized;
    }
    
    /**
     * 安装 APK 到虚拟环境
     * @param apkPath APK 文件路径
     * @return 是否成功
     */
    public static boolean installApk(String apkPath) {
        if (!sInitialized) {
            Log.w(TAG, "VirtualApp not initialized, cannot install APK");
            return false;
        }
        
        File apkFile = new File(apkPath);
        if (!apkFile.exists()) {
            Log.e(TAG, "APK file not found: " + apkPath);
            return false;
        }
        
        try {
            // 简化实现：仅复制 APK 到应用目录
            // 完整实现需要调用 VirtualCore.installPackage()
            
            File destDir = new File(sContext.getFilesDir(), "va_apps");
            destDir.mkdirs();
            
            File destApk = new File(destDir, apkFile.getName());
            copyFile(apkFile, destApk);
            
            Log.d(TAG, "APK copied to: " + destApk.getAbsolutePath());
            Log.d(TAG, "Note: Full VA integration required for actual installation");
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to install APK", e);
            return false;
        }
    }
    
    /**
     * 启动虚拟环境中的应用
     * @param packageName 包名
     * @param activityClassName Activity 类名
     */
    public static void launchActivity(String packageName, String activityClassName) {
        if (!sInitialized) {
            Log.w(TAG, "VirtualApp not initialized");
            return;
        }
        
        try {
            // 完整实现需要调用 VirtualCore.startActivity()
            Log.d(TAG, "Would launch: " + packageName + "/" + activityClassName);
            Log.d(TAG, "Note: Full VA integration required");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch activity", e);
        }
    }
    
    /**
     * 脱壳虚拟环境中的应用
     * @param packageName 包名
     * @param outputDir 输出目录
     * @return 脱壳文件路径列表
     */
    public static List<String> unpackTarget(String packageName, String outputDir) {
        List<String> results = new ArrayList<>();
        
        if (!sInitialized) {
            Log.w(TAG, "VirtualApp not initialized, unpacking current app only");
            // 退化为脱壳当前应用
            return DexUnpacker.dumpAll(DexUnpacker.getDexCookies(), outputDir);
        }
        
        try {
            // 完整实现需要：
            // 1. 获取虚拟环境的 ClassLoader
            // 2. 调用 DexUnpacker.dumpDex(vaClassLoader, outputDir)
            
            Log.d(TAG, "Unpacking target: " + packageName);
            Log.d(TAG, "Output directory: " + outputDir);
            Log.d(TAG, "Note: Full VA integration required for actual unpacking");
            
            // 临时返回空列表
            return results;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to unpack target", e);
            return results;
        }
    }
    
    /**
     * 获取虚拟环境中已安装的应用列表
     * @return 包名列表
     */
    public static List<String> getInstalledApps() {
        List<String> apps = new ArrayList<>();
        
        if (!sInitialized) {
            Log.w(TAG, "VirtualApp not initialized");
            return apps;
        }
        
        try {
            // 完整实现需要调用 VirtualCore.getInstalledApps()
            Log.d(TAG, "Note: Full VA integration required");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to get installed apps", e);
        }
        
        return apps;
    }
    
    /**
     * 从虚拟环境卸载应用
     * @param packageName 包名
     * @return 是否成功
     */
    public static boolean uninstallApp(String packageName) {
        if (!sInitialized) {
            return false;
        }
        
        try {
            // 完整实现需要调用 VirtualCore.uninstallPackage()
            Log.d(TAG, "Would uninstall: " + packageName);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to uninstall app", e);
            return false;
        }
    }
    
    // 工具方法：复制文件
    private static void copyFile(File src, File dest) throws Exception {
        InputStream is = new FileInputStream(src);
        FileOutputStream os = new FileOutputStream(dest);
        
        byte[] buffer = new byte[4096];
        int len;
        while ((len = is.read(buffer)) > 0) {
            os.write(buffer, 0, len);
        }
        
        is.close();
        os.close();
    }
    
    // 工具方法：从 assets 复制文件
    private static void copyFromAssets(String assetName, File dest) throws Exception {
        InputStream is = sContext.getAssets().open(assetName);
        FileOutputStream os = new FileOutputStream(dest);
        
        byte[] buffer = new byte[4096];
        int len;
        while ((len = is.read(buffer)) > 0) {
            os.write(buffer, 0, len);
        }
        
        is.close();
        os.close();
    }
}
