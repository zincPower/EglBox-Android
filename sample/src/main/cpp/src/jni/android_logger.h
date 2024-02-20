//
// Created by 江澎涌 on 2022/1/20.
//

#ifndef POSTMARK_LOGGER_H
#define POSTMARK_LOGGER_H

#include "android/log.h"

#define TAG "FontStudyNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

#endif //POSTMARK_LOGGER_H
