//
// Created by 江澎涌 on 2022/2/26.
//

#include "jni_env_ptr.h"

JNIEnvPtr::JNIEnvPtr() : env_{nullptr}, need_detach_{false} {
    if (jvm->GetEnv((void **) &env_, JNI_VERSION_1_4) == JNI_EDETACHED) {
        jvm->AttachCurrentThread(&env_, nullptr);
        need_detach_ = true;
    }
}

JNIEnvPtr::~JNIEnvPtr() {
    if (need_detach_) {
        jvm->DetachCurrentThread();
    }
}

JNIEnv *JNIEnvPtr::getEnv() {
    return env_;
}

JNIEnv *JNIEnvPtr::operator->() {
    return env_;
}
