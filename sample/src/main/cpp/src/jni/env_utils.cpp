//
// Created by 江澎涌 on 2022/1/20.
//

#include <jni.h>
#include "env_utils.h"

std::string getStringFromJString(JNIEnv *_env, jstring _jstring_content) {
    auto utf_string = _env->GetStringUTFChars(_jstring_content, nullptr);
    std::string result = std::string(utf_string);
    _env->ReleaseStringUTFChars(_jstring_content, utf_string);
    return result;
}
