#include <jni.h>

#include "android_logger.h"
#include "method_inject.h"
#include "jni_env_ptr.h"

JNIEXPORT jint JNI_OnLoad(JavaVM *_vm, void *_reserved) {
    LOGI("JNI_OnLoad");

    jvm = _vm;

    JNIEnv *env = nullptr;
    if ((*_vm).GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOGI("[ERROR]GetEnv failed");
        return -1;
    }

    registerMethods(env);

    return JNI_VERSION_1_4;
}