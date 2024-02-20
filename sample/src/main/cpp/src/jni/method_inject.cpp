//
// Created by 江澎涌 on 2022/1/21.
//

#include "method_inject.h"
#include "env_utils.h"
#include <freetype/freetype.h>

#include <cstdlib>
#include <string>
#include <locale>
#include <codecvt>

#define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))

jstring getUsername(JNIEnv *_env,
                    jobject _thiz) {
    return _env->NewStringUTF("江澎涌！！！");
}

void getFreeType(JNIEnv *_env,
                 jobject _thiz,
                 jstring _font_path) {
    FT_Library ft;

    if (FT_Init_FreeType(&ft)) {
        LOGI("Could not init FreeType Library");
        return;
    }

    std::string font_path = getStringFromJString(_env, _font_path);
    LOGI("font_path: %s", font_path.c_str());

    FT_Face face;
    if (FT_New_Face(ft, font_path.c_str(), 0, &face)) {
        LOGI("Failed to load font");
        return;
    }

    FT_Set_Pixel_Sizes(face, 0, 200);

    if (FT_Load_Char(face, 'M', FT_LOAD_NO_BITMAP)) {
        LOGI("Failed to load Glyph");
        return;
    }

    LOGI("%s(%s)\n", __FILE__, __FUNCTION__);
    LOGI("-------------------------\n");
    LOGI("%15s = %ld\n", "advance.x", face->glyph->advance.x);
    LOGI("%15s = %d\n", "bitmap_left", face->glyph->bitmap_left);
    LOGI("%15s = %d\n", "bitmap_top", face->glyph->bitmap_top);
    LOGI("%15s = %d\n", "bitmap.rows", face->glyph->bitmap.rows);
    LOGI("%15s = %d\n", "bitmap.width", face->glyph->bitmap.width);
    LOGI("%15s = %ld\n", "bitmap.buffer", sizeof(face->glyph->bitmap.buffer));
//    printf("%15s = %ld\n", "bitmap_buffer", std::strlen(face->glyph->bitmap.buffer));
    printf("-------------------------\n");

    // 释放
    FT_Done_Face(face);
    FT_Done_FreeType(ft);
}

static const JNINativeMethod native_methods[] = {
        {
                "getUsername",
                "()Ljava/lang/String;",
                (void *) getUsername
        },
        {
                "getFreeType",
                "(Ljava/lang/String;)V",
                (void *) getFreeType
        },
};

bool registerMethods(JNIEnv *_env) {
    LOGI("RegisterNatives begin");
    jclass clazz;
    clazz = (*_env).FindClass("com/jiangpengyong/sample/EglBoxSample");

    if (clazz == nullptr) {
        LOGI("clazz is null");
        return JNI_FALSE;
    }

    if ((*_env).RegisterNatives(clazz, native_methods, NELEM(native_methods)) < 0) {
        LOGI("RegisterNatives error");
        return JNI_FALSE;
    }

    LOGI("RegisterNatives success");
    return JNI_TRUE;
}
