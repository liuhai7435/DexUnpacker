package com.dexunpacker;

import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * DexUnpacker - 免 Root Dex 脱壳工具
 * 
 * 使用示例:
 * <pre>
 * // 初始化
 * DexUnpacker.init();
 * 
 * // 获取所有 dex cookies
 * long[] cookies = DexUnpacker.getDexCookies();
 * 
 * // 脱壳到指定目录
 * String outputDir = getExternalFilesDir("unpacked").getAbsolutePath();
 * List<String> results = DexUnpacker.dumpAll(cookies, outputDir);
 * 
 * // 输出结果
 * for (String path : results) {
 *     Log.d("Unpack", "Saved: " + path);
 * }
 * </pre>
 */
public class DexUnpacker {
    
    private static final String TAG = "DexUnpacker";
    private static boolean sInitialized = false;
    
    // 静态代码块加载 native 库
    static {
        try {
            System.loadLibrary("dexunpacker");
            Log.d(TAG, "Native library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load native library", e);
        }
    }
    
    /**
     * 初始化脱壳引擎
     */
    public static void init() {
        if (!sInitialized) {
            nativeInitVmCore();
            sInitialized = true;
            Log.d(TAG, "DexUnpacker initialized");
        }
    }
    
    /**
     * 获取当前应用所有 Dex 文件的 cookies
     * @return dex cookies 数组
     */
    public static long[] getDexCookies() {
        ensureInitialized();
        return nativeGetDexCookies();
    }
    
    /**
     * 通过 Cookie 方式脱壳单个 Dex
     * @param cookie dex cookie
     * @param outputPath 输出路径 (文件或目录)
     * @return 保存的文件路径，失败返回 null
     */
    public static String dumpDex(long cookie, String outputPath) {
        ensureInitialized();
        
        // 如果是目录，自动生成文件名
        File out = new File(outputPath);
        if (out.isDirectory()) {
            outputPath = new File(out, "dump_" + cookie + ".dex").getAbsolutePath();
        }
        
        String result = nativeDumpDexByCookie(cookie, outputPath);
        Log.d(TAG, "Dumped dex cookie 0x" + Long.toHexString(cookie) + " to: " + result);
        return result;
    }
    
    /**
     * 通过 Hook 方式脱壳指定 ClassLoader 的 Dex
     * @param classLoader 目标 ClassLoader
     * @param outputPath 输出目录
     * @return 是否成功
     */
    public static boolean dumpDex(ClassLoader classLoader, String outputPath) {
        ensureInitialized();
        return nativeDumpDexByHook(classLoader, outputPath);
    }
    
    /**
     * 批量脱壳所有 Dex
     * @param cookies dex cookies 数组
     * @param outputDir 输出目录
     * @return 保存的文件路径列表
     */
    public static List<String> dumpAll(long[] cookies, String outputDir) {
        ensureInitialized();
        
        List<String> results = new ArrayList<>();
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        for (int i = 0; i < cookies.length; i++) {
            String path = dumpDex(cookies[i], outputDir);
            if (path != null) {
                results.add(path);
            }
        }
        
        Log.d(TAG, "Dumped " + results.size() + " dex files to: " + outputDir);
        return results;
    }
    
    /**
     * 获取当前应用的 Dex 文件数量
     */
    public static int getDexCount() {
        return getDexCookies().length;
    }
    
    /**
     * 检查是否已初始化
     */
    public static boolean isInitialized() {
        return sInitialized;
    }
    
    private static void ensureInitialized() {
        if (!sInitialized) {
            init();
        }
    }
    
    // ==================== Native Methods ====================
    
    private static native void nativeInitVmCore();
    
    private static native long[] nativeGetDexCookies();
    
    private static native String nativeDumpDexByCookie(long cookie, String outputPath);
    
    private static native boolean nativeDumpDexByHook(ClassLoader classLoader, String outputPath);
}
