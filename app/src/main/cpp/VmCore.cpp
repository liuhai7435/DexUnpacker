#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <unistd.h>
#include <dlfcn.h>

#define LOG_TAG "VmCore"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// ART 运行时相关结构 (简化版)
// 注意：不同 Android 版本结构不同，需要适配
namespace art {
    // DexFile 结构 (Android 10+)
    struct DexFile {
        const uint8_t* begin_;
        size_t size_;
        std::string location_;
        uint32_t checksum_;
        const uint8_t* data_;
    };
    
    // DexFileLoader (用于从内存加载 dex)
    class DexFileLoader {
    public:
        static DexFileLoader* GetInstance() {
            // 通过 libart.so 符号获取
            using Getter = DexFileLoader*(*)();
            void* handle = dlopen("libart.so", RTLD_NOW);
            if (!handle) return nullptr;
            
            Getter getter = (Getter)dlsym(handle, "_ZN3art13DexFileLoader20GetDexFileLoaderEv");
            if (!getter) {
                dlclose(handle);
                return nullptr;
            }
            
            DexFileLoader* instance = getter();
            dlclose(handle);
            return instance;
        }
        
        // 从内存加载 DexFile
        const DexFile* OpenMemory(const uint8_t* base, size_t size, 
                                   const std::string& location, 
                                   uint32_t checksum, std::string* error_msg) {
            // 简化实现，实际需要调用 ART 内部 API
            LOGD("Loading dex from memory: %s, size: %zu", location.c_str(), size);
            return nullptr;
        }
    };
}

// 全局变量 - 存储 dex cookies
static std::vector<long> g_dex_cookies;
static bool g_vm_core_initialized = false;

// 初始化 VmCore
void initVmCore(JNIEnv* env) {
    if (g_vm_core_initialized) return;
    
    LOGD("Initializing VmCore...");
    
    // 1. 获取当前 ClassLoader 加载的所有 dex
    jclass classLoaderClass = env->FindClass("java/lang/ClassLoader");
    if (classLoaderClass) {
        jmethodID getSystemClassLoader = env->GetStaticMethodID(
            classLoaderClass, "getSystemClassLoader", "()Ljava/lang/ClassLoader;");
        
        if (getSystemClassLoader) {
            jobject systemCL = env->CallStaticObjectMethod(classLoaderClass, getSystemClassLoader);
            
            // 获取 PathClassLoader 的 dexPath
            jclass pathCLClass = env->FindClass("dalvik/system/PathClassLoader");
            if (pathCLClass && env->IsInstanceOf(systemCL, pathCLClass)) {
                jfieldID dexPathField = env->GetFieldID(pathCLClass, "dexPath", "Ljava/lang/String;");
                if (dexPathField) {
                    jstring dexPath = (jstring)env->GetObjectField(systemCL, dexPathField);
                    if (dexPath) {
                        const char* path = env->GetStringUTFChars(dexPath, nullptr);
                        LOGD("System ClassLoader dexPath: %s", path);
                        env->ReleaseStringUTFChars(dexPath, path);
                    }
                }
            }
        }
    }
    
    // 2. 尝试获取 ART 运行时 dex cookies
    // 注意：这需要访问 ART 内部结构，不同版本需要不同实现
    void* art_handle = dlopen("libart.so", RTLD_NOW);
    if (art_handle) {
        LOGD("libart.so loaded successfully");
        
        // 尝试获取 Runtime 单例
        void* runtime = dlsym(art_handle, "_ZN3art7Runtime9instance_E");
        if (runtime) {
            LOGD("ART Runtime instance found at: %p", runtime);
            // 可以通过 runtime 指针访问 ClassLinker 和 DexFile 列表
        }
        
        dlclose(art_handle);
    }
    
    g_vm_core_initialized = true;
    LOGD("VmCore initialized");
}

// 获取所有 dex cookies
extern "C" jlongArray Java_com_dexunpacker_DexUnpacker_nativeGetDexCookies(JNIEnv* env, jobject thiz) {
    LOGD("Getting dex cookies...");
    
    // 简化实现：返回空数组
    // 实际实现需要遍历 ART ClassLinker 的 DexFile 链表
    jlongArray result = env->NewLongArray(0);
    return result;
    
    /* 完整实现示例:
    std::vector<long> cookies = collectDexCookiesFromART();
    jlongArray result = env->NewLongArray(cookies.size());
    env->SetLongArrayRegion(result, 0, cookies.size(), (jlong*)cookies.data());
    return result;
    */
}

// 释放 cookies 内存
void freeDexCookies(long* cookies) {
    if (cookies) {
        delete[] cookies;
    }
}
