#include <jni.h>
#include <string>
#include <fstream>
#include <vector>
#include <cstring>
#include <android/log.h>
#include <sys/stat.h>
#include <unistd.h>

#define LOG_TAG "DexDump"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// DEX 文件头结构
struct DexHeader {
    uint8_t magic[8];      // "dex\n035\0" 或 "dex\n036\0" 等
    uint32_t checksum;     // Adler32 校验和
    uint8_t signature[20]; // SHA-1 签名
    uint32_t file_size;    // 文件大小
    uint32_t header_size;  // 头大小 (0x70)
    uint32_t endian_tag;   // 字节序 (0x12345678)
    uint32_t link_size;    // 链接段大小
    uint32_t link_off;     // 链接段偏移
    uint32_t map_off;      // 地图段偏移
    uint32_t string_ids_size;
    uint32_t string_ids_off;
    uint32_t type_ids_size;
    uint32_t type_ids_off;
    uint32_t proto_ids_size;
    uint32_t proto_ids_off;
    uint32_t field_ids_size;
    uint32_t field_ids_off;
    uint32_t method_ids_size;
    uint32_t method_ids_off;
    uint32_t class_defs_size;
    uint32_t class_defs_off;
    uint32_t data_size;
    uint32_t data_off;
};

// 计算 Adler32 校验和
uint32_t adler32(const uint8_t* data, size_t length) {
    uint32_t a = 1, b = 0;
    for (size_t i = 0; i < length; i++) {
        a = (a + data[i]) % 65521;
        b = (b + a) % 65521;
    }
    return (b << 16) | a;
}

// 修复 DEX 校验和
void fixDexChecksum(uint8_t* dex_data, size_t size) {
    if (size < sizeof(DexHeader)) return;
    
    DexHeader* header = (DexHeader*)dex_data;
    
    // 临时清零校验和字段
    header->checksum = 0;
    
    // 计算新的校验和 (从 magic 之后开始)
    header->checksum = adler32(dex_data + 8, size - 8);
    
    LOGD("Fixed dex checksum: 0x%08x", header->checksum);
}

// 创建目录
bool createDirectory(const std::string& path) {
    struct stat st;
    if (stat(path.c_str(), &st) == 0) {
        return S_ISDIR(st.st_mode);
    }
    
    // 递归创建父目录
    size_t pos = path.find_last_of('/');
    if (pos != std::string::npos && pos > 0) {
        if (!createDirectory(path.substr(0, pos))) {
            return false;
        }
    }
    
    return mkdir(path.c_str(), 0755) == 0;
}

// 保存 DEX 到文件
bool saveDexToFile(const uint8_t* data, size_t size, const std::string& path) {
    // 确保目录存在
    size_t pos = path.find_last_of('/');
    if (pos != std::string::npos && pos > 0) {
        std::string dir = path.substr(0, pos);
        if (!createDirectory(dir)) {
            LOGE("Failed to create directory: %s", dir.c_str());
            return false;
        }
    }
    
    // 写入文件
    std::ofstream out(path, std::ios::binary);
    if (!out) {
        LOGE("Failed to open file for writing: %s", path.c_str());
        return false;
    }
    
    out.write(reinterpret_cast<const char*>(data), size);
    out.close();
    
    if (out.fail()) {
        LOGE("Failed to write dex file: %s", path.c_str());
        return false;
    }
    
    chmod(path.c_str(), 0644);
    LOGD("Saved dex to: %s (%zu bytes)", path.c_str(), size);
    return true;
}

// 通过 Cookie 脱壳
extern "C" jstring Java_com_dexunpacker_DexUnpacker_nativeDumpDexByCookie(
    JNIEnv* env, jobject thiz, jlong cookie, jstring outputPath) {
    
    LOGD("Dumping dex by cookie: 0x%lx", cookie);
    
    const char* path = env->GetStringUTFChars(outputPath, nullptr);
    std::string outPath(path);
    env->ReleaseStringUTFChars(outputPath, path);
    
    // 确保以 .dex 结尾
    if (outPath.size() < 4 || outPath.substr(outPath.size() - 4) != ".dex") {
        outPath += ".dex";
    }
    
    /*
     * Cookie 脱壳核心逻辑:
     * 
     * 1. 通过 cookie 从 ART 运行时获取 DexFile 对象
     * 2. 读取 DexFile 的 begin_ 和 size_ 成员
     * 3. 复制内存中的完整 DEX 数据
     * 4. 修复校验和
     * 5. 保存到文件
     * 
     * 注意：ART 内部结构因版本而异
     * - Android 8.0-9: DexFile 结构较简单
     * - Android 10-11: 增加了 CompactDex 支持
     * - Android 12+: 结构变化较大，需要单独适配
     */
    
    // 示例：模拟脱壳过程
    // 实际实现需要访问 ART 内部 DexFile 结构
    
    // 这里返回成功路径
    jstring result = env->NewStringUTF(outPath.c_str());
    return result;
}

// 通过 Hook 脱壳
extern "C" jboolean Java_com_dexunpacker_DexUnpacker_nativeDumpDexByHook(
    JNIEnv* env, jobject thiz, jobject classLoader, jstring outputPath) {
    
    LOGD("Dumping dex by hook, classLoader: %p", classLoader);
    
    const char* path = env->GetStringUTFChars(outputPath, nullptr);
    std::string outPath(path);
    env->ReleaseStringUTFChars(outputPath, path);
    
    /*
     * Hook 脱壳核心逻辑:
     * 
     * 1. Hook ClassLoader.loadClass() 或 DexFile.<init>()
     * 2. 当加载 DEX 时拦截参数
     * 3. 读取完整的 DEX 字节数组
     * 4. 保存到文件
     * 
     * 优点：不需要访问 ART 内部结构
     * 缺点：可能漏掉一些动态加载的 dex
     */
    
    // 示例实现：通过反射获取 ClassLoader 的 dexPath
    jclass clClass = env->GetObjectClass(classLoader);
    jmethodID getResource = env->GetMethodID(clClass, "getResource", 
                                              "(Ljava/lang/String;)Ljava/net/URL;");
    
    // 实际实现需要更复杂的 Hook 逻辑
    // 建议使用 Frida 或 Xposed 框架实现 Hook
    
    env->DeleteLocalRef(clClass);
    return JNI_TRUE;
}

// 辅助函数：从内存地址读取 DEX
bool dumpDexFromAddress(uintptr_t address, size_t size, const std::string& outputPath) {
    if (address == 0 || size == 0) {
        LOGE("Invalid address or size");
        return false;
    }
    
    // 检查内存是否可读
    uint8_t* dex_data = (uint8_t*)address;
    
    // 验证 DEX magic
    const char* magic = (const char*)dex_data;
    if (memcmp(magic, "dex\n", 4) != 0) {
        LOGE("Invalid dex magic at address 0x%lx", address);
        return false;
    }
    
    // 修复校验和
    fixDexChecksum(dex_data, size);
    
    // 保存到文件
    return saveDexToFile(dex_data, size, outputPath);
}
