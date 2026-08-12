cmake_minimum_required(VERSION 3.22.1)

project("dexunpacker")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 添加源文件
set(SOURCES
    native-lib.cpp
    VmCore.cpp
    DexDump.cpp
)

# 创建共享库
add_library(${CMAKE_PROJECT_NAME} SHARED ${SOURCES})

# 查找系统库
find_library(log-lib log)
find_library(android-lib android)

# 链接库
target_link_libraries(${CMAKE_PROJECT_NAME}
    ${log-lib}
    ${android-lib}
)

# 编译选项
target_compile_options(${CMAKE_PROJECT_NAME} PRIVATE
    -Wall
    -Wextra
    -fvisibility=hidden
    -fomit-frame-pointer
)

# 定义 Android 版本宏
target_compile_definitions(${CMAKE_PROJECT_NAME} PRIVATE
    __ANDROID_API__=26
)
