//
// Created by 江澎涌 on 2022/1/20.
//

#ifndef POSTMARK_ENVUTILS_H
#define POSTMARK_ENVUTILS_H

#include <string>

std::string getStringFromJString(JNIEnv *env, jstring jstringContent);

#endif //POSTMARK_ENVUTILS_H
