cmake_minimum_required(VERSION 3.10.2)

project("EglBox-Android-Sample")

# 使用C++ 17标准
set(CMAKE_CXX_STANDARD 17)

# ======================== Freetype start ===========================
# FreeType 源码
set(FreeTypeSourcePath ${CMAKE_CURRENT_SOURCE_DIR}/../../../../lib/freetype-2.13.2)
include_directories(
        ${FreeTypeSourcePath}/src
        ${FreeTypeSourcePath}/include
)
add_subdirectory(${FreeTypeSourcePath}/android freetype_binary_dir)
# ======================== Freetype end ===========================

add_library(
        EglBox-Android-Sample
        SHARED
        src/jni/egl_box_sample.cpp
        src/jni/android_logger.cpp
        src/jni/env_utils.cpp
        src/jni/jni_env_ptr.cpp
        src/jni/method_inject.cpp
)

find_library(log-lib log)

target_link_libraries(
        EglBox-Android-Sample
        EGL
        GLESv3
        ${log-lib}
        freetype
)