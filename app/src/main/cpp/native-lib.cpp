#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "DexUnpacker"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// 前置声明
extern long* getDexCookies(JNIEnv* env, int* count);
extern void freeDexCookies(long* cookies);
extern jstring dumpDexByCookie(JNIEnv* env, jlong cookie, jstring outputPath);
extern jboolean dumpDexByHook(JNIEnv* env, jobject classLoader, jstring outputPath);
extern void initVmCore(JNIEnv* env);

// JNI 方法表
static JNINativeMethod gMethods[] = {
    {"nativeGetDexCookies", "()[J", (void*)getDexCookies},
    {"nativeDumpDexByCookie", "(JLjava/lang/String;)Ljava/lang/String;", (void*)dumpDexByCookie},
    {"nativeDumpDexByHook", "(Ljava/lang/ClassLoader;Ljava/lang/String;)Z", (void*)dumpDexByHook},
    {"nativeInitVmCore", "()V", (void*)initVmCore},
};

// 注册 JNI 方法
static int registerNativeMethods(JNIEnv* env, const char* className) {
    jclass clazz = env->FindClass(className);
    if (clazz == nullptr) {
        LOGE("Failed to find class: %s", className);
        return JNI_FALSE;
    }
    
    int numMethods = sizeof(gMethods) / sizeof(gMethods[0]);
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        LOGE("Failed to register native methods");
        return JNI_FALSE;
    }
    
    return JNI_TRUE;
}

// JNI_OnLoad - 库加载时自动调用
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = nullptr;
    
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        LOGE("Failed to get JNIEnv");
        return JNI_ERR;
    }
    
    // 注册 native 方法
    if (!registerNativeMethods(env, "com/dexunpacker/DexUnpacker")) {
        return JNI_ERR;
    }
    
    // 初始化 VmCore
    initVmCore(env);
    
    LOGD("DexUnpacker native library loaded successfully");
    return JNI_VERSION_1_6;
}

// JNI_OnUnload - 库卸载时调用
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
    JNIEnv* env = nullptr;
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) == JNI_OK) {
        LOGD("DexUnpacker native library unloaded");
    }
}
