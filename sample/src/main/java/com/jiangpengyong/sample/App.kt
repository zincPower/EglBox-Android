package com.jiangpengyong.sample

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.jiangpengyong.eglbox_filter.EGLBoxRuntime
import com.jiangpengyong.sample.utils.FileUtils

class App : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        EGLBoxRuntime.init(this)
        FileUtils.copyFiles(
            assetManager = assets,
            assetDir = "fonts",
            srcFolder = filesDir
        )
        FileUtils.copyFiles(
            assetManager = assets,
            assetDir = "images",
            srcFolder = filesDir
        )
        FileUtils.copyFiles(
            assetManager = assets,
            assetDir = "model",
            srcFolder = filesDir
        )
    }
}