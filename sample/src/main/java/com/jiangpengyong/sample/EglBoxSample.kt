package com.jiangpengyong.sample

object EglBoxSample {

    external fun getUsername(): String
    external fun getFreeType(path: String)

    init {
        System.loadLibrary("EglBox-Android-Sample")
    }

}