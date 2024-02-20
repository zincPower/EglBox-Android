//
// Created by 江澎涌 on 2022/2/26.
//

#ifndef GPUMARK_JNIENVPTR_H
#define GPUMARK_JNIENVPTR_H

#include <jni.h>
#include <string>
#include <map>
#include <android/log.h>

static JavaVM *jvm = nullptr;

class JNIEnvPtr {
public:
    JNIEnvPtr();

    ~JNIEnvPtr();

    JNIEnv *operator->();

    JNIEnv *getEnv();

private:
    JNIEnvPtr(const JNIEnvPtr &) = delete;

    JNIEnvPtr &operator=(const JNIEnvPtr &) = delete;

private:
    JNIEnv *env_;
    bool need_detach_;
};

#endif //GPUMARK_JNIENVPTR_H
